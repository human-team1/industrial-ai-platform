import { useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../../features/auth/model'
import { useNotificationPage } from '../../features/notification/model'
import {
  NotificationDetailPanel,
  NotificationFilterTabs,
  NotificationList,
  NotificationToolbar,
} from '../../features/notification/ui'
import { PaginationBar } from '../../shared/ui/pagination/PaginationBar'

export function NotificationPageContent() {
  const navigate = useNavigate()
  const { user } = useAuth()
  const isSiteAdmin = user?.role === 'ROLE_SITE_ADMIN'

  const {
    filter,
    data,
    page,
    listLoading,
    listError,
    empty,
    isFilterDisabled,

    selectedSummary,
    selectedDetail,
    detailLoading,
    detailError,

    markAllPending,

    handleChangeFilter,
    handleSelect,
    handleMarkAllAsRead,
    changePage,
    refresh,
  } = useNotificationPage()

  const isAdminPath = useCallback(
    (url: string) => /^\/admin(\/|$)/.test(url),
    [],
  )

  const handleOpenTarget = useCallback(
    (url: string) => {
      if (isAdminPath(url) && !isSiteAdmin) return
      if (/^https?:\/\//.test(url)) {
        window.open(url, '_blank', 'noopener,noreferrer')
        return
      }
      navigate(url)
    },
    [isAdminPath, isSiteAdmin, navigate],
  )

  const isAdminPathWithRole = useCallback(
    (url: string) => isAdminPath(url) && !isSiteAdmin,
    [isAdminPath, isSiteAdmin],
  )

  const totalElements = data?.totalElements ?? 0
  const totalPages = data?.totalPages ?? 0
  const unreadInList = data?.content.filter((item) => !item.isRead).length ?? 0

  const retryDetail = useCallback(() => {
    if (selectedSummary) {
      handleSelect(selectedSummary)
    }
  }, [handleSelect, selectedSummary])

  return (
    <div className="space-y-4">
      <header className="rounded-lg border border-slate-200 bg-white px-5 py-4 shadow-sm">
        <h1 className="text-xl font-bold text-slate-900">알림</h1>
        <p className="mt-1 text-sm text-slate-500">
          최근 발생한 이상 탐지·시스템·보고서 알림을 한곳에서 확인하고 읽음 처리할 수 있습니다.
        </p>
      </header>

      <NotificationFilterTabs
        filter={filter}
        onChange={handleChangeFilter}
        loading={listLoading}
      />

      <NotificationToolbar
        totalElements={totalElements}
        unreadInList={unreadInList}
        onMarkAllAsRead={handleMarkAllAsRead}
        onRefresh={refresh}
        markAllPending={markAllPending}
        loading={listLoading}
      />

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-[minmax(0,1fr)_minmax(0,1.2fr)]">
        <div className="space-y-3">
          <NotificationList
            data={data}
            loading={listLoading}
            error={listError}
            empty={empty}
            isFilterDisabled={isFilterDisabled}
            selectedId={selectedSummary?.notificationId ?? null}
            onSelect={handleSelect}
            onRetry={refresh}
          />

          {!isFilterDisabled && totalPages > 0 ? (
            <PaginationBar
              page={page}
              totalPages={totalPages}
              totalElements={totalElements}
              loading={listLoading}
              onPageChange={changePage}
            />
          ) : null}
        </div>

        <NotificationDetailPanel
          detail={selectedDetail}
          loading={detailLoading}
          error={detailError}
          onRetry={retryDetail}
          onOpenTarget={handleOpenTarget}
          isAdminPath={isAdminPathWithRole}
        />
      </div>
    </div>
  )
}
