import { SectionCard } from '../../../shared/ui/card/SectionCard'
import { ListRowButton } from '../../../shared/ui/button/ListRowButton'
import { ACCOUNT_SETTINGS } from '../model/menu'

export function AccountSettingsCard() {
  return (
    <SectionCard title="계정 설정">
      <div className="flex flex-col gap-2">
        {ACCOUNT_SETTINGS.map((item) => (
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
