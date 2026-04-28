import { apiClient } from '../../../shared/api/client'

type SignupRequestPayload = {
  signupToken: string
  email: string
  name: string
  picture?: string
}

type SignupRequestResponse = {
  userId: number
  status: string
}

export async function postSignupRequest(
  payload: SignupRequestPayload,
): Promise<SignupRequestResponse> {
  const response = await apiClient.post<{ success: boolean; data: SignupRequestResponse }>(
    '/signup-requests',
    payload,
  )
  return response.data.data
}
