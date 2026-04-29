import { useCallback, useMemo, useState } from 'react'
import type { NewUserInfo } from '../../../entities/auth'
import { postSignupRequest } from '../api/postSignupRequest'
import type { SignupFormData, SignupRequestResponse } from '../types'
import { mapToSignupPayload } from './mapSignup'

type UseSignupFormResult = {
  form: SignupFormData
  loading: boolean
  error: string | null
  canSubmit: boolean
  updateField: <K extends keyof SignupFormData>(field: K, value: SignupFormData[K]) => void
  submit: () => Promise<SignupRequestResponse>
}

export function useSignupForm(userInfo: NewUserInfo): UseSignupFormResult {
  const [form, setForm] = useState<SignupFormData>({
    name: userInfo.name,
    phone: '',
    organizationId: null,
    consent: false,
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const updateField = useCallback(
    <K extends keyof SignupFormData>(field: K, value: SignupFormData[K]) => {
      setForm((prev) => ({ ...prev, [field]: value }))
    },
    [],
  )

  const canSubmit = useMemo(() => {
    if (loading) return false
    if (!form.consent) return false
    if (form.organizationId === null) return false
    if (form.phone.replace(/\D/g, '').length === 0) return false
    return true
  }, [loading, form.consent, form.organizationId, form.phone])

  const submit = useCallback(async (): Promise<SignupRequestResponse> => {
    setLoading(true)
    setError(null)
    try {
      const payload = mapToSignupPayload(userInfo.signupToken, form)
      return await postSignupRequest(payload)
    } catch (e) {
      setError('가입 신청 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.')
      throw e
    } finally {
      setLoading(false)
    }
  }, [form, userInfo.signupToken])

  return { form, loading, error, canSubmit, updateField, submit }
}
