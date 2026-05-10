export type ResultDecision = 'NORMAL' | 'DEFECT' | 'RETEST'

export type ResultStatus = 'SUCCESS' | 'FAILED' | 'REVIEW_REQUIRED' | 'CORRECTED'

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

export type ResultModelInfo = {
  modelId?: number | null
  modelName?: string | null
  versionName?: string | null
  modelCategory?: string | null
  modelProfile?: string | null
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
  /** DB `image` 행이 없이 inspection 입력만으로 채워질 때 null */
  imageId?: number | null
  fileId?: number | null
  imageRole?: string | null
  regions: AnomalyRegion[]
}

export type ReviewQueueSummary = {
  reviewQueueId?: number | null
  reviewRequired?: boolean | null
  queueStatus?: string | null
  queuedReason?: string | null
}
