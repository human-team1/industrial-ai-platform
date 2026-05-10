export type IndexingStatus = 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED'

export type DocumentTag = string

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

export type Document = {
  documentId: number
  title: string
  documentType: string
  category?: string | null
  equipmentType?: string | null
  tags: DocumentTag[]
  authorName?: string | null
  indexingStatus: IndexingStatus
  versionNo: number
  createdAt?: string | null
  updatedAt?: string | null
  lastUsedAt?: string | null
  fileSize?: number | null
  fileName?: string | null
}
