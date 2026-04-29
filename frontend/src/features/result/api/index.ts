import { apiClient } from '../../../shared/api/client'
import type { ResultDetail, ResultListQuery, ResultPageResponse } from '../types'

type ApiResponse<T> = {
  success: boolean
  data: T
  message?: string
}

export async function fetchResults(query: ResultListQuery): Promise<ResultPageResponse> {
  const params = compactParams(query)
  const response = await apiClient.get<ApiResponse<ResultPageResponse>>('/results', { params })
  return response.data.data
}

export async function fetchResultDetail(resultId: number): Promise<ResultDetail> {
  const response = await apiClient.get<ApiResponse<ResultDetail>>(`/results/${resultId}`)
  return response.data.data
}

function compactParams(query: ResultListQuery) {
  return Object.fromEntries(
    Object.entries(query).filter(([, value]) => value !== undefined && value !== null && value !== ''),
  )
}
