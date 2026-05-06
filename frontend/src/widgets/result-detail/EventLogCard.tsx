import { formatDateMinute } from '../../entities/result/model/labels'
import { InfoCard } from './InfoCard'
import type { EventLogItem } from './types'

const eventLabels: Record<string, string> = {
  INSPECTION_STARTED: '탐지 시작',
  IMAGE_CAPTURED: '이미지 수집',
  ANOMALY_DETECTED: '이상 탐지',
  INSPECTION_FAILED: '탐지 실패',
}

type Props = {
  events: EventLogItem[]
}

export function EventLogCard({ events }: Props) {
  return (
    <InfoCard title="이벤트 로그">
      {events.length === 0 ? (
        <p className="text-sm text-slate-500">이벤트 로그가 없습니다.</p>
      ) : (
        <ol className="space-y-3">
          {events.map((log, index) => (
            <li key={log.eventId ?? index} className="flex gap-3 text-sm">
              <span className="mt-1 h-2.5 w-2.5 rounded-full bg-slate-400" />
              <div>
                <p className="font-medium text-slate-900">
                  {eventLabels[String(log.eventType)] ?? display(log.message)}
                </p>
                <p className="text-slate-500">{formatDateMinute(log.createdAt)}</p>
              </div>
            </li>
          ))}
        </ol>
      )}
    </InfoCard>
  )
}

function display(value: string | null | undefined) {
  return value === null || value === undefined || value === '' ? '-' : value
}
