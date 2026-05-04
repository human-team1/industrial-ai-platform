import type { ReactNode } from 'react'
import { DecisionBadge } from '../../entities/result/ui/DecisionBadge'
import { ScoreBar } from '../../entities/result/ui/ScoreBar'
import type { ResultPageResponse, ResultSummary } from '../../features/result/api/types'
import { mapResultListRow } from '../../features/result/model/mapper'

type Props = {
  data: ResultPageResponse | null
  loading: boolean
  error: string | null
  empty: boolean
  onRetry: () => void
  onResetFilters: () => void
  onDetail: (resultId: number) => void
}

export function ResultListTable({
  data,
  loading,
  error,
  empty,
  onRetry,
  onResetFilters,
  onDetail,
}: Props) {
  if (loading) return <StateBox title="검사 결과를 불러오는 중입니다." />
  if (error) return <StateBox title={error} actionLabel="재시도" onAction={onRetry} />
  if (empty) {
    return (
      <StateBox
        title="조회된 검사 결과가 없습니다."
        actionLabel="필터 초기화"
        onAction={onResetFilters}
      />
    )
  }

  return (
    <section className="overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
      <div className="overflow-x-auto">
        <table className="min-w-[1100px] w-full border-collapse text-left text-sm">
          <thead className="bg-slate-50 text-xs uppercase text-slate-500">
            <tr>
              {['결과 ID', '검사 ID', '검사시간', '설비/대상', '검사유형', '판정', '이상점수', '상태', '상세'].map((head) => (
                <th key={head} className="border-b border-slate-200 px-4 py-3 font-semibold">
                  {head}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {data?.content.map((item) => (
              <ResultRow key={item.resultId} item={item} onDetail={onDetail} />
            ))}
          </tbody>
        </table>
      </div>
    </section>
  )
}

function ResultRow({ item, onDetail }: { item: ResultSummary; onDetail: (resultId: number) => void }) {
  const mapped = mapResultListRow(item)
  const effectiveDecisionCode = mapped.finalDecisionCode ?? mapped.decisionCode

  return (
    <tr
      className="cursor-pointer border-b border-slate-100 last:border-0 hover:bg-slate-50"
      onClick={() => onDetail(item.resultId)}
    >
      <td className="px-4 py-3 font-mono text-xs text-slate-700">{mapped.resultId}</td>
      <td className="px-4 py-3 font-mono text-xs text-slate-700">{mapped.inspectionId}</td>
      <td className="px-4 py-3">{mapped.inspectedAt}</td>
      <td className="px-4 py-3">{display(mapped.targetLine)}</td>
      <td className="px-4 py-3">{mapped.runTypeLabel}</td>
      <td className="px-4 py-3">
        <DecisionBadge decisionCode={effectiveDecisionCode} />
      </td>
      <td className="px-4 py-3">
        <ScoreBar score={mapped.score} decisionCode={effectiveDecisionCode} />
      </td>
      <td className="px-4 py-3 text-xs text-slate-600">{mapped.statusLabel}</td>
      <td className="px-4 py-3 text-right">
        <button
          type="button"
          onClick={(event) => {
            event.stopPropagation()
            onDetail(item.resultId)
          }}
          className="btn-secondary"
        >
          상세
        </button>
      </td>
    </tr>
  )
}

function StateBox({
  title,
  actionLabel,
  onAction,
}: {
  title: string
  actionLabel?: string
  onAction?: () => void
}) {
  return (
    <div className="flex min-h-40 flex-col items-center justify-center gap-3 rounded-lg border border-slate-200 bg-white p-6 text-center text-sm text-slate-600 shadow-sm">
      <p>{title}</p>
      {actionLabel && onAction ? (
        <button type="button" onClick={onAction} className="btn-primary">
          {actionLabel}
        </button>
      ) : null}
    </div>
  )
}

function display(value: ReactNode) {
  return value === null || value === undefined || value === '' ? '-' : value
}
