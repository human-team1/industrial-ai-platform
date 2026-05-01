import { useCallback, useEffect, useRef, useState } from 'react'
import {
  createDocument,
  deleteDocument,
  getDocumentDetail,
  getDocuments,
  getDocumentSummary,
  type UpdateDocumentMetadataPayload,
  updateDocument,
  uploadDocumentVersion,
} from '../api'
import type {
  DocumentDetail,
  DocumentPageResponse,
  DocumentSearchParams,
  DocumentSummary,
} from '../types'

const DEFAULT_SIZE = 20

const initialFilters: DocumentSearchParams = {
  page: 0,
  size: DEFAULT_SIZE,
  keyword: '',
  documentType: undefined,
  category: '',
  equipmentType: '',
  indexingStatus: undefined,
  startDate: '',
  endDate: '',
}

function normalizeForRequest(params: DocumentSearchParams): DocumentSearchParams {
  const next: DocumentSearchParams = {
    ...params,
    page: params.page ?? 0,
    size: params.size ?? DEFAULT_SIZE,
  }
  if (!next.keyword?.trim()) delete next.keyword
  if (!next.category?.trim()) delete next.category
  if (!next.equipmentType?.trim()) delete next.equipmentType
  if (!next.startDate?.trim()) delete next.startDate
  if (!next.endDate?.trim()) delete next.endDate
  if (!next.documentType?.trim()) delete next.documentType
  if (!next.indexingStatus?.trim()) delete next.indexingStatus
  return next
}

export function useDocumentList() {
  const [draft, setDraft] = useState<DocumentSearchParams>(initialFilters)
  const [applied, setApplied] = useState<DocumentSearchParams>(() => normalizeForRequest(initialFilters))
  const [data, setData] = useState<DocumentPageResponse | null>(null)
  const [summary, setSummary] = useState<DocumentSummary | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const requestGeneration = useRef(0)

  const load = useCallback(async (query: DocumentSearchParams, signal: AbortSignal) => {
    const gen = ++requestGeneration.current
    try {
      setLoading(true)
      setError(null)
      const normalized = normalizeForRequest(query)
      const [pageResult, summaryResult] = await Promise.all([
        getDocuments(normalized, signal),
        getDocumentSummary(signal),
      ])
      if (gen !== requestGeneration.current || signal.aborted) return
      setData(pageResult)
      setSummary(summaryResult)
    } catch (err) {
      if (signal.aborted) return
      setError(err instanceof Error ? err.message : '문서 목록 조회에 실패했습니다.')
    } finally {
      if (gen === requestGeneration.current && !signal.aborted) {
        setLoading(false)
      }
    }
  }, [])

  useEffect(() => {
    const controller = new AbortController()
    void load(applied, controller.signal)
    return () => controller.abort()
  }, [applied, load])

  const search = useCallback(() => {
    setApplied(normalizeForRequest({ ...draft, page: 0, size: DEFAULT_SIZE }))
  }, [draft])

  const resetFilters = useCallback(() => {
    setDraft(initialFilters)
    setApplied(normalizeForRequest(initialFilters))
  }, [])

  const setPage = useCallback((page: number) => {
    setApplied((prev) => normalizeForRequest({ ...prev, page: Math.max(0, page) }))
  }, [])

  const reload = useCallback(() => {
    const controller = new AbortController()
    void load(applied, controller.signal)
  }, [applied, load])

  return {
    draft,
    setDraft,
    applied,
    data,
    summary,
    loading,
    error,
    search,
    resetFilters,
    setPage,
    reload,
  }
}

export function useDocumentDetail(documentId: number) {
  const [detail, setDetail] = useState<DocumentDetail | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const load = useCallback(async () => {
    try {
      setLoading(true)
      setError(null)
      setDetail(await getDocumentDetail(documentId))
    } catch (err) {
      setError(err instanceof Error ? err.message : '문서 상세 조회에 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }, [documentId])

  useEffect(() => {
    if (Number.isFinite(documentId) && documentId > 0) {
      void load()
    }
  }, [documentId, load])

  return { detail, loading, error, reload: load }
}

export async function submitNewDocument(formData: FormData) {
  return createDocument(formData)
}

export async function submitDocumentMetadata(documentId: number, body: UpdateDocumentMetadataPayload) {
  return updateDocument(documentId, body)
}

export async function submitDocumentVersion(documentId: number, formData: FormData) {
  return uploadDocumentVersion(documentId, formData)
}

export async function removeDocument(documentId: number) {
  return deleteDocument(documentId)
}
