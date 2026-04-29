import { ChevronRight } from '../icons/ChevronRight'

export function ListRowButton({
  title,
  description,
  status,
  onClick,
  variant = 'default',
}: {
  title: string
  description: string
  status?: string
  onClick?: () => void
  variant?: 'default' | 'danger'
}) {
  const titleColor = variant === 'danger' ? 'text-[#dc2626]' : 'text-[#1f2937]'
  const borderColor = variant === 'danger' ? 'border-[#fecaca]' : 'border-[#e5e7eb]'

  return (
    <button
      type="button"
      onClick={onClick}
      className={`w-full flex items-center justify-between gap-4 px-4 py-3 bg-[#fefefe] border ${borderColor} rounded-[3px] text-left transition-colors hover:bg-[#f8f9fc] focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#1166e0]`}
    >
      <div className="flex flex-col gap-0.5 min-w-0">
        <span className={`text-[13px] font-medium ${titleColor}`}>{title}</span>
        <span className="text-[#6b7280] text-[11px] truncate">{description}</span>
      </div>
      <div className="flex items-center gap-2 shrink-0">
        {status && <span className="text-[#15803d] text-[11px] font-medium">{status}</span>}
        <ChevronRight className="w-2.5 h-2.5 text-[#6b7280]" />
      </div>
    </button>
  )
}
