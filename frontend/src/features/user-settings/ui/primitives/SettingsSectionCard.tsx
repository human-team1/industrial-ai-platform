import type { ReactNode } from 'react'

type SettingsSectionCardProps = {
  title: string
  description: string
  children: ReactNode
}

export function SettingsSectionCard({ title, description, children }: SettingsSectionCardProps) {
  return (
    <div className="bg-[#fefefe] border border-[#f0f1f5] rounded p-5 flex flex-col gap-4">
      <div>
        <h2 className="text-[#4b5563] text-base font-normal leading-none mb-1">{title}</h2>
        <p className="text-[#374151] text-[11px]">{description}</p>
      </div>
      {children}
    </div>
  )
}
