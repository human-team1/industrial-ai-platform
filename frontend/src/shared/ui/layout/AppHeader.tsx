import { NavLink } from 'react-router-dom'
import { useUnreadNotificationCount } from '../../hooks/useUnreadNotificationCount'

function IconNotification() {
  return (
    <svg className="h-[19px] w-[18px]" fill="none" viewBox="0 0 18 19" stroke="currentColor" strokeWidth={1.4}>
      <path strokeLinecap="round" d="M9 1.5a6 6 0 0 1 6 6V12l1.5 2h-15L3 12V7.5a6 6 0 0 1 6-6z" />
      <path strokeLinecap="round" d="M7 16a2 2 0 0 0 4 0" />
    </svg>
  )
}

function IconGear() {
  return (
    <svg className="h-[30px] w-[30px]" fill="none" viewBox="0 0 30 30" stroke="currentColor" strokeWidth={1.3}>
      <circle cx="15" cy="15" r="3" />
      <path strokeLinejoin="round" d="m15 5 1.5 2.6a8 8 0 0 1 1.8.75l3-.5 1.8 3.15-2.1 2.2v1.6l2.1 2.2-1.8 3.15-3-.5a8 8 0 0 1-1.8.75L15 25l-1.5-2.6a8 8 0 0 1-1.8-.75l-3 .5-1.8-3.15 2.1-2.2v-1.6l-2.1-2.2 1.8-3.15 3 .5a8 8 0 0 1 1.8-.75z" />
    </svg>
  )
}

function IconUser() {
  return (
    <svg className="h-5 w-5 text-[#6a7089]" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
      <path strokeLinecap="round" d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2M12 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z" />
    </svg>
  )
}

function IconChatbot() {
  return (
    <svg className="h-4 w-[17px]" fill="none" viewBox="0 0 17 16" stroke="currentColor" strokeWidth={1.3}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M14 2H3a1 1 0 0 0-1 1v7a1 1 0 0 0 1 1h2l2 3 2-3h5a1 1 0 0 0 1-1V3a1 1 0 0 0-1-1z" />
    </svg>
  )
}

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

  const { unreadCount } = useUnreadNotificationCount()
  const badgeText = unreadCount > 99 ? '99+' : unreadCount > 0 ? String(unreadCount) : null

  return (
    <header
      aria-label="산업 이상 탐지 시스템 상단 헤더"
      className="flex h-[58px] shrink-0 items-center justify-between bg-[#1e2333] px-6"
    >
      <div className="flex items-center gap-3 text-lg font-bold leading-none text-[#a5b0c4]">
        <img src="/icons/logo_icon.png" alt="" aria-hidden="true" className="h-10 w-10 shrink-0" />
        산업 이상 탐지 시스템
      </div>

      <div className="flex items-center gap-4">
        <NavLink
          to="/notifications"
          aria-label={badgeText ? `알림 ${badgeText}건` : '알림'}
          className="relative text-[#6a7089] focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-400"
        >
          <IconNotification />
          {badgeText ? (
            <span className="absolute -right-1 -top-1 text-[10px] font-light leading-none text-[#f5afa3]">
              {badgeText}
            </span>
          ) : null}
        </NavLink>

        <div className="h-[33px] w-px bg-[#2d3347]" />

        <NavLink
          to="/settings"
          aria-label="설정"
          className="text-[#6a7089] focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-400"
        >
          <IconGear />
        </NavLink>

        <div className="h-[33px] w-px bg-[#2d3347]" />

        <NavLink to="/mypage" className="flex items-center gap-3 transition-opacity hover:opacity-80">
          <div className="flex flex-col items-end">
            <span className="text-xs leading-[1.3] text-[#a5b0c4]">{userName}</span>
            <span className="text-[10px] leading-[1.3] text-[#6a7089]">{roleLabel}</span>
          </div>
          <div className="flex h-[35px] w-[31px] items-center justify-center overflow-hidden rounded bg-[#2d3347]">
            {picture ? (
              <img src={picture} alt="프로필" className="h-full w-full object-cover" />
            ) : (
              <IconUser />
            )}
          </div>
        </NavLink>

        <div className="h-[33px] w-px bg-[#2d3347]" />

        <NavLink
          to="/chatbot"
          className="flex items-center gap-1.5 rounded-[15.5px] border border-[#07687e] bg-[#109498] px-4 py-[7px] text-[11px] text-[#dff9fa] transition-colors hover:bg-[#0d8285]"
        >
          <IconChatbot />
          챗봇 상담
        </NavLink>

        {onLogout ? (
          <>
            <div className="h-[33px] w-px bg-[#2d3347]" />
            <button
              type="button"
              onClick={onLogout}
              className="text-xs text-[#6a7089] transition-colors hover:text-[#b0b5c1]"
            >
              로그아웃
            </button>
          </>
        ) : null}
      </div>
    </header>
  )
}
