import { apiClient } from '../../../shared/api/client'
import type { UserMeResponse } from '../../user-profile-view/types'
import type { UpdateMyProfilePayload } from '../types'

export async function updateMyProfile(payload: UpdateMyProfilePayload): Promise<UserMeResponse> {
  const response = await apiClient.patch<{ success: boolean; data: UserMeResponse }>(
    '/users/me',
    payload,
  )
  return response.data.data
}
