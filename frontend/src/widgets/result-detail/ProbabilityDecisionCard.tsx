import type { ResultDecisionInfo } from '../../entities/result/model/types'
import { DecisionBadge } from '../../entities/result/ui/DecisionBadge'
import { InfoCard } from './InfoCard'

type Props = {
  result: ResultDecisionInfo
}

export function ProbabilityDecisionCard({ result }: Props) {
  const effectiveDecisionCode = result.finalDecisionCode ?? result.decisionCode
  const percent = toPercent(result.score)
  return (
    <InfoCard title="이상 확률 / 판정">
      <div className="flex items-end justify-between gap-4">
        <div>
          <p className="text-4xl font-bold text-slate-950">{percent}</p>
          <p className="mt-1 text-sm text-slate-500">anomaly score</p>
        </div>
        <DecisionBadge decisionCode={effectiveDecisionCode} />
      </div>
      <div className="mt-5 h-3 overflow-hidden rounded-full bg-slate-100">
        <div
          className={`h-full ${progressColor(effectiveDecisionCode)}`}
          style={{ width: percent === '-' ? '0%' : percent }}
        />
      </div>
    </InfoCard>
  )
}

function toPercent(value?: number | null) {
  if (value === null || value === undefined) return '-'
  return `${Math.max(0, Math.min(100, Number(value) * 100)).toFixed(1)}%`
}

function progressColor(value?: string | null) {
  if (value === 'NORMAL') return 'bg-emerald-500'
  if (['RETEST', 'RECHECK', 'REINSPECTION'].includes(String(value))) return 'bg-orange-500'
  return 'bg-red-500'
}
