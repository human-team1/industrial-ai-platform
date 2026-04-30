import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  createDocument,
  getDocumentFilePreview,
  getDocumentDetail,
  updateDocument,
  uploadDocumentVersion,
} from '../api'
import type { DocumentCreateResult, DocumentDetail } from '../../document/types'

export type DocumentFormMode = 'create' | 'edit'

export type DocumentFormValues = {
  title: string
  category: string
  equipmentType: string
  tags: string
  description: string
}

const EMPTY_VALUES: DocumentFormValues = {
  title: '',
  category: '',
  equipmentType: '',
  tags: '',
  description: '',
}

const ALLOWED_EXTENSIONS = ['pdf', 'docx', 'md']

export function useDocumentForm(documentId?: number) {
  const mode: DocumentFormMode = documentId ? 'edit' : 'create'
  const [values, setValues] = useState<DocumentFormValues>(EMPTY_VALUES)
  const [file, setFile] = useState<File | null>(null)
  const [detail, setDetail] = useState<DocumentDetail | null>(null)
  const [loading, setLoading] = useState(mode === 'edit')
  const [saving, setSaving] = useState(false)
  const [previewLoading, setPreviewLoading] = useState(false)
  const [previewErrorMessage, setPreviewErrorMessage] = useState<string | null>(null)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [fieldErrors, setFieldErrors] = useState<Partial<Record<keyof DocumentFormValues | 'file', string>>>({})

  const loadDetail = useCallback(async (): Promise<DocumentDetail | null> => {
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

  const validate = useCallback(() => {
    const nextErrors: Partial<Record<keyof DocumentFormValues | 'file', string>> = {}

    if (!values.title.trim()) {
      nextErrors.title = '문서명을 입력하세요.'
    }

    if (mode === 'create' && !file) {
      nextErrors.file = '등록할 문서 파일을 선택하세요.'
    }

    if (file) {
      const extension = file.name.split('.').pop()?.toLowerCase()
      if (!extension || !ALLOWED_EXTENSIONS.includes(extension)) {
        nextErrors.file = '허용 파일 형식은 PDF, DOCX, MD입니다.'
      }
    }

    setFieldErrors(nextErrors)
    return Object.keys(nextErrors).length === 0
  }, [file, mode, values.title])

  const submit = useCallback(async (): Promise<DocumentCreateResult | DocumentDetail | null> => {
    setErrorMessage(null)
    if (!validate()) return null

    setSaving(true)
    try {
      if (mode === 'create') {
        const formData = new FormData()
        formData.append('file', file as File)
        formData.append('title', values.title.trim())
        const category = values.category.trim()
        const equipmentType = values.equipmentType.trim()
        const description = values.description.trim()
        const tags = values.tags.trim()
        if (category) formData.append('category', category)
        if (equipmentType) formData.append('equipmentType', equipmentType)
        if (description) formData.append('description', description)
        if (tags) formData.append('tags', tags)
        return await createDocument(formData)
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
        const formData = new FormData()
        formData.append('file', file)
        formData.append('changeReason', 'manual update')
        await uploadDocumentVersion(documentId, formData)
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
  }, [documentId, file, loadDetail, mode, validate, values])

  const openPreview = useCallback(async () => {
    const fileId = detail?.latestVersion?.fileId
    if (!fileId) {
      setPreviewErrorMessage('미리보기 데이터를 제공하는 API가 없습니다.')
      return
    }

    try {
      setPreviewLoading(true)
      setPreviewErrorMessage(null)
      const blob = await getDocumentFilePreview(fileId)
      const url = URL.createObjectURL(blob)
      window.open(url, '_blank', 'noopener,noreferrer')
      window.setTimeout(() => URL.revokeObjectURL(url), 60_000)
    } catch (error) {
      setPreviewErrorMessage(error instanceof Error ? error.message : '미리보기를 불러오지 못했습니다.')
    } finally {
      setPreviewLoading(false)
    }
  }, [detail?.latestVersion?.fileId])

  const pageText = useMemo(() => {
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
    previewLoading,
    previewErrorMessage,
    errorMessage,
    fieldErrors,
    setFieldValue,
    setSelectedFile,
    openPreview,
    submit,
  }
}
