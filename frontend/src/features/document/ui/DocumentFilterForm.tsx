import type { DocumentSearchParams } from '../api/types'

type Props = {
  draft: DocumentSearchParams
  loading: boolean
  onChange: (next: DocumentSearchParams) => void
  onSearch: () => void
  onReset: () => void
}

export function DocumentSearchBar({ draft, loading, onChange, onSearch }: Omit<Props, 'onReset'>) {
  return (
    <section className="page-panel">
      <div className="relative w-full lg:w-1/3">
        <input
          className="control w-full pr-10"
          placeholder="제목/설명/태그 검색"
          value={draft.keyword ?? ''}
          onChange={(event) => onChange({ ...draft, keyword: event.target.value })}
          onKeyDown={(event) => {
            if (event.key === 'Enter') onSearch()
          }}
        />
        <button
          type="button"
          onClick={onSearch}
          disabled={loading}
          aria-label="검색"
          className="absolute inset-y-0 right-0 flex w-10 items-center justify-center text-slate-500 hover:text-slate-900 disabled:opacity-50"
        >
          <svg
            className="h-4 w-4"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth={1.8}
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <circle cx="11" cy="11" r="7" />
            <path d="M20 20l-3.5-3.5" />
          </svg>
        </button>
      </div>
    </section>
  )
}

export function DocumentFilterForm({ draft, loading, onChange, onSearch, onReset }: Props) {
  return (
    <section className="page-panel">
      <div className="grid grid-cols-1 gap-3 md:grid-cols-2 lg:grid-cols-[minmax(0,1fr)_minmax(0,1fr)_minmax(0,1fr)_minmax(0,1fr)_minmax(0,1fr)_minmax(0,1fr)_auto_auto]">
        <select
          className="control min-w-0"
          value={draft.documentType ?? ''}
          onChange={(event) =>
            onChange({ ...draft, documentType: event.target.value || undefined })
          }
        >
          <option value="">전체 유형</option>
          <option value="PDF">PDF</option>
          <option value="DOCX">DOCX</option>
          <option value="MD">MD</option>
        </select>
        <input
          className="control min-w-0"
          placeholder="카테고리"
          value={draft.category ?? ''}
          onChange={(event) => onChange({ ...draft, category: event.target.value })}
        />
        <input
          className="control min-w-0"
          placeholder="설비 유형"
          value={draft.equipmentType ?? ''}
          onChange={(event) => onChange({ ...draft, equipmentType: event.target.value })}
        />
        <select
          className="control min-w-0"
          value={typeof draft.indexingStatus === 'string' ? draft.indexingStatus : ''}
          onChange={(event) =>
            onChange({ ...draft, indexingStatus: event.target.value || undefined })
          }
        >
          <option value="">전체 반영 상태</option>
          <option value="PENDING">대기</option>
          <option value="PROCESSING">처리중</option>
          <option value="COMPLETED">반영완료</option>
          <option value="FAILED">반영실패</option>
        </select>
        <input
          className="control min-w-0"
          type="date"
          value={draft.startDate ?? ''}
          onChange={(event) => onChange({ ...draft, startDate: event.target.value })}
        />
        <input
          className="control min-w-0"
          type="date"
          value={draft.endDate ?? ''}
          onChange={(event) => onChange({ ...draft, endDate: event.target.value })}
        />
        <button
          type="button"
          className="btn-secondary whitespace-nowrap"
          disabled={loading}
          onClick={onReset}
        >
          필터 초기화
        </button>
        <button
          type="button"
          className="btn-primary whitespace-nowrap"
          disabled={loading}
          onClick={onSearch}
        >
          검색
        </button>
      </div>
    </section>
  )
}
