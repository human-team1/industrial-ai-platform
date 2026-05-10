import type { DocumentFormFieldErrors, DocumentFormMode, DocumentFormValues } from './types'

export const ALLOWED_DOCUMENT_EXTENSIONS = ['pdf', 'docx', 'md']

export type ValidateDocumentFormInput = {
  mode: DocumentFormMode
  values: DocumentFormValues
  file: File | null
}

export function validateDocumentForm({
  mode,
  values,
  file,
}: ValidateDocumentFormInput): DocumentFormFieldErrors {
  const nextErrors: DocumentFormFieldErrors = {}

  if (!values.title.trim()) {
    nextErrors.title = '문서명을 입력하세요.'
  }

  if (mode === 'create' && !file) {
    nextErrors.file = '등록할 문서 파일을 선택하세요.'
  }

  if (file) {
    const extension = file.name.split('.').pop()?.toLowerCase()
    if (!extension || !ALLOWED_DOCUMENT_EXTENSIONS.includes(extension)) {
      nextErrors.file = '허용 파일 형식은 PDF, DOCX, MD입니다.'
    }
  }

  return nextErrors
}
