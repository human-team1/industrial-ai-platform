import { apiClient, normalizeApiError } from '../../../shared/api/client'
import type {
  DocumentCreateResult,
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

function compactParams(params: DocumentSearchParams) {
  return Object.fromEntries(
    Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== ''),
  )
}

export async function getDocuments(
  params: DocumentSearchParams,
  signal?: AbortSignal,
): Promise<DocumentPageResponse> {
  try {
    const response = await apiClient.get<ApiResponse<DocumentPageResponse>>('/documents', {
      params: compactParams(params),
      signal,
    })
    return response.data.data
  } catch (error) {
    throw normalizeApiError(error)
  }
}

export async function getDocumentSummary(signal?: AbortSignal): Promise<DocumentSummary> {
  try {
    const response = await apiClient.get<ApiResponse<DocumentSummary>>('/documents/summary', { signal })
    return response.data.data
  } catch (error) {
    throw normalizeApiError(error)
  }
}

export async function getDocumentDetail(documentId: number): Promise<DocumentDetail> {
  try {
    const response = await apiClient.get<ApiResponse<DocumentDetail>>(`/documents/${documentId}`)
    return response.data.data
  } catch (error) {
    throw normalizeApiError(error)
  }
}

export async function createDocument(formData: FormData): Promise<DocumentCreateResult> {
  try {
    const response = await apiClient.post<ApiResponse<DocumentCreateResult>>('/documents', formData)
    return response.data.data
  } catch (error) {
    throw normalizeApiError(error)
  }
}

export type UpdateDocumentMetadataPayload = {
  title: string
  category?: string
  equipmentType?: string
  description?: string
  tags: string[]
}

export async function updateDocument(documentId: number, body: UpdateDocumentMetadataPayload) {
  try {
    const response = await apiClient.patch<ApiResponse<DocumentDetail>>(`/documents/${documentId}`, body)
    return response.data.data
  } catch (error) {
    throw normalizeApiError(error)
  }
}

export async function uploadDocumentVersion(documentId: number, formData: FormData) {
  try {
    const response = await apiClient.post<ApiResponse<{ documentVersionId: number }>>(
      `/documents/${documentId}/versions`,
      formData,
    )
    return response.data.data
  } catch (error) {
    throw normalizeApiError(error)
  }
}

export async function deleteDocument(documentId: number) {
  try {
    await apiClient.delete(`/documents/${documentId}`)
  } catch (error) {
    throw normalizeApiError(error)
  }
}
