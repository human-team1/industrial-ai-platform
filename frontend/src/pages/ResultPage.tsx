import { useNavigate } from 'react-router-dom'
import { useResultList } from '../features/result/model'
import { toResultDecisionChart, toResultScoreRangeChart } from '../features/result/model/chartViewModels'
import { ResultFilterForm } from '../features/result/ui/ResultFilterForm'
import { AppliedFilterChips, ResultListTable, ResultSummaryCards } from '../widgets/result-list'
import { BarChartCard } from '../shared/ui/chart/BarChartCard'
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

      <ResultFilterForm
        filters={filters}
        loading={loading}
        onChange={setFilters}
        onSearch={search}
        onReset={reset}
      />

      <ResultSummaryCards summary={data?.summary ?? null} loading={loading && !data} />

      <div className="grid grid-cols-1 gap-5 lg:grid-cols-2">
        <BarChartCard
          viewModel={toResultDecisionChart(data)}
          loading={loading && !data}
          empty={{ reason: 'NO_DATA', message: '현재 페이지에 표시할 결과가 없습니다.' }}
        />
        <BarChartCard
          viewModel={toResultScoreRangeChart(data)}
          loading={loading && !data}
          empty={{ reason: 'NO_DATA', message: 'score가 있는 결과가 없습니다.' }}
        />
      </div>

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
