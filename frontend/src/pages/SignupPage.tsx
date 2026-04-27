import { useId, useState } from 'react'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import type { NewUserInfo } from '../features/auth/types'
import { postSignupRequest } from '../features/auth/api'
import { AppSidebar } from '../shared/ui/layout/AppSidebar'
import { AppHeader } from '../shared/ui/layout/AppHeader'

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

// ─── Icons ────────────────────────────────────────────────────────────────────

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
      <AppSidebar
        isCollapsed={sidebarCollapsed}
        onCollapse={() => setSidebarCollapsed(true)}
        onExpand={() => setSidebarCollapsed(false)}
      />

      {/* 우측 전체 영역 */}
      <div className="flex-1 flex flex-col min-h-screen">
        {/* 헤더 */}
        <AppHeader userName={formData.name || userInfo.name} userRole="신규 사용자" />

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