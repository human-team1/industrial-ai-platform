import type { ChatHistoryFilterBarProps } from './types'

export function ChatHistoryFilterBar({
  keyword,
  from,
  to,
  onKeywordChange,
  onFromChange,
  onToChange,
  onSearch,
}: ChatHistoryFilterBarProps) {
  const triggerSearch = () => {
    if (onSearch) onSearch()
    else onKeywordChange(keyword)
  }

  return (
    <div className="page-panel space-y-3 py-4">
      <div className="relative w-full">
        <input
          className="control w-full pr-10"
          placeholder="대화 제목 또는 메시지 검색"
          value={keyword}
          onChange={(event) => onKeywordChange(event.target.value)}
          onKeyDown={(event) => {
            if (event.key === 'Enter') triggerSearch()
          }}
        />
        <button
          type="button"
          onClick={triggerSearch}
          aria-label="검색"
          className="absolute inset-y-0 right-0 flex w-10 items-center justify-center text-slate-500 hover:text-slate-900"
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

      <div className="grid grid-cols-1 gap-3 md:grid-cols-2 lg:grid-cols-[minmax(0,1fr)_minmax(0,1fr)_minmax(0,140px)_minmax(0,140px)]">
        <input
          className="control min-w-0"
          type="date"
          title="시작일"
          value={from}
          onChange={(event) => onFromChange(event.target.value)}
        />
        <input
          className="control min-w-0"
          type="date"
          title="종료일"
          value={to}
          onChange={(event) => onToChange(event.target.value)}
        />
        <select
          className="control min-w-0"
          title="태그 (후속 API 연동 예정)"
          defaultValue=""
          disabled
        >
          <option value="">전체 태그</option>
        </select>
        <select
          className="control min-w-0"
          title="문서 유형 (후속 API 연동 예정)"
          defaultValue=""
          disabled
        >
          <option value="">전체 문서 유형</option>
        </select>
      </div>
    </div>
  )
}
