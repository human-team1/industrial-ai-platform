import { useId, useState } from 'react'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import type { NewUserInfo } from '../features/auth/types'
import { postSignupRequest } from '../features/auth/api'

// ─── Types ────────────────────────────────────────────────────────────────────

type FormData = {
  name: string
  countryCode: string
  phone: string
  organization: string
  department: string
  title: string
  role: string
  consent: boolean
}

// ─── Static data ──────────────────────────────────────────────────────────────

const organizationOptions = [
  { value: 'smart-factory', label: '(주) 스마트팩토리' },
  { value: 'production', label: '생산본부' },
  { value: 'research', label: '연구개발본부' },
]

const departmentOptions = [
  { value: 'production-tech', label: '생산기술팀' },
  { value: 'quality', label: '품질관리팀' },
  { value: 'automation', label: '자동화개발팀' },
]

const roleOptions = [
  { value: 'member', label: '일반 사용자' },
  { value: 'manager', label: '매니저' },
  { value: 'admin', label: '관리자' },
]

const countryCodeOptions = [
  { value: '+82', label: '+82' },
  { value: '+81', label: '+81' },
  { value: '+1', label: '+1' },
]

const navItems = [
  { label: '대시보드', icon: <IconDashboard /> },
  { label: '실시간 탐지', icon: <IconRadar /> },
  { label: '탐지 업로드', icon: <IconUpload /> },
  { label: '탐지 이력', icon: <IconHistory /> },
  { label: '설비 관리', icon: <IconEquipment /> },
  { label: '모델 관리', icon: <IconModel /> },
  { label: '알림 관리', icon: <IconBell /> },
  { label: '보고서', icon: <IconReport /> },
  { label: '시스템 관리', icon: <IconSettings /> },
]

const infoCards = [
  {
    icon: '/icons/auth_icon.png',
    title: '권한 및 접근 제어',
    text: '소속 조직과 역할을 기준으로 서비스 접근 권한이 설정됩니다.',
  },
  {
    icon: '/icons/alarm_icon.png',
    title: '맞춤 알림 제공',
    text: '이상 탐지, 시스템 점검 등 중요한 알림을 정확하게 받아보실 수 있습니다.',
  },
  {
    icon: '/icons/secure_icon.png',
    title: '안전한 정보 관리',
    text: '입력하신 정보는 안전하게 보호되며, 관련 법령에 따라 관리됩니다.',
  },
]

// ─── SVG Icons ────────────────────────────────────────────────────────────────

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

function IconGoogle() {
  return (
    <svg className="w-[19px] h-[19px] shrink-0" viewBox="0 0 24 24">
      <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" />
      <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" />
      <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l3.66-2.84z" />
      <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" />
    </svg>
  )
}

function IconChevronDown() {
  return (
    <svg className="w-2.5 h-1.5 pointer-events-none shrink-0" fill="none" viewBox="0 0 10 6" stroke="currentColor" strokeWidth={1.5}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M1 1l4 4 4-4" />
    </svg>
  )
}

function IconCheck() {
  return (
    <svg className="w-4 h-4" fill="none" viewBox="0 0 16 16" stroke="currentColor" strokeWidth={1.5}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M3 8l4 4 6-6" />
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

function IconNotification() {
  return (
    <svg className="w-[18px] h-[19px]" fill="none" viewBox="0 0 18 19" stroke="currentColor" strokeWidth={1.4}>
      <path strokeLinecap="round" d="M9 1.5a6 6 0 016 6v4.5l1.5 2H1.5L3 12V7.5a6 6 0 016-6z" />
      <path strokeLinecap="round" d="M7 16a2 2 0 004 0" />
    </svg>
  )
}

// ─── Sub-components ───────────────────────────────────────────────────────────

function SidebarNavigationSection({
  isCollapsed,
  onCollapse,
  onExpand,
}: {
  isCollapsed: boolean
  onCollapse: () => void
  onExpand: () => void
}) {
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
          {navItems.map((item) => (
            <li key={item.label}>
              <button
                type="button"
                className="w-full flex items-center gap-3 px-5 py-[13px] text-[#d8dbe2] text-[13px] hover:bg-white/5 transition-colors text-left"
              >
                <span className="text-[#6a7089]">{item.icon}</span>
                {item.label}
              </button>
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

function RegistrationHeaderSection({ userName }: { userName: string }) {
  return (
    <header
      aria-label="산업 이상 탐지 시스템 상단 헤더"
      className="h-[58px] bg-[#1e2333] flex items-center justify-between px-6 shrink-0"
    >
      <div className="flex items-center gap-3 text-[#a5b0c4] text-lg font-bold leading-none">
        <img src="/icons/logo_icon.png" alt="" aria-hidden="true" className="w-[40px] h-[40px] shrink-0" />
        산업 이상 탐지 시스템
      </div>

      <div className="flex items-center gap-4">
        {/* 알림 */}
        <button
          type="button"
          aria-label="알림 12개"
          className="relative text-[#6a7089] focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-400"
        >
          <IconNotification />
          <span className="absolute -top-1 -right-1 text-[10px] font-light text-[#f5afa3] leading-none">
            12
          </span>
        </button>

        {/* 구분선 */}
        <div className="h-[33px] w-px bg-[#2d3347]" />

        {/* 설정 아이콘 */}
        <button type="button" aria-label="설정" className="text-[#6a7089]">
          <svg className="w-[30px] h-[30px]" fill="none" viewBox="0 0 30 30" stroke="currentColor" strokeWidth={1.3}>
            <circle cx="15" cy="15" r="3" />
            <path d="M15 5l1.5 2.6a8 8 0 011.8.75l3-.5 1.8 3.15-2.1 2.2v1.6l2.1 2.2-1.8 3.15-3-.5a8 8 0 01-1.8.75L15 25l-1.5-2.6a8 8 0 01-1.8-.75l-3 .5-1.8-3.15 2.1-2.2v-1.6l-2.1-2.2 1.8-3.15 3 .5a8 8 0 011.8-.75L15 5z" strokeLinejoin="round" />
          </svg>
        </button>

        {/* 구분선 */}
        <div className="h-[33px] w-px bg-[#2d3347]" />

        {/* 사용자 정보 */}
        <div className="flex flex-col items-end">
          <span className="text-[#a5b0c4] text-xs leading-[1.3]">{userName}</span>
          <span className="text-[#6a7089] text-[10px] leading-[1.3]">관리자</span>
        </div>

        {/* 프로필 아바타 */}
        <div className="w-[31px] h-[35px] rounded bg-[#2d3347] flex items-center justify-center overflow-hidden">
          <svg className="w-5 h-5 text-[#6a7089]" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
            <path strokeLinecap="round" d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2M12 11a4 4 0 100-8 4 4 0 000 8z" />
          </svg>
        </div>

        {/* 구분선 */}
        <div className="h-[33px] w-px bg-[#2d3347]" />

        {/* 챗봇 버튼 */}
        <button
          type="button"
          aria-label="챗봇 상담"
          className="flex items-center gap-1.5 px-4 py-[7px] bg-[#109498] border border-[#07687e] rounded-[15.5px] text-[#8dccce] text-[11px] hover:bg-[#0d8285] transition-colors"
        >
          <svg className="w-[17px] h-4" fill="none" viewBox="0 0 17 16" stroke="currentColor" strokeWidth={1.3}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M14 2H3a1 1 0 00-1 1v7a1 1 0 001 1h2l2 3 2-3h5a1 1 0 001-1V3a1 1 0 00-1-1z" />
          </svg>
          챗봇 상담
        </button>
      </div>
    </header>
  )
}

// ─── Select wrapper with chevron ──────────────────────────────────────────────

function SelectField({
  id,
  value,
  onChange,
  options,
  className = '',
  'aria-label': ariaLabel,
}: {
  id?: string
  value: string
  onChange: (v: string) => void
  options: { value: string; label: string }[]
  className?: string
  'aria-label'?: string
}) {
  return (
    <div className="relative">
      <select
        id={id}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        aria-label={ariaLabel}
        className={`w-full appearance-none pr-7 ${className}`}
      >
        {options.map((o) => (
          <option key={o.value} value={o.value}>
            {o.label}
          </option>
        ))}
      </select>
      <span className="absolute right-2.5 top-1/2 -translate-y-1/2 pointer-events-none text-[#c3c7d2]">
        <IconChevronDown />
      </span>
    </div>
  )
}

// ─── Main form section ────────────────────────────────────────────────────────

function MemberInfoFormSection({
  userInfo,
  loading,
  error,
  formData,
  setFormData,
  onBack,
  onSubmit,
}: {
  userInfo: NewUserInfo
  loading: boolean
  error: string | null
  formData: FormData
  setFormData: React.Dispatch<React.SetStateAction<FormData>>
  onBack: () => void
  onSubmit: () => void
}) {
  const nameId = useId()
  const phoneId = useId()
  const countryCodeId = useId()
  const organizationId = useId()
  const departmentId = useId()
  const titleId = useId()
  const roleId = useId()
  const consentId = useId()

  function handleChange(field: keyof Omit<FormData, 'consent'>, value: string) {
    setFormData((prev) => ({ ...prev, [field]: value }))
  }

  const fieldBase =
    'w-full px-3 py-2 bg-[#fdfdfd] border border-[#edeef2] rounded text-[#1f2937] text-[13px] outline-none focus:border-[#4a90e2] transition-colors'

  return (
    <div className="flex flex-1 gap-0 min-h-0">
      {/* ── 메인 폼 영역 ── */}
      <main className="flex-1 flex flex-col px-8 pt-7 pb-0 min-w-0">
        {/* 제목 */}
        <h1
          id="member-info-form-title"
          className="text-[#494d57] text-[23px] font-medium leading-none mb-1.5"
        >
          회원가입/초기정보 입력
        </h1>
        <p className="text-[#6b7280] text-sm mb-7">
          서비스 이용을 위해 추가 정보를 입력해주세요.
        </p>

        {/* 단계 표시기 */}
        <nav aria-label="가입 단계" className="flex items-center gap-2 mb-8">
          {/* Step 1 – 완료 */}
          <div className="flex items-center gap-1.5 px-4 h-9 bg-[#f6f8fa] border border-[#eaecf1] rounded-md">
            <span className="text-[#a7adb9]">
              <IconCheck />
            </span>
            <span className="text-[#a7adb9] text-sm whitespace-nowrap">1. 계정 확인</span>
          </div>

          {/* Step 2 – 현재 */}
          <div
            className="flex items-center px-4 h-[37px] bg-[#1166e0] border border-[#337ae6] rounded-[7px]"
            aria-current="step"
          >
            <span className="text-[#90b9ee] text-[13px] whitespace-nowrap">2. 기본 정보 입력</span>
          </div>

          {/* Step 3 – 미완료 */}
          <div className="flex items-center px-4 h-[37px] bg-[#f6f8f9] border-2 border-[#ebedf2] rounded-md">
            <span className="text-[#a7acb9] text-sm whitespace-nowrap">3. 가입 완료</span>
          </div>
        </nav>

        {/* Google 계정 카드 */}
        <div className="flex items-center gap-4 px-4 py-3 bg-[#fdfdfd] border border-[#374151] rounded-md mb-6">
          {userInfo.picture ? (
            <img
              src={userInfo.picture}
              alt="프로필"
              className="w-[52px] h-[52px] rounded-full object-cover shrink-0"
            />
          ) : (
            <div className="w-[52px] h-[52px] rounded-full bg-[#e8ecf3] flex items-center justify-center shrink-0">
              <svg className="w-6 h-6 text-[#9aa3b2]" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                <path strokeLinecap="round" d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2M12 11a4 4 0 100-8 4 4 0 000 8z" />
              </svg>
            </div>
          )}
          <div className="min-w-0">
            <div className="text-[#707684] text-[17px] font-medium truncate">{userInfo.name}</div>
            <div className="text-[#c3c7d2] text-[13px] truncate">{userInfo.email}</div>
          </div>
          <div className="ml-auto flex items-center gap-3 shrink-0">
            <div className="h-[34px] w-px bg-[#eef0f4]" />
            <IconGoogle />
            <span className="text-[#828690] text-[13px] whitespace-nowrap">Google 계정 연동 완료</span>
          </div>
        </div>

        {/* 폼 */}
        <form
          aria-labelledby="member-info-form-title"
          onSubmit={(e) => e.preventDefault()}
          className="flex flex-col gap-4 flex-1"
        >
          {/* 이름 */}
          <div>
            <label htmlFor={nameId} className="block text-[#374151] text-sm mb-1.5">
              이름<span className="text-red-400 ml-0.5">*</span>
            </label>
            <input
              id={nameId}
              name="name"
              type="text"
              value={userInfo.name}
              readOnly
              className={`${fieldBase} bg-[#f6f8fa] cursor-default`}
            />
          </div>

          {/* 휴대전화 */}
          <div>
            <label htmlFor={phoneId} className="block text-[#374151] text-sm mb-1.5">
              휴대전화<span className="text-red-400 ml-0.5">*</span>
            </label>
            <div className="flex gap-2">
              <div className="relative">
                <select
                  id={countryCodeId}
                  value={formData.countryCode}
                  onChange={(e) => handleChange('countryCode', e.target.value)}
                  aria-label="국가번호"
                  className="appearance-none pl-3 pr-7 py-2 bg-[#fefefe] border border-[#d1d7dd] rounded-[5.75px] text-[#1f2937] text-[13px] outline-none focus:border-[#4a90e2] transition-colors"
                >
                  {countryCodeOptions.map((o) => (
                    <option key={o.value} value={o.value}>
                      {o.label}
                    </option>
                  ))}
                </select>
                <span className="absolute right-2 top-1/2 -translate-y-1/2 pointer-events-none text-[#c3c7d2]">
                  <IconChevronDown />
                </span>
              </div>
              <input
                id={phoneId}
                name="phone"
                type="tel"
                inputMode="tel"
                value={formData.phone}
                onChange={(e) => handleChange('phone', e.target.value)}
                placeholder="010-0000-0000"
                required
                className="flex-1 px-3 py-2 bg-[#fdfdfd] border border-[#eef0f4] rounded text-[#1f2937] text-[13px] outline-none focus:border-[#4a90e2] transition-colors"
              />
            </div>
          </div>

          {/* 소속조직 */}
          <div>
            <label htmlFor={organizationId} className="block text-[#374151] text-sm mb-1.5">
              소속조직<span className="text-red-400 ml-0.5">*</span>
            </label>
            <SelectField
              id={organizationId}
              value={formData.organization}
              onChange={(v) => handleChange('organization', v)}
              options={organizationOptions}
              className="py-2 px-3 bg-[#fdfdfd] border-2 border-[#edeef2] rounded text-[#1f2937] text-[13px] outline-none focus:border-[#4a90e2] transition-colors"
            />
          </div>

          {/* 부서 */}
          <div>
            <label htmlFor={departmentId} className="block text-[#374151] text-sm mb-1.5">
              부서
            </label>
            <SelectField
              id={departmentId}
              value={formData.department}
              onChange={(v) => handleChange('department', v)}
              options={departmentOptions}
              className="py-2 px-3 bg-[#fdfdfd] border border-[#edeef2] rounded text-[#1f2937] text-[13px] outline-none focus:border-[#4a90e2] transition-colors"
            />
          </div>

          {/* 직책 */}
          <div>
            <label htmlFor={titleId} className="block text-[#374151] text-sm mb-1.5">
              직책
            </label>
            <input
              id={titleId}
              name="title"
              type="text"
              value={formData.title}
              onChange={(e) => handleChange('title', e.target.value)}
              placeholder="직책을 입력하세요"
              className="w-full px-3 py-2 bg-[#fdfdfd] border border-[#e8ebf1] rounded text-[#1f2937] text-sm outline-none focus:border-[#4a90e2] transition-colors"
            />
          </div>

          {/* 역할 선택 */}
          <div>
            <label htmlFor={roleId} className="block text-[#374151] text-[13px] mb-1.5">
              역할 선택<span className="text-red-400 ml-0.5">*</span>
            </label>
            <SelectField
              id={roleId}
              value={formData.role}
              onChange={(v) => handleChange('role', v)}
              options={roleOptions}
              className="py-2 px-3 bg-[#fdfdfd] border border-[#eaecf1] rounded text-[#1f2937] text-sm outline-none focus:border-[#4a90e2] transition-colors"
            />
          </div>

          {/* 개인정보 동의 */}
          <div className="flex items-center gap-2">
            <input
              id={consentId}
              name="consent"
              type="checkbox"
              checked={formData.consent}
              onChange={(e) =>
                setFormData((prev) => ({ ...prev, consent: e.target.checked }))
              }
              required
              className="w-[18px] h-[18px] accent-[#1166e0] cursor-pointer shrink-0"
            />
            <span className="text-[#374151] text-[13px]">개인정보 처리 동의</span>
            <span className="text-[#6b7280] text-[13px]">(필수)</span>
            <label htmlFor={consentId} className="text-[#6b7280] text-xs cursor-pointer">
              개인정보 처리방침에 동의합니다.
            </label>
            <button
              type="button"
              className="text-[#91b9f4] text-xs underline hover:text-[#5591e7] transition-colors"
              aria-label="개인정보 처리방침 전문 보기"
            >
              전문 보기
            </button>
          </div>

          {error && <p className="text-red-500 text-sm">{error}</p>}

          {/* 구분선 */}
          <div className="h-px bg-[#f7f8fa] my-1" />

          {/* 버튼 */}
          <div className="flex justify-end gap-3 pb-8">
            <button
              type="button"
              onClick={onBack}
              disabled={loading}
              className="px-[46px] py-3 bg-[#fdfdfd] border border-[#dde0e7] rounded-md text-[#4b5563] text-[15px] hover:bg-[#f5f6fa] transition-colors disabled:opacity-50"
            >
              이전
            </button>
            <button
              type="submit"
              onClick={onSubmit}
              disabled={loading || !formData.consent || !formData.phone.trim()}
              className="px-[46px] py-3 bg-[#1164df] border border-[#5591e7] rounded-md text-[#91b9ee] text-sm hover:bg-[#0d55c4] transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? '처리 중...' : '가입 완료'}
            </button>
          </div>
        </form>
      </main>

      {/* ── 우측 정보 사이드카드 ── */}
      <aside
        aria-label="입력 정보 안내"
        className="w-[329px] shrink-0 bg-[#fbfcfd] border-l border-[#f5f6fa] flex flex-col px-9 pt-[27px] pb-5"
      >
        {/* 안내 텍스트 */}
        <div className="flex items-center gap-3 mb-5">
          <img src="/icons/info_icon.png" alt="" aria-hidden="true" className="w-[40px] h-[40px] shrink-0" />
          <p className="text-[#818695] text-sm leading-[1.6]">
            입력한 정보는 권한 설정과<br />알림 수신에 사용됩니다.
          </p>
        </div>

        {/* 정보 카드 목록 */}
        <div className="bg-[#fdfdfd] border-2 border-[#f5f6fa] rounded-[7px] overflow-hidden mb-4 flex-1">
          {infoCards.map((card, idx) => (
            <div
              key={card.title}
              className={`flex items-start gap-3 p-4 ${idx < infoCards.length - 1 ? 'border-b border-[#f5f6fa]' : ''}`}
            >
              <img
                src={card.icon}
                alt=""
                aria-hidden="true"
                className="w-[50px] h-[50px] shrink-0 object-contain"
              />
              <div>
                <div className="text-[#878e9c] text-[13px] mb-1.5">{card.title}</div>
                <p className="text-[#b5bac4] text-xs leading-[1.65]">{card.text}</p>
              </div>
            </div>
          ))}
        </div>

        {/* 암호화 안내 */}
        <div className="flex items-center gap-3 bg-[#eff6fd] border border-[#d0e8fb] rounded p-3">
          <img src="/icons/secure_icon2.png" alt="" aria-hidden="true" className="w-[20px] h-[20px] shrink-0" />
          <p className="text-[#4b6a8a] text-xs leading-[1.65]">
            모든 정보는 암호화되어 안전하게<br />
            저장되며, 외부에 공개되지 않습니다.
          </p>
        </div>
      </aside>
    </div>
  )
}

// ─── Page ─────────────────────────────────────────────────────────────────────

export function SignupPage() {
  const location = useLocation()
  const navigate = useNavigate()
  // TODO: 테스트 후 제거
  const userInfo = (location.state as NewUserInfo | null) ?? {
    signupToken: 'test-signup-token',
    email: 'hong@example.com',
    name: '홍길동',
    picture: undefined,
  }

  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false)
  const [formData, setFormData] = useState<FormData>({
    name: userInfo?.name ?? '',
    countryCode: '+82',
    phone: '',
    organization: 'smart-factory',
    department: 'production-tech',
    title: '',
    role: 'member',
    consent: false,
  })

  if (!userInfo?.signupToken) {
    return <Navigate to="/auth" replace />
  }

  async function handleSubmit() {
    setLoading(true)
    setError(null)
    try {
      const result = await postSignupRequest(userInfo!)
      if (result.status === 'REJECTED') {
        navigate('/rejected', { replace: true })
      } else {
        navigate('/pending', { replace: true })
      }
    } catch {
      setError('가입 신청 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="flex min-h-screen bg-[#fbfcfe] min-w-[1342px]">
      {/* 사이드바 */}
      <SidebarNavigationSection
        isCollapsed={sidebarCollapsed}
        onCollapse={() => setSidebarCollapsed(true)}
        onExpand={() => setSidebarCollapsed(false)}
      />

      {/* 우측 전체 영역 */}
      <div className="flex-1 flex flex-col min-h-screen">
        {/* 헤더 */}
        <RegistrationHeaderSection userName={formData.name || userInfo.name} />

        {/* 콘텐츠 */}
        <div className="flex flex-1 min-h-0">
          <MemberInfoFormSection
            userInfo={userInfo}
            loading={loading}
            error={error}
            formData={formData}
            setFormData={setFormData}
            onBack={() => navigate('/auth')}
            onSubmit={handleSubmit}
          />
        </div>
      </div>
    </div>
  )
}