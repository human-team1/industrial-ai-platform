import { labelDecision, labelRunType } from '../../entities/result/model/labels'
import type { ResultListQuery } from '../../features/result/api/types'

type Props = {
  filters: ResultListQuery
  onReset: () => void
}

export function AppliedFilterChips({ filters, onReset }: Props) {
  const chips = [
    filters.from || filters.to
      ? `기간: ${formatShortDate(filters.from)}~${formatShortDate(filters.to)}`
      : null,
    filters.equipmentName ? `설비: ${filters.equipmentName}` : '전체 설비',
    filters.runType ? `검사 유형: ${labelRunType(filters.runType)}` : '전체 검사 유형',
    filters.decisionCode ? `결과: ${labelDecision(filters.decisionCode)}` : '전체 결과',
    filters.keyword ? `검색: ${filters.keyword}` : null,
  ].filter(Boolean)

  return (
    <section className="flex flex-wrap items-center gap-2 rounded-lg border border-slate-200 bg-white px-4 py-3 shadow-sm">
      <span className="text-sm font-semibold text-slate-700">적용 필터</span>
      {chips.map((chip) => (
        <span
          key={chip}
          className="rounded-full bg-slate-100 px-3 py-1 text-xs font-medium text-slate-700"
        >
          {chip}
        </span>
      ))}
      <button
        type="button"
        onClick={onReset}
        className="ml-auto text-xs font-semibold text-slate-500 hover:text-slate-900"
      >
        전체 초기화
      </button>
    </section>
  )
}

function formatShortDate(value?: string | null) {
  if (!value) return ''
  return value.slice(0, 10)
}
