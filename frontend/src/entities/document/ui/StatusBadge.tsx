import type { IndexingStatus } from '../model/types'
import { labelIndexingStatus } from '../model/labels'

type Props = {
  status: IndexingStatus
}

export function StatusBadge({ status }: Props) {
  const color =
    status === 'PENDING'
      ? 'bg-slate-100 text-slate-700'
      : status === 'PROCESSING'
        ? 'bg-blue-100 text-blue-700'
        : status === 'COMPLETED'
          ? 'bg-emerald-100 text-emerald-700'
          : 'bg-red-100 text-red-700'

  return (
    <span className={`rounded-full px-2 py-1 text-xs font-semibold ${color}`}>
      {labelIndexingStatus(status)}
    </span>
  )
}
