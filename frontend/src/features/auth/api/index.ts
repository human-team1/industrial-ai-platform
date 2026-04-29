import { apiClient, refreshAxios } from '../../../shared/api/client'
import type { AuthMeResponse, GoogleLoginResponse } from '../types'

export async function postGoogleLogin(idToken: string): Promise<GoogleLoginResponse> {
  const response = await apiClient.post<{ success: boolean; data: GoogleLoginResponse }>(
    '/auth/google',
    { idToken },
  )
  return response.data.data
}

export async function postRefreshToken(): Promise<{ accessToken: string }> {
  const response = await refreshAxios.post<{ success: boolean; data: { accessToken: string } }>(
    '/auth/refresh',
  )
  return response.data.data
}

export async function postLogout(): Promise<void> {
  await apiClient.post('/auth/logout')
}

export async function getAuthMe(): Promise<AuthMeResponse> {
  const response = await apiClient.get<{ success: boolean; data: AuthMeResponse }>('/auth/me')
  return response.data.data
}