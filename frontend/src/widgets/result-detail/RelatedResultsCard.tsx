import { formatDateMinute, labelDecision } from '../../entities/result/model/labels'
import { InfoCard } from './InfoCard'
import type { RelatedResult } from './types'

type Props = {
  relatedResults: RelatedResult[]
  onDetail: (resultId: number) => void
}

export function RelatedResultsCard({ relatedResults, onDetail }: Props) {
  return (
    <InfoCard title="관련 탐지 이력">
      {relatedResults.length === 0 ? (
        <p className="text-sm text-slate-500">관련 탐지 이력이 없습니다.</p>
      ) : (
        <div className="space-y-2">
          {relatedResults.map((item) => (
            <div
              key={item.resultId}
              className="grid grid-cols-[1fr_auto] gap-3 rounded border border-slate-200 p-3 text-sm"
            >
              <div>
                <p className="font-medium text-slate-900">{formatDateMinute(item.createdAt)}</p>
                <p className="mt-1 text-slate-500">
                  {display(item.location)} · {formatPercent(item.score)} · {labelDecision(item.decisionCode)}
                </p>
              </div>
              <button
                type="button"
                onClick={() => onDetail(item.resultId)}
                className="btn-secondary"
              >
                상세 보기
              </button>
            </div>
          ))}
        </div>
      )}
    </InfoCard>
  )
}

function display(value: string | null | undefined) {
  return value === null || value === undefined || value === '' ? '-' : value
}

function formatPercent(value?: number | null) {
  if (value === null || value === undefined) return '-'
  return `${Math.max(0, Math.min(100, Number(value) * 100)).toFixed(1)}%`
}
