export type UploadInspectionResponse = {
  inspectionId: number
  runStatus: string
}

export type UploadInspectionPayload = {
  file: File
  targetId?: number | null
  thresholdId?: number | null
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
  fileKind: 'image' | 'video'
  selectedAt: Date
  width?: number
  height?: number
}

export type CameraSource = {
  cameraId: number
  organizationId?: number
  userId?: number
  cameraName: string
  streamUrl?: string
  status?: string
  createdAt?: string
  updatedAt?: string
}

export type StartRealtimeInspectionRequest = {
  targetId?: number | null
  cameraId: number
  thresholdId?: number | null
}

export type StartRealtimeInspectionResponse = {
  inspectionId: number
  runStatus: string
  cameraId: number
  startedAt?: string
}

export type StopRealtimeInspectionResponse = {
  inspectionId: number
  runStatus: string
  completedAt?: string
}

export type InspectionEvent = {
  eventId: number
  inspectionId: number
  eventType: string
  message?: string
  createdAt?: string
}
