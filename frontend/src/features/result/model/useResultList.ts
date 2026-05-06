import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { fetchResults } from '../api'
import type { ResultPageResponse } from '../api/types'
import {
  RESULT_PAGE_SIZE,
  useResultFilter,
  type ResultListRequest,
} from './useResultFilter'

export function useResultList() {
  const { filters, setFilters, request, search, reset, changePage } = useResultFilter()
  const [data, setData] = useState<ResultPageResponse | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const requestGeneration = useRef(0)

  const load = useCallback(async (nextRequest: ResultListRequest, signal?: AbortSignal) => {
    const gen = ++requestGeneration.current
    try {
      setLoading(true)
      setError(null)
      const result = await fetchResults(
        {
          ...nextRequest.filters,
          page: nextRequest.page,
          size: RESULT_PAGE_SIZE,
        },
        signal,
      )
      if (gen !== requestGeneration.current) return
      setData(result)
    } catch (err) {
      if (signal?.aborted) return
      setError(err instanceof Error ? err.message : '검사 결과 목록을 불러오지 못했습니다.')
    } finally {
      if (gen === requestGeneration.current) {
        setLoading(false)
      }
    }
  }, [])

  useEffect(() => {
    const controller = new AbortController()
    void load(request, controller.signal)
    return () => controller.abort()
  }, [load, request])

  const retry = useCallback(() => {
    void load(request)
  }, [load, request])

  const empty = useMemo(
    () => !loading && !error && (data?.content.length ?? 0) === 0,
    [data, error, loading],
  )

  return {
    filters,
    setFilters,
    data,
    page: request.page,
    size: RESULT_PAGE_SIZE,
    loading,
    error,
    empty,
    search,
    reset,
    changePage,
    retry,
  }
}
