import { useCallback, useEffect, useRef, useState } from 'react'
import type { AsyncJob, OperationLog, PageResponse, SystemComponentStatus, SystemStatus } from '../../../entities/operation/model/types'
import { fetchAsyncJobs, fetchOperationLogs, fetchSystemComponents, fetchSystemStatus, type OperationLogQuery } from '../api'

type PollingState<T> = {
  data: T | null
  initialLoading: boolean
  isRefreshing: boolean
  error: string | null
  lastUpdatedAt: string | null
  refetch: () => void
}

function errorMessage(error: unknown, fallback: string) {
  return error instanceof Error ? error.message : fallback
}

function usePollingResource<T>(
  loader: (signal: AbortSignal) => Promise<T>,
  intervalMs: number | null,
  fallbackMessage: string,
): PollingState<T> {
  const [data, setData] = useState<T | null>(null)
  const [initialLoading, setInitialLoading] = useState(true)
  const [isRefreshing, setIsRefreshing] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [lastUpdatedAt, setLastUpdatedAt] = useState<string | null>(null)
  const inFlightRef = useRef(false)
  const controllerRef = useRef<AbortController | null>(null)
  const hasLoadedRef = useRef(false)
  const loaderRef = useRef(loader)

  useEffect(() => {
    loaderRef.current = loader
  }, [loader])

  const load = useCallback(() => {
    if (inFlightRef.current) return
    const controller = new AbortController()
    controllerRef.current = controller
    inFlightRef.current = true
    if (hasLoadedRef.current) {
      setIsRefreshing(true)
    } else {
      setInitialLoading(true)
    }

    loaderRef.current(controller.signal)
      .then((next) => {
        if (!controller.signal.aborted) {
          setData(next)
          setError(null)
          setLastUpdatedAt(new Date().toISOString())
          hasLoadedRef.current = true
        }
      })
      .catch((err) => {
        if (!controller.signal.aborted) {
          setError(errorMessage(err, fallbackMessage))
          hasLoadedRef.current = true
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setInitialLoading(false)
          setIsRefreshing(false)
        }
        inFlightRef.current = false
      })
  }, [fallbackMessage])

  useEffect(() => {
    load()
    return () => controllerRef.current?.abort()
  }, [load])

  useEffect(() => {
    if (intervalMs == null) return undefined
    const timer = window.setInterval(load, intervalMs)
    return () => window.clearInterval(timer)
  }, [intervalMs, load])

  return { data, initialLoading, isRefreshing, error, lastUpdatedAt, refetch: load }
}

export function useSystemStatusPolling() {
  return usePollingResource<SystemStatus>(
    fetchSystemStatus,
    10000,
    '시스템 상태를 불러오지 못했습니다.',
  )
}

export function useSystemComponentsPolling() {
  return usePollingResource<SystemComponentStatus[]>(
    fetchSystemComponents,
    10000,
    '컴포넌트 상태를 불러오지 못했습니다.',
  )
}

export function useAsyncJobsPolling() {
  return usePollingResource<PageResponse<AsyncJob>>(
    fetchAsyncJobs,
    30000,
    '비동기 작업 목록을 불러오지 못했습니다.',
  )
}

export function useOperationLogs() {
  const [filters, setFilters] = useState<OperationLogQuery>({ page: 0, size: 20, sort: 'desc' })
  const [data, setData] = useState<PageResponse<OperationLog> | null>(null)
  const [initialLoading, setInitialLoading] = useState(true)
  const [isRefreshing, setIsRefreshing] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [lastUpdatedAt, setLastUpdatedAt] = useState<string | null>(null)
  const inFlightRef = useRef(false)
  const controllerRef = useRef<AbortController | null>(null)
  const hasLoadedRef = useRef(false)

  const load = useCallback(() => {
    if (inFlightRef.current) return
    const controller = new AbortController()
    controllerRef.current = controller
    inFlightRef.current = true
    if (hasLoadedRef.current) {
      setIsRefreshing(true)
    } else {
      setInitialLoading(true)
    }

    fetchOperationLogs(filters, controller.signal)
      .then((next) => {
        if (!controller.signal.aborted) {
          setData(next)
          setError(null)
          setLastUpdatedAt(new Date().toISOString())
          hasLoadedRef.current = true
        }
      })
      .catch((err) => {
        if (!controller.signal.aborted) {
          setError(errorMessage(err, '운영 로그를 불러오지 못했습니다.'))
          hasLoadedRef.current = true
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setInitialLoading(false)
          setIsRefreshing(false)
        }
        inFlightRef.current = false
      })
  }, [filters])

  useEffect(() => {
    load()
    return () => controllerRef.current?.abort()
  }, [load])

  return {
    data,
    filters,
    setFilters,
    initialLoading,
    isRefreshing,
    error,
    lastUpdatedAt,
    refetch: load,
  }
}
