import { apiClient } from '../../../shared/api/client'
import type { UserMeResponse } from '../types'

export async function getMyProfile(): Promise<UserMeResponse> {
  const response = await apiClient.get<{ success: boolean; data: UserMeResponse }>('/users/me')
  return response.data.data
}
