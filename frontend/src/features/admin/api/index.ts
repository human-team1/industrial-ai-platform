import { apiClient } from '../../../shared/api/client'

export type SignupRequestSummary = {
  requestId: number
  userId: number
  name: string
  email: string
  picture?: string
  requestedAt: string
}

export async function getSignupRequests(): Promise<SignupRequestSummary[]> {
  const response = await apiClient.get<{ success: boolean; data: SignupRequestSummary[] }>(
    '/signup-requests',
  )
  return response.data.data
}

export async function approveSignup(requestId: number): Promise<void> {
  await apiClient.patch(`/signup-requests/${requestId}/approve`)
}

export async function rejectSignup(requestId: number, rejectReason?: string): Promise<void> {
  await apiClient.patch(
    `/signup-requests/${requestId}/reject`,
    rejectReason ? { rejectReason } : undefined,
  )
}
