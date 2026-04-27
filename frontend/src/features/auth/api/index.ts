import { apiClient, refreshAxios } from '../../../shared/api/client'
import type { GoogleLoginResponse } from '../types'

export async function postGoogleLogin(idToken: string): Promise<GoogleLoginResponse> {
  const response = await apiClient.post<{ success: boolean; data: GoogleLoginResponse }>(
    '/auth/google',
    { idToken },
  )
  return response.data.data
}

type SignupRequestPayload = {
  signupToken: string
  email: string
  name: string
  picture?: string
}

export async function postSignupRequest(
  payload: SignupRequestPayload,
): Promise<{ userId: number; status: string }> {
  const response = await apiClient.post<{ success: boolean; data: { userId: number; status: string } }>(
    '/signup-requests',
    payload,
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