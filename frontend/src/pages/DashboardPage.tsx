import { DashboardOverviewView } from '../widgets/dashboard'
import { useDashboardOverview } from '../features/dashboard/model/useDashboardOverview'

export function DashboardPage() {
  const {
    data,
    startDate,
    endDate,
    setStartDate,
    setEndDate,
    isLoading,
    error,
    reload,
  } = useDashboardOverview()

  return (
    <div className="space-y-5">
      <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">대시보드</h1>
          <p className="mt-2 text-sm text-slate-500">
            시스템의 주요 현황과 이상 탐지 요약 정보를 확인할 수 있습니다.
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          <input
            type="date"
            value={startDate}
            onChange={(event) => setStartDate(event.target.value)}
            className="h-10 rounded border border-slate-200 bg-white px-3 text-sm text-slate-700"
          />
          <span className="text-sm text-slate-400">~</span>
          <input
            type="date"
            value={endDate}
            onChange={(event) => setEndDate(event.target.value)}
            className="h-10 rounded border border-slate-200 bg-white px-3 text-sm text-slate-700"
          />
          <button
            type="button"
            onClick={reload}
            className="h-10 rounded bg-[#109498] px-4 text-sm font-semibold text-white transition-colors hover:bg-[#0d8285]"
          >
            새로고침
          </button>
        </div>
      </div>

      {isLoading ? <DashboardSkeleton /> : null}

      {!isLoading && error ? (
        <div className="rounded border border-rose-200 bg-rose-50 p-5">
          <p className="text-sm font-medium text-rose-700">{error}</p>
          <button
            type="button"
            onClick={reload}
            className="mt-3 rounded border border-rose-200 bg-white px-3 py-2 text-sm font-medium text-rose-700"
          >
            재시도
          </button>
        </div>
      ) : null}

      {!isLoading && !error && data ? <DashboardOverviewView data={data} /> : null}

      {!isLoading && !error && !data ? (
        <div className="rounded border border-slate-200 bg-white p-8 text-center text-sm text-slate-500">
          표시할 대시보드 데이터가 없습니다.
        </div>
      ) : null}
    </div>
  )
}

function DashboardSkeleton() {
  return (
    <div className="space-y-5">
      <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
        {Array.from({ length: 4 }).map((_, index) => (
          <div key={index} className="h-32 animate-pulse rounded border border-slate-200 bg-white" />
        ))}
      </div>
      <div className="grid grid-cols-1 gap-5 xl:grid-cols-[minmax(0,1fr)_320px]">
        <div className="h-80 animate-pulse rounded border border-slate-200 bg-white" />
        <div className="h-80 animate-pulse rounded border border-slate-200 bg-white" />
      </div>
    </div>
  )
}
