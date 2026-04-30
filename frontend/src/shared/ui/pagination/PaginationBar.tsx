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

  return (
    <div className="flex flex-col gap-3 rounded-lg border border-slate-200 bg-white px-4 py-3 text-sm text-slate-600 shadow-sm sm:flex-row sm:items-center sm:justify-between">
      <span>전체 {totalElements.toLocaleString()}건</span>
      <div className="flex items-center gap-2">
        <button
          type="button"
          onClick={() => onPageChange(0)}
          disabled={!canGoPrevious || disabled}
          className="btn-secondary"
        >
          처음
        </button>
        <button
          type="button"
          onClick={() => onPageChange(currentPage - 1)}
          disabled={!canGoPrevious || disabled}
          className="btn-secondary"
        >
          이전
        </button>
        <span className="min-w-20 text-center">
          {normalizedTotalPages === 0 ? 0 : currentPage + 1} / {normalizedTotalPages}
        </span>
        <button
          type="button"
          onClick={() => onPageChange(currentPage + 1)}
          disabled={!canGoNext || disabled}
          className="btn-secondary"
        >
          다음
        </button>
        <button
          type="button"
          onClick={() => onPageChange(lastPage)}
          disabled={!canGoNext || disabled}
          className="btn-secondary"
        >
          마지막
        </button>
      </div>
    </div>
  )
}
