import { useMyProfile } from '../features/user-profile-view/model/useMyProfile'
import { useEditMyProfile } from '../features/user-profile-edit/model/useEditMyProfile'
import { ProfileInfoCard } from '../entities/user/ui/ProfileInfoCard'
import { AffiliationInfoCard } from '../entities/user/ui/AffiliationInfoCard'
import { EnvironmentSettingsCard } from '../features/environment-settings/ui/EnvironmentSettingsCard'
import { AccountManagementCard } from '../features/auth/ui'

export function MyPage() {
  const { profile, affiliation, applyServerProfile } = useMyProfile()
  const editor = useEditMyProfile({
    initial: { name: profile.name, phone: profile.phone },
    onSaved: applyServerProfile,
  })

  return (
    <div className="flex flex-col min-h-full">
      <div className="mb-6">
        <h1 className="text-[#111827] text-[22px] font-semibold leading-none mb-1.5">마이페이지</h1>
        <p className="text-[#4b5563] text-xs">개인 정보 및 계정 관리, 활동 내역을 확인할 수 있습니다.</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-[280px_1fr] gap-4 mb-4">
        <ProfileInfoCard
          profile={profile}
          isEditing={editor.isEditing}
          isSaving={editor.isSaving}
          draftName={editor.name}
          draftPhone={editor.phone}
          errorMessage={editor.errorMessage}
          onNameChange={editor.onNameChange}
          onPhoneChange={editor.onPhoneChange}
          onStartEdit={editor.onStartEdit}
          onCancel={editor.onCancel}
          onSave={editor.onSave}
        />
        <AffiliationInfoCard affiliation={affiliation} />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 mb-4">
        <EnvironmentSettingsCard />
        <AccountManagementCard />
      </div>
    </div>
  )
}
