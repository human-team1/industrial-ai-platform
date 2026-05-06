import { InfoCard } from './InfoCard'
import type { ResultChecklistItem } from './types'

const priorityLabels: Record<string, string> = {
  REQUIRED: '필수',
  RECOMMENDED: '권장',
  OPTIONAL: '선택',
}

type Props = {
  checklist: ResultChecklistItem[]
}

export function ChecklistCard({ checklist }: Props) {
  return (
    <InfoCard title="대응 절차 및 체크리스트">
      {checklist.length === 0 ? (
        <p className="text-sm text-slate-500">표시할 대응 절차가 없습니다.</p>
      ) : (
        <div className="space-y-3">
          {checklist.map((item) => (
            <div key={`${item.title}-${item.priority}`} className="rounded border border-slate-200 p-3">
              <div className="flex items-center justify-between gap-2">
                <p className="font-medium text-slate-900">{item.title}</p>
                <span className="rounded-full bg-slate-100 px-2.5 py-1 text-xs font-semibold text-slate-600">
                  {priorityLabels[item.priority] ?? item.priority}
                </span>
              </div>
              <p className="mt-1 text-sm text-slate-600">{display(item.description)}</p>
            </div>
          ))}
        </div>
      )}
    </InfoCard>
  )
}

function display(value: string | null | undefined) {
  return value === null || value === undefined || value === '' ? '-' : value
}
