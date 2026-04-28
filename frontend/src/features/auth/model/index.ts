import { useState, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import type { SignupFormData, SignupFormErrors, SignupRequest, SignupResponse, SignupStep } from '../types'
import { signup as signupApi } from '../api'

const initialFormData: SignupFormData = {
  email: '',
  name: '',
  phone: '',
  company: '',
  department: '',
  position: ''
}

// 폼 유효성 검사
export const validateForm = (data: SignupFormData): SignupFormErrors => {
  const errors: SignupFormErrors = {}

  if (!data.email) {
    errors.email = '이메일을 입력해주세요'
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(data.email)) {
    errors.email = '올바른 이메일 형식이 아닙니다'
  }

  if (!data.name) {
    errors.name = '이름을 입력해주세요'
  }

  if (!data.company) {
    errors.company = '소속을 입력해주세요'
  }

  if (!data.phone) {
    errors.phone = '연락처를 입력해주세요'
  } else if (!/^[0-9]{10,11}$/.test(data.phone.replace(/-/g, ''))) {
    errors.phone = '올바른 연락처 형식이 아닙니다'
  }

  return errors
}

// useSignup Hook
export const useSignup = (googleSub?: string, googlePicture?: string) => {
  const navigate = useNavigate()
  const [step, setStep] = useState<SignupStep>(1)
  const [formData, setFormData] = useState<SignupFormData>(initialFormData)
  const [errors, setErrors] = useState<SignupFormErrors>({})
  const [isLoading, setIsLoading] = useState(false)
  const [isSuccess, setIsSuccess] = useState(false)
  const [response, setResponse] = useState<SignupResponse | null>(null)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  // 입력 변경 핸들러
  const handleChange = useCallback((e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
    if (errors[name as keyof SignupFormErrors]) {
      setErrors(prev => ({ ...prev, [name]: undefined }))
    }
  }, [errors])

  // 다음 단계
  const handleNextStep = useCallback(() => {
    setStep(prev => (prev < 3 ? (prev + 1) as SignupStep : prev))
  }, [])

  // 이전 단계
  const handlePrevStep = useCallback(() => {
    setStep(prev => (prev > 1 ? (prev - 1) as SignupStep : prev))
  }, [])

  // 폼 제출
  const handleSubmit = useCallback(async (e: React.FormEvent) => {
    e.preventDefault()
    setErrorMessage(null)

    if (!googleSub) {
      setErrorMessage('Google 인증 정보가 없습니다. 다시 로그인해 주세요.')
      return
    }

    const validationErrors = validateForm(formData)
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors)
      return
    }

    setIsLoading(true)

    try {
      const requestData: SignupRequest = {
        email: formData.email,
        name: formData.name,
        googleSub,
        picture: googlePicture,
        company: formData.company,
        department: formData.department || undefined,
        position: formData.position || undefined,
        phone: formData.phone
      }

      const result = await signupApi(requestData)
      setResponse(result)
      setIsSuccess(true)
      handleNextStep() // 3단계로 이동
    } catch (error: unknown) {
      const err = error as { response?: { data?: { detail?: string; message?: string } }; message?: string }
      setErrorMessage(
        err.response?.data?.detail ||
        err.response?.data?.message ||
        err.message ||
        '회원가입에 실패했습니다'
      )
    } finally {
      setIsLoading(false)
    }
  }, [formData, googleSub, googlePicture, handleNextStep])

  // 가입 완료 후 메인으로
  const handleGoToMain = useCallback(() => {
    localStorage.removeItem('googleUserInfo')
    navigate('/dashboard', { replace: true })
  }, [navigate])

  return {
    step,
    formData,
    errors,
    isLoading,
    isSuccess,
    response,
    errorMessage,
    handleChange,
    handleNextStep,
    handlePrevStep,
    handleSubmit,
    handleGoToMain,
    setFormData
  }
}