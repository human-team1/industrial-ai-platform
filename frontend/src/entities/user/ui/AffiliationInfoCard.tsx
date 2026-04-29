import { SectionCard } from '../../../shared/ui/card/SectionCard'
import { ChevronRight } from '../../../shared/ui/icons/ChevronRight'
import type { Affiliation } from '../types'
import { PLACEHOLDER } from '../model/constants'

export function AffiliationInfoCard({
  affiliation,
  onViewOrganization,
}: {
  affiliation: Affiliation
  onViewOrganization?: () => void
}) {
  const rows: { label: string; value?: string }[] = [
    { label: '조직', value: affiliation.organization },
    { label: '팀', value: affiliation.team },
    { label: '역할', value: affiliation.role },
    { label: '권한' },
  ]

  return (
    <SectionCard title="소속 정보">
      <dl className="flex flex-col gap-3">
        {rows.map((row) => (
          <div key={row.label} className="flex items-start gap-4">
            <dt className="w-12 text-[#6b7280] text-[12px] font-medium shrink-0">{row.label}</dt>
            {row.value ? (
              <dd className="text-[#1f2937] text-[13px]">{row.value}</dd>
            ) : (
              <dd className="flex flex-wrap gap-1.5">
                {affiliation.permissions.length > 0 ? (
                  affiliation.permissions.map((permission) => (
                    <span
                      key={permission}
                      className="px-2.5 py-1 bg-[#dbeafe] border border-[#bfdbfe] rounded-[5px] text-[#1d4ed8] text-[11px] font-medium"
                    >
                      {permission}
                    </span>
                  ))
                ) : (
                  <span className="text-[#1f2937] text-[13px]">{PLACEHOLDER}</span>
                )}
              </dd>
            )}
          </div>
        ))}
      </dl>

      <button
        type="button"
        onClick={onViewOrganization}
        className="w-full flex items-center justify-center gap-1.5 mt-1 px-4 py-2.5 bg-[#fefefe] border border-[#e5e7eb] rounded-[3px] text-[#374151] text-xs font-medium hover:bg-[#f5f6fa] transition-colors focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#1166e0]"
      >
        조직 정보 보기
        <ChevronRight className="w-2.5 h-2.5" />
      </button>
    </SectionCard>
  )
}
