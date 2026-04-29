import { useNavigate } from 'react-router-dom'
import { SectionCard } from '../../../shared/ui/card/SectionCard'
import { ListRowButton } from '../../../shared/ui/button/ListRowButton'
import { ChevronRight } from '../../../shared/ui/icons/ChevronRight'
import { ENVIRONMENT_SETTINGS } from '../model/menu'

export function EnvironmentSettingsCard() {
  const navigate = useNavigate()

  return (
    <SectionCard title="환경 설정">
      <div className="flex flex-col gap-2">
        {ENVIRONMENT_SETTINGS.map((item) => (
          <ListRowButton key={item.id} title={item.title} description={item.description} />
        ))}
      </div>

      <button
        type="button"
        onClick={() => navigate('/settings')}
        className="w-full flex items-center justify-center gap-1.5 mt-1 px-4 py-2.5 bg-[#1d4ed8] border border-[#1d4ed8] rounded-[3px] text-white text-[12px] font-medium hover:bg-[#1e40af] transition-colors focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#1166e0]"
      >
        설정 페이지로 이동
        <ChevronRight className="w-2.5 h-2.5" />
      </button>
    </SectionCard>
  )
}
