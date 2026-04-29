import { useCallback, useEffect, useState } from 'react'
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

const DEFAULT_QUERY: DocumentSearchParams = { page: 0, size: 10 }

export function useDocumentList() {
  const [query, setQuery] = useState<DocumentSearchParams>(DEFAULT_QUERY)
  const [data, setData] = useState<DocumentPageResponse | null>(null)
  const [summary, setSummary] = useState<DocumentSummary | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const load = useCallback(async () => {
    try {
      setLoading(true)
      setError(null)
      const [pageResult, summaryResult] = await Promise.all([
        getDocuments(query),
        getDocumentSummary(),
      ])
      setData(pageResult)
      setSummary(summaryResult)
    } catch (err) {
      setError(err instanceof Error ? err.message : '문서 목록 조회에 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }, [query])

  useEffect(() => {
    void load()
  }, [load])

  return { query, setQuery, data, summary, loading, error, reload: load }
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
