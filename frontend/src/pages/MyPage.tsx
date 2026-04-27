import { useEffect, useState } from 'react'
import { useAuth } from '../features/auth/model'
import { getMyProfile } from '../features/auth/api'
import type { UserMeResponse } from '../features/auth/types'

// ─── Types ────────────────────────────────────────────────────────────────────

type ProfileInfo = {
  name: string
  email: string
  role: string
  joinedAt: string
  lastLoginAt: string
  accountStatus: string
}

type AffiliationInfo = {
  organization: string
  team: string
  role: string
  permissions: string[]
}

type AccountSettingItem = {
  id: string
  title: string
  description: string
  status?: string
}

type EnvironmentSettingItem = {
  id: string
  title: string
  description: string
}

const ACCOUNT_SETTINGS: AccountSettingItem[] = [
  { id: 'password', title: '비밀번호 변경', description: '주기적으로 비밀번호를 변경하세요.' },
  { id: '2fa', title: '2단계 인증', description: '계정 보안을 강화하세요.', status: '사용중' },
  { id: 'login-history', title: '로그인 기록', description: '최근 로그인 기록을 확인하세요.' },
  { id: 'session', title: '세션 관리', description: '활성 세션을 관리하고 로그아웃하세요.' },
]

const ENVIRONMENT_SETTINGS: EnvironmentSettingItem[] = [
  { id: 'notification', title: '알림 설정', description: '이메일 및 앱 알림을 설정하세요.' },
  { id: 'dashboard-default', title: '대시보드 기본 설정', description: '화면 구성 및 기본 옵션을 설정하세요.' },
]

const PLACEHOLDER = '-'

function formatDate(iso?: string): string {
  if (!iso) return PLACEHOLDER
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return PLACEHOLDER
  return d.toISOString().slice(0, 10)
}

function formatDateTime(iso?: string): string {
  if (!iso) return PLACEHOLDER
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return PLACEHOLDER
  const date = d.toISOString().slice(0, 10)
  const time = d.toTimeString().slice(0, 5)
  return `${date} ${time}`
}

function buildProfile(profile: UserMeResponse | null): ProfileInfo {
  return {
    name: profile?.name ?? PLACEHOLDER,
    email: profile?.email ?? PLACEHOLDER,
    role: profile?.role ?? PLACEHOLDER,
    joinedAt: formatDate(profile?.createdAt),
    lastLoginAt: formatDateTime(profile?.lastLoginAt),
    accountStatus: profile?.status ?? '정상',
  }
}

function buildAffiliation(profile: UserMeResponse | null): AffiliationInfo {
  return {
    organization: profile?.organizationName ?? PLACEHOLDER,
    team: PLACEHOLDER,
    role: profile?.role ?? PLACEHOLDER,
    permissions: [],
  }
}

// ─── Shared UI ────────────────────────────────────────────────────────────────

function SectionCard({
  title,
  children,
  className = '',
}: {
  title: string
  children: React.ReactNode
  className?: string
}) {
  return (
    <div className={`bg-[#fefefe] border border-[#e5e7eb] rounded p-5 flex flex-col gap-4 ${className}`}>
      <h2 className="text-[#1f2937] text-[15px] font-semibold leading-none">{title}</h2>
      {children}
    </div>
  )
}

function ChevronRight({ className = 'w-3 h-3' }: { className?: string }) {
  return (
    <svg className={className} fill="none" viewBox="0 0 12 12" stroke="currentColor" strokeWidth={1.5}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M4 2l4 4-4 4" />
    </svg>
  )
}

function ListRowButton({
  title,
  description,
  status,
  onClick,
  variant = 'default',
}: {
  title: string
  description: string
  status?: string
  onClick?: () => void
  variant?: 'default' | 'danger'
}) {
  const titleColor = variant === 'danger' ? 'text-[#dc2626]' : 'text-[#1f2937]'
  const borderColor = variant === 'danger' ? 'border-[#fecaca]' : 'border-[#e5e7eb]'

  return (
    <button
      type="button"
      onClick={onClick}
      className={`w-full flex items-center justify-between gap-4 px-4 py-3 bg-[#fefefe] border ${borderColor} rounded-[3px] text-left transition-colors hover:bg-[#f8f9fc] focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#1166e0]`}
    >
      <div className="flex flex-col gap-0.5 min-w-0">
        <span className={`text-[13px] font-medium ${titleColor}`}>{title}</span>
        <span className="text-[#6b7280] text-[11px] truncate">{description}</span>
      </div>
      <div className="flex items-center gap-2 shrink-0">
        {status && <span className="text-[#15803d] text-[11px] font-medium">{status}</span>}
        <ChevronRight className="w-2.5 h-2.5 text-[#6b7280]" />
      </div>
    </button>
  )
}

// ─── Profile Info Card ────────────────────────────────────────────────────────

function ProfileInfoCard({ profile }: { profile: ProfileInfo }) {
  const initial = profile.name.charAt(0).toUpperCase()
  const metaItems: { label: string; value: string; valueClassName?: string }[] = [
    { label: '가입일', value: profile.joinedAt },
    { label: '최근 로그인', value: profile.lastLoginAt },
    { label: '계정 상태', value: profile.accountStatus, valueClassName: 'text-[#15803d] font-medium' },
  ]

  return (
    <SectionCard title="프로필 정보">
      <div className="flex flex-col items-center gap-3 pt-2">
        <div className="w-[88px] h-[88px] rounded-full bg-[#dbeafe] flex items-center justify-center text-[#1d4ed8] text-[28px] font-semibold">
          {initial}
        </div>
        <div className="flex items-center gap-2">
          <span className="text-[#111827] text-[19px] font-medium">{profile.name}</span>
          <span className="px-2 py-0.5 bg-[#dbeafe] border border-[#bfdbfe] rounded text-[#1d4ed8] text-[10px] font-medium">
            {profile.role}
          </span>
        </div>
        <span className="text-[#4b5563] text-[12px]">{profile.email}</span>
      </div>

      <dl className="flex flex-col gap-2 pt-3 border-t border-[#f3f4f6]">
        {metaItems.map((item) => (
          <div key={item.label} className="flex items-center justify-between">
            <dt className="text-[#6b7280] text-[11px]">{item.label}</dt>
            <dd className={`text-[11px] ${item.valueClassName ?? 'text-[#1f2937]'}`}>{item.value}</dd>
          </div>
        ))}
      </dl>
    </SectionCard>
  )
}

// ─── Affiliation Info Card ────────────────────────────────────────────────────

function AffiliationInfoCard({ affiliation }: { affiliation: AffiliationInfo }) {
  const rows: { label: string; value?: string }[] = [
    { label: '조직', value: affiliation.organization },
    { label: '팀', value: affiliation.team },
    { label: '역할', value: affiliation.role },
    { label: '권한' },
  ]

  return (
    <SectionCard title="소속 정보">
      <dl className="flex flex-col gap-3">
        {rows.map((row) => (
          <div key={row.label} className="flex items-start gap-4">
            <dt className="w-12 text-[#6b7280] text-[12px] font-medium shrink-0">{row.label}</dt>
            {row.value ? (
              <dd className="text-[#1f2937] text-[13px]">{row.value}</dd>
            ) : (
              <dd className="flex flex-wrap gap-1.5">
                {affiliation.permissions.length > 0 ? (
                  affiliation.permissions.map((permission) => (
                    <span
                      key={permission}
                      className="px-2.5 py-1 bg-[#dbeafe] border border-[#bfdbfe] rounded-[5px] text-[#1d4ed8] text-[11px] font-medium"
                    >
                      {permission}
                    </span>
                  ))
                ) : (
                  <span className="text-[#1f2937] text-[13px]">{PLACEHOLDER}</span>
                )}
              </dd>
            )}
          </div>
        ))}
      </dl>

      <button
        type="button"
        className="w-full flex items-center justify-center gap-1.5 mt-1 px-4 py-2.5 bg-[#fefefe] border border-[#e5e7eb] rounded-[3px] text-[#374151] text-xs font-medium hover:bg-[#f5f6fa] transition-colors focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#1166e0]"
      >
        조직 정보 보기
        <ChevronRight className="w-2.5 h-2.5" />
      </button>
    </SectionCard>
  )
}

// ─── Account Settings Card ────────────────────────────────────────────────────

function AccountSettingsCard({ items }: { items: AccountSettingItem[] }) {
  return (
    <SectionCard title="계정 설정">
      <div className="flex flex-col gap-2">
        {items.map((item) => (
          <ListRowButton
            key={item.id}
            title={item.title}
            description={item.description}
            status={item.status}
          />
        ))}
      </div>
    </SectionCard>
  )
}

// ─── Environment Settings Card ────────────────────────────────────────────────

function EnvironmentSettingsCard({ items }: { items: EnvironmentSettingItem[] }) {
  return (
    <SectionCard title="환경 설정">
      <div className="flex flex-col gap-2">
        {items.map((item) => (
          <ListRowButton key={item.id} title={item.title} description={item.description} />
        ))}
      </div>

      <button
        type="button"
        className="w-full flex items-center justify-center gap-1.5 mt-1 px-4 py-2.5 bg-[#1d4ed8] border border-[#1d4ed8] rounded-[3px] text-white text-[12px] font-medium hover:bg-[#1e40af] transition-colors focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#1166e0]"
      >
        설정 페이지로 이동
        <ChevronRight className="w-2.5 h-2.5" />
      </button>
    </SectionCard>
  )
}

// ─── Account Management Card ──────────────────────────────────────────────────

function AccountManagementCard({ onLogout }: { onLogout: () => void }) {
  return (
    <SectionCard title="계정 관리">
      <ListRowButton
        title="로그아웃"
        description="현재 계정에서 로그아웃합니다."
        variant="danger"
        onClick={onLogout}
      />
    </SectionCard>
  )
}

// ─── Page ─────────────────────────────────────────────────────────────────────

export function MyPage() {
  const { logout } = useAuth()
  const [profileData, setProfileData] = useState<UserMeResponse | null>(null)

  useEffect(() => {
    let cancelled = false
    getMyProfile()
      .then((data) => {
        if (!cancelled) setProfileData(data)
      })
      .catch(() => {
        if (!cancelled) setProfileData(null)
      })
    return () => {
      cancelled = true
    }
  }, [])

  const profile = buildProfile(profileData)
  const affiliation = buildAffiliation(profileData)

  return (
    <div className="flex flex-col min-h-full">
      <div className="mb-6">
        <h1 className="text-[#111827] text-[22px] font-semibold leading-none mb-1.5">마이페이지</h1>
        <p className="text-[#4b5563] text-xs">개인 정보 및 계정 관리, 활동 내역을 확인할 수 있습니다.</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-[280px_1fr] gap-4 mb-4">
        <ProfileInfoCard profile={profile} />
        <AffiliationInfoCard affiliation={affiliation} />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 mb-4">
        <AccountSettingsCard items={ACCOUNT_SETTINGS} />
        <EnvironmentSettingsCard items={ENVIRONMENT_SETTINGS} />
      </div>

      <AccountManagementCard onLogout={logout} />
    </div>
  )
}
