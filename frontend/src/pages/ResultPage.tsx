import { useNavigate } from 'react-router-dom'
import { useResultList } from '../features/result/model'
import {
  AppliedFilterChips,
  ResultListFilter,
  ResultListTable,
  ResultPagination,
  ResultSummaryCards,
  buildEquipmentOptions,
  buildListSummary,
} from '../features/result/ui'

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

  const summary = buildListSummary(data)
  const equipmentOptions = buildEquipmentOptions(data)

  return (
    <section className="space-y-5">
      <div className="page-panel">
        <p className="mb-2 text-xs font-semibold text-slate-500">탐지 이력 &gt; 결과 목록</p>
        <h1 className="text-2xl font-semibold text-slate-900">결과 목록</h1>
        <p className="mt-2 text-sm text-slate-600">
          탐지 수행 결과를 조회하고 상세 정보를 확인할 수 있습니다.
        </p>
      </div>

      <ResultListFilter
        filters={filters}
        loading={loading}
        equipmentOptions={equipmentOptions}
        onChange={setFilters}
        onSearch={search}
        onReset={reset}
      />

      <ResultSummaryCards summary={summary} />

      <AppliedFilterChips filters={filters} onReset={reset} />

      <ResultListTable
        data={data}
        loading={loading}
        error={error}
        empty={empty}
        onRetry={retry}
        onDetail={(resultId) => navigate(`/results/${resultId}`)}
      />

      {data ? (
        <ResultPagination
          page={page}
          totalPages={data.totalPages}
          totalElements={data.totalElements}
          onPageChange={changePage}
        />
      ) : null}
    </section>
  )
}
