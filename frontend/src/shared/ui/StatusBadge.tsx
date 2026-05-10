import { statusBadgeLabel } from '../lib/operationDisplay'

type Props = {
  value: string | null | undefined
}

export function StatusBadge({ value }: Props) {
  const raw = (value ?? 'UNKNOWN').toUpperCase()
  const label = statusBadgeLabel(value)
  const tone =
    raw === 'ERROR' || raw === 'FAILED'
      ? 'bg-rose-50 text-rose-700'
      : raw === 'WARNING' || raw === 'WARN' || raw === 'SLOW' || raw === 'RUNNING' || raw === 'PROCESSING' || raw === 'PENDING'
        ? 'bg-amber-50 text-amber-700'
        : raw === 'UNKNOWN'
          ? 'bg-slate-100 text-slate-600'
          : 'bg-emerald-50 text-emerald-700'

  return <span className={`rounded px-2 py-1 text-xs font-semibold ${tone}`}>{label}</span>
}
