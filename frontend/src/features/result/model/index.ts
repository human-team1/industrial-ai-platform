import { useCallback, useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { fetchResultDetail, fetchResults } from '../api'
import type { ResultDetail, ResultListQuery, ResultPageResponse } from '../types'

const DEFAULT_FILTERS: ResultListQuery = {
  keyword: '',
  from: '',
  to: '',
  equipmentName: '',
  productName: '',
  runType: '',
  decision: '',
  resultStatus: '',
}

type ResultListRequest = {
  filters: ResultListQuery
  page: number
  size: number
}

export function useResultList() {
  const [filters, setFilters] = useState<ResultListQuery>(DEFAULT_FILTERS)
  const [request, setRequest] = useState<ResultListRequest>({
    filters: DEFAULT_FILTERS,
    page: 0,
    size: 20,
  })
  const [data, setData] = useState<ResultPageResponse | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const load = useCallback(async (nextRequest: ResultListRequest) => {
    try {
      setLoading(true)
      setError(null)
      const result = await fetchResults({
        ...nextRequest.filters,
        page: nextRequest.page,
        size: nextRequest.size,
      })
      setData(result)
    } catch (err) {
      setError(err instanceof Error ? err.message : '검사 결과 목록을 불러오지 못했습니다.')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void load(request)
  }, [load, request])

  const search = useCallback(() => {
    setRequest((prev) => ({ ...prev, filters, page: 0 }))
  }, [filters])

  const reset = useCallback(() => {
    setFilters(DEFAULT_FILTERS)
    setRequest((prev) => ({ ...prev, filters: DEFAULT_FILTERS, page: 0 }))
  }, [])

  const changePage = useCallback((page: number) => {
    setRequest((prev) => ({ ...prev, page }))
  }, [])

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
    size: request.size,
    loading,
    error,
    empty,
    search,
    reset,
    changePage,
    retry,
  }
}

export function useResultDetail(resultIdParam: string | undefined) {
  const navigate = useNavigate()
  const [data, setData] = useState<ResultDetail | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const resultId = Number(resultIdParam)
  const invalidResultId = !resultIdParam || Number.isNaN(resultId) || resultId < 1

  const load = useCallback(async () => {
    if (invalidResultId) {
      setData(null)
      setError('유효하지 않은 검사 결과 ID입니다.')
      return
    }

    try {
      setLoading(true)
      setError(null)
      const result = await fetchResultDetail(resultId)
      setData(result)
    } catch (err) {
      setError(err instanceof Error ? err.message : '검사 결과 상세를 불러오지 못했습니다.')
    } finally {
      setLoading(false)
    }
  }, [invalidResultId, resultId])

  useEffect(() => {
    void load()
  }, [load])

  return {
    data,
    loading,
    error,
    retry: load,
    goBack: () => navigate('/results'),
  }
}
