export type SignupFormData = {
  name: string
  phone: string
  organizationId: number | null
  consent: boolean
}

export type SignupRequestPayload = {
  signupToken: string
  phone: string
  organizationId: number
}

export type SignupRequestResponse = {
  userId: number
  status: 'PENDING' | 'REJECTED' | string
}
