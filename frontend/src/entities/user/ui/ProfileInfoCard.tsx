import { SectionCard } from '../../../shared/ui/card/SectionCard'
import type { UserProfile } from '../types'

type ProfileInfoCardProps = {
  profile: UserProfile
  isEditing?: boolean
  isSaving?: boolean
  draftName?: string
  draftPhone?: string
  errorMessage?: string | null
  onNameChange?: (value: string) => void
  onPhoneChange?: (value: string) => void
  onStartEdit?: () => void
  onCancel?: () => void
  onSave?: () => void
}

export function ProfileInfoCard({
  profile,
  isEditing = false,
  isSaving = false,
  draftName,
  draftPhone,
  errorMessage,
  onNameChange,
  onPhoneChange,
  onStartEdit,
  onCancel,
  onSave,
}: ProfileInfoCardProps) {
  const displayedName = isEditing ? draftName ?? '' : profile.name
  const initial = displayedName.charAt(0).toUpperCase() || '?'
  const metaItems: { label: string; value: string; valueClassName?: string }[] = [
    { label: '가입일', value: profile.joinedAt },
    { label: '계정 상태', value: profile.accountStatus, valueClassName: 'text-[#15803d] font-medium' },
  ]
  const canEdit = typeof onStartEdit === 'function'

  return (
    <SectionCard title="프로필 정보">
      <div className="flex flex-col items-center gap-3 pt-2">
        <div className="w-[88px] h-[88px] rounded-full bg-[#dbeafe] flex items-center justify-center text-[#1d4ed8] text-[28px] font-semibold">
          {initial}
        </div>
        {isEditing ? (
          <input
            type="text"
            value={draftName ?? ''}
            onChange={(e) => onNameChange?.(e.target.value)}
            disabled={isSaving}
            maxLength={50}
            placeholder="이름"
            className="w-full max-w-[200px] px-2 py-1 text-center text-[19px] font-medium text-[#111827] border border-[#d1d5db] rounded focus:outline-none focus:border-[#1166e0]"
          />
        ) : (
          <div className="flex items-center gap-2">
            <span className="text-[#111827] text-[19px] font-medium">{profile.name}</span>
            <span className="px-2 py-0.5 bg-[#dbeafe] border border-[#bfdbfe] rounded text-[#1d4ed8] text-[10px] font-medium">
              {profile.role}
            </span>
          </div>
        )}
        <span className="text-[#4b5563] text-[12px]">{profile.email}</span>
      </div>

      <dl className="flex flex-col gap-2 pt-3 border-t border-[#f3f4f6]">
        <div className="flex items-center justify-between gap-2">
          <dt className="text-[#6b7280] text-[11px]">전화번호</dt>
          {isEditing ? (
            <input
              type="tel"
              value={draftPhone ?? ''}
              onChange={(e) => onPhoneChange?.(e.target.value)}
              disabled={isSaving}
              maxLength={30}
              placeholder="010-0000-0000"
              className="flex-1 max-w-[160px] px-2 py-1 text-right text-[11px] text-[#1f2937] border border-[#d1d5db] rounded focus:outline-none focus:border-[#1166e0]"
            />
          ) : (
            <dd className="text-[11px] text-[#1f2937]">{profile.phone || '미등록'}</dd>
          )}
        </div>
        {metaItems.map((item) => (
          <div key={item.label} className="flex items-center justify-between">
            <dt className="text-[#6b7280] text-[11px]">{item.label}</dt>
            <dd className={`text-[11px] ${item.valueClassName ?? 'text-[#1f2937]'}`}>{item.value}</dd>
          </div>
        ))}
      </dl>

      {errorMessage ? (
        <p className="mt-3 text-[11px] text-[#dc2626]">{errorMessage}</p>
      ) : null}

      {canEdit ? (
        <div className="mt-4 flex justify-end gap-2">
          {isEditing ? (
            <>
              <button
                type="button"
                onClick={onCancel}
                disabled={isSaving}
                className="px-3 py-1.5 text-[12px] text-[#374151] bg-white border border-[#d1d5db] rounded hover:bg-[#f9fafb] disabled:opacity-60"
              >
                취소
              </button>
              <button
                type="button"
                onClick={onSave}
                disabled={isSaving}
                className="px-3 py-1.5 text-[12px] text-white bg-[#1166e0] rounded hover:bg-[#0d4eb8] disabled:opacity-60"
              >
                {isSaving ? '저장 중…' : '저장'}
              </button>
            </>
          ) : (
            <button
              type="button"
              onClick={onStartEdit}
              className="px-3 py-1.5 text-[12px] text-[#1166e0] bg-white border border-[#1166e0] rounded hover:bg-[#eff6ff]"
            >
              수정
            </button>
          )}
        </div>
      ) : null}
    </SectionCard>
  )
}
