import { useCallback, useEffect, useRef, useState } from 'react'
import { getDocuments } from '../api'
import type { DocumentPageResponse, DocumentSearchParams } from '../api/types'
import { useDocumentFilter } from './useDocumentFilter'

export function useDocumentList() {
  const filter = useDocumentFilter()
  const { applied } = filter
  const [data, setData] = useState<DocumentPageResponse | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const requestGeneration = useRef(0)

  const load = useCallback(async (query: DocumentSearchParams, signal: AbortSignal) => {
    const gen = ++requestGeneration.current
    try {
      setLoading(true)
      setError(null)
      const pageResult = await getDocuments(query, signal)
      if (gen !== requestGeneration.current || signal.aborted) return
      setData(pageResult)
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

  const reload = useCallback(() => {
    const controller = new AbortController()
    void load(applied, controller.signal)
  }, [applied, load])

  return {
    draft: filter.draft,
    setDraft: filter.setDraft,
    applied,
    data,
    loading,
    error,
    search: filter.search,
    resetFilters: filter.reset,
    setPage: filter.setPage,
    reload,
  }
}
