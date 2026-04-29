import { useId } from 'react'
import type { PublicOrganization } from '../../../entities/organization/types'
import { IconChevronDown } from '../../../shared/ui/icons/IconChevronDown'

type OrganizationFieldProps = {
  value: number | null
  organizations: PublicOrganization[]
  loading: boolean
  error: string | null
  onChange: (organizationId: number | null) => void
  disabled?: boolean
}

export function OrganizationField({
  value,
  organizations,
  loading,
  error,
  onChange,
  disabled = false,
}: OrganizationFieldProps) {
  const organizationId = useId()

  return (
    <div>
      <label htmlFor={organizationId} className="block text-[#374151] text-sm mb-1.5">
        소속조직<span className="text-red-400 ml-0.5">*</span>
      </label>

      <div className="relative">
        <select
          id={organizationId}
          value={value === null ? '' : String(value)}
          onChange={(e) => {
            const raw = e.target.value
            onChange(raw === '' ? null : Number(raw))
          }}
          disabled={disabled || loading || Boolean(error)}
          aria-invalid={Boolean(error)}
          className="w-full appearance-none pr-7 py-2 px-3 bg-[#fdfdfd] border-2 border-[#edeef2] rounded text-[#1f2937] text-[13px] outline-none focus:border-[#4a90e2] transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <option value="">
            {loading ? '조직 목록 불러오는 중...' : '조직을 선택하세요'}
          </option>
          {organizations.map((o) => (
            <option key={o.id} value={String(o.id)}>
              {o.name}
            </option>
          ))}
        </select>
        <span className="absolute right-2.5 top-1/2 -translate-y-1/2 pointer-events-none text-[#c3c7d2]">
          <IconChevronDown />
        </span>
      </div>

      {error && <p className="text-red-500 text-xs mt-1">{error}</p>}
    </div>
  )
}
