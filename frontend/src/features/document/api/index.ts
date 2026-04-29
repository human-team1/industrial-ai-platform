import { apiClient } from '../../../shared/api/client'
import type { CreateDocumentRequest, CreateDocumentResponse, DocumentIndexingStatus } from '../types'

// 파일 업로드 API
export const uploadFile = async (file: File): Promise<{ fileId: number }> => {
  const formData = new FormData()
  formData.append('file', file)

  const response = await apiClient.post<{
    success: boolean
    data: { fileId: number }
    message: string
  }>('/api/v1/files', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
  return response.data.data
}

// 문서 생성 API
export const createDocument = async (
  data: CreateDocumentRequest
): Promise<CreateDocumentResponse> => {
  const response = await apiClient.post<{
    success: boolean
    data: CreateDocumentResponse
    message: string
  }>('/api/v1/documents', data)
  return response.data.data
}

// 인덱싱 상태 조회 API
export const getIndexingStatus = async (
  documentVersionId: number
): Promise<DocumentIndexingStatus> => {
  const response = await apiClient.get<{
    success: boolean
    data: DocumentIndexingStatus
    message: string
  }>(`/api/v1/documents/versions/${documentVersionId}/indexing-status`)
  return response.data.data
}