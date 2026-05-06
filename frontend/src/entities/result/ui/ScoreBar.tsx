type Props = {
  score?: number | null
  decisionCode?: string | null
}

export function ScoreBar({ score, decisionCode }: Props) {
  const percent = toPercent(score)
  return (
    <div className="min-w-32">
      <div className="mb-1 text-xs font-semibold text-slate-700">{formatScore(score)}</div>
      <div className="h-2 overflow-hidden rounded-full bg-slate-100">
        <div
          className={`h-full ${progressColor(decisionCode)}`}
          style={{ width: percent === '-' ? '0%' : percent }}
        />
      </div>
    </div>
  )
}

function toPercent(value?: number | null) {
  if (value === null || value === undefined) return '-'
  return `${Math.max(0, Math.min(100, Number(value) * 100)).toFixed(1)}%`
}

function formatScore(value?: number | null) {
  if (value === null || value === undefined) return '-'
  return Number(value).toFixed(2)
}

function progressColor(value?: string | null) {
  if (value === 'NORMAL') return 'bg-emerald-500'
  if (['RETEST', 'RECHECK', 'REINSPECTION'].includes(String(value))) return 'bg-orange-500'
  return 'bg-red-500'
}
