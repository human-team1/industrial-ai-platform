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
  deploymentId: number
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

export type AvailableInspectionModel = {
  deploymentId: number
  modelVersionId: number
  modelId: number
  modelName: string
  versionName: string
  displayName: string
  modelCategory?: string | null
  modelProfile?: string | null
  deploymentScope?: string | null
  organizationId: number
  targetId?: number | null
  thresholdDefault?: number | null
}

export type AvailableRealtimeCamera = {
  cameraId: number
  cameraName: string
  organizationId: number
  targetId?: number | null
  targetName?: string | null
  status?: string | null
  displayName: string
}

export type AvailableInspectionModelsParams = {
  targetId?: number | null
  inspectionType?: 'UPLOAD' | 'REALTIME' | 'REVIEW'
  modelCategory?: 'OBJECT' | 'TEXTURE'
}

export type AvailableRealtimeCamerasParams = {
  targetId?: number | null
}

export type StartRealtimeInspectionPayload = {
  targetId?: number | null
  cameraId: number
  deploymentId: number
  thresholdId?: number | null
}

export type InspectionEvent = {
  eventId: number
  inspectionId: number
  eventType: string
  message?: string
  createdAt?: string
}

export type ProgressStepState = 'complete' | 'progress' | 'pending'

export type ProgressStep = {
  label: string
  state: ProgressStepState
}
