export * from '../../../entities/document/model/types'
export * from '../api/types'

// Backward-compat aliases (제거 예정, 후속 정리 PR에서 사용처 0건 확인 후 삭제)
export type { Document as DocumentListItem } from '../../../entities/document/model/types'
export type { DocumentDetailResponse as DocumentDetail } from '../api/types'
