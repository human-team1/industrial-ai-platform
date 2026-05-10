import { apiClient, normalizeApiError } from '../../../shared/api/client'
import type {
  AsyncJob,
  OperationLog,
  OperationPolicy,
  PageResponse,
  SystemComponentStatus,
  SystemStatus,
} from '../../../entities/operation/model/types'

type ApiResponse<T> = {
  success: boolean
  data: T
  message?: string
}

export type OperationLogQuery = {
  level?: string
  sourceComponent?: string
  eventType?: string
  eventStatus?: string
  startDate?: string
  endDate?: string
  sort?: string
  page?: number
  size?: number
}

export async function fetchSystemStatus(signal?: AbortSignal) {
  const response = await apiClient.get<ApiResponse<SystemStatus>>('/admin/system-status', { signal })
  return response.data.data
}

export async function fetchSystemComponents(signal?: AbortSignal) {
  const response = await apiClient.get<ApiResponse<SystemComponentStatus[]>>('/admin/system-components', { signal })
  return response.data.data
}

export async function fetchOperationLogs(query: OperationLogQuery, signal?: AbortSignal) {
  const response = await apiClient.get<ApiResponse<PageResponse<OperationLog>>>('/admin/operation-logs', {
    params: compactParams(query),
    signal,
  })
  return response.data.data
}

export async function fetchAsyncJobs(signal?: AbortSignal) {
  const response = await apiClient.get<ApiResponse<PageResponse<AsyncJob>>>('/admin/async-jobs', {
    params: { page: 0, size: 10 },
    signal,
  })
  return response.data.data
}

export async function fetchOperationPolicies(query: { category?: string; activeOnly?: boolean }, signal?: AbortSignal) {
  const params: Record<string, unknown> = {}
  if (query.category) params.category = query.category
  if (query.activeOnly) params.activeOnly = true
  const response = await apiClient.get<ApiResponse<OperationPolicy[]>>('/admin/operation-policies', {
    params,
    signal,
  })
  return response.data.data
}

export async function updateOperationPolicy(policyId: number, body: { policyValue?: string; isActive?: boolean }) {
  try {
    const response = await apiClient.patch<ApiResponse<OperationPolicy>>(`/admin/operation-policies/${policyId}`, body)
    return response.data.data
  } catch (error) {
    throw normalizeApiError(error)
  }
}

function compactParams(query: Record<string, unknown>) {
  return Object.fromEntries(
    Object.entries(query).filter(([, value]) => value !== undefined && value !== null && value !== ''),
  )
}
