import type {
  AnomalyRegion,
  ResultArtifact,
  ResultDecision,
  ResultDecisionInfo,
  ResultImage,
  ResultInspection,
  ResultModelInfo,
  ResultStatus,
  ResultTarget,
  ReviewQueueSummary,
} from '../../../entities/result/model/types'

export type ResultListSummary = {
  totalCount: number
  normalCount: number
  defectCount: number
  retestCount: number
  avgScore?: number | null
}

export type ResultSummary = {
  resultId: number
  inspectionId: number
  targetId?: number | null
  targetName?: string | null
  equipmentName?: string | null
  productName?: string | null
  runType?: string | null
  inputType?: string | null
  sourceType?: string | null
  score?: number | null
  confidence?: number | null
  decisionCode?: ResultDecision | string | null
  finalDecisionCode?: ResultDecision | string | null
  resultStatus?: ResultStatus | string | null
  location?: string | null
  reviewRequired?: boolean | null
  startedAt?: string | null
  completedAt?: string | null
  createdAt?: string | null
}

export type ResultListQuery = {
  keyword?: string
  from?: string
  to?: string
  equipmentName?: string
  productName?: string
  runType?: string
  decisionCode?: ResultDecision | ''
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
  /** 목록과 동일 필터 기준 전역 집계(백엔드 `ResultPageResponse.summary`) */
  summary?: ResultListSummary | null
}

export type ResultDetail = {
  resultId: number
  inspectionId: number
  target: ResultTarget
  inspection: ResultInspection
  result: ResultDecisionInfo
  model?: ResultModelInfo | null
  artifacts: ResultArtifact[]
  images: ResultImage[]
  review: ReviewQueueSummary
}

export type {
  AnomalyRegion,
  ResultArtifact,
  ResultDecision,
  ResultDecisionInfo,
  ResultImage,
  ResultInspection,
  ResultModelInfo,
  ResultStatus,
  ResultTarget,
  ReviewQueueSummary,
}
