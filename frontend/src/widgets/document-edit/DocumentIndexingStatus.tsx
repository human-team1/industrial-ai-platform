import type { IndexingStatus } from '../../entities/document/model/types'
import { StatusBadge } from '../../entities/document/ui/StatusBadge'
import { CircularProgress } from './CircularProgress'
import { INDEXING_STEPS, IndexingStepper } from './IndexingStepper'
import type { DocumentIndexingStatusProps } from './types'

function activeStepFromStatus(status: IndexingStatus | null | undefined): number {
  if (!status) return 0
  if (status === 'PENDING') return 1
  if (status === 'PROCESSING') return 3
  if (status === 'COMPLETED') return INDEXING_STEPS.length
  if (status === 'FAILED') return 2
  return 0
}

function progressPercentFromStatus(status: IndexingStatus | null | undefined): number {
  if (!status) return 0
  if (status === 'PENDING') return 15
  if (status === 'PROCESSING') return 78
  if (status === 'COMPLETED') return 100
  if (status === 'FAILED') return 40
  return 0
}

function formatDateTime(value?: string | null) {
  if (!value) return '-'
  return value.slice(0, 16).replace('T', ' ')
}

export function DocumentIndexingStatus({ mode, latestVersion }: DocumentIndexingStatusProps) {
  if (mode === 'create') {
    return (
      <section className="space-y-3">
        <h2 className="text-sm font-semibold text-slate-800">인덱싱 상태</h2>
        <div className="flex min-h-[100px] items-center rounded-lg border border-slate-200 bg-slate-50 px-5 py-4 text-sm leading-relaxed text-slate-600">
          문서 저장 후 인덱싱 상태가 표시됩니다.
        </div>
      </section>
    )
  }

  const status = latestVersion?.indexingStatus ?? null
  const activeStep = activeStepFromStatus(status)
  const percent = progressPercentFromStatus(status)
  const totalChunks = latestVersion?.indexedChunkCount ?? 0
  const processed = totalChunks > 0 ? Math.round(totalChunks * (percent / 100)) : 0
  const queued = totalChunks > 0 ? Math.max(0, totalChunks - processed) : 0

  return (
    <section className="space-y-4">
      <h2 className="text-sm font-semibold text-slate-800">인덱싱 상태</h2>

      <IndexingStepper activeStep={activeStep} failed={status === 'FAILED'} />

      <div className="grid grid-cols-1 gap-4 rounded-lg border border-slate-200 bg-white p-5 md:grid-cols-[auto_minmax(0,1fr)]">
        <CircularProgress percent={percent} status={status} />
        <div className="space-y-2 text-sm">
          <div className="flex items-center gap-2">
            {status ? <StatusBadge status={status} /> : null}
            <span className="text-xs text-slate-500">
              {formatDateTime(latestVersion?.indexedAt) || '시각 정보 없음'}
            </span>
          </div>
          <dl className="grid grid-cols-3 gap-3 pt-2 text-xs">
            <div>
              <dt className="text-slate-500">전체 청크</dt>
              <dd className="mt-0.5 text-base font-semibold text-slate-900">{totalChunks.toLocaleString()}</dd>
            </div>
            <div>
              <dt className="text-slate-500">처리 완료</dt>
              <dd className="mt-0.5 text-base font-semibold text-slate-900">{processed.toLocaleString()}</dd>
            </div>
            <div>
              <dt className="text-slate-500">대기/실패</dt>
              <dd className="mt-0.5 text-base font-semibold text-slate-900">{queued.toLocaleString()}</dd>
            </div>
          </dl>
        </div>
      </div>

      {status === 'FAILED' ? (
        <p className="rounded border border-rose-200 bg-rose-50 px-3 py-2 text-xs text-rose-700">
          실패 사유: {latestVersion?.indexErrorMessage ?? '제공되지 않았습니다.'}
        </p>
      ) : null}

      <p className="text-[11px] text-slate-400">
        ※ 단계 진행/원형 진행률/처리 통계는 시각 placeholder이며, 실제 단계별 상태 API 연동 후 정합화됩니다.
      </p>
    </section>
  )
}
