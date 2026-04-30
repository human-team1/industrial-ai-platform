import { useNavigate } from 'react-router-dom'
import { useResultList } from '../features/result/model'
import {
  AppliedFilterChips,
  ResultListFilter,
  ResultListTable,
  ResultSummaryCards,
} from '../features/result/ui'
import { PaginationBar } from '../shared/ui/pagination/PaginationBar'

export function ResultPage() {
  const navigate = useNavigate()
  const {
    filters,
    setFilters,
    data,
    page,
    loading,
    error,
    empty,
    search,
    reset,
    changePage,
    retry,
  } = useResultList()

  return (
    <section className="space-y-5">
      <div className="page-panel">
        <h1 className="text-2xl font-semibold text-slate-900">결과 목록</h1>
        <p className="mt-2 text-sm text-slate-600">
          탐지 수행 결과를 조회하고 상세 정보를 확인할 수 있습니다.
        </p>
      </div>

      <ResultListFilter
        filters={filters}
        loading={loading}
        onChange={setFilters}
        onSearch={search}
        onReset={reset}
      />

      <ResultSummaryCards summary={data?.summary ?? null} loading={loading && !data} />

      <AppliedFilterChips filters={filters} onReset={reset} />

      <ResultListTable
        data={data}
        loading={loading}
        error={error}
        empty={empty}
        onRetry={retry}
        onResetFilters={reset}
        onDetail={(resultId) => navigate(`/results/${resultId}`)}
      />

      {data ? (
        <PaginationBar
          page={page}
          totalPages={data.totalPages}
          totalElements={data.totalElements}
          loading={loading}
          onPageChange={changePage}
        />
      ) : null}
    </section>
  )
}
