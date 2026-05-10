export type SignupRequestSummary = {
  requestId: number
  userId: number
  name: string
  email: string
  picture?: string
  requestedAt: string
}

export type RejectSignupRequestPayload = {
  rejectReason?: string
}
