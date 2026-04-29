import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { GoogleLoginButton } from '../features/auth/ui/GoogleLoginButton'
import { NoticeBanner } from '../shared/ui/feedback/NoticeBanner'

const footerLinks = [
  { label: '이용약관', href: '#' },       // TODO: 이용약관 페이지 내용 추가
  { label: '개인정보처리방침', href: '#' }, // TODO: 개인정보처리방침 페이지 내용 추가
  { label: '도움말', href: '#' },          // TODO: 도움말 페이지 내용 추가
]

export function AuthPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const inactive = searchParams.get('inactive') === '1'
  const [showInactive, setShowInactive] = useState(false)

  useEffect(() => {
    if (inactive) {
      setShowInactive(true)
      const next = new URLSearchParams(searchParams)
      next.delete('inactive')
      setSearchParams(next, { replace: true })
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [inactive])

  return (
    <div className="min-h-screen flex flex-col">
      {showInactive && (
        <NoticeBanner
          message="비활성화된 계정입니다. 관리자에게 문의하세요."
          variant="warning"
          autoCloseMs={6000}
          onClose={() => setShowInactive(false)}
        />
      )}
      {/* 상단 헤더 */}
      <header
        aria-label="산업 이상 탐지 시스템 상단 헤더"
        className="h-[58px] bg-[#1e2333] flex items-center justify-between px-6 shrink-0"
      >
        <div className="flex items-center gap-3 text-[#a5b0c4] text-lg font-bold leading-none">
          <img src="/icons/logo_icon.png" alt="" aria-hidden="true" className="w-[60px] h-[60px] shrink-0" />
          산업 이상 탐지 시스템
        </div>
      </header>

      {/* 메인 — 배경 이미지 */}
      <main
        className="flex-1 flex flex-col items-center justify-center"
        style={{
          backgroundImage: 'url(/icons/login_background.png)',
          backgroundSize: 'cover',
          backgroundPosition: 'center',
          backgroundRepeat: 'no-repeat',
        }}
      >
        <div className="w-full max-w-[420px] flex flex-col px-4">
          {/* 로그인 카드 */}
          <div className="bg-white rounded-2xl shadow-[0_8px_40px_rgba(0,0,0,0.18)] overflow-hidden">
            {/* 카드 상단 타이틀 영역 */}
            <div className="px-10 pt-9 pb-7 flex flex-col items-center border-b border-[#f0f2f5]">
              <img src="/icons/logo_icon_black.png" alt="" aria-hidden="true" className="w-[150px] h-[150px] mb-4" />
              <p className="text-[#1f2937] text-[18px] font-bold mb-1">산업 이상 탐지 시스템</p>
              <p className="text-[#6c757d] text-[13px] text-center">
                설비데이터를 실시간으로 분석하고<br/>이상을 탐지하여 운영 안정성을 높입니다.
              </p>
            </div>

            {/* Google 로그인 버튼 */}
            <div className="px-10 py-8 flex flex-col items-center gap-6">
              <div className="w-full flex justify-center">
                <GoogleLoginButton />
              </div>

              {/* 최초 로그인 안내 */}
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

          {/* 푸터 링크 */}
          <div className="flex items-center justify-center gap-4 mt-6">
            {footerLinks.map((link, idx) => (
              <span key={link.label} className="flex items-center gap-4">
                <a
                  href={link.href}
                  className="text-[#1f2937] text-[12px] font-medium hover:text-[#1166e0] transition-colors"
                  style={{ textShadow: '0 0 8px rgba(255,255,255,0.8)' }}
                >
                  {link.label}
                </a>
                {idx < footerLinks.length - 1 && (
                  <span className="text-[#6c757d] text-[11px]">|</span>
                )}
              </span>
            ))}
          </div>

          <p className="text-center text-[#374151] text-[11px] mt-3 font-medium"
            style={{ textShadow: '0 0 8px rgba(255,255,255,0.8)' }}
          >
            © 2026 Industrial AI Platform. All rights reserved.
          </p>
        </div>
      </main>
    </div>
  )
}