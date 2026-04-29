export type IndexingStatus = 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED'

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
  page?: number
  size?: number
}
