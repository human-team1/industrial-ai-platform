import {
  createDocument as createDocumentBase,
  getDocumentDetail as getDocumentDetailBase,
  updateDocument as updateDocumentBase,
  uploadDocumentVersion as uploadDocumentVersionBase,
} from '../../document/api'
import type {
  DocumentCreateResult,
  DocumentDetailResponse,
  UpdateDocumentMetadataPayload,
} from '../../document/api/types'
import { apiClient } from '../../../shared/api/client'

export const createDocument = createDocumentBase
export const getDocumentDetail = getDocumentDetailBase
export const updateDocument = updateDocumentBase
export const uploadDocumentVersion = uploadDocumentVersionBase

export type { UpdateDocumentMetadataPayload, DocumentCreateResult, DocumentDetailResponse }

export async function getDocumentFilePreview(fileId: number): Promise<Blob> {
  const response = await apiClient.get<Blob>(`/files/${fileId}/download`, {
    responseType: 'blob',
  })
  return response.data
}
