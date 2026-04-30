export type IndexingStatus = 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED'

/**
 * TODO: 목록 API에 인덱싱 실패 사유(`indexErrorMessage` 등) 필드가 생기면 테이블에서 바로 표시한다.
 * 현재는 `GET /documents` 목록 DTO에 해당 필드가 없어 상세/수정 화면에서만 확인 가능하다.
 */
export type DocumentListItem = {
  documentId: number
  title: string
  documentType: string
  category?: string | null
  equipmentType?: string | null
  tags: string[]
  authorName?: string | null
  indexingStatus: IndexingStatus
  versionNo: number
  createdAt?: string | null
  updatedAt?: string | null
  lastUsedAt?: string | null
  fileSize?: number | null
  fileName?: string | null
}

export type DocumentPageResponse = {
  content: DocumentListItem[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export type DocumentSummary = {
  totalCount: number
  pdfCount: number
  docxCount: number
  completedCount: number
  processingCount: number
  failedCount: number
}

export type DocumentVersion = {
  documentVersionId: number
  versionNo: number
  fileId?: number | null
  fileName?: string | null
  fileSize?: number | null
  fileExt?: string | null
  mimeType?: string | null
  fileHash?: string | null
  indexingStatus: IndexingStatus
  indexedChunkCount?: number | null
  indexErrorMessage?: string | null
  indexedAt?: string | null
  createdAt?: string | null
}

export type DocumentDetail = {
  documentId: number
  title: string
  documentType: string
  category?: string | null
  equipmentType?: string | null
  description?: string | null
  tags: string[]
  ownerUserId: number
  authorName: string
  currentStatus: string
  latestVersion: DocumentVersion
  createdAt?: string | null
  updatedAt?: string | null
}

export type DocumentSearchParams = {
  keyword?: string
  documentType?: string
  indexingStatus?: string
  category?: string
  equipmentType?: string
  author?: string
  startDate?: string
  endDate?: string
  page?: number
  size?: number
}

/** POST /documents 응답 */
export type DocumentCreateResult = {
  documentId: number
  documentVersionId?: number
  indexingStatus?: string
}
