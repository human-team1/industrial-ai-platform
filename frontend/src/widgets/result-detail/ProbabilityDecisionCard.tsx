import type { ResultDecisionInfo } from '../../entities/result/model/types'
import { DecisionBadge } from '../../entities/result/ui/DecisionBadge'
import { InfoCard } from './InfoCard'

type Props = {
  result: ResultDecisionInfo
}

export function ProbabilityDecisionCard({ result }: Props) {
  const effectiveDecisionCode = result.finalDecisionCode ?? result.decisionCode
  const scoreText = toScore(result.score)
  const thresholdText = toScore(result.appliedThreshold)
  const scoreRatio = toScoreRatio(result.score, result.appliedThreshold)
  return (
    <InfoCard title="이미지 이상 점수 / 판정">
      <div className="flex items-end justify-between gap-4">
        <div>
          <p className="text-4xl font-bold text-slate-950">{scoreText}</p>
          <p className="mt-1 text-sm text-slate-500">임계값 {thresholdText} (판정 기준: 점수 {'>='} 임계값)</p>
        </div>
        <DecisionBadge decisionCode={effectiveDecisionCode} />
      </div>
      <div className="mt-5 h-3 overflow-hidden rounded-full bg-slate-100">
        <div
          className={`h-full ${progressColor(effectiveDecisionCode)}`}
          style={{ width: `${scoreRatio}%` }}
        />
      </div>
    </InfoCard>
  )
}

function toScore(value?: number | null) {
  if (value === null || value === undefined) return '-'
  return Number(value).toFixed(4)
}

function toScoreRatio(value?: number | null, threshold?: number | null) {
  if (value === null || value === undefined) return 0
  if (threshold === null || threshold === undefined || Number(threshold) <= 0) return 0
  const score = Math.max(0, Number(value))
  return Math.min(100, (score / Number(threshold)) * 100)
}

function progressColor(value?: string | null) {
  if (value === 'NORMAL') return 'bg-emerald-500'
  if (['RETEST', 'RECHECK', 'REINSPECTION'].includes(String(value))) return 'bg-orange-500'
  return 'bg-red-500'
}
