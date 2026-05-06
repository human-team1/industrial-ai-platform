import type { ReactNode } from 'react'

type Props = {
  label: string
  value?: ReactNode
}

export function Info({ label, value }: Props) {
  return (
    <div>
      <p className="text-xs font-medium text-slate-500">{label}</p>
      <div className="mt-1 text-sm text-slate-800">{value || '-'}</div>
    </div>
  )
}
