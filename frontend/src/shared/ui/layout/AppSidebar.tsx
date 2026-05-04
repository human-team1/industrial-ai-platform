import type { ReactNode } from 'react'
import { NavLink } from 'react-router-dom'
import { useAuth } from '../../../features/auth/model'
import type { AuthRole } from '../../../features/auth/types'

function IconDashboard() {
  return (
    <svg className="h-[18px] w-[18px] shrink-0" fill="none" viewBox="0 0 18 18" stroke="currentColor" strokeWidth={1.4}>
      <rect x="2" y="2" width="6" height="6" rx="1" />
      <rect x="10" y="2" width="6" height="6" rx="1" />
      <rect x="2" y="10" width="6" height="6" rx="1" />
      <rect x="10" y="10" width="6" height="6" rx="1" />
    </svg>
  )
}

function IconRadar() {
  return (
    <svg className="h-[18px] w-[18px] shrink-0" fill="none" viewBox="0 0 18 18" stroke="currentColor" strokeWidth={1.4}>
      <circle cx="9" cy="9" r="7" />
      <circle cx="9" cy="9" r="3.5" />
      <line x1="9" y1="9" x2="14.5" y2="4.5" />
    </svg>
  )
}

function IconUpload() {
  return (
    <svg className="h-4 w-[18px] shrink-0" fill="none" viewBox="0 0 18 16" stroke="currentColor" strokeWidth={1.4}>
      <path strokeLinecap="round" d="M9 11V2M5.5 5.5L9 2l3.5 3.5" />
      <path strokeLinecap="round" d="M3 10v3a1 1 0 0 0 1 1h10a1 1 0 0 0 1-1v-3" />
    </svg>
  )
}

function IconHistory() {
  return (
    <svg className="h-[18px] w-[18px] shrink-0" fill="none" viewBox="0 0 18 18" stroke="currentColor" strokeWidth={1.4}>
      <circle cx="9" cy="9" r="7" />
      <path strokeLinecap="round" d="M9 5v4l3 2" />
    </svg>
  )
}

function IconReport() {
  return (
    <svg className="h-[18px] w-[18px] shrink-0" fill="none" viewBox="0 0 16 18" stroke="currentColor" strokeWidth={1.4}>
      <rect x="2" y="1" width="12" height="16" rx="1" />
      <line x1="5" y1="6" x2="11" y2="6" />
      <line x1="5" y1="9" x2="11" y2="9" />
      <line x1="5" y1="12" x2="9" y2="12" />
    </svg>
  )
}

function IconChat() {
  return (
    <svg className="h-[18px] w-[18px] shrink-0" fill="none" viewBox="0 0 18 18" stroke="currentColor" strokeWidth={1.4}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M3 4h12v8H9l-3 3v-3H3z" />
    </svg>
  )
}

function IconSettings() {
  return (
    <svg className="h-[18px] w-[18px] shrink-0" fill="none" viewBox="0 0 17 18" stroke="currentColor" strokeWidth={1.4}>
      <circle cx="8.5" cy="9" r="2.5" />
      <path strokeLinejoin="round" d="M8.5 1.5l1.2 2.1a5.5 5.5 0 0 1 1.4.6l2.4-.4 1.5 2.6-1.7 1.8v1.2l1.7 1.8-1.5 2.6-2.4-.4a5.5 5.5 0 0 1-1.4.6l-1.2 2.1-1.2-2.1a5.5 5.5 0 0 1-1.4-.6l-2.4.4L2 11.6l1.7-1.8V8.6L2 6.8l1.5-2.6 2.4.4a5.5 5.5 0 0 1 1.4-.6z" />
    </svg>
  )
}

function SidebarSystemStatus() {
  const rows: { label: string; value: string }[] = [
    { label: '전체', value: 'OK' },
    { label: '모델 서버', value: 'OK' },
    { label: '스트리밍 서버', value: 'OK' },
    { label: '스토리지', value: 'OK' },
  ]

  return (
    <div className="mb-5 rounded border border-[#2d3347] bg-[#171c2a] p-4 text-xs text-[#a5b0c4]">
      <p className="mb-3 font-semibold text-[#d8dbe2]">시스템 상태</p>
      <div className="space-y-2">
        {rows.map((row) => (
          <div key={row.label} className="flex items-center justify-between">
            <span className="text-[11px] text-[#8d95aa]">{row.label}</span>
            <span
              className={`rounded px-2 py-0.5 text-[10px] font-semibold ${statusBadgeTone(row.value)}`}
            >
              {statusBadgeLabel(row.value)}
            </span>
          </div>
        ))}
      </div>
      <p className="mt-3 text-[10px] text-[#6a7089]">최근 업데이트 -</p>
    </div>
  )
}

function statusBadgeTone(value: string) {
  if (value === 'ERROR') return 'bg-rose-500/15 text-rose-300'
  if (value === 'WARNING') return 'bg-amber-500/15 text-amber-300'
  return 'bg-emerald-500/15 text-emerald-300'
}

function statusBadgeLabel(value: string) {
  if (value === 'ERROR') return '오류'
  if (value === 'WARNING') return '주의'
  return '정상'
}

function IconCollapseMenu() {
  return (
    <svg className="h-[11px] w-2.5 shrink-0" fill="none" viewBox="0 0 10 11" stroke="currentColor" strokeWidth={1.5}>
      <path strokeLinecap="round" d="M8 2 3 5.5 8 9" />
    </svg>
  )
}

type NavItem = {
  label: string
  icon: ReactNode
  to: string | null
  roles?: AuthRole[]
}

const navItems: NavItem[] = [
  { label: '대시보드', icon: <IconDashboard />, to: '/dashboard' },
  { label: '실시간 탐지', icon: <IconRadar />, to: '/inspections' },
  { label: '탐지업로드', icon: <IconUpload />, to: '/inspections/upload' },
  { label: '탐지이력', icon: <IconHistory />, to: '/results' },
  { label: '문서 목록', icon: <IconReport />, to: '/documents' },
  { label: '문서 등록 및 수정', icon: <IconReport />, to: '/documents/new' },
  { label: '챗봇 히스토리', icon: <IconChat />, to: '/chatbot/history' },
  { label: '설정', icon: <IconSettings />, to: '/settings' },
  { label: '가입 신청 관리', icon: <IconSettings />, to: '/admin/signup-requests', roles: ['ROLE_SITE_ADMIN'] },
  { label: '운영 모니터링', icon: <IconRadar />, to: '/admin/operation-monitoring', roles: ['ROLE_SITE_ADMIN'] },
  { label: '사이트 관리자 설정', icon: <IconSettings />, to: '/admin/site-settings', roles: ['ROLE_SITE_ADMIN'] },
]

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
          className="flex h-9 w-9 items-center justify-center rounded border border-[#2d3347] bg-[#1e2333] text-[#b0b5c1] transition-colors hover:bg-[#2d3347]"
        >
          <svg className="h-3.5 w-3.5" fill="none" viewBox="0 0 14 14" stroke="currentColor" strokeWidth={1.8}>
            <path strokeLinecap="round" strokeLinejoin="round" d="m5 2 5 5-5 5" />
          </svg>
        </button>
      </div>
    )
  }

  return (
    <aside aria-label="사이드바 내비게이션" className="flex min-h-screen w-[219px] shrink-0 flex-col bg-[#1e2333]">
      <div className="px-5 pb-5 pt-[22px]">
        <span className="text-[11px] font-medium uppercase tracking-widest text-[#5a6175]">
          Factory Guard
        </span>
      </div>

      <nav aria-label="주요 메뉴" className="flex-1">
        <ul className="m-0 list-none p-0">
          {visibleNavItems.map((item) => (
            <li key={item.label}>
              {item.to ? (
                <NavLink
                  to={item.to}
                  end={item.to === '/inspections'}
                  className={({ isActive }) =>
                    `flex w-full items-center gap-3 px-5 py-[13px] text-left text-[13px] transition-colors ${
                      isActive
                        ? 'bg-blue-50 text-blue-700'
                        : 'text-[#d8dbe2] hover:bg-white/5'
                    }`
                  }
                >
                  <span className="text-current">{item.icon}</span>
                  {item.label}
                </NavLink>
              ) : (
                <button
                  type="button"
                  disabled
                  className="flex w-full cursor-not-allowed items-center gap-3 px-5 py-[13px] text-left text-[13px] text-[#d8dbe2] opacity-40"
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
        <SidebarSystemStatus />

        <button
          type="button"
          onClick={onCollapse}
          className="flex items-center gap-3 text-[13px] text-[#b0b5c1] transition-colors hover:text-white"
        >
          <span className="text-[#6a7089]">
            <IconCollapseMenu />
          </span>
          메뉴 접기
        </button>
      </div>
    </aside>
  )
}
