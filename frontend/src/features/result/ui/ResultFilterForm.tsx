import type { ReactNode } from 'react'
import type { ResultListQuery } from '../api/types'

type Props = {
  filters: ResultListQuery
  loading: boolean
  onChange: (filters: ResultListQuery) => void
  onSearch: () => void
  onReset: () => void
}

export function ResultFilterForm({ filters, loading, onChange, onSearch, onReset }: Props) {
  return (
    <section className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
      <div className="grid grid-cols-1 gap-3 lg:grid-cols-[2fr_1.4fr_1fr_1fr_1fr_auto]">
        <Field label="키워드">
          <input
            value={filters.keyword ?? ''}
            onChange={(event) => onChange({ ...filters, keyword: event.target.value })}
            placeholder="설비명, 위치, 검사 유형 검색"
            className="control"
          />
        </Field>

        <Field label="날짜 범위">
          <div className="grid grid-cols-2 gap-2">
            <input
              type="datetime-local"
              value={filters.from ?? ''}
              onChange={(event) => onChange({ ...filters, from: event.target.value })}
              className="control"
            />
            <input
              type="datetime-local"
              value={filters.to ?? ''}
              onChange={(event) => onChange({ ...filters, to: event.target.value })}
              className="control"
            />
          </div>
        </Field>

        <Field label="설비명">
          <input
            value={filters.equipmentName ?? ''}
            onChange={(event) => onChange({ ...filters, equipmentName: event.target.value })}
            placeholder="설비명 (부분 일치)"
            className="control"
          />
        </Field>

        <Field label="검사 유형">
          <select
            value={filters.runType ?? ''}
            onChange={(event) => onChange({ ...filters, runType: event.target.value })}
            className="control"
          >
            <option value="">전체 검사 유형</option>
            <option value="REALTIME">실시간 탐지</option>
            <option value="UPLOAD">업로드 탐지</option>
          </select>
        </Field>

        <Field label="결과">
          <select
            value={filters.decisionCode ?? ''}
            onChange={(event) =>
              onChange({ ...filters, decisionCode: event.target.value as ResultListQuery['decisionCode'] })
            }
            className="control"
          >
            <option value="">전체 결과</option>
            <option value="NORMAL">정상</option>
            <option value="DEFECT">이상</option>
            <option value="RETEST">재검사</option>
          </select>
        </Field>

        <div className="flex items-end gap-2">
          <button
            type="button"
            onClick={onReset}
            disabled={loading}
            className="btn-secondary whitespace-nowrap"
          >
            필터 초기화
          </button>
          <button
            type="button"
            onClick={onSearch}
            disabled={loading}
            className="btn-primary whitespace-nowrap"
          >
            검색
          </button>
        </div>
      </div>
    </section>
  )
}

function Field({ label, children }: { label: string; children: ReactNode }) {
  return (
    <label className="flex flex-col gap-1 text-sm font-medium text-slate-700">
      {label}
      {children}
    </label>
  )
}
