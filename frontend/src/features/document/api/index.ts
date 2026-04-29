import { apiClient } from '../../../shared/api/client'
import type {
  DocumentDetail,
  DocumentPageResponse,
  DocumentSearchParams,
  DocumentSummary,
} from '../types'

type ApiResponse<T> = {
  success: boolean
  data: T
  message?: string
}

export async function getDocuments(params: DocumentSearchParams): Promise<DocumentPageResponse> {
  const response = await apiClient.get<ApiResponse<DocumentPageResponse>>('/documents', { params })
  return response.data.data
}

export async function getDocumentSummary(): Promise<DocumentSummary> {
  const response = await apiClient.get<ApiResponse<DocumentSummary>>('/documents/summary')
  return response.data.data
}

export async function getDocumentDetail(documentId: number): Promise<DocumentDetail> {
  const response = await apiClient.get<ApiResponse<DocumentDetail>>(`/documents/${documentId}`)
  return response.data.data
}

export async function createDocument(formData: FormData) {
  const response = await apiClient.post<ApiResponse<{ documentId: number }>>('/documents', formData)
  return response.data.data
}

export async function updateDocument(documentId: number, body: Record<string, unknown>) {
  const response = await apiClient.patch<ApiResponse<DocumentDetail>>(`/documents/${documentId}`, body)
  return response.data.data
}

export async function uploadDocumentVersion(documentId: number, formData: FormData) {
  const response = await apiClient.post<ApiResponse<{ documentVersionId: number }>>(
    `/documents/${documentId}/versions`,
    formData,
  )
  return response.data.data
}

export async function deleteDocument(documentId: number) {
  await apiClient.delete(`/documents/${documentId}`)
}
