import type { Document, DocumentVersion, IndexingStatus } from '../../../entities/document/model/types'

export type DocumentListItemDto = Document

export type DocumentPageResponse = {
  content: DocumentListItemDto[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export type DocumentDetailResponse = {
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
  indexingStatus?: IndexingStatus | string
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

export type UpdateDocumentMetadataPayload = {
  title: string
  category?: string
  equipmentType?: string
  description?: string
  tags: string[]
}
