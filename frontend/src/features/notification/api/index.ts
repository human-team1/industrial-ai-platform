import { apiClient, normalizeApiError } from '../../../shared/api/client'
import type {
  NotificationDetail,
  NotificationListData,
  NotificationListQuery,
} from '../types'

type ApiResponse<T> = {
  success: boolean
  data: T
  message?: string
}

export async function fetchNotifications(
  query: NotificationListQuery,
  signal?: AbortSignal,
): Promise<NotificationListData> {
  try {
    const params = compactParams(query)
    const response = await apiClient.get<ApiResponse<NotificationListData>>('/notifications', {
      params,
      signal,
    })
    return response.data.data
  } catch (error) {
    throw normalizeApiError(error)
  }
}

export async function fetchNotificationDetail(
  notificationId: number,
): Promise<NotificationDetail> {
  try {
    const response = await apiClient.get<ApiResponse<NotificationDetail>>(
      `/notifications/${notificationId}`,
    )
    return response.data.data
  } catch (error) {
    throw normalizeApiError(error)
  }
}

export async function markNotificationAsRead(notificationId: number): Promise<void> {
  try {
    await apiClient.patch(`/notifications/${notificationId}/read`)
  } catch (error) {
    throw normalizeApiError(error)
  }
}

export async function markAllNotificationsAsRead(): Promise<void> {
  try {
    await apiClient.patch('/notifications/read-all')
  } catch (error) {
    throw normalizeApiError(error)
  }
}

export async function fetchUnreadNotificationCount(signal?: AbortSignal): Promise<number> {
  try {
    const response = await apiClient.get<ApiResponse<NotificationListData>>('/notifications', {
      params: { isRead: false, page: 0, size: 1 },
      signal,
    })
    return response.data.data.totalElements
  } catch (error) {
    throw normalizeApiError(error)
  }
}

function compactParams(query: NotificationListQuery): Record<string, unknown> {
  return Object.fromEntries(
    Object.entries(query).filter(([, value]) => value !== undefined && value !== null && value !== ''),
  )
}
