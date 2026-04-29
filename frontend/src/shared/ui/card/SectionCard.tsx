import type { ReactNode } from 'react'

export function SectionCard({
  title,
  children,
  className = '',
}: {
  title: string
  children: ReactNode
  className?: string
}) {
  return (
    <div className={`bg-[#fefefe] border border-[#e5e7eb] rounded p-5 flex flex-col gap-4 ${className}`}>
      <h2 className="text-[#1f2937] text-[15px] font-semibold leading-none">{title}</h2>
      {children}
    </div>
  )
}
