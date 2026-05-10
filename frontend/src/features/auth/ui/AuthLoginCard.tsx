import { GoogleLoginButton } from './GoogleLoginButton'

export function AuthLoginCard() {
  return (
    <div className="bg-white rounded-2xl shadow-[0_8px_40px_rgba(0,0,0,0.18)] overflow-hidden">
      <div className="px-10 pt-9 pb-7 flex flex-col items-center border-b border-[#f0f2f5]">
        <img src="/icons/logo_icon_black.png" alt="" aria-hidden="true" className="w-[150px] h-[150px] mb-4" />
        <p className="text-[#1f2937] text-[18px] font-bold mb-1">산업 이상 탐지 시스템</p>
        <p className="text-[#6c757d] text-[13px] text-center">
          설비데이터를 실시간으로 분석하고<br/>이상을 탐지하여 운영 안정성을 높입니다.
        </p>
      </div>

      <div className="px-10 py-8 flex flex-col items-center gap-6">
        <div className="w-full flex justify-center">
          <GoogleLoginButton />
        </div>

        <div className="w-full bg-[#f8f9ff] border border-[#dde5ff] rounded-lg px-4 py-3 flex items-start gap-2.5">
          <svg className="w-4 h-4 text-[#1166e0] mt-0.5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M11.25 11.25l.041-.02a.75.75 0 011.063.852l-.708 2.836a.75.75 0 001.063.853l.041-.021M21 12a9 9 0 11-18 0 9 9 0 0118 0zm-9-3.75h.008v.008H12V8.25z" />
          </svg>
          <p className="text-[#374151] text-[12px] leading-relaxed">
            최초 로그인 시 추가 정보 입력 후 가입이 완료됩니다.<br />
            가입 승인은 관리자 검토 후 처리됩니다.
          </p>
        </div>
      </div>
    </div>
  )
}
