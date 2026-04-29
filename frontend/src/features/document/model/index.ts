import { useState, useCallback, useRef } from 'react'
import type {
  DocumentFormData,
  DocumentFormErrors,
  DocumentType,
  CreateDocumentResponse,
  DocumentIndexingStatus,
  UploadStep
} from '../types'
import { uploadFile, createDocument, getIndexingStatus } from '../api'

const initialFormData: DocumentFormData = {
  title: '',
  documentType: '',
  description: '',
  tags: '',
  file: null
}

// 폼 유효성 검사
export const validateForm = (data: DocumentFormData): DocumentFormErrors => {
  const errors: DocumentFormErrors = {}

  if (!data.title) {
    errors.title = '문서 제목을 입력해주세요'
  }

  if (!data.documentType) {
    errors.documentType = '문서 유형을 선택해주세요'
  }

  if (!data.file) {
    errors.file = '파일을 선택해주세요'
  }

  return errors
}

// useDocumentUpload Hook
export const useDocumentUpload = (organizationId: number) => {
  const [formData, setFormData] = useState<DocumentFormData>(initialFormData)
  const [errors, setErrors] = useState<DocumentFormErrors>({})
  const [uploadStep, setUploadStep] = useState<UploadStep>('IDLE')
  const [progress, setProgress] = useState(0)
  const [response, setResponse] = useState<CreateDocumentResponse | null>(null)
  const [indexingStatus, setIndexingStatus] = useState<DocumentIndexingStatus | null>(null)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const pollingRef = useRef<ReturnType<typeof setInterval> | null>(null)

  // 텍스트 입력 변경 핸들러
  const handleChange = useCallback((
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
    if (errors[name as keyof DocumentFormErrors]) {
      setErrors(prev => ({ ...prev, [name]: undefined }))
    }
  }, [errors])

  // 파일 변경 핸들러
  const handleFileChange = useCallback((
    e: React.ChangeEvent<HTMLInputElement>
  ) => {
    const file = e.target.files?.[0] || null
    setFormData(prev => ({ ...prev, file }))
    if (errors.file) {
      setErrors(prev => ({ ...prev, file: undefined }))
    }
  }, [errors])

  // 인덱싱 상태 폴링
  const startPolling = useCallback((documentVersionId: number) => {
    pollingRef.current = setInterval(async () => {
      try {
        const status = await getIndexingStatus(documentVersionId)
        setIndexingStatus(status)

        if (status.jobStatus === 'COMPLETED') {
          setProgress(100)
          setUploadStep('COMPLETED')
          clearInterval(pollingRef.current!)
        } else if (status.jobStatus === 'FAILED') {
          setUploadStep('FAILED')
          setErrorMessage(status.errorMessage || '인덱싱에 실패했습니다')
          clearInterval(pollingRef.current!)
        } else if (status.jobStatus === 'PROCESSING') {
          setProgress(prev => Math.min(prev + 10, 90))
        }
      } catch {
        clearInterval(pollingRef.current!)
      }
    }, 2000) // 2초마다 폴링
  }, [])

  // 폼 제출 핸들러
  const handleSubmit = useCallback(async (e: React.FormEvent) => {
    e.preventDefault()
    setErrorMessage(null)

    const validationErrors = validateForm(formData)
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors)
      return
    }

    try {
      // 1. 파일 업로드
      setUploadStep('UPLOADING')
      setProgress(20)
      const { fileId } = await uploadFile(formData.file!)
      setProgress(40)

      // 2. 문서 생성
      const result = await createDocument({
        title: formData.title,
        documentType: formData.documentType as DocumentType,
        fileId,
        organizationId
      })
      setResponse(result)
      setProgress(60)

      // 3. 인덱싱 폴링 시작
      setUploadStep('INDEXING')
      if (result.latestVersionId) {
        startPolling(result.latestVersionId)
      }
    } catch (error: unknown) {
      const err = error as {
        response?: { data?: { detail?: string; message?: string } }
        message?: string
      }
      setUploadStep('FAILED')
      setErrorMessage(
        err.response?.data?.detail ||
        err.response?.data?.message ||
        err.message ||
        '문서 업로드에 실패했습니다'
      )
    }
  }, [formData, organizationId, startPolling])

  // 폼 초기화
  const resetForm = useCallback(() => {
    if (pollingRef.current) {
      clearInterval(pollingRef.current)
    }
    setFormData(initialFormData)
    setErrors({})
    setErrorMessage(null)
    setUploadStep('IDLE')
    setProgress(0)
    setResponse(null)
    setIndexingStatus(null)
  }, [])

  return {
    formData,
    errors,
    uploadStep,
    progress,
    response,
    indexingStatus,
    errorMessage,
    handleChange,
    handleFileChange,
    handleSubmit,
    resetForm
  }
}