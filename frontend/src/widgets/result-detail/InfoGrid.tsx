import type { ReactNode } from 'react'

type Props = {
  items: Array<[string, ReactNode]>
}

export function InfoGrid({ items }: Props) {
  return (
    <dl className="grid grid-cols-1 gap-3 sm:grid-cols-2">
      {items.map(([label, value]) => (
        <div key={label}>
          <dt className="text-xs font-semibold uppercase text-slate-500">{label}</dt>
          <dd className="mt-1 text-sm text-slate-900">{display(value)}</dd>
        </div>
      ))}
    </dl>
  )
}

function display(value: ReactNode) {
  return value === null || value === undefined || value === '' ? '-' : value
}
