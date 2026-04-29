import { SectionCard } from '../../../shared/ui/card/SectionCard'
import { ListRowButton } from '../../../shared/ui/button/ListRowButton'
import { useAuth } from '../model'

export function AccountManagementCard() {
  const { logout } = useAuth()

  return (
    <SectionCard title="계정 관리">
      <ListRowButton
        title="로그아웃"
        description="현재 계정에서 로그아웃합니다."
        variant="danger"
        onClick={logout}
      />
    </SectionCard>
  )
}
