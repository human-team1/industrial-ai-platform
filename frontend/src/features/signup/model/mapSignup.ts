import type { SignupFormData, SignupRequestPayload } from '../types'

export function mapToSignupPayload(
  signupToken: string,
  form: SignupFormData,
): SignupRequestPayload {
  if (form.organizationId === null) {
    throw new Error('organizationId is required')
  }
  return {
    signupToken,
    phone: form.phone.replace(/\D/g, ''),
    organizationId: form.organizationId,
  }
}
