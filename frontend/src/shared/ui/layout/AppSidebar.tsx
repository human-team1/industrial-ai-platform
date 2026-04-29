import { NavLink } from 'react-router-dom'
import { useAuth } from '../../../features/auth/model'
import type { AuthRole } from '../../../features/auth/types'

// ─── Icons ────────────────────────────────────────────────────────────────────

function IconDashboard() {
  return (
    <svg className="w-[18px] h-[18px] shrink-0" fill="none" viewBox="0 0 18 18" stroke="currentColor" strokeWidth={1.4}>
      <rect x="2" y="2" width="6" height="6" rx="1" />
      <rect x="10" y="2" width="6" height="6" rx="1" />
      <rect x="2" y="10" width="6" height="6" rx="1" />
      <rect x="10" y="10" width="6" height="6" rx="1" />
    </svg>
  )
}

function IconRadar() {
  return (
    <svg className="w-[18px] h-[19px] shrink-0" fill="none" viewBox="0 0 18 18" stroke="currentColor" strokeWidth={1.4}>
      <circle cx="9" cy="9" r="7" />
      <circle cx="9" cy="9" r="3.5" />
      <line x1="9" y1="9" x2="14.5" y2="4.5" />
    </svg>
  )
}

function IconUpload() {
  return (
    <svg className="w-[18px] h-[16px] shrink-0" fill="none" viewBox="0 0 18 16" stroke="currentColor" strokeWidth={1.4}>
      <path strokeLinecap="round" d="M9 11V2M5.5 5.5L9 2l3.5 3.5" />
      <path strokeLinecap="round" d="M3 10v3a1 1 0 001 1h10a1 1 0 001-1v-3" />
    </svg>
  )
}

function IconHistory() {
  return (
    <svg className="w-[18px] h-[18px] shrink-0" fill="none" viewBox="0 0 18 18" stroke="currentColor" strokeWidth={1.4}>
      <circle cx="9" cy="9" r="7" />
      <path strokeLinecap="round" d="M9 5v4l3 2" />
    </svg>
  )
}

function IconEquipment() {
  return (
    <svg className="w-[18px] h-[18px] shrink-0" fill="none" viewBox="0 0 18 18" stroke="currentColor" strokeWidth={1.4}>
      <rect x="2" y="5" width="14" height="9" rx="1" />
      <path strokeLinecap="round" d="M6 5V3h6v2" />
      <circle cx="9" cy="9.5" r="2" />
    </svg>
  )
}

function IconModel() {
  return (
    <svg className="w-[18px] h-[18px] shrink-0" fill="none" viewBox="0 0 18 18" stroke="currentColor" strokeWidth={1.4}>
      <path d="M9 2l7 4v6l-7 4L2 12V6z" strokeLinejoin="round" />
      <path d="M9 2v14M2 6l7 4 7-4" strokeLinejoin="round" />
    </svg>
  )
}

function IconBell() {
  return (
    <svg className="w-[16px] h-[19px] shrink-0" fill="none" viewBox="0 0 16 18" stroke="currentColor" strokeWidth={1.4}>
      <path strokeLinecap="round" d="M8 1a5 5 0 015 5v4l1.5 2.5H1.5L3 10V6a5 5 0 015-5z" />
      <path strokeLinecap="round" d="M6 15a2 2 0 004 0" />
    </svg>
  )
}

function IconReport() {
  return (
    <svg className="w-[16px] h-[19px] shrink-0" fill="none" viewBox="0 0 16 18" stroke="currentColor" strokeWidth={1.4}>
      <rect x="2" y="1" width="12" height="16" rx="1" />
      <line x1="5" y1="6" x2="11" y2="6" />
      <line x1="5" y1="9" x2="11" y2="9" />
      <line x1="5" y1="12" x2="9" y2="12" />
    </svg>
  )
}

function IconSettings() {
  return (
    <svg className="w-[17px] h-[18px] shrink-0" fill="none" viewBox="0 0 17 18" stroke="currentColor" strokeWidth={1.4}>
      <circle cx="8.5" cy="9" r="2.5" />
      <path strokeLinejoin="round" d="M8.5 1.5l1.2 2.1a5.5 5.5 0 011.4.6l2.4-.4 1.5 2.6-1.7 1.8v1.2l1.7 1.8-1.5 2.6-2.4-.4a5.5 5.5 0 01-1.4.6L8.5 16.5 7.3 14.4a5.5 5.5 0 01-1.4-.6l-2.4.4L2 11.6l1.7-1.8V8.6L2 6.8 3.5 4.2l2.4.4a5.5 5.5 0 011.4-.6L8.5 1.5z" />
    </svg>
  )
}

function IconCollapseMenu() {
  return (
    <svg className="w-2.5 h-[11px] shrink-0" fill="none" viewBox="0 0 10 11" stroke="currentColor" strokeWidth={1.5}>
      <path strokeLinecap="round" d="M8 2L3 5.5 8 9" />
    </svg>
  )
}

// ─── Nav items ────────────────────────────────────────────────────────────────

type NavItem = {
  label: string
  icon: React.ReactNode
  to: string | null
  roles?: AuthRole[]
}

const navItems: NavItem[] = [
  { label: '대시보드', icon: <IconDashboard />, to: '/dashboard' },
  { label: '실시간 탐지', icon: <IconRadar />, to: '/inspections' },
  { label: '탐지 업로드', icon: <IconUpload />, to: null },
  { label: '탐지 이력', icon: <IconHistory />, to: '/results' },
  { label: '설비 관리', icon: <IconEquipment />, to: null },
  { label: '모델 관리', icon: <IconModel />, to: null },
  { label: '알림 관리', icon: <IconBell />, to: null },
  { label: '보고서', icon: <IconReport />, to: null },
  { label: '설정', icon: <IconSettings />, to: '/settings' },
  { label: '가입 신청 관리', icon: <IconSettings />, to: '/admin/signup-requests', roles: ['ROLE_SITE_ADMIN'] },
  { label: '시스템 관리', icon: <IconSettings />, to: null, roles: ['ROLE_SITE_ADMIN'] },
  // TODO: 시스템 상태 추가 해야함
]

// ─── Component ────────────────────────────────────────────────────────────────

export function AppSidebar({
  isCollapsed,
  onCollapse,
  onExpand,
}: {
  isCollapsed: boolean
  onCollapse: () => void
  onExpand: () => void
}) {
  const { user } = useAuth()
  const visibleNavItems = navItems.filter(
    (item) => !item.roles || (user && item.roles.includes(user.role)),
  )

  if (isCollapsed) {
    return (
      <div className="fixed bottom-6 left-6 z-50">
        <button
          type="button"
          onClick={onExpand}
          aria-label="사이드바 펼치기"
          className="w-9 h-9 bg-[#1e2333] border border-[#2d3347] rounded flex items-center justify-center text-[#b0b5c1] hover:bg-[#2d3347] transition-colors"
        >
          <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 14 14" stroke="currentColor" strokeWidth={1.8}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M5 2l5 5-5 5" />
          </svg>
        </button>
      </div>
    )
  }

  return (
    <aside
      aria-label="사이드바 내비게이션"
      className="w-[219px] min-h-screen bg-[#1e2333] flex flex-col shrink-0"
    >
      <div className="px-5 pt-[22px] pb-5">
        <span className="text-[#5a6175] text-[11px] font-medium uppercase tracking-widest">
          Factory Guard
        </span>
      </div>

      <nav aria-label="주요 메뉴" className="flex-1">
        <ul className="m-0 p-0 list-none">
          {visibleNavItems.map((item) => (
            <li key={item.label}>
              {item.to ? (
                <NavLink
                  to={item.to}
                  className={({ isActive }) =>
                    `w-full flex items-center gap-3 px-5 py-[13px] text-[13px] transition-colors text-left ${
                      isActive
                        ? 'bg-white/10 text-white'
                        : 'text-[#d8dbe2] hover:bg-white/5'
                    }`
                  }
                >
                  <span className="text-[#6a7089]">{item.icon}</span>
                  {item.label}
                </NavLink>
              ) : (
                <button
                  type="button"
                  disabled
                  className="w-full flex items-center gap-3 px-5 py-[13px] text-[#d8dbe2] text-[13px] transition-colors text-left opacity-40 cursor-not-allowed"
                >
                  <span className="text-[#6a7089]">{item.icon}</span>
                  {item.label}
                </button>
              )}
            </li>
          ))}
        </ul>
      </nav>

      <div className="px-5 pb-[55px]">
        <button
          type="button"
          onClick={onCollapse}
          className="flex items-center gap-3 text-[#b0b5c1] text-[13px] hover:text-white transition-colors"
        >
          <span className="text-[#6a7089]">
            <IconCollapseMenu />
          </span>
          메뉴접기
        </button>
      </div>
    </aside>
  )
}