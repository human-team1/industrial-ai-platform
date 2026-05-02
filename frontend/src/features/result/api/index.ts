import { apiClient, normalizeApiError } from '../../../shared/api/client'
import type { ResultDetail, ResultListQuery, ResultPageResponse } from '../types'

type ApiResponse<T> = {
  success: boolean
  data: T
  message?: string
}

export async function fetchResults(
  query: ResultListQuery,
  signal?: AbortSignal,
): Promise<ResultPageResponse> {
  try {
    const params = compactParams(query)
    const response = await apiClient.get<ApiResponse<ResultPageResponse>>('/results', { params, signal })
    return response.data.data
  } catch (error) {
    throw normalizeApiError(error)
  }
}

export async function fetchResultDetail(resultId: number): Promise<ResultDetail> {
  try {
    const response = await apiClient.get<ApiResponse<ResultDetail>>(`/results/${resultId}`)
    return response.data.data
  } catch (error) {
    throw normalizeApiError(error)
  }
}

export async function fetchResultFilePreview(fileId: number): Promise<Blob> {
  try {
    const response = await apiClient.get<Blob>(`/files/${fileId}/preview`, { responseType: 'blob' })
    return response.data
  } catch (error) {
    throw normalizeApiError(error)
  }
}

function compactParams(query: ResultListQuery) {
  return Object.fromEntries(
    Object.entries(query).filter(([, value]) => value !== undefined && value !== null && value !== ''),
  )
}
