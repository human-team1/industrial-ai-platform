import { useCallback, useEffect, useState } from 'react'
import { useLocation } from 'react-router-dom'
import { useAuth } from '../../features/auth/model'
import { fetchUnreadNotificationCount } from '../../features/notification/api'

export function useUnreadNotificationCount() {
  const location = useLocation()
  const { accessToken, user } = useAuth()
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
    if (!accessToken || !user) {
      setUnreadCount(0)
      setLoading(false)
      setError(null)
      return
    }

    const controller = new AbortController()
    void refresh(controller.signal)
    return () => controller.abort()
  }, [accessToken, user, location.pathname, refresh])

  return {
    unreadCount,
    loading,
    error,
    refresh: () => {
      void refresh()
    },
  }
}
