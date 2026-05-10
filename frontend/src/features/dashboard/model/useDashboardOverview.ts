import { useCallback, useEffect, useMemo, useState } from 'react'
import type { DashboardOverview } from '../../../entities/dashboard/model/types'
import { fetchDashboardOverview } from '../api'

function toDateInputValue(date: Date) {
  const yyyy = date.getFullYear()
  const mm = String(date.getMonth() + 1).padStart(2, '0')
  const dd = String(date.getDate()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd}`
}

function defaultRange() {
  const end = new Date()
  const start = new Date()
  start.setDate(end.getDate() - 6)
  return {
    startDate: toDateInputValue(start),
    endDate: toDateInputValue(end),
  }
}

export function useDashboardOverview() {
  const initialRange = useMemo(defaultRange, [])
  const [startDate, setStartDate] = useState(initialRange.startDate)
  const [endDate, setEndDate] = useState(initialRange.endDate)
  const [data, setData] = useState<DashboardOverview | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [reloadKey, setReloadKey] = useState(0)

  const reload = useCallback(() => setReloadKey((value) => value + 1), [])

  useEffect(() => {
    const controller = new AbortController()
    setIsLoading(true)
    setError(null)

    fetchDashboardOverview({ startDate, endDate }, controller.signal)
      .then(setData)
      .catch((err) => {
        if (!controller.signal.aborted) {
          setError(err instanceof Error ? err.message : '대시보드 정보를 불러오지 못했습니다.')
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setIsLoading(false)
        }
      })

    return () => controller.abort()
  }, [startDate, endDate, reloadKey])

  return {
    data,
    startDate,
    endDate,
    setStartDate,
    setEndDate,
    isLoading,
    error,
    reload,
  }
}
