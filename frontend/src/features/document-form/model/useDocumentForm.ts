import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  createDocument,
  getDocumentDetail,
  updateDocument,
  uploadDocumentVersion,
  type DocumentCreateResult,
  type DocumentDetailResponse,
} from '../api'
import { buildCreateFormData, buildVersionFormData } from './buildFormData'
import { useDocumentFilePreview } from './preview'
import {
  EMPTY_DOCUMENT_FORM_VALUES,
  type DocumentFormFieldErrors,
  type DocumentFormMode,
  type DocumentFormPageText,
  type DocumentFormValues,
} from './types'
import { validateDocumentForm } from './validation'

export function useDocumentForm(documentId?: number) {
  const mode: DocumentFormMode = documentId ? 'edit' : 'create'
  const [values, setValues] = useState<DocumentFormValues>(EMPTY_DOCUMENT_FORM_VALUES)
  const [file, setFile] = useState<File | null>(null)
  const [detail, setDetail] = useState<DocumentDetailResponse | null>(null)
  const [loading, setLoading] = useState(mode === 'edit')
  const [saving, setSaving] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [fieldErrors, setFieldErrors] = useState<DocumentFormFieldErrors>({})
  const preview = useDocumentFilePreview()

  const loadDetail = useCallback(async (): Promise<DocumentDetailResponse | null> => {
    if (!documentId) return null

    try {
      setLoading(true)
      setErrorMessage(null)
      const nextDetail = await getDocumentDetail(documentId)
      setDetail(nextDetail)
      setValues({
        title: nextDetail.title ?? '',
        category: nextDetail.category ?? '',
        equipmentType: nextDetail.equipmentType ?? '',
        tags: nextDetail.tags?.join(', ') ?? '',
        description: nextDetail.description ?? '',
      })
      return nextDetail
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : '문서 상세를 불러오지 못했습니다.')
      return null
    } finally {
      setLoading(false)
    }
  }, [documentId])

  useEffect(() => {
    if (mode === 'edit') {
      void loadDetail()
    }
  }, [loadDetail, mode])

  const setFieldValue = useCallback((name: keyof DocumentFormValues, value: string) => {
    setValues((prev) => ({ ...prev, [name]: value }))
    setFieldErrors((prev) => ({ ...prev, [name]: undefined }))
  }, [])

  const setSelectedFile = useCallback((nextFile: File | null) => {
    setFile(nextFile)
    setFieldErrors((prev) => ({ ...prev, file: undefined }))
  }, [])

  const submit = useCallback(async (): Promise<DocumentCreateResult | DocumentDetailResponse | null> => {
    setErrorMessage(null)
    const nextErrors = validateDocumentForm({ mode, values, file })
    setFieldErrors(nextErrors)
    if (Object.keys(nextErrors).length > 0) return null

    setSaving(true)
    try {
      if (mode === 'create') {
        return await createDocument(buildCreateFormData(values, file as File))
      }

      if (!documentId) return null

      const updated = await updateDocument(documentId, {
        title: values.title.trim(),
        category: values.category.trim(),
        equipmentType: values.equipmentType.trim(),
        description: values.description.trim(),
        tags: values.tags
          .split(',')
          .map((tag) => tag.trim())
          .filter(Boolean),
      })

      if (file) {
        await uploadDocumentVersion(documentId, buildVersionFormData(file))
        const refreshed = await loadDetail()
        return refreshed ?? updated
      }

      setDetail(updated)
      return updated
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : '문서를 저장하지 못했습니다.')
      return null
    } finally {
      setSaving(false)
    }
  }, [documentId, file, loadDetail, mode, values])

  const openPreview = useCallback(async () => {
    await preview.open(detail?.latestVersion?.fileId)
  }, [detail?.latestVersion?.fileId, preview])

  const pageText = useMemo<DocumentFormPageText>(() => {
    if (mode === 'create') {
      return {
        title: '문서 등록',
        description: '파일과 문서 메타데이터를 함께 등록합니다.',
        submitLabel: '저장',
      }
    }

    return {
      title: '문서 수정',
      description: '등록된 문서 정보와 파일/인덱싱 상태를 확인하고 수정합니다.',
      submitLabel: '수정 저장',
    }
  }, [mode])

  return {
    mode,
    pageText,
    values,
    file,
    detail,
    loading,
    saving,
    previewLoading: preview.loading,
    previewErrorMessage: preview.errorMessage,
    errorMessage,
    fieldErrors,
    setFieldValue,
    setSelectedFile,
    openPreview,
    submit,
  }
}
