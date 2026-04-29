import { IconCheck } from '../../../shared/ui/icons/IconCheck'

export function StepIndicator() {
  return (
    <nav aria-label="가입 단계" className="flex items-center gap-2 mb-8">
      <div className="flex items-center gap-1.5 px-4 h-9 bg-[#f6f8fa] border border-[#eaecf1] rounded-md">
        <span className="text-[#a7adb9]">
          <IconCheck />
        </span>
        <span className="text-[#a7adb9] text-sm whitespace-nowrap">1. 계정 확인</span>
      </div>

      <div
        className="flex items-center px-4 h-[37px] bg-[#1166e0] border border-[#337ae6] rounded-[7px]"
        aria-current="step"
      >
        <span className="text-[#90b9ee] text-[13px] whitespace-nowrap">2. 기본 정보 입력</span>
      </div>

      <div className="flex items-center px-4 h-[37px] bg-[#f6f8f9] border-2 border-[#ebedf2] rounded-md">
        <span className="text-[#a7acb9] text-sm whitespace-nowrap">3. 가입 완료</span>
      </div>
    </nav>
  )
}
