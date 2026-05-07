import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import {
  fetchAvailableInspectionModels,
  fetchAvailableRealtimeCameras,
  getAnalysisTargets,
  getInspectionDetail,
  getInspectionEvents,
  getMyThresholds,
  startRealtimeInspection,
  uploadInspection,
} from '../api'
import type {
  AnalysisTargetOption,
  AvailableInspectionModel,
  AvailableRealtimeCamera,
  BrowserCameraDevice,
  InspectionDetail,
  InspectionEvent,
  ProgressStep,
  SelectedInspectionFile,
  ThresholdOption,
  UploadInspectionResponse,
} from '../types'

export function computeProgressSteps({
  selectedFile,
  uploading,
  uploadResult,
}: {
  selectedFile: SelectedInspectionFile | null
  uploading: boolean
  uploadResult: UploadInspectionResponse | null
}): ProgressStep[] {
  const labels = ['파일 업로드', '전처리', '모델 추론', '결과 분석']

  if (uploadResult) {
    return labels.map((label) => ({ label, state: 'complete' as const }))
  }
  if (uploading) {
    return labels.map((label, i) => ({
      label,
      state: i === 0 ? ('progress' as const) : ('pending' as const),
    }))
  }
  return labels.map((label, i) => ({
    label,
    state: i === 0 && selectedFile ? ('progress' as const) : ('pending' as const),
  }))
}

const MAX_UPLOAD_SIZE = 2 * 1024 * 1024 * 1024
const ALLOWED_EXTENSIONS = ['jpg', 'jpeg', 'png', 'webp']
const ALLOWED_MIME_TYPES = ['image/jpeg', 'image/png', 'image/webp']
const POLLING_INTERVAL_MS = 2000
const POLLING_TIMEOUT_MS = 180000

export function useUploadInspection() {
  const [selectedFile, setSelectedFileState] = useState<SelectedInspectionFile | null>(null)
  const [targetOptions, setTargetOptions] = useState<AnalysisTargetOption[]>([])
  const [thresholdOptions, setThresholdOptions] = useState<ThresholdOption[]>([])
  const [modelOptions, setModelOptions] = useState<AvailableInspectionModel[]>([])
  const [selectedTargetId, setSelectedTargetId] = useState<number | null>(null)
  const [selectedThresholdId, setSelectedThresholdId] = useState<number | null>(null)
  const [selectedDeploymentId, setSelectedDeploymentId] = useState<number | null>(null)
  const [loadingOptions, setLoadingOptions] = useState(true)
  const [loadingModels, setLoadingModels] = useState(false)
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
    setErrorMessage(null)

    void Promise.allSettled([
      getAnalysisTargets(controller.signal),
      getMyThresholds(controller.signal),
      fetchAvailableInspectionModels({ inspectionType: 'UPLOAD' }, controller.signal),
    ]).then((results) => {
      if (controller.signal.aborted) return

      const [targetsResult, thresholdsResult, modelsResult] = results

      if (targetsResult.status === 'fulfilled') {
        setTargetOptions(targetsResult.value)
      } else {
        setTargetOptions([])
      }

      if (thresholdsResult.status === 'fulfilled') {
        setThresholdOptions(thresholdsResult.value)
      } else {
        setThresholdOptions([])
      }

      if (modelsResult.status === 'fulfilled') {
        const models = modelsResult.value
        setModelOptions(models)
        setSelectedDeploymentId((current) => pickPreferredId(current, models))
      } else {
        setModelOptions([])
        setSelectedDeploymentId(null)
      }

      const messages: string[] = []
      if (targetsResult.status === 'rejected') {
        messages.push(
          targetsResult.reason instanceof Error
            ? targetsResult.reason.message
            : '검사 대상 목록을 불러오지 못했습니다.',
        )
      }
      if (thresholdsResult.status === 'rejected') {
        messages.push(
          thresholdsResult.reason instanceof Error
            ? thresholdsResult.reason.message
            : '임계값 목록을 불러오지 못했습니다.',
        )
      }
      if (modelsResult.status === 'rejected') {
        messages.push(
          modelsResult.reason instanceof Error
            ? modelsResult.reason.message
            : '사용 가능한 배포 모델을 불러오지 못했습니다.',
        )
      }
      if (messages.length > 0) {
        setErrorMessage(messages.join(' '))
      }
    }).finally(() => setLoadingOptions(false))

    return () => controller.abort()
  }, [])

  useEffect(() => {
    const controller = new AbortController()
    setLoadingModels(true)

    fetchAvailableInspectionModels(
      {
        targetId: selectedTargetId,
        inspectionType: 'UPLOAD',
      },
      controller.signal,
    )
      .then((models) => {
        setModelOptions(models)
        setSelectedDeploymentId((current) => pickPreferredId(current, models))
      })
      .catch((error) => {
        if (!controller.signal.aborted) {
          setModelOptions([])
          setSelectedDeploymentId(null)
          setErrorMessage(error instanceof Error ? error.message : '사용 가능한 배포 모델을 조회하지 못했습니다.')
        }
      })
      .finally(() => setLoadingModels(false))

    return () => controller.abort()
  }, [selectedTargetId])

  useEffect(() => {
    return () => {
      if (objectUrlRef.current) {
        URL.revokeObjectURL(objectUrlRef.current)
      }
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
        setNoticeMessage('AI 분석이 지연되고 있습니다. 잠시 후 검사 상세에서 상태를 다시 확인해 주세요.')
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

  const selectedModel = useMemo(
    () => modelOptions.find((option) => option.deploymentId === selectedDeploymentId) ?? null,
    [modelOptions, selectedDeploymentId],
  )

  const statusMessage = useMemo(() => {
    if (uploading) return '이미지 검사 요청을 접수하는 중입니다.'
    if (uploadResult) return '이미지 검사 요청이 접수되었습니다.'
    if (selectedFile) return '이미지가 선택되었습니다. 모델과 검사 대상을 확인한 뒤 검사 실행 버튼을 눌러 주세요.'
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
    if (!selectedFile || !selectedDeploymentId || uploading) return

    setUploading(true)
    setErrorMessage(null)
    setNoticeMessage(null)
    const startedAt = performance.now()

    try {
      const result = await uploadInspection({
        file: selectedFile.file,
        deploymentId: selectedDeploymentId,
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
  }, [selectedDeploymentId, selectedFile, selectedTargetId, selectedThresholdId, uploading])

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
    modelOptions,
    selectedTargetId,
    selectedThresholdId,
    selectedThreshold,
    selectedDeploymentId,
    selectedModel,
    loadingOptions,
    loadingModels,
    uploading,
    errorMessage,
    noticeMessage,
    uploadResult,
    requestDurationMs,
    statusMessage,
    setSelectedFile,
    setSelectedTargetId,
    setSelectedThresholdId,
    setSelectedDeploymentId,
    submit,
    reset,
  }
}

export function useRealtimeInspection() {
  const [targetOptions, setTargetOptions] = useState<AnalysisTargetOption[]>([])
  const [thresholdOptions, setThresholdOptions] = useState<ThresholdOption[]>([])
  const [cameraOptions, setCameraOptions] = useState<AvailableRealtimeCamera[]>([])
  const [modelOptions, setModelOptions] = useState<AvailableInspectionModel[]>([])
  const [selectedTargetId, setSelectedTargetId] = useState<number | null>(null)
  const [selectedThresholdId, setSelectedThresholdId] = useState<number | null>(null)
  // MVP: 상단 카메라 select는 브라우저 카메라(deviceId)를 선택한다.
  const [selectedCameraId, setSelectedCameraId] = useState<string | null>(null)
  const [selectedDeploymentId, setSelectedDeploymentId] = useState<number | null>(null)
  const [currentInspectionId, setCurrentInspectionId] = useState<number | null>(null)
  const [events, setEvents] = useState<InspectionEvent[]>([])
  const [loadingOptions, setLoadingOptions] = useState(true)
  const [loadingCameras, setLoadingCameras] = useState(false)
  const [loadingModels, setLoadingModels] = useState(false)
  const [isStarting, setIsStarting] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [noticeMessage, setNoticeMessage] = useState<string | null>(null)
  const [uploadResult, setUploadResult] = useState<UploadInspectionResponse | null>(null)
  const [requestDurationMs, setRequestDurationMs] = useState<number | null>(null)
  const [captureLoading, setCaptureLoading] = useState(false)
  const [browserStream, setBrowserStream] = useState<MediaStream | null>(null)
  const [browserCameraError, setBrowserCameraError] = useState<string | null>(null)
  const [browserCameraStarting, setBrowserCameraStarting] = useState(false)
  const [browserDevices, setBrowserDevices] = useState<BrowserCameraDevice[]>([])
  const [roiPercent, setRoiPercent] = useState({ x: 25, y: 20, width: 50, height: 50 })
  const pollingStartedAtRef = useRef<number | null>(null)

  const loadBrowserCameraDevices = useCallback(async () => {
    if (typeof navigator === 'undefined' || !navigator.mediaDevices?.enumerateDevices) {
      return
    }
    const devices = await navigator.mediaDevices.enumerateDevices()
    const cameras = devices
      .filter((device) => device.kind === 'videoinput')
      .map((device, index) => ({
        deviceId: device.deviceId,
        label: device.label || `카메라 ${index + 1}`,
      }))
    setBrowserDevices(cameras)
    setSelectedCameraId((current) => {
      if (current && cameras.some((camera) => camera.deviceId === current)) {
        return current
      }
      return cameras[0]?.deviceId ?? null
    })
  }, [])

  const stopBrowserCameraPreview = useCallback(() => {
    setBrowserStream((prev) => {
      if (prev) {
        prev.getTracks().forEach((track) => track.stop())
      }
      return null
    })
  }, [])

  const startBrowserCameraPreview = useCallback(async () => {
    if (typeof navigator === 'undefined' || !navigator.mediaDevices?.getUserMedia) {
      setBrowserCameraError('현재 브라우저는 카메라 미리보기를 지원하지 않습니다.')
      return
    }
    setBrowserCameraStarting(true)
    setBrowserCameraError(null)

    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: selectedCameraId ? { deviceId: { exact: selectedCameraId } } : true,
        audio: false,
      })
      setBrowserStream((prev) => {
        if (prev) {
          prev.getTracks().forEach((track) => track.stop())
        }
        return stream
      })
      await loadBrowserCameraDevices()
    } catch (error) {
      setBrowserCameraError(toBrowserCameraErrorMessage(error))
      setBrowserStream(null)
    } finally {
      setBrowserCameraStarting(false)
    }
  }, [loadBrowserCameraDevices, selectedCameraId])

  useEffect(() => {
    const controller = new AbortController()
    setLoadingOptions(true)
    setErrorMessage(null)

    void Promise.allSettled([
      getAnalysisTargets(controller.signal),
      getMyThresholds(controller.signal),
      // 서버 등록 카메라 목록은 MVP에서 브라우저 카메라와 분리한다.
      fetchAvailableInspectionModels({ inspectionType: 'REALTIME' }, controller.signal),
    ]).then((results) => {
      if (controller.signal.aborted) return

      const [targetsResult, thresholdsResult, modelsResult] = results

      if (targetsResult.status === 'fulfilled') {
        setTargetOptions(targetsResult.value)
      } else {
        setTargetOptions([])
      }

      if (thresholdsResult.status === 'fulfilled') {
        setThresholdOptions(thresholdsResult.value)
      } else {
        setThresholdOptions([])
      }

      if (modelsResult.status === 'fulfilled') {
        const models = modelsResult.value
        setModelOptions(models)
        setSelectedDeploymentId((current) => pickPreferredId(current, models))
      } else {
        setModelOptions([])
        setSelectedDeploymentId(null)
      }

      const messages: string[] = []
      if (targetsResult.status === 'rejected') {
        messages.push(
          targetsResult.reason instanceof Error
            ? targetsResult.reason.message
            : '검사 대상 목록을 불러오지 못했습니다.',
        )
      }
      if (thresholdsResult.status === 'rejected') {
        messages.push(
          thresholdsResult.reason instanceof Error
            ? thresholdsResult.reason.message
            : '임계값 목록을 불러오지 못했습니다.',
        )
      }
      if (modelsResult.status === 'rejected') {
        messages.push(
          modelsResult.reason instanceof Error
            ? modelsResult.reason.message
            : '사용 가능한 배포 모델을 불러오지 못했습니다.',
        )
      }
      if (messages.length > 0) {
        setErrorMessage(messages.join(' '))
      }
    }).finally(() => setLoadingOptions(false))

    return () => controller.abort()
  }, [])

  useEffect(() => {
    void loadBrowserCameraDevices().then(() => startBrowserCameraPreview())
    return () => {
      stopBrowserCameraPreview()
    }
  }, [loadBrowserCameraDevices, startBrowserCameraPreview, stopBrowserCameraPreview])

  useEffect(() => {
    if (!selectedCameraId) return
    void startBrowserCameraPreview()
  }, [selectedCameraId, startBrowserCameraPreview])

  useEffect(() => {
    const controller = new AbortController()
    setLoadingCameras(true)
    setLoadingModels(true)

    void Promise.allSettled([
      fetchAvailableInspectionModels(
        {
          targetId: selectedTargetId,
          inspectionType: 'REALTIME',
        },
        controller.signal,
      ),
    ]).then((results) => {
      if (controller.signal.aborted) return
      const [modelsResult] = results

      if (modelsResult.status === 'fulfilled') {
        setModelOptions(modelsResult.value)
        setSelectedDeploymentId((current) => pickPreferredId(current, modelsResult.value))
      } else {
        setModelOptions([])
        setSelectedDeploymentId(null)
      }

      const messages: string[] = []
      if (modelsResult.status === 'rejected') {
        messages.push(
          modelsResult.reason instanceof Error
            ? modelsResult.reason.message
            : '사용 가능한 배포 모델을 불러오지 못했습니다.',
        )
      }
      if (messages.length > 0) {
        setErrorMessage(messages.join(' '))
      }
    }).finally(() => {
      setLoadingCameras(false)
      setLoadingModels(false)
    })

    return () => controller.abort()
  }, [selectedTargetId])

  useEffect(() => {
    if (!currentInspectionId) {
      setEvents([])
      return
    }
    void getInspectionEvents(currentInspectionId)
      .then(setEvents)
      .catch(() => setEvents([]))
  }, [currentInspectionId])

  useEffect(() => {
    if (!uploadResult || uploadResult.runStatus !== 'PROCESSING') return

    pollingStartedAtRef.current = Date.now()
    const controller = new AbortController()
    const intervalId = window.setInterval(() => {
      if (!uploadResult) return
      if (pollingStartedAtRef.current && Date.now() - pollingStartedAtRef.current > POLLING_TIMEOUT_MS) {
        window.clearInterval(intervalId)
        setNoticeMessage('실시간 탐지 세션이 지연되고 있습니다. 잠시 후 검사 상세에서 상태를 다시 확인해 주세요.')
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
  const selectedModel = useMemo(
    () => modelOptions.find((option) => option.deploymentId === selectedDeploymentId) ?? null,
    [modelOptions, selectedDeploymentId],
  )
  const selectedCamera = useMemo(
    () => browserDevices.find((d) => d.deviceId === selectedCameraId) ?? null,
    [browserDevices, selectedCameraId],
  )

  const canStart = Boolean(selectedCameraId && selectedDeploymentId) && !isStarting

  const statusMessage = useMemo(() => {
    if (isStarting) return '실시간 탐지 세션을 시작하는 중입니다.'
    if (uploadResult) return '실시간 탐지 세션 요청이 접수되었습니다.'
    if (!selectedCameraId) return '실시간 탐지에 사용할 카메라를 선택해 주세요.'
    if (!selectedDeploymentId) return '실시간 탐지에 사용할 배포 모델을 선택해 주세요.'
    return '카메라와 모델을 선택한 뒤 실시간 탐지 시작 버튼을 눌러 주세요.'
  }, [isStarting, selectedCameraId, selectedDeploymentId, uploadResult])

  const start = useCallback(async () => {
    // MVP: 브라우저 카메라 캡처 기반이며 서버 카메라 세션 시작은 후속.
    return
  }, [])

  const inspectCapturedImage = useCallback(
    async (capturedFile: File) => {
      if (!selectedDeploymentId || captureLoading) return
      const roiX = roiPercent.x / 100
      const roiY = roiPercent.y / 100
      const roiWidth = roiPercent.width / 100
      const roiHeight = roiPercent.height / 100
      setCaptureLoading(true)
      setErrorMessage(null)
      setNoticeMessage(null)
      const startedAt = performance.now()
      try {
        const result = await uploadInspection({
          file: capturedFile,
          deploymentId: selectedDeploymentId,
          targetId: selectedTargetId,
          thresholdId: selectedThresholdId,
          inputMode: 'IMAGE',
          sourceType: 'BROWSER_CAMERA',
          roiMode: 'FIXED',
          roiCoordinateType: 'NORMALIZED',
          roiX,
          roiY,
          roiWidth,
          roiHeight,
          qualityGateEnabled: true,
          idempotencyKey: createInspectionIdempotencyKey('browser-capture'),
        })
        setUploadResult(result)
        setCurrentInspectionId(result.inspectionId)
        setRequestDurationMs(performance.now() - startedAt)
        setNoticeMessage('ROI 촬영 이미지 검사 요청이 접수되었습니다.')
      } catch (error) {
        setErrorMessage(error instanceof Error ? error.message : 'ROI 촬영 이미지 검사 요청에 실패했습니다.')
      } finally {
        setCaptureLoading(false)
      }
    },
    [captureLoading, roiPercent.height, roiPercent.width, roiPercent.x, roiPercent.y, selectedDeploymentId, selectedTargetId, selectedThresholdId],
  )

  const refreshEvents = useCallback(async () => {
    if (!currentInspectionId) return
    try {
      const nextEvents = await getInspectionEvents(currentInspectionId)
      setEvents(nextEvents)
    } catch {
      setEvents([])
    }
  }, [currentInspectionId])

  return {
    targetOptions,
    thresholdOptions,
    cameraOptions,
    browserDevices,
    roiPercent,
    modelOptions,
    selectedTargetId,
    selectedThresholdId,
    selectedCameraId,
    selectedDeploymentId,
    selectedThreshold,
    selectedModel,
    selectedCamera,
    currentInspectionId,
    browserStream,
    browserCameraError,
    browserCameraStarting,
    events,
    loadingOptions,
    loadingCameras,
    loadingModels,
    isStarting,
    canStart,
    errorMessage,
    noticeMessage,
    statusMessage,
    uploadResult,
    requestDurationMs,
    captureLoading,
    setSelectedTargetId,
    setSelectedThresholdId,
    setSelectedCameraId,
    setSelectedDeploymentId,
    setRoiPercent,
    startBrowserCameraPreview,
    stopBrowserCameraPreview,
    inspectCapturedImage,
    refreshEvents,
    start,
  }
}

function toBrowserCameraErrorMessage(error: unknown) {
  if (!(error instanceof DOMException)) {
    return '노트북 카메라를 시작하지 못했습니다.'
  }
  if (error.name === 'NotAllowedError') {
    return '카메라 권한이 거부되었습니다. 브라우저 주소창에서 카메라 권한을 허용해 주세요.'
  }
  if (error.name === 'NotFoundError') {
    return '사용 가능한 카메라 장치를 찾지 못했습니다.'
  }
  if (error.name === 'NotReadableError') {
    return '카메라가 다른 앱에서 사용 중입니다. 사용 중인 앱을 종료한 뒤 다시 시도해 주세요.'
  }
  return '노트북 카메라를 시작하지 못했습니다.'
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

function coerceSelectId(value: unknown): number | null {
  if (typeof value === 'number' && Number.isFinite(value)) {
    const n = Math.trunc(value)
    return n > 0 ? n : null
  }
  if (typeof value === 'string' && value.trim() !== '') {
    const n = Math.trunc(Number(value))
    return Number.isFinite(n) && n > 0 ? n : null
  }
  return null
}

function pickPreferredId<T extends Record<string, unknown>>(
  current: number | null,
  items: T[],
  field: 'deploymentId' | 'cameraId' = 'deploymentId',
) {
  const currentNorm = coerceSelectId(current)
  if (currentNorm != null && items.some((item) => coerceSelectId(item[field]) === currentNorm)) {
    return currentNorm
  }

  return coerceSelectId(items[0]?.[field])
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
    setNoticeMessage('AI 분석이 완료되었습니다. 검사 결과를 확인해 주세요.')
    return
  }

  if (detail.runStatus === 'FAILED') {
    window.clearInterval(intervalId)
    setNoticeMessage(
      detail.errorCode
        ? `AI 분석이 실패했습니다. errorCode: ${detail.errorCode}`
        : 'AI 분석이 실패했습니다. 검사 상세에서 실패 사유를 확인해 주세요.',
    )
  }
}
