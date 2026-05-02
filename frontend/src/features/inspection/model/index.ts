import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import {
  getAnalysisTargets,
  getCameraSources,
  getInspectionEvents,
  getMyThresholds,
  startRealtimeInspection,
  stopRealtimeInspection,
  uploadInspection,
} from '../api'
import type {
  AnalysisTargetOption,
  CameraSource,
  InspectionEvent,
  SelectedInspectionFile,
  ThresholdOption,
  UploadInspectionResponse,
} from '../types'

const MAX_UPLOAD_SIZE = 2 * 1024 * 1024 * 1024
const ALLOWED_EXTENSIONS = ['jpg', 'jpeg', 'png', 'mp4', 'mov', 'avi']
const ALLOWED_MIME_TYPES = [
  'image/jpeg',
  'image/png',
  'video/mp4',
  'video/quicktime',
  'video/x-msvideo',
  'video/avi',
]

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

  useEffect(() => {
    const controller = new AbortController()
    setLoadingOptions(true)
    Promise.all([
      getAnalysisTargets(controller.signal),
      getMyThresholds(controller.signal),
    ])
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

  const selectedThreshold = useMemo(
    () => thresholdOptions.find((option) => option.id === selectedThresholdId) ?? null,
    [selectedThresholdId, thresholdOptions],
  )

  const statusMessage = useMemo(() => {
    if (uploading) return '검사 요청을 접수하는 중입니다.'
    if (uploadResult) return '검사 요청이 접수되었습니다.'
    if (selectedFile) return '파일이 선택되었습니다. 탐지 실행을 눌러 검사 요청을 접수하세요.'
    return '검사할 이미지 또는 영상을 업로드하세요.'
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
    const fileKind = getFileKind(file)
    const nextFile: SelectedInspectionFile = {
      file,
      previewUrl,
      fileKind,
      selectedAt: new Date(),
    }
    setSelectedFileState(nextFile)

    if (fileKind === 'image') {
      const image = new Image()
      image.onload = () => {
        setSelectedFileState((prev) =>
          prev?.previewUrl === previewUrl
            ? { ...prev, width: image.naturalWidth, height: image.naturalHeight }
            : prev,
        )
      }
      image.src = previewUrl
    }
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
      })
      setUploadResult(result)
      setRequestDurationMs(performance.now() - startedAt)
      setNoticeMessage('검사 요청이 접수되었습니다. 현재 상태: PROCESSING')
    } catch (error) {
      setErrorMessage(
        error instanceof Error
          ? error.message
          : '검사 요청에 실패했습니다. 파일 형식과 네트워크 상태를 확인해주세요.',
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
  const [cameras, setCameras] = useState<CameraSource[]>([])
  const [selectedCameraId, setSelectedCameraId] = useState<number | null>(null)
  const [currentInspectionId, setCurrentInspectionId] = useState<number | null>(null)
  const [runStatus, setRunStatus] = useState<'IDLE' | 'PROCESSING' | 'STOPPED' | string>('IDLE')
  const [startedAt, setStartedAt] = useState<Date | null>(null)
  const [elapsedSeconds, setElapsedSeconds] = useState(0)
  const [events, setEvents] = useState<InspectionEvent[]>([])
  const [loadingCameras, setLoadingCameras] = useState(false)
  const [isStarting, setIsStarting] = useState(false)
  const [isStopping, setIsStopping] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [noticeMessage, setNoticeMessage] = useState<string | null>(null)

  const selectedCamera = useMemo(
    () => cameras.find((camera) => camera.cameraId === selectedCameraId) ?? null,
    [cameras, selectedCameraId],
  )
  const isRunning = runStatus === 'PROCESSING'

  const refreshCameras = useCallback(async () => {
    setLoadingCameras(true)
    setErrorMessage(null)
    try {
      const nextCameras = await getCameraSources()
      setCameras(nextCameras)
      setSelectedCameraId((current) => {
        if (current && nextCameras.some((camera) => camera.cameraId === current)) return current
        return nextCameras[0]?.cameraId ?? null
      })
    } catch (error) {
      setCameras([])
      setErrorMessage(error instanceof Error ? error.message : '카메라 목록을 불러오지 못했습니다.')
    } finally {
      setLoadingCameras(false)
    }
  }, [])

  useEffect(() => {
    void refreshCameras()
  }, [refreshCameras])

  useEffect(() => {
    if (!isRunning || !startedAt) return undefined
    const timer = window.setInterval(() => {
      setElapsedSeconds(Math.max(0, Math.floor((Date.now() - startedAt.getTime()) / 1000)))
    }, 1000)
    return () => window.clearInterval(timer)
  }, [isRunning, startedAt])

  const refreshEvents = useCallback(async () => {
    if (!currentInspectionId) return
    try {
      const nextEvents = await getInspectionEvents(currentInspectionId)
      setEvents(nextEvents)
    } catch {
      setEvents([])
    }
  }, [currentInspectionId])

  const start = useCallback(async () => {
    if (!selectedCameraId || isRunning || isStarting) return
    setIsStarting(true)
    setErrorMessage(null)
    setNoticeMessage('실시간 탐지를 시작하는 중입니다.')
    try {
      const result = await startRealtimeInspection({ cameraId: selectedCameraId })
      setCurrentInspectionId(result.inspectionId)
      setRunStatus(result.runStatus)
      setStartedAt(result.startedAt ? new Date(result.startedAt) : new Date())
      setElapsedSeconds(0)
      setEvents([])
      setNoticeMessage('실시간 탐지가 진행 중입니다.')
    } catch (error) {
      setCurrentInspectionId(null)
      setRunStatus('IDLE')
      setStartedAt(null)
      setNoticeMessage(null)
      setErrorMessage(error instanceof Error ? error.message : '실시간 탐지 요청에 실패했습니다.')
    } finally {
      setIsStarting(false)
    }
  }, [isRunning, isStarting, selectedCameraId])

  const stop = useCallback(async () => {
    if (!currentInspectionId || !isRunning || isStopping) return
    setIsStopping(true)
    setErrorMessage(null)
    setNoticeMessage('실시간 탐지를 중지하는 중입니다.')
    try {
      const result = await stopRealtimeInspection(currentInspectionId)
      setRunStatus(result.runStatus)
      setStartedAt(null)
      setNoticeMessage('실시간 탐지가 중지되었습니다.')
      await refreshEvents()
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : '실시간 탐지 요청에 실패했습니다.')
    } finally {
      setIsStopping(false)
    }
  }, [currentInspectionId, isRunning, isStopping, refreshEvents])

  useEffect(() => {
    if (currentInspectionId) void refreshEvents()
  }, [currentInspectionId, refreshEvents])

  const statusMessage = useMemo(() => {
    if (loadingCameras) return '카메라 목록을 불러오는 중입니다.'
    if (cameras.length === 0) return '등록된 카메라가 없습니다.'
    if (!selectedCameraId) return '카메라를 선택해주세요.'
    if (isStarting) return '실시간 탐지를 시작하는 중입니다.'
    if (isStopping) return '실시간 탐지를 중지하는 중입니다.'
    if (isRunning) return '실시간 탐지가 진행 중입니다.'
    if (runStatus === 'STOPPED') return '실시간 탐지가 중지되었습니다.'
    return '실시간 탐지를 시작하려면 카메라를 선택하고 시작 버튼을 눌러주세요.'
  }, [cameras.length, isRunning, isStarting, isStopping, loadingCameras, runStatus, selectedCameraId])

  return {
    cameras,
    selectedCamera,
    selectedCameraId,
    currentInspectionId,
    runStatus,
    isRunning,
    isStarting,
    isStopping,
    loadingCameras,
    elapsedSeconds,
    events,
    errorMessage,
    noticeMessage,
    statusMessage,
    setSelectedCameraId,
    refreshCameras,
    refreshEvents,
    start,
    stop,
  }
}

function validateInspectionFile(file: File) {
  const extension = file.name.split('.').pop()?.toLowerCase()
  if (!extension || !ALLOWED_EXTENSIONS.includes(extension)) {
    return '지원하지 않는 파일 형식입니다. JPG, PNG, MP4, MOV, AVI 파일을 선택해주세요.'
  }

  if (file.type && !ALLOWED_MIME_TYPES.includes(file.type)) {
    return '지원하지 않는 MIME 타입입니다. 파일 형식을 확인해주세요.'
  }

  if (file.size > MAX_UPLOAD_SIZE) {
    return '파일 크기가 2GB를 초과했습니다.'
  }

  return null
}

function getFileKind(file: File): 'image' | 'video' {
  if (file.type.startsWith('image/')) return 'image'
  const extension = file.name.split('.').pop()?.toLowerCase()
  return extension === 'jpg' || extension === 'jpeg' || extension === 'png' ? 'image' : 'video'
}
