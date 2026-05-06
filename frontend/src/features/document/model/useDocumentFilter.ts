import { useCallback, useState } from 'react'
import type { DocumentSearchParams } from '../api/types'

export const DEFAULT_DOCUMENT_PAGE_SIZE = 20

export const DEFAULT_DOCUMENT_FILTERS: DocumentSearchParams = {
  page: 0,
  size: DEFAULT_DOCUMENT_PAGE_SIZE,
  keyword: '',
  documentType: undefined,
  category: '',
  equipmentType: '',
  indexingStatus: undefined,
  startDate: '',
  endDate: '',
}

export function normalizeDocumentParams(params: DocumentSearchParams): DocumentSearchParams {
  const next: DocumentSearchParams = {
    ...params,
    page: params.page ?? 0,
    size: params.size ?? DEFAULT_DOCUMENT_PAGE_SIZE,
  }
  if (!next.keyword?.trim()) delete next.keyword
  if (!next.category?.trim()) delete next.category
  if (!next.equipmentType?.trim()) delete next.equipmentType
  if (!next.startDate?.trim()) delete next.startDate
  if (!next.endDate?.trim()) delete next.endDate
  if (!next.documentType?.trim()) delete next.documentType
  if (typeof next.indexingStatus === 'string' && !next.indexingStatus.trim()) {
    delete next.indexingStatus
  }
  return next
}

export function useDocumentFilter() {
  const [draft, setDraft] = useState<DocumentSearchParams>(DEFAULT_DOCUMENT_FILTERS)
  const [applied, setApplied] = useState<DocumentSearchParams>(() =>
    normalizeDocumentParams(DEFAULT_DOCUMENT_FILTERS),
  )

  const search = useCallback(() => {
    setApplied(normalizeDocumentParams({ ...draft, page: 0, size: DEFAULT_DOCUMENT_PAGE_SIZE }))
  }, [draft])

  const reset = useCallback(() => {
    setDraft(DEFAULT_DOCUMENT_FILTERS)
    setApplied(normalizeDocumentParams(DEFAULT_DOCUMENT_FILTERS))
  }, [])

  const setPage = useCallback((page: number) => {
    setApplied((prev) => normalizeDocumentParams({ ...prev, page: Math.max(0, page) }))
  }, [])

  return {
    draft,
    setDraft,
    applied,
    setApplied,
    search,
    reset,
    setPage,
  }
}
