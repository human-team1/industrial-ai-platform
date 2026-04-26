import { apiClient } from '../../../shared/api/client'
import type { GoogleLoginResponse, NewUserInfo } from '../types'

export async function postGoogleLogin(idToken: string): Promise<GoogleLoginResponse> {
  const response = await apiClient.post<{ success: boolean; data: GoogleLoginResponse }>(
    '/auth/google',
    { idToken },
  )
  return response.data.data
}

export async function postSignupRequest(userInfo: NewUserInfo): Promise<{ userId: number; status: string }> {
  const response = await apiClient.post<{ success: boolean; data: { userId: number; status: string } }>(
    '/signup-requests',
    userInfo,
  )
  return response.data.data
}

export async function postLogout(): Promise<void> {
  await apiClient.post('/auth/logout')
}
