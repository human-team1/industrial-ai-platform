export type ResultDecision = 'NORMAL' | 'DEFECT' | 'RETEST'

export type ResultStatus = 'SUCCESS' | 'FAILED' | 'REVIEW_REQUIRED' | 'CORRECTED'

export type ResultSummary = {
  resultId: number
  inspectionId: number
  targetId?: number | null
  targetName?: string | null
  equipmentName?: string | null
  productName?: string | null
  runType?: string | null
  inputType?: string | null
  score?: number | null
  confidence?: number | null
  decisionCode?: ResultDecision | string | null
  finalDecisionCode?: ResultDecision | string | null
  resultStatus?: ResultStatus | string | null
  reviewRequired?: boolean | null
  startedAt?: string | null
  completedAt?: string | null
  createdAt?: string | null
}

export type ResultListQuery = {
  from?: string
  to?: string
  equipmentName?: string
  productName?: string
  decision?: ResultDecision | ''
  resultStatus?: ResultStatus | ''
  page?: number
  size?: number
}

export type ResultPageResponse = {
  content: ResultSummary[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export type ResultTarget = {
  targetId?: number | null
  targetName?: string | null
  equipmentName?: string | null
  productName?: string | null
}

export type ResultInspection = {
  runType?: string | null
  inputType?: string | null
  sourceType?: string | null
  runStatus?: string | null
  startedAt?: string | null
  completedAt?: string | null
}

export type ResultDecisionInfo = {
  score?: number | null
  confidence?: number | null
  decisionCode?: ResultDecision | string | null
  finalDecisionCode?: ResultDecision | string | null
  resultStatus?: ResultStatus | string | null
  thresholdSource?: string | null
  appliedThreshold?: number | null
  modelVersionId?: number | null
  failureReason?: string | null
  createdAt?: string | null
}

export type ResultArtifact = {
  artifactId: number
  artifactType?: string | null
  fileId?: number | null
}

export type AnomalyRegion = {
  regionId: number
  labelCode?: string | null
  bboxX?: number | null
  bboxY?: number | null
  bboxW?: number | null
  bboxH?: number | null
  score?: number | null
}

export type ResultImage = {
  imageId: number
  fileId?: number | null
  imageRole?: string | null
  regions: AnomalyRegion[]
}

export type ReviewQueueSummary = {
  reviewRequired?: boolean | null
  queueStatus?: string | null
  queuedReason?: string | null
}

export type ResultDescription = {
  summary?: string | null
  recommendedAction?: string | null
}

export type ResultChecklistItem = {
  title: string
  description?: string | null
  priority: 'REQUIRED' | 'RECOMMENDED' | 'OPTIONAL' | string
}

export type ResultEventLog = {
  eventId: number
  eventType?: string | null
  message?: string | null
  createdAt?: string | null
}

export type RelatedResult = {
  resultId: number
  createdAt?: string | null
  score?: number | null
  decisionCode?: string | null
  location?: string | null
}

export type ResultDetail = {
  resultId: number
  inspectionId: number
  target: ResultTarget
  inspection: ResultInspection
  result: ResultDecisionInfo
  artifacts: ResultArtifact[]
  images: ResultImage[]
  review: ReviewQueueSummary
  description?: ResultDescription | null
  checklist?: ResultChecklistItem[] | null
  eventLogs?: ResultEventLog[] | null
  relatedResults?: RelatedResult[] | null
}
