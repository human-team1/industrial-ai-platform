type PaginationBarProps = {
  page: number
  totalPages: number
  totalElements: number
  loading?: boolean
  onPageChange: (page: number) => void
}

export function PaginationBar({
  page,
  totalPages,
  totalElements,
  loading = false,
  onPageChange,
}: PaginationBarProps) {
  const normalizedTotalPages = Math.max(0, totalPages)
  const lastPage = Math.max(0, normalizedTotalPages - 1)
  const currentPage = normalizedTotalPages === 0 ? 0 : Math.min(Math.max(0, page), lastPage)
  const canGoPrevious = normalizedTotalPages > 1 && currentPage > 0
  const canGoNext = normalizedTotalPages > 1 && currentPage < lastPage
  const disabled = loading

  const pageBtn =
    'btn-secondary inline-flex shrink-0 items-center justify-center whitespace-nowrap px-3 sm:px-4 disabled:cursor-not-allowed'

  return (
    <div className="flex flex-col gap-3 rounded-lg border border-slate-200 bg-white px-4 py-3 text-sm text-slate-600 shadow-sm sm:flex-row sm:items-center sm:justify-between">
      <span className="shrink-0 font-medium">전체 {totalElements.toLocaleString()}건</span>
      <div className="flex flex-nowrap items-center gap-1.5 overflow-x-auto sm:gap-2">
        <button type="button" onClick={() => onPageChange(0)} disabled={!canGoPrevious || disabled} className={pageBtn}>
          처음
        </button>
        <button
          type="button"
          onClick={() => onPageChange(currentPage - 1)}
          disabled={!canGoPrevious || disabled}
          className={pageBtn}
        >
          이전
        </button>
        <span className="min-w-[5.5rem] shrink-0 px-1 text-center tabular-nums">
          {normalizedTotalPages === 0 ? 0 : currentPage + 1} / {normalizedTotalPages}
        </span>
        <button
          type="button"
          onClick={() => onPageChange(currentPage + 1)}
          disabled={!canGoNext || disabled}
          className={pageBtn}
        >
          다음
        </button>
        <button type="button" onClick={() => onPageChange(lastPage)} disabled={!canGoNext || disabled} className={pageBtn}>
          마지막
        </button>
      </div>
    </div>
  )
}
