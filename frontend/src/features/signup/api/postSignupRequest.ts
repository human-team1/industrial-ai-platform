import { publicAxios } from '../../../shared/api/client'
import type { SignupRequestPayload, SignupRequestResponse } from '../types'

export async function postSignupRequest(
  payload: SignupRequestPayload,
): Promise<SignupRequestResponse> {
  const response = await publicAxios.post<{ success: boolean; data: SignupRequestResponse }>(
    '/signup-requests',
    payload,
  )
  return response.data.data
}
