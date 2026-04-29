import { SectionCard } from '../../../shared/ui/card/SectionCard'
import type { UserProfile } from '../types'

export function ProfileInfoCard({ profile }: { profile: UserProfile }) {
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
