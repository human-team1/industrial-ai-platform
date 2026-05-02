import type { Dispatch, ReactNode, SetStateAction } from 'react'
import type { AsyncJob, OperationLog, PageResponse, SystemComponentStatus, SystemStatus } from '../../entities/operation/model/types'
import type { OperationLogQuery } from '../../features/operation/api'
import { formatDateTime } from '../../shared/lib/date'

type ResourceState<T> = {
  data: T | null
  initialLoading: boolean
  isRefreshing: boolean
  error: string | null
  lastUpdatedAt: string | null
  refetch: () => void
}

type OperationLogsState = ResourceState<PageResponse<OperationLog>> & {
  filters: OperationLogQuery
  setFilters: Dispatch<SetStateAction<OperationLogQuery>>
}

export function OperationMonitoringView({
  systemStatus,
  systemComponents,
  operationLogs,
  asyncJobs,
}: {
  systemStatus: ResourceState<SystemStatus>
  systemComponents: ResourceState<SystemComponentStatus[]>
  operationLogs: OperationLogsState
  asyncJobs: ResourceState<PageResponse<AsyncJob>>
}) {
  return (
    <div className="space-y-5">
      <SystemStatusCards state={systemStatus} />
      <ComponentGrid state={systemComponents} />
      <div className="grid grid-cols-1 gap-5 xl:grid-cols-[minmax(0,1.4fr)_minmax(320px,0.8fr)]">
        <OperationLogTable state={operationLogs} />
        <div className="space-y-5">
          <AsyncJobTable state={asyncJobs} />
          <ModelVersionPanel />
        </div>
      </div>
    </div>
  )
}

function ModelVersionPanel() {
  return (
    <section className="rounded border border-slate-200 bg-white p-5 shadow-sm">
      <h2 className="text-base font-semibold text-slate-900">모델 버전 정보</h2>
      <p className="mt-4 text-sm text-slate-500">
        모델 관리 API가 연결되면 활성 모델 버전, 배포 상태, 검증일자를 표시합니다.
      </p>
    </section>
  )
}

function SystemStatusCards({ state }: { state: ResourceState<SystemStatus> }) {
  const status = state.data
  const items = [
    ['CPU 사용률', status?.cpuUsage != null ? `${status.cpuUsage}%` : '-'],
    ['메모리 사용률', status?.memoryUsage != null ? `${status.memoryUsage}%` : '-'],
    ['디스크 사용률', status?.diskUsage != null ? `${status.diskUsage}%` : '-'],
    ['API 응답시간', status?.responseTimeMs != null ? `${status.responseTimeMs}ms` : '-'],
  ]
  return (
    <section className="rounded border border-slate-200 bg-white p-5 shadow-sm">
      <WidgetHeader title="시스템 상태" state={state} badge={<StatusBadge value={status?.overallStatus ?? 'UNKNOWN'} />} />
      {state.initialLoading ? <CardSkeleton /> : (
        <>
          <WidgetInlineError state={state} />
          <div className="mt-4 grid grid-cols-1 gap-4 md:grid-cols-4">
            {items.map(([label, value]) => (
              <div key={label} className="rounded bg-slate-50 p-4">
                <p className="text-xs text-slate-500">{label}</p>
                <p className="mt-2 text-xl font-bold text-slate-900">{value}</p>
              </div>
            ))}
          </div>
        </>
      )}
    </section>
  )
}

function ComponentGrid({ state }: { state: ResourceState<SystemComponentStatus[]> }) {
  const components = state.data ?? []
  return (
    <section className="rounded border border-slate-200 bg-white p-5 shadow-sm">
      <WidgetHeader title="컴포넌트 상태" state={state} />
      {state.initialLoading ? <CardSkeleton /> : (
        <>
          <WidgetInlineError state={state} />
          {components.length === 0 ? <EmptyState /> : (
            <div className="mt-4 grid grid-cols-1 gap-3 md:grid-cols-2 xl:grid-cols-4">
              {components.map((item) => (
                <div key={`${item.componentType}-${item.componentStatusId ?? item.checkedAt ?? 'latest'}`} className="rounded border border-slate-100 bg-slate-50 p-4">
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <p className="font-semibold text-slate-900">{item.componentName}</p>
                      <p className="mt-1 text-xs text-slate-500">{item.componentType}</p>
                    </div>
                    <StatusBadge value={item.status} />
                  </div>
                  <p className="mt-3 text-sm text-slate-600">{item.message ?? '-'}</p>
                  <p className="mt-2 text-xs text-slate-500">응답 {item.responseTimeMs ?? '-'}ms</p>
                  <p className="mt-1 text-xs text-slate-500">
                    CPU {formatPercent(item.cpuUsage)} · MEM {formatPercent(item.memoryUsage)} · DISK {formatPercent(item.diskUsage)}
                  </p>
                  <p className="mt-1 truncate text-xs text-slate-400">{item.hostName ?? '-'} / {item.instanceId ?? '-'}</p>
                  <p className="mt-1 text-xs text-slate-400">checked {formatDateTime(item.checkedAt ?? undefined)}</p>
                </div>
              ))}
            </div>
          )}
        </>
      )}
    </section>
  )
}

function OperationLogTable({ state }: { state: OperationLogsState }) {
  const rows = state.data?.content ?? []
  const { filters, setFilters } = state
  return (
    <section className="rounded border border-slate-200 bg-white p-5 shadow-sm">
      <div className="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
        <WidgetHeader title="운영 로그" state={state} />
        <div className="flex flex-wrap gap-2">
          <Select value={filters.level ?? ''} onChange={(level) => setFilters({ ...filters, level, page: 0 })} options={['', 'INFO', 'WARN', 'ERROR']} />
          <Select value={filters.eventStatus ?? ''} onChange={(eventStatus) => setFilters({ ...filters, eventStatus, page: 0 })} options={['', 'SUCCESS', 'FAILED', 'SLOW', 'UNKNOWN']} />
          <input className="h-9 rounded border border-slate-200 px-2 text-xs" placeholder="컴포넌트" value={filters.sourceComponent ?? ''} onChange={(event) => setFilters({ ...filters, sourceComponent: event.target.value, page: 0 })} />
          <input type="date" className="h-9 rounded border border-slate-200 px-2 text-xs" value={filters.startDate ?? ''} onChange={(event) => setFilters({ ...filters, startDate: event.target.value, page: 0 })} />
          <input type="date" className="h-9 rounded border border-slate-200 px-2 text-xs" value={filters.endDate ?? ''} onChange={(event) => setFilters({ ...filters, endDate: event.target.value, page: 0 })} />
        </div>
      </div>
      {state.initialLoading ? <TableSkeleton /> : (
        <>
          <WidgetInlineError state={state} />
          <div className="mt-4 overflow-x-auto">
            {rows.length === 0 ? <EmptyState /> : (
              <table className="min-w-full text-left text-sm">
                <thead className="text-xs text-slate-500">
                  <tr className="border-b border-slate-100">
                    {['시간', '레벨', '컴포넌트', '이벤트 유형', '상태', '메시지', 'requestId', '관련 경로'].map((head) => <th key={head} className="py-3 font-medium">{head}</th>)}
                  </tr>
                </thead>
                <tbody>
                  {rows.map((row) => (
                    <tr key={row.operationLogId} className="border-b border-slate-50 last:border-0">
                      <td className="whitespace-nowrap py-3 text-slate-600">{formatDateTime(row.createdAt ?? undefined)}</td>
                      <td className="py-3"><LevelBadge value={row.logLevel ?? 'INFO'} /></td>
                      <td className="py-3 text-slate-700">{row.sourceComponent ?? '-'}</td>
                      <td className="py-3 text-slate-700">{row.eventType ?? '-'}</td>
                      <td className="py-3"><StatusBadge value={row.eventStatus ?? 'UNKNOWN'} /></td>
                      <td className="max-w-[240px] truncate py-3 text-slate-700">{row.detailMessage ?? '-'}</td>
                      <td className="py-3 text-xs text-slate-500">{row.requestId ?? '-'}</td>
                      <td className="py-3 text-xs text-slate-500">{row.relatedPath ?? '-'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </>
      )}
    </section>
  )
}

function AsyncJobTable({ state }: { state: ResourceState<PageResponse<AsyncJob>> }) {
  const rows = state.data?.content ?? []
  return (
    <section className="rounded border border-slate-200 bg-white p-5 shadow-sm">
      <WidgetHeader title="비동기 작업" state={state} />
      {state.initialLoading ? <CardSkeleton /> : (
        <div className="mt-4 space-y-3">
          <WidgetInlineError state={state} />
          {rows.length === 0 ? <EmptyState /> : null}
          {rows.map((job) => (
            <div key={job.jobId} className="rounded border border-slate-100 bg-slate-50 p-3">
              <div className="flex items-center justify-between gap-3">
                <p className="font-medium text-slate-900">{job.jobType}</p>
                <StatusBadge value={job.jobStatus ?? 'UNKNOWN'} />
              </div>
              <p className="mt-1 text-xs text-slate-500">{job.targetType ?? '-'} #{job.targetId ?? '-'}</p>
              <p className="mt-2 text-xs text-slate-500">{formatDateTime(job.createdAt ?? undefined)} → {formatDateTime(job.completedAt ?? undefined)}</p>
              {job.errorMessage ? <p className="mt-2 text-xs text-rose-600">{job.errorMessage}</p> : null}
            </div>
          ))}
        </div>
      )}
    </section>
  )
}

function WidgetHeader<T>({ title, state, badge }: { title: string; state: ResourceState<T>; badge?: ReactNode }) {
  return (
    <div className="flex min-w-0 items-center justify-between gap-3">
      <div className="min-w-0">
        <div className="flex items-center gap-2">
          <h2 className="text-base font-semibold text-slate-900">{title}</h2>
          {state.isRefreshing ? <span className="text-xs font-medium text-cyan-700">갱신 중</span> : null}
        </div>
        {state.lastUpdatedAt ? <p className="mt-1 text-xs text-slate-400">{formatDateTime(state.lastUpdatedAt)}</p> : null}
      </div>
      <div className="flex items-center gap-2">
        {badge}
        <button type="button" onClick={state.refetch} className="h-8 rounded border border-slate-200 bg-white px-3 text-xs font-medium text-slate-600">
          새로고침
        </button>
      </div>
    </div>
  )
}

function WidgetInlineError<T>({ state }: { state: ResourceState<T> }) {
  if (!state.error) return null
  return (
    <div className="mt-3 flex items-center justify-between gap-3 rounded border border-amber-200 bg-amber-50 px-3 py-2 text-xs text-amber-700">
      <span>{state.error}</span>
      <button type="button" onClick={state.refetch} className="font-semibold">재시도</button>
    </div>
  )
}

function Select({ value, options, onChange }: { value: string; options: string[]; onChange: (value: string) => void }) {
  return <select className="h-9 rounded border border-slate-200 px-2 text-xs" value={value} onChange={(event) => onChange(event.target.value)}>{options.map((option) => <option key={option} value={option}>{option || '전체'}</option>)}</select>
}

export function StatusBadge({ value }: { value: string }) {
  const tone = value === 'ERROR' || value === 'FAILED' ? 'bg-rose-50 text-rose-700' : value === 'WARNING' || value === 'WARN' || value === 'SLOW' ? 'bg-amber-50 text-amber-700' : value === 'UNKNOWN' ? 'bg-slate-100 text-slate-600' : 'bg-emerald-50 text-emerald-700'
  return <span className={`rounded px-2 py-1 text-xs font-semibold ${tone}`}>{value}</span>
}

function LevelBadge({ value }: { value: string }) {
  const tone = value === 'ERROR' ? 'bg-rose-50 text-rose-700' : value === 'WARN' ? 'bg-amber-50 text-amber-700' : 'bg-sky-50 text-sky-700'
  return <span className={`rounded px-2 py-1 text-xs font-semibold ${tone}`}>{value}</span>
}

function formatPercent(value?: number | null) {
  return value == null ? '-' : `${value}%`
}

function CardSkeleton() {
  return <div className="mt-4 h-32 animate-pulse rounded bg-slate-100" />
}

function TableSkeleton() {
  return <div className="mt-4 h-64 animate-pulse rounded bg-slate-100" />
}

function EmptyState() {
  return <p className="py-8 text-center text-sm text-slate-500">표시할 데이터가 없습니다.</p>
}
