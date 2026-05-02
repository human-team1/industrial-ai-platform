import {
  useAsyncJobsPolling,
  useOperationLogs,
  useSystemComponentsPolling,
  useSystemStatusPolling,
} from '../features/operation/model/useOperationMonitoring'
import { OperationMonitoringView } from '../widgets/operation/OperationMonitoringView'
import { formatDateTime } from '../shared/lib/date'

export function OperationMonitoringPage() {
  const systemStatus = useSystemStatusPolling()
  const systemComponents = useSystemComponentsPolling()
  const operationLogs = useOperationLogs()
  const asyncJobs = useAsyncJobsPolling()

  function refreshAll() {
    systemStatus.refetch()
    systemComponents.refetch()
    operationLogs.refetch()
    asyncJobs.refetch()
  }

  const updatedTimes = [
    systemStatus.lastUpdatedAt,
    systemComponents.lastUpdatedAt,
    operationLogs.lastUpdatedAt,
    asyncJobs.lastUpdatedAt,
  ].filter(Boolean).sort()
  const latestUpdatedAt = updatedTimes[updatedTimes.length - 1]

  return (
    <div className="space-y-5">
      <div className="flex flex-col gap-3 lg:flex-row lg:items-end lg:justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">운영 모니터링</h1>
          <p className="mt-2 text-sm text-slate-500">시스템 상태, 컴포넌트 상태, 운영 로그, 작업 상태를 확인할 수 있습니다.</p>
          <p className="mt-1 text-xs text-slate-400">최근 갱신 {formatDateTime(latestUpdatedAt ?? undefined)}</p>
        </div>
        <button type="button" onClick={refreshAll} className="h-10 rounded bg-[#109498] px-4 text-sm font-semibold text-white">
          새로고침
        </button>
      </div>

      <OperationMonitoringView
        systemStatus={systemStatus}
        systemComponents={systemComponents}
        operationLogs={operationLogs}
        asyncJobs={asyncJobs}
      />
    </div>
  )
}
