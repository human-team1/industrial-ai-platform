import { apiClient, normalizeApiError } from '../../../shared/api/client'
import type { DashboardOverview } from '../../../entities/dashboard/model/types'

type ApiResponse<T> = {
  success: boolean
  data: T
  message?: string
}

export type DashboardOverviewQuery = {
  startDate?: string
  endDate?: string
  organizationId?: number
}

export async function fetchDashboardOverview(
  query: DashboardOverviewQuery,
  signal?: AbortSignal,
): Promise<DashboardOverview> {
  try {
    const response = await apiClient.get<ApiResponse<DashboardOverview>>('/dashboard/overview', {
      params: compactParams(query),
      signal,
    })
    return response.data.data
  } catch (error) {
    throw normalizeApiError(error)
  }
}

function compactParams(query: DashboardOverviewQuery) {
  return Object.fromEntries(
    Object.entries(query).filter(([, value]) => value !== undefined && value !== null && value !== ''),
  )
}
