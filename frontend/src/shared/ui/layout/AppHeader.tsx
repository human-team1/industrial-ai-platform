import { NavLink } from 'react-router-dom'

// ─── Icons ────────────────────────────────────────────────────────────────────

function IconNotification() {
  return (
    <svg className="w-[18px] h-[19px]" fill="none" viewBox="0 0 18 19" stroke="currentColor" strokeWidth={1.4}>
      <path strokeLinecap="round" d="M9 1.5a6 6 0 016 6v4.5l1.5 2H1.5L3 12V7.5a6 6 0 016-6z" />
      <path strokeLinecap="round" d="M7 16a2 2 0 004 0" />
    </svg>
  )
}

function IconGear() {
  return (
    <svg className="w-[30px] h-[30px]" fill="none" viewBox="0 0 30 30" stroke="currentColor" strokeWidth={1.3}>
      <circle cx="15" cy="15" r="3" />
      <path d="M15 5l1.5 2.6a8 8 0 011.8.75l3-.5 1.8 3.15-2.1 2.2v1.6l2.1 2.2-1.8 3.15-3-.5a8 8 0 01-1.8.75L15 25l-1.5-2.6a8 8 0 01-1.8-.75l-3 .5-1.8-3.15 2.1-2.2v-1.6l-2.1-2.2 1.8-3.15 3 .5a8 8 0 011.8-.75L15 5z" strokeLinejoin="round" />
    </svg>
  )
}

function IconUser() {
  return (
    <svg className="w-5 h-5 text-[#6a7089]" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
      <path strokeLinecap="round" d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2M12 11a4 4 0 100-8 4 4 0 000 8z" />
    </svg>
  )
}

function IconChatbot() {
  return (
    <svg className="w-[17px] h-4" fill="none" viewBox="0 0 17 16" stroke="currentColor" strokeWidth={1.3}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M14 2H3a1 1 0 00-1 1v7a1 1 0 001 1h2l2 3 2-3h5a1 1 0 001-1V3a1 1 0 00-1-1z" />
    </svg>
  )
}

// ─── Component ────────────────────────────────────────────────────────────────

export function AppHeader({
  userName,
  userRole,
  picture,
  onLogout,
}: {
  userName: string
  userRole?: string
  picture?: string
  onLogout?: () => void
}) {
  const roleLabel =
    userRole === 'ROLE_SITE_ADMIN' || userRole === 'ROLE_COMPANY_ADMIN' ? '관리자' : '사용자'

  return (
    <header
      aria-label="산업 이상 탐지 시스템 상단 헤더"
      className="h-[58px] bg-[#1e2333] flex items-center justify-between px-6 shrink-0"
    >
      {/* 로고 */}
      <div className="flex items-center gap-3 text-[#a5b0c4] text-lg font-bold leading-none">
        <img src="/icons/logo_icon.png" alt="" aria-hidden="true" className="w-[40px] h-[40px] shrink-0" />
        산업 이상 탐지 시스템
      </div>

      <div className="flex items-center gap-4">
        {/* 알림 */}
        <button
          type="button"
          aria-label="알림"
          className="relative text-[#6a7089] focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-400"
        >
          <IconNotification />
          <span className="absolute -top-1 -right-1 text-[10px] font-light text-[#f5afa3] leading-none">
            12
          </span>
        </button>

        <div className="h-[33px] w-px bg-[#2d3347]" />

        {/* 설정 */}
        <button type="button" aria-label="설정" className="text-[#6a7089]">
          <IconGear />
        </button>

        <div className="h-[33px] w-px bg-[#2d3347]" />

        {/* 사용자 정보 + 아바타 — 클릭 시 마이페이지 이동 */}
        <NavLink
          to="/mypage"
          className="flex items-center gap-3 hover:opacity-80 transition-opacity"
        >
          <div className="flex flex-col items-end">
            <span className="text-[#a5b0c4] text-xs leading-[1.3]">{userName}</span>
            <span className="text-[#6a7089] text-[10px] leading-[1.3]">{roleLabel}</span>
          </div>
          <div className="w-[31px] h-[35px] rounded bg-[#2d3347] flex items-center justify-center overflow-hidden">
            {picture ? (
              <img src={picture} alt="프로필" className="w-full h-full object-cover" />
            ) : (
              <IconUser />
            )}
          </div>
        </NavLink>

        <div className="h-[33px] w-px bg-[#2d3347]" />

        {/* 챗봇 버튼 */}
        <NavLink
          to="/chatbot"
          className="flex items-center gap-1.5 px-4 py-[7px] bg-[#109498] border border-[#07687e] rounded-[15.5px] text-[#8dccce] text-[11px] hover:bg-[#0d8285] transition-colors"
        >
          <IconChatbot />
          챗봇 상담
        </NavLink>

        {/* 로그아웃 */}
        {onLogout && (
          <>
            <div className="h-[33px] w-px bg-[#2d3347]" />
            <button
              type="button"
              onClick={onLogout}
              className="text-[#6a7089] text-xs hover:text-[#b0b5c1] transition-colors"
            >
              로그아웃
            </button>
          </>
        )}
      </div>
    </header>
  )
}