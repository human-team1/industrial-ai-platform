import { labelDecision } from '../model/labels'

type Props = {
  decisionCode?: string | null
}

export function DecisionBadge({ decisionCode }: Props) {
  const normalized = String(decisionCode ?? '')
  const className =
    normalized === 'NORMAL'
      ? 'bg-emerald-50 text-emerald-700 ring-emerald-200'
      : ['RETEST', 'RECHECK', 'REINSPECTION'].includes(normalized)
        ? 'bg-orange-50 text-orange-700 ring-orange-200'
        : 'bg-red-50 text-red-700 ring-red-200'
  return (
    <span className={`inline-flex rounded-full px-2.5 py-1 text-xs font-semibold ring-1 ${className}`}>
      {labelDecision(normalized)}
    </span>
  )
}
