export type UploadInspectionResponse = {
  inspectionId: number
  runStatus: string
}

export type InspectionDetail = {
  inspectionId: number
  runStatus: string
  errorCode?: string | null
  completedAt?: string | null
}

export type InspectionInputMode = 'IMAGE' | 'VIDEO'
export type InspectionSourceType = 'IMAGE' | 'BROWSER_CAMERA'
export type InspectionRoiMode = 'FULL_FRAME' | 'FIXED'

export type UploadInspectionPayload = {
  file: File
  targetId?: number | null
  thresholdId?: number | null
  inputMode?: InspectionInputMode
  sourceType?: InspectionSourceType
  roiMode?: InspectionRoiMode
  qualityGateEnabled?: boolean
  idempotencyKey?: string
}

export type AnalysisTargetOption = {
  id: number
  name: string
  type?: string
  status?: string
}

export type ThresholdOption = {
  id?: number
  name: string
  anomalyThreshold?: number
  lowConfidenceThreshold?: number
  source?: string
}

export type SelectedInspectionFile = {
  file: File
  previewUrl: string
  fileKind: 'image'
  selectedAt: Date
  width?: number
  height?: number
}

export type BrowserCameraDevice = {
  deviceId: string
  label: string
}

export type InspectionEvent = {
  eventId: number
  inspectionId: number
  eventType: string
  message?: string
  createdAt?: string
}
