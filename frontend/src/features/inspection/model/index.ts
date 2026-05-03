import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import {
  getAnalysisTargets,
  getInspectionDetail,
  getInspectionEvents,
  getMyThresholds,
  uploadInspection,
} from '../api'
import type {
  AnalysisTargetOption,
  BrowserCameraDevice,
  InspectionDetail,
  InspectionEvent,
  SelectedInspectionFile,
  ThresholdOption,
  UploadInspectionResponse,
} from '../types'

const MAX_UPLOAD_SIZE = 2 * 1024 * 1024 * 1024
const ALLOWED_EXTENSIONS = ['jpg', 'jpeg', 'png', 'webp']
const ALLOWED_MIME_TYPES = ['image/jpeg', 'image/png', 'image/webp']
const POLLING_INTERVAL_MS = 2000
const POLLING_TIMEOUT_MS = 180000

export function useUploadInspection() {
  const [selectedFile, setSelectedFileState] = useState<SelectedInspectionFile | null>(null)
  const [targetOptions, setTargetOptions] = useState<AnalysisTargetOption[]>([])
  const [thresholdOptions, setThresholdOptions] = useState<ThresholdOption[]>([])
  const [selectedTargetId, setSelectedTargetId] = useState<number | null>(null)
  const [selectedThresholdId, setSelectedThresholdId] = useState<number | null>(null)
  const [selectedModel, setSelectedModel] = useState('default')
  const [loadingOptions, setLoadingOptions] = useState(true)
  const [uploading, setUploading] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [noticeMessage, setNoticeMessage] = useState<string | null>(null)
  const [uploadResult, setUploadResult] = useState<UploadInspectionResponse | null>(null)
  const [requestDurationMs, setRequestDurationMs] = useState<number | null>(null)
  const objectUrlRef = useRef<string | null>(null)
  const pollingStartedAtRef = useRef<number | null>(null)

  useEffect(() => {
    const controller = new AbortController()
    setLoadingOptions(true)

    Promise.all([getAnalysisTargets(controller.signal), getMyThresholds(controller.signal)])
      .then(([targets, thresholds]) => {
        setTargetOptions(targets)
        setThresholdOptions(thresholds)
      })
      .catch((error) => {
        if (!controller.signal.aborted) {
          setTargetOptions([])
          setThresholdOptions([])
          setErrorMessage(
            error instanceof Error
              ? error.message
              : '검사 옵션을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.',
          )
        }
      })
      .finally(() => setLoadingOptions(false))

    return () => controller.abort()
  }, [])

  useEffect(() => {
    return () => {
      if (objectUrlRef.current) URL.revokeObjectURL(objectUrlRef.current)
    }
  }, [])

  useEffect(() => {
    if (!uploadResult || uploadResult.runStatus !== 'PROCESSING') return

    pollingStartedAtRef.current = Date.now()
    const controller = new AbortController()
    const intervalId = window.setInterval(() => {
      if (!uploadResult) return
      if (pollingStartedAtRef.current && Date.now() - pollingStartedAtRef.current > POLLING_TIMEOUT_MS) {
        window.clearInterval(intervalId)
        setNoticeMessage('AI 분석이 지연되고 있습니다. 잠시 후 검사 상세에서 상태를 다시 확인해주세요.')
        return
      }
      void getInspectionDetail(uploadResult.inspectionId, controller.signal)
        .then((detail) => {
          applyInspectionDetail(detail, setUploadResult, setNoticeMessage, intervalId)
        })
        .catch(() => undefined)
    }, POLLING_INTERVAL_MS)

    return () => {
      controller.abort()
      window.clearInterval(intervalId)
    }
  }, [uploadResult])

  const selectedThreshold = useMemo(
    () => thresholdOptions.find((option) => option.id === selectedThresholdId) ?? null,
    [selectedThresholdId, thresholdOptions],
  )

  const statusMessage = useMemo(() => {
    if (uploading) return '이미지 검사 요청을 접수하는 중입니다.'
    if (uploadResult) return '이미지 검사 요청이 접수되었습니다.'
    if (selectedFile) return '이미지가 선택되었습니다. 검사 실행 버튼으로 요청을 전송할 수 있습니다.'
    return '현재 MVP에서는 JPG, PNG, WEBP 이미지 파일만 업로드할 수 있습니다.'
  }, [selectedFile, uploadResult, uploading])

  const setSelectedFile = useCallback((file: File | null) => {
    setErrorMessage(null)
    setNoticeMessage(null)
    setUploadResult(null)
    setRequestDurationMs(null)

    if (objectUrlRef.current) {
      URL.revokeObjectURL(objectUrlRef.current)
      objectUrlRef.current = null
    }

    if (!file) {
      setSelectedFileState(null)
      return
    }

    const validationError = validateInspectionFile(file)
    if (validationError) {
      setSelectedFileState(null)
      setErrorMessage(validationError)
      return
    }

    const previewUrl = URL.createObjectURL(file)
    objectUrlRef.current = previewUrl
    const nextFile: SelectedInspectionFile = {
      file,
      previewUrl,
      fileKind: 'image',
      selectedAt: new Date(),
    }
    setSelectedFileState(nextFile)

    const image = new Image()
    image.onload = () => {
      setSelectedFileState((prev) =>
        prev?.previewUrl === previewUrl
          ? { ...prev, width: image.naturalWidth, height: image.naturalHeight }
          : prev,
      )
    }
    image.src = previewUrl
  }, [])

  const submit = useCallback(async () => {
    if (!selectedFile || uploading) return

    setUploading(true)
    setErrorMessage(null)
    setNoticeMessage(null)
    const startedAt = performance.now()

    try {
      const result = await uploadInspection({
        file: selectedFile.file,
        targetId: selectedTargetId,
        thresholdId: selectedThresholdId,
        inputMode: 'IMAGE',
        sourceType: 'IMAGE',
        roiMode: 'FULL_FRAME',
        qualityGateEnabled: true,
        idempotencyKey: createInspectionIdempotencyKey('upload'),
      })
      setUploadResult(result)
      setRequestDurationMs(performance.now() - startedAt)
      setNoticeMessage('이미지 검사 요청이 접수되었습니다. 현재 상태: PROCESSING')
    } catch (error) {
      setErrorMessage(
        error instanceof Error
          ? error.message
          : '검사 요청에 실패했습니다. 파일 형식과 네트워크 상태를 확인해 주세요.',
      )
    } finally {
      setUploading(false)
    }
  }, [selectedFile, selectedTargetId, selectedThresholdId, uploading])

  const reset = useCallback(() => {
    setSelectedFile(null)
    setUploadResult(null)
    setRequestDurationMs(null)
    setErrorMessage(null)
    setNoticeMessage(null)
  }, [setSelectedFile])

  return {
    selectedFile,
    targetOptions,
    thresholdOptions,
    selectedTargetId,
    selectedThresholdId,
    selectedThreshold,
    selectedModel,
    loadingOptions,
    uploading,
    errorMessage,
    noticeMessage,
    uploadResult,
    requestDurationMs,
    statusMessage,
    setSelectedFile,
    setSelectedTargetId,
    setSelectedThresholdId,
    setSelectedModel,
    submit,
    reset,
  }
}

export function useRealtimeInspection() {
  const videoRef = useRef<HTMLVideoElement | null>(null)
  const streamRef = useRef<MediaStream | null>(null)
  const [devices, setDevices] = useState<BrowserCameraDevice[]>([])
  const [selectedDeviceId, setSelectedDeviceId] = useState<string | null>(null)
  const [currentInspectionId, setCurrentInspectionId] = useState<number | null>(null)
  const [events, setEvents] = useState<InspectionEvent[]>([])
  const [isCameraLoading, setIsCameraLoading] = useState(false)
  const [isCameraReady, setIsCameraReady] = useState(false)
  const [isCapturing, setIsCapturing] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [noticeMessage, setNoticeMessage] = useState<string | null>(null)
  const [uploadResult, setUploadResult] = useState<UploadInspectionResponse | null>(null)
  const [requestDurationMs, setRequestDurationMs] = useState<number | null>(null)
  const pollingStartedAtRef = useRef<number | null>(null)

  const refreshDevices = useCallback(async () => {
    if (!navigator.mediaDevices?.enumerateDevices) {
      setDevices([])
      setSelectedDeviceId(null)
      setErrorMessage('브라우저에서 카메라 장치 조회를 지원하지 않습니다.')
      return
    }

    try {
      const mediaDevices = await navigator.mediaDevices.enumerateDevices()
      const nextDevices = mediaDevices
        .filter((device) => device.kind === 'videoinput')
        .map((device, index) => ({
          deviceId: device.deviceId,
          label: device.label || `카메라 ${index + 1}`,
        }))

      setDevices(nextDevices)
      setSelectedDeviceId((current) => {
        if (current && nextDevices.some((device) => device.deviceId === current)) return current
        return nextDevices[0]?.deviceId ?? null
      })
    } catch (error) {
      setDevices([])
      setSelectedDeviceId(null)
      setErrorMessage(
        error instanceof Error ? error.message : '카메라 장치 목록을 불러오지 못했습니다.',
      )
    }
  }, [])

  const stopStream = useCallback(() => {
    const stream = streamRef.current
    if (stream) {
      stream.getTracks().forEach((track) => track.stop())
      streamRef.current = null
    }

    if (videoRef.current) {
      videoRef.current.srcObject = null
    }
    setIsCameraReady(false)
  }, [])

  const startPreview = useCallback(async () => {
    if (!navigator.mediaDevices?.getUserMedia) {
      setErrorMessage('브라우저에서 카메라 미리보기를 지원하지 않습니다.')
      return
    }

    setIsCameraLoading(true)
    setErrorMessage(null)

    try {
      stopStream()
      const stream = await navigator.mediaDevices.getUserMedia({
        audio: false,
        video: selectedDeviceId
          ? {
              deviceId: { exact: selectedDeviceId },
            }
          : {
              facingMode: 'environment',
            },
      })
      streamRef.current = stream

      if (videoRef.current) {
        videoRef.current.srcObject = stream
        videoRef.current.muted = true
        videoRef.current.playsInline = true
        await videoRef.current.play()
      }

      setIsCameraReady(true)
      setNoticeMessage('카메라 미리보기가 준비되었습니다. 현재 화면 검사 버튼으로 프레임 1장을 분석할 수 있습니다.')
      await refreshDevices()
    } catch (error) {
      stopStream()
      setNoticeMessage(null)
      setErrorMessage(
        error instanceof Error
          ? error.message
          : '카메라 접근에 실패했습니다. 브라우저 권한을 확인해 주세요.',
      )
    } finally {
      setIsCameraLoading(false)
    }
  }, [refreshDevices, selectedDeviceId, stopStream])

  useEffect(() => {
    void refreshDevices()
  }, [refreshDevices])

  useEffect(() => {
    void startPreview()
    return () => stopStream()
  }, [selectedDeviceId, startPreview, stopStream])

  const refreshEvents = useCallback(async () => {
    if (!currentInspectionId) return
    try {
      const nextEvents = await getInspectionEvents(currentInspectionId)
      setEvents(nextEvents)
    } catch {
      setEvents([])
    }
  }, [currentInspectionId])

  useEffect(() => {
    if (currentInspectionId) void refreshEvents()
  }, [currentInspectionId, refreshEvents])

  useEffect(() => {
    if (!uploadResult || uploadResult.runStatus !== 'PROCESSING') return

    pollingStartedAtRef.current = Date.now()
    const controller = new AbortController()
    const intervalId = window.setInterval(() => {
      if (!uploadResult) return
      if (pollingStartedAtRef.current && Date.now() - pollingStartedAtRef.current > POLLING_TIMEOUT_MS) {
        window.clearInterval(intervalId)
        setNoticeMessage('AI 분석이 지연되고 있습니다. 잠시 후 검사 상세에서 상태를 다시 확인해주세요.')
        return
      }
      void getInspectionDetail(uploadResult.inspectionId, controller.signal)
        .then((detail) => {
          applyInspectionDetail(detail, setUploadResult, setNoticeMessage, intervalId)
        })
        .catch(() => undefined)
    }, POLLING_INTERVAL_MS)

    return () => {
      controller.abort()
      window.clearInterval(intervalId)
    }
  }, [uploadResult])

  const captureFrame = useCallback(async () => {
    const video = videoRef.current
    if (!video || isCapturing) return
    if (video.videoWidth <= 0 || video.videoHeight <= 0) {
      setErrorMessage('카메라 프레임을 아직 읽을 수 없습니다. 잠시 후 다시 시도해 주세요.')
      return
    }

    setIsCapturing(true)
    setErrorMessage(null)
    setNoticeMessage(null)

    try {
      const canvas = document.createElement('canvas')
      canvas.width = video.videoWidth
      canvas.height = video.videoHeight
      const context = canvas.getContext('2d')

      if (!context) {
        throw new Error('프레임 캡처용 캔버스를 초기화하지 못했습니다.')
      }

      context.drawImage(video, 0, 0, canvas.width, canvas.height)

      const blob = await new Promise<Blob>((resolve, reject) => {
        canvas.toBlob(
          (nextBlob) => {
            if (nextBlob) {
              resolve(nextBlob)
              return
            }
            reject(new Error('카메라 프레임 Blob 변환에 실패했습니다.'))
          },
          'image/jpeg',
          0.92,
        )
      })

      const timestamp = Date.now()
      const file = new File([blob], `captured-frame-${timestamp}.jpg`, {
        type: 'image/jpeg',
      })
      const startedAt = performance.now()
      const result = await uploadInspection({
        file,
        inputMode: 'IMAGE',
        sourceType: 'BROWSER_CAMERA',
        roiMode: 'FULL_FRAME',
        qualityGateEnabled: true,
        idempotencyKey: createInspectionIdempotencyKey('camera-capture'),
      })

      setUploadResult(result)
      setCurrentInspectionId(result.inspectionId)
      setRequestDurationMs(performance.now() - startedAt)
      setNoticeMessage('카메라 프레임 1장을 캡처해 검사 요청을 접수했습니다.')
      await refreshEvents()
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : '카메라 검사 요청에 실패했습니다.')
    } finally {
      setIsCapturing(false)
    }
  }, [isCapturing, refreshEvents])

  const statusMessage = useMemo(() => {
    if (isCameraLoading) return '카메라 미리보기를 준비하는 중입니다.'
    if (devices.length === 0) return '사용 가능한 브라우저 카메라가 없습니다.'
    if (!selectedDeviceId) return '검사에 사용할 카메라를 선택해 주세요.'
    if (isCapturing) return '현재 프레임을 캡처해 검사 요청을 전송하는 중입니다.'
    if (uploadResult) return '최근 카메라 캡처 검사 요청이 접수되었습니다.'
    if (isCameraReady) return '버튼을 누른 순간의 프레임 1장을 이미지 검사로 처리합니다.'
    return '브라우저 카메라 권한을 허용하면 현재 화면 검사 기능을 사용할 수 있습니다.'
  }, [devices.length, isCameraLoading, isCameraReady, isCapturing, selectedDeviceId, uploadResult])

  return {
    videoRef,
    devices,
    selectedDeviceId,
    currentInspectionId,
    isCameraLoading,
    isCameraReady,
    isCapturing,
    events,
    errorMessage,
    noticeMessage,
    statusMessage,
    uploadResult,
    requestDurationMs,
    setSelectedDeviceId,
    refreshDevices,
    refreshEvents,
    captureFrame,
  }
}

function validateInspectionFile(file: File) {
  const extension = file.name.split('.').pop()?.toLowerCase()
  if (!extension || !ALLOWED_EXTENSIONS.includes(extension)) {
    return '현재 MVP에서는 JPG, JPEG, PNG, WEBP 이미지 파일만 지원합니다.'
  }

  if (file.type?.startsWith('video/')) {
    return '현재 MVP에서는 이미지 파일만 지원합니다.'
  }

  if (file.type && !ALLOWED_MIME_TYPES.includes(file.type)) {
    return '지원하지 않는 이미지 MIME 타입입니다. JPG, PNG, WEBP 파일만 업로드할 수 있습니다.'
  }

  if (file.size > MAX_UPLOAD_SIZE) {
    return '파일 크기가 2GB를 초과했습니다.'
  }

  return null
}

function createInspectionIdempotencyKey(prefix: string) {
  const random = Math.random().toString(36).slice(2, 10)
  return `${prefix}-${Date.now()}-${random}`
}

function applyInspectionDetail(
  detail: InspectionDetail,
  setUploadResult: (value: UploadInspectionResponse | null | ((prev: UploadInspectionResponse | null) => UploadInspectionResponse | null)) => void,
  setNoticeMessage: (value: string | null) => void,
  intervalId: number,
) {
  setUploadResult((prev) =>
    prev
      ? {
          ...prev,
          runStatus: detail.runStatus,
        }
      : prev,
  )

  if (detail.runStatus === 'COMPLETED') {
    window.clearInterval(intervalId)
    setNoticeMessage('AI 분석이 완료되었습니다. 검사 결과를 확인해주세요.')
    return
  }

  if (detail.runStatus === 'FAILED') {
    window.clearInterval(intervalId)
    setNoticeMessage(
      detail.errorCode
        ? `AI 분석이 실패했습니다. errorCode: ${detail.errorCode}`
        : 'AI 분석이 실패했습니다. 검사 상세에서 실패 사유를 확인해주세요.',
    )
  }
}
