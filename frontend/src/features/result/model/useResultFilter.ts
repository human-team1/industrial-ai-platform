import { useCallback, useState } from 'react'
import type { ResultListQuery } from '../api/types'

export const RESULT_PAGE_SIZE = 7

export const DEFAULT_RESULT_FILTERS: ResultListQuery = {
  keyword: '',
  from: '',
  to: '',
  equipmentName: '',
  productName: '',
  runType: '',
  decisionCode: '',
  resultStatus: '',
}

export type ResultListRequest = {
  filters: ResultListQuery
  page: number
  size: number
}

export function useResultFilter() {
  const [filters, setFilters] = useState<ResultListQuery>(DEFAULT_RESULT_FILTERS)
  const [request, setRequest] = useState<ResultListRequest>({
    filters: DEFAULT_RESULT_FILTERS,
    page: 0,
    size: RESULT_PAGE_SIZE,
  })

  const search = useCallback(() => {
    setRequest((prev) => ({ ...prev, filters, page: 0, size: RESULT_PAGE_SIZE }))
  }, [filters])

  const reset = useCallback(() => {
    setFilters(DEFAULT_RESULT_FILTERS)
    setRequest((prev) => ({
      ...prev,
      filters: DEFAULT_RESULT_FILTERS,
      page: 0,
      size: RESULT_PAGE_SIZE,
    }))
  }, [])

  const changePage = useCallback((page: number) => {
    const nextPage = Number.isFinite(page) ? Math.max(0, Math.floor(page)) : 0
    setRequest((prev) => ({ ...prev, page: nextPage, size: RESULT_PAGE_SIZE }))
  }, [])

  return {
    filters,
    setFilters,
    request,
    search,
    reset,
    changePage,
  }
}
