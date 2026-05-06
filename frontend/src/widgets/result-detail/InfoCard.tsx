import type { ReactNode } from 'react'

type Props = {
  title: string
  children: ReactNode
}

export function InfoCard({ title, children }: Props) {
  return (
    <section className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
      <h2 className="mb-4 text-base font-semibold text-slate-950">{title}</h2>
      {children}
    </section>
  )
}
