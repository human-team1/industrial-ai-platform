import type { ReactNode } from 'react'
import { formatDateTime } from '../../../shared/lib/date'
import {
  notificationTypeLabelMap,
  notificationVisualGroupMap,
  severityLabelMap,
  type NotificationDetailViewModel,
  type NotificationFilter,
  type NotificationListData,
  type NotificationSeverity,
  type NotificationSummaryItem,
  type NotificationType,
  type NotificationVisualGroup,
} from '../types'

const FILTER_TABS: Array<{ key: NotificationFilter; label: string }> = [
  { key: 'ALL', label: '전체' },
  { key: 'UNREAD', label: '읽지 않음' },
  { key: 'ANOMALY', label: '이상 탐지' },
  { key: 'SYSTEM', label: '시스템' },
  { key: 'REPORT', label: '보고서' },
]

const DISABLED_FILTERS: NotificationFilter[] = ['ANOMALY']

export function NotificationFilterTabs({
  filter,
  onChange,
  loading,
}: {
  filter: NotificationFilter
  onChange: (next: NotificationFilter) => void
  loading?: boolean
}) {
  return (
    <div className="flex flex-wrap gap-2" role="tablist" aria-label="알림 필터">
      {FILTER_TABS.map((tab) => {
        const isActive = filter === tab.key
        const isDisabled = DISABLED_FILTERS.includes(tab.key)
        const baseClass =
          'rounded-full border px-4 py-1.5 text-xs font-semibold transition-colors'
        const activeClass = isActive
          ? 'border-slate-900 bg-slate-900 text-white'
          : 'border-slate-200 bg-white text-slate-700 hover:border-slate-300 hover:bg-slate-50'
        const disabledClass = isDisabled
          ? 'cursor-not-allowed opacity-60 hover:border-slate-200 hover:bg-white'
          : ''

        return (
          <button
            key={tab.key}
            type="button"
            role="tab"
            aria-selected={isActive}
            aria-disabled={isDisabled || undefined}
            disabled={loading}
            onClick={() => {
              if (isDisabled) {
                onChange(tab.key)
                return
              }
              onChange(tab.key)
            }}
            className={`${baseClass} ${activeClass} ${disabledClass}`}
            title={isDisabled ? '준비중인 필터입니다.' : undefined}
          >
            {tab.label}
            {isDisabled ? <span className="ml-1.5 text-[10px] font-medium">준비중</span> : null}
          </button>
        )
      })}
    </div>
  )
}

export function NotificationToolbar({
  totalElements,
  unreadInList,
  onMarkAllAsRead,
  onRefresh,
  markAllPending,
  loading,
}: {
  totalElements: number
  unreadInList: number
  onMarkAllAsRead: () => void
  onRefresh: () => void
  markAllPending: boolean
  loading: boolean
}) {
  return (
    <div className="flex flex-wrap items-center justify-between gap-3 rounded-lg border border-slate-200 bg-white px-4 py-3 shadow-sm">
      <div className="text-sm text-slate-600">
        현재 페이지에 <span className="font-semibold text-slate-900">{totalElements.toLocaleString()}</span>건
        {unreadInList > 0 ? (
          <span className="ml-2 text-slate-500">
            · 안읽음 <span className="font-semibold text-blue-600">{unreadInList.toLocaleString()}</span>건
          </span>
        ) : null}
      </div>
      <div className="flex items-center gap-2">
        <button type="button" onClick={onRefresh} disabled={loading} className="btn-secondary">
          새로고침
        </button>
        <button
          type="button"
          onClick={onMarkAllAsRead}
          disabled={markAllPending || loading || unreadInList === 0}
          className="btn-primary"
        >
          {markAllPending ? '처리 중…' : '전체 읽음'}
        </button>
      </div>
    </div>
  )
}

export function NotificationList({
  data,
  loading,
  error,
  empty,
  isFilterDisabled,
  selectedId,
  onSelect,
  onRetry,
}: {
  data: NotificationListData | null
  loading: boolean
  error: string | null
  empty: boolean
  isFilterDisabled: boolean
  selectedId: number | null
  onSelect: (item: NotificationSummaryItem) => void
  onRetry: () => void
}) {
  if (isFilterDisabled) {
    return (
      <StateBox
        title="이상 탐지 필터는 준비 중입니다."
        description="현재 백엔드 필터 계약에서는 이상 탐지 묶음 조회가 지원되지 않아 비활성 처리 중입니다."
      />
    )
  }
  if (loading) return <NotificationSkeleton />
  if (error) {
    return <StateBox title={error} actionLabel="재시도" onAction={onRetry} />
  }
  if (empty) {
    return <NotificationEmptyState />
  }

  return (
    <ul className="divide-y divide-slate-100 overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
      {data?.content.map((item) => (
        <NotificationListItem
          key={item.notificationId}
          item={item}
          selected={item.notificationId === selectedId}
          onSelect={onSelect}
        />
      ))}
    </ul>
  )
}

export function NotificationListItem({
  item,
  selected,
  onSelect,
}: {
  item: NotificationSummaryItem
  selected: boolean
  onSelect: (item: NotificationSummaryItem) => void
}) {
  const group = notificationVisualGroupMap[item.notificationType]
  const isUnread = !item.isRead

  return (
    <li>
      <button
        type="button"
        onClick={() => onSelect(item)}
        className={`flex w-full items-start gap-3 px-4 py-3 text-left transition-colors hover:bg-slate-50 ${
          selected ? 'bg-blue-50/60' : 'bg-white'
        }`}
        aria-current={selected || undefined}
      >
        <NotificationIcon group={group} />
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2">
            <NotificationSeverityBadge severity={item.severity} />
            <span className="truncate text-xs text-slate-500">
              {notificationTypeLabelMap[item.notificationType]}
            </span>
          </div>
          <p
            className={`mt-1 truncate text-sm ${
              isUnread ? 'font-semibold text-slate-900' : 'font-medium text-slate-700'
            }`}
          >
            {item.title}
          </p>
          <p className="mt-1 text-[11px] text-slate-400">{formatDateTime(item.createdAt)}</p>
        </div>
        {isUnread ? (
          <span
            aria-label="읽지 않음"
            className="mt-1 h-2 w-2 shrink-0 rounded-full bg-blue-500"
          />
        ) : null}
      </button>
    </li>
  )
}

export function NotificationDetailPanel({
  detail,
  loading,
  error,
  onRetry,
  onOpenTarget,
  isAdminPath,
}: {
  detail: NotificationDetailViewModel | null
  loading: boolean
  error: string | null
  onRetry: () => void
  onOpenTarget: (url: string) => void
  isAdminPath: (url: string) => boolean
}) {
  if (loading) {
    return (
      <section className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
        <div className="animate-pulse space-y-4">
          <div className="h-5 w-20 rounded bg-slate-100" />
          <div className="h-7 w-2/3 rounded bg-slate-100" />
          <div className="h-4 w-1/2 rounded bg-slate-100" />
          <div className="mt-6 h-24 w-full rounded bg-slate-100" />
        </div>
      </section>
    )
  }

  if (error) {
    return <StateBox title={error} actionLabel="재시도" onAction={onRetry} />
  }

  if (!detail) {
    return (
      <section className="flex min-h-60 items-center justify-center rounded-lg border border-dashed border-slate-300 bg-white p-8 text-sm text-slate-500 shadow-sm">
        좌측 목록에서 알림을 선택하면 상세 내용을 확인할 수 있습니다.
      </section>
    )
  }

  const adminOnly = detail.targetUrl ? isAdminPath(detail.targetUrl) : false

  return (
    <section className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
      <div className="flex items-center gap-2">
        <NotificationSeverityBadge severity={detail.severity} />
        <span className="text-xs text-slate-500">
          {notificationTypeLabelMap[detail.notificationType]}
        </span>
      </div>

      <h2 className="mt-3 text-lg font-bold text-slate-900">{detail.title}</h2>

      {detail.createdAt ? (
        <p className="mt-1 text-xs text-slate-500">{formatDateTime(detail.createdAt)}</p>
      ) : null}

      <div className="mt-5 whitespace-pre-line rounded bg-slate-50 p-4 text-sm leading-6 text-slate-700">
        {detail.message || '상세 메시지가 없습니다.'}
      </div>

      {detail.targetUrl ? (
        <div className="mt-5 flex items-center gap-3">
          <button
            type="button"
            onClick={() => detail.targetUrl && onOpenTarget(detail.targetUrl)}
            disabled={adminOnly}
            className="btn-primary disabled:cursor-not-allowed disabled:opacity-60"
          >
            관련 페이지로 이동
          </button>
          {adminOnly ? (
            <span className="text-xs text-slate-500">관리자 전용 링크입니다.</span>
          ) : null}
        </div>
      ) : null}
    </section>
  )
}

export function NotificationSeverityBadge({ severity }: { severity: NotificationSeverity }) {
  const className =
    severity === 'CRITICAL'
      ? 'bg-red-50 text-red-700 ring-red-200'
      : severity === 'WARNING'
        ? 'bg-orange-50 text-orange-700 ring-orange-200'
        : 'bg-slate-100 text-slate-700 ring-slate-200'

  return (
    <span
      className={`inline-flex rounded-full px-2 py-0.5 text-[11px] font-semibold ring-1 ${className}`}
    >
      {severityLabelMap[severity]}
    </span>
  )
}

export function NotificationIcon({ group }: { group: NotificationVisualGroup }) {
  const colorClass = {
    anomaly: 'bg-red-50 text-red-600',
    review: 'bg-emerald-50 text-emerald-600',
    system: 'bg-slate-100 text-slate-600',
    user: 'bg-blue-50 text-blue-600',
    report: 'bg-violet-50 text-violet-600',
  }[group]

  return (
    <div
      className={`flex h-9 w-9 shrink-0 items-center justify-center rounded-full text-base ${colorClass}`}
      aria-hidden="true"
    >
      {iconChar(group)}
    </div>
  )
}

export function NotificationEmptyState() {
  return (
    <StateBox
      title="표시할 알림이 없습니다."
      description="새 알림이 도착하면 이곳에 표시됩니다."
    />
  )
}

export function NotificationSkeleton() {
  return (
    <ul className="divide-y divide-slate-100 overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
      {[0, 1, 2, 3, 4].map((key) => (
        <li key={key} className="flex items-start gap-3 px-4 py-3">
          <div className="h-9 w-9 shrink-0 animate-pulse rounded-full bg-slate-100" />
          <div className="flex-1 space-y-2">
            <div className="h-3 w-24 animate-pulse rounded bg-slate-100" />
            <div className="h-4 w-2/3 animate-pulse rounded bg-slate-100" />
            <div className="h-3 w-1/3 animate-pulse rounded bg-slate-100" />
          </div>
        </li>
      ))}
    </ul>
  )
}

function StateBox({
  title,
  description,
  actionLabel,
  onAction,
}: {
  title: string
  description?: string
  actionLabel?: string
  onAction?: () => void
}) {
  return (
    <div className="flex min-h-60 flex-col items-center justify-center gap-2 rounded-lg border border-slate-200 bg-white p-8 text-center text-sm text-slate-600 shadow-sm">
      <p className="font-medium text-slate-900">{title}</p>
      {description ? <p className="text-slate-500">{description}</p> : null}
      {actionLabel && onAction ? (
        <button type="button" onClick={onAction} className="btn-primary mt-2">
          {actionLabel}
        </button>
      ) : null}
    </div>
  )
}

function iconChar(group: NotificationVisualGroup): ReactNode {
  if (group === 'anomaly') return '⚠'
  if (group === 'review') return '✓'
  if (group === 'system') return '⚙'
  if (group === 'user') return '👤'
  if (group === 'report') return '📄'
  return '•'
}

// 사용된 NotificationType 인덱스 보존용 (트리쉐이킹 안전)
export type { NotificationType }
