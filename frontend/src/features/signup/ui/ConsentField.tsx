import { useId, useState } from 'react'
import { PrivacyPolicyModal } from '../../../shared/ui/legal/PrivacyPolicyModal'

type ConsentFieldProps = {
  value: boolean
  onChange: (value: boolean) => void
  disabled?: boolean
}

export function ConsentField({ value, onChange, disabled = false }: ConsentFieldProps) {
  const consentId = useId()
  const [policyOpen, setPolicyOpen] = useState(false)

  return (
    <div className="flex items-center gap-2">
      <input
        id={consentId}
        name="consent"
        type="checkbox"
        checked={value}
        onChange={(e) => onChange(e.target.checked)}
        required
        disabled={disabled}
        className="w-[18px] h-[18px] accent-[#1166e0] cursor-pointer shrink-0 disabled:cursor-not-allowed"
      />
      <span className="text-[#374151] text-[13px]">개인정보 처리 동의</span>
      <span className="text-[#6b7280] text-[13px]">(필수)</span>
      <label htmlFor={consentId} className="text-[#6b7280] text-xs cursor-pointer">
        개인정보 처리방침에 동의합니다.
      </label>
      <button
        type="button"
        onClick={() => setPolicyOpen(true)}
        className="text-[#91b9f4] text-xs underline hover:text-[#5591e7] transition-colors"
        aria-label="개인정보 처리방침 전문 보기"
      >
        전문 보기
      </button>
      <PrivacyPolicyModal open={policyOpen} onClose={() => setPolicyOpen(false)} />
    </div>
  )
}
