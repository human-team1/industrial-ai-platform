import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import {
  fetchNotificationDetail,
  fetchNotifications,
  markAllNotificationsAsRead,
  markNotificationAsRead,
} from '../api'
import type {
  NotificationDetail,
  NotificationDetailViewModel,
  NotificationFilter,
  NotificationListData,
  NotificationListQuery,
  NotificationServerFilterType,
  NotificationSummaryItem,
} from '../types'

const NOTIFICATION_PAGE_SIZE = 20

const filterToQuery: Record<NotificationFilter, Partial<NotificationListQuery>> = {
  ALL: {},
  UNREAD: { isRead: false },
  ANOMALY: {},
  SYSTEM: { type: 'SYSTEM_ERROR' satisfies NotificationServerFilterType },
  REPORT: { type: 'REPORT_GENERATED' satisfies NotificationServerFilterType },
}

export const NOTIFICATION_DISABLED_FILTERS: NotificationFilter[] = ['ANOMALY']

type ListRequest = {
  filter: NotificationFilter
  page: number
}

export function useNotificationPage() {
  const [filter, setFilter] = useState<NotificationFilter>('ALL')
  const [request, setRequest] = useState<ListRequest>({ filter: 'ALL', page: 0 })
  const [data, setData] = useState<NotificationListData | null>(null)
  const [listLoading, setListLoading] = useState(false)
  const [listError, setListError] = useState<string | null>(null)

  const [selectedSummary, setSelectedSummary] = useState<NotificationSummaryItem | null>(null)
  const [selectedDetail, setSelectedDetail] = useState<NotificationDetail | null>(null)
  const [detailLoading, setDetailLoading] = useState(false)
  const [detailError, setDetailError] = useState<string | null>(null)

  const [markAllPending, setMarkAllPending] = useState(false)

  const listGen = useRef(0)
  const detailGen = useRef(0)

  const loadList = useCallback(async (next: ListRequest, signal?: AbortSignal) => {
    if (NOTIFICATION_DISABLED_FILTERS.includes(next.filter)) {
      setData({
        content: [],
        page: 0,
        size: NOTIFICATION_PAGE_SIZE,
        totalElements: 0,
        totalPages: 0,
      })
      setListLoading(false)
      setListError(null)
      return
    }

    const gen = ++listGen.current
    try {
      setListLoading(true)
      setListError(null)
      const result = await fetchNotifications(
        {
          ...filterToQuery[next.filter],
          page: next.page,
          size: NOTIFICATION_PAGE_SIZE,
        },
        signal,
      )
      if (gen !== listGen.current) return
      setData(result)
    } catch (err) {
      if (signal?.aborted) return
      if (gen !== listGen.current) return
      setListError(err instanceof Error ? err.message : '알림 목록을 불러오지 못했습니다.')
    } finally {
      if (gen === listGen.current) {
        setListLoading(false)
      }
    }
  }, [])

  useEffect(() => {
    const controller = new AbortController()
    void loadList(request, controller.signal)
    return () => controller.abort()
  }, [loadList, request])

  const loadDetail = useCallback(
    async (summary: NotificationSummaryItem, options: { markAsRead: boolean }) => {
      const gen = ++detailGen.current
      try {
        setDetailLoading(true)
        setDetailError(null)
        const detail = await fetchNotificationDetail(summary.notificationId)
        if (gen !== detailGen.current) return
        setSelectedDetail(detail)

        if (options.markAsRead && !summary.isRead) {
          try {
            await markNotificationAsRead(summary.notificationId)
            setData((prev) => {
              if (!prev) return prev
              return {
                ...prev,
                content: prev.content.map((item) =>
                  item.notificationId === summary.notificationId
                    ? { ...item, isRead: true }
                    : item,
                ),
              }
            })
            setSelectedSummary((prev) =>
              prev && prev.notificationId === summary.notificationId
                ? { ...prev, isRead: true }
                : prev,
            )
            setSelectedDetail((prev) =>
              prev && prev.notificationId === summary.notificationId
                ? { ...prev, isRead: true }
                : prev,
            )
          } catch {
            // 읽음 처리 실패는 상세 화면 자체에는 영향 주지 않음
          }
        }
      } catch (err) {
        if (gen !== detailGen.current) return
        setDetailError(err instanceof Error ? err.message : '알림 상세를 불러오지 못했습니다.')
      } finally {
        if (gen === detailGen.current) {
          setDetailLoading(false)
        }
      }
    },
    [],
  )

  // 사용자 클릭 — 읽음 처리 수행
  const handleSelect = useCallback(
    (summary: NotificationSummaryItem) => {
      setSelectedSummary(summary)
      setSelectedDetail(null)
      setDetailError(null)
      void loadDetail(summary, { markAsRead: true })
    },
    [loadDetail],
  )

  // 자동 선택 — 미리보기만, 읽음 처리 안 함
  const autoSelect = useCallback(
    (summary: NotificationSummaryItem) => {
      setSelectedSummary(summary)
      setSelectedDetail(null)
      setDetailError(null)
      void loadDetail(summary, { markAsRead: false })
    },
    [loadDetail],
  )

  // 목록 로딩 후 첫 항목 자동 선택 (사용자가 아직 선택 안 한 경우)
  useEffect(() => {
    if (!data || data.content.length === 0) return
    if (selectedSummary) return
    const first = data.content[0]
    autoSelect(first)
  }, [autoSelect, data, selectedSummary])

  const handleChangeFilter = useCallback((nextFilter: NotificationFilter) => {
    if (NOTIFICATION_DISABLED_FILTERS.includes(nextFilter)) {
      setFilter(nextFilter)
      setRequest({ filter: nextFilter, page: 0 })
      setSelectedSummary(null)
      setSelectedDetail(null)
      setDetailError(null)
      return
    }
    setFilter(nextFilter)
    setRequest({ filter: nextFilter, page: 0 })
    setSelectedSummary(null)
    setSelectedDetail(null)
    setDetailError(null)
  }, [])

  const changePage = useCallback((page: number) => {
    const nextPage = Number.isFinite(page) ? Math.max(0, Math.floor(page)) : 0
    setRequest((prev) => ({ ...prev, page: nextPage }))
    setSelectedSummary(null)
    setSelectedDetail(null)
  }, [])

  const handleMarkAllAsRead = useCallback(async () => {
    if (markAllPending) return
    try {
      setMarkAllPending(true)
      await markAllNotificationsAsRead()
      setData((prev) => {
        if (!prev) return prev
        return {
          ...prev,
          content: prev.content.map((item) => ({ ...item, isRead: true })),
        }
      })
      setSelectedSummary((prev) => (prev ? { ...prev, isRead: true } : prev))
      setSelectedDetail((prev) => (prev ? { ...prev, isRead: true } : prev))
      // UNREAD 필터에서는 목록이 비어야 하므로 재조회
      if (filter === 'UNREAD') {
        setRequest({ filter: 'UNREAD', page: 0 })
      }
    } catch (err) {
      setListError(err instanceof Error ? err.message : '전체 읽음 처리에 실패했습니다.')
    } finally {
      setMarkAllPending(false)
    }
  }, [filter, markAllPending])

  const refresh = useCallback(() => {
    void loadList(request)
  }, [loadList, request])

  const detailViewModel: NotificationDetailViewModel | null = useMemo(() => {
    if (!selectedDetail) return null
    return {
      ...selectedDetail,
      createdAt: selectedSummary?.createdAt,
    }
  }, [selectedDetail, selectedSummary])

  const empty = useMemo(
    () => !listLoading && !listError && (data?.content.length ?? 0) === 0,
    [data, listError, listLoading],
  )

  return {
    filter,
    data,
    page: request.page,
    size: NOTIFICATION_PAGE_SIZE,
    listLoading,
    listError,
    empty,
    isFilterDisabled: NOTIFICATION_DISABLED_FILTERS.includes(filter),

    selectedSummary,
    selectedDetail: detailViewModel,
    detailLoading,
    detailError,

    markAllPending,

    handleChangeFilter,
    handleSelect,
    handleMarkAllAsRead,
    changePage,
    refresh,
  }
}
