import { useCallback, useEffect, useState } from 'react'
import { useLocation } from 'react-router-dom'
import { fetchUnreadNotificationCount } from '../../features/notification/api'

export function useUnreadNotificationCount() {
  const location = useLocation()
  const [unreadCount, setUnreadCount] = useState<number>(0)
  const [loading, setLoading] = useState<boolean>(false)
  const [error, setError] = useState<string | null>(null)

  const refresh = useCallback(async (signal?: AbortSignal) => {
    try {
      setLoading(true)
      setError(null)
      const count = await fetchUnreadNotificationCount(signal)
      setUnreadCount(count)
    } catch (err) {
      if (signal?.aborted) return
      setError(err instanceof Error ? err.message : '알림 개수를 불러오지 못했습니다.')
    } finally {
      if (!signal?.aborted) {
        setLoading(false)
      }
    }
  }, [])

  // 페이지 이동(pathname 변경) 시 재조회 — AppHeader가 리마운트되지 않아도 갱신
  useEffect(() => {
    const controller = new AbortController()
    void refresh(controller.signal)
    return () => controller.abort()
  }, [location.pathname, refresh])

  return {
    unreadCount,
    loading,
    error,
    refresh: () => {
      void refresh()
    },
  }
}
