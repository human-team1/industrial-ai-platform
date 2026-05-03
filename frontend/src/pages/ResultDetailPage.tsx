import { useMemo } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useResultDetail } from '../features/result/model'
import { toRegionScoreChart } from '../features/result/model/chartViewModels'
import {
  ChecklistCard,
  DescriptionCard,
  DetectionInfoCard,
  EventLogCard,
  OriginalImageCard,
  ProbabilityDecisionCard,
  RelatedResultsCard,
  VisualizationCard,
} from '../features/result/ui'
import { BarChartCard } from '../shared/ui/chart/BarChartCard'

export function ResultDetailPage() {
  const { resultId } = useParams()
  const navigate = useNavigate()
  const { data, loading, error, retry, goBack } = useResultDetail(resultId)
  const regions = useMemo(() => data?.images.flatMap((image) => image.regions) ?? [], [data])
  const originalImage = useMemo(
    () => data?.images.find((image) => image.imageRole === 'ORIGINAL') ?? data?.images[0],
    [data],
  )

  if (loading) {
    return (
      <section className="page-panel">
        <p className="text-sm text-slate-600">검사 결과 상세를 불러오는 중입니다.</p>
      </section>
    )
  }

  if (error) {
    return (
      <section className="page-panel space-y-4">
        <p className="text-sm text-red-600">{error}</p>
        <div className="flex gap-2">
          <button type="button" onClick={retry} className="btn-primary">
            재시도
          </button>
          <button type="button" onClick={goBack} className="btn-secondary">
            목록으로
          </button>
        </div>
      </section>
    )
  }

  if (!data) {
    return (
      <section className="page-panel">
        <p className="text-sm text-slate-600">표시할 검사 결과가 없습니다.</p>
      </section>
    )
  }

  return (
    <div className="space-y-5">
      <section className="page-panel flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <p className="mb-2 text-xs font-semibold text-slate-500">탐지 이력 &gt; 결과 상세</p>
          <h1 className="text-2xl font-semibold text-slate-900">결과 상세</h1>
        </div>
        <button type="button" onClick={goBack} className="btn-secondary">
          목록으로
        </button>
      </section>

      <div className="grid gap-5 xl:grid-cols-[45fr_55fr]">
        <div className="space-y-5">
          <OriginalImageCard images={data.images} />
          <VisualizationCard artifacts={data.artifacts} regions={regions} />
          <EventLogCard eventLogs={data.eventLogs} />
        </div>

        <div className="space-y-5">
          <DetectionInfoCard
            target={data.target}
            inspection={data.inspection}
            result={data.result}
            originalImage={originalImage}
          />
          <section className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
            <h2 className="mb-4 text-base font-semibold text-slate-950">사용 모델</h2>
            {data.model ? (
              <dl className="grid grid-cols-1 gap-3 sm:grid-cols-2">
                <div>
                  <dt className="text-xs font-semibold uppercase text-slate-500">모델명</dt>
                  <dd className="mt-1 text-sm text-slate-900">{data.model.modelName ?? '-'}</dd>
                </div>
                <div>
                  <dt className="text-xs font-semibold uppercase text-slate-500">버전명</dt>
                  <dd className="mt-1 text-sm text-slate-900">{data.model.versionName ?? '-'}</dd>
                </div>
                <div>
                  <dt className="text-xs font-semibold uppercase text-slate-500">카테고리</dt>
                  <dd className="mt-1 text-sm text-slate-900">{data.model.modelCategory ?? '-'}</dd>
                </div>
                <div>
                  <dt className="text-xs font-semibold uppercase text-slate-500">프로필</dt>
                  <dd className="mt-1 text-sm text-slate-900">{data.model.modelProfile ?? '-'}</dd>
                </div>
                <div>
                  <dt className="text-xs font-semibold uppercase text-slate-500">modelVersionId</dt>
                  <dd className="mt-1 text-sm text-slate-900">{data.result.modelVersionId ?? '-'}</dd>
                </div>
              </dl>
            ) : (
              <p className="text-sm text-slate-500">모델 정보 없음</p>
            )}
          </section>
          <ProbabilityDecisionCard result={data.result} />
          <BarChartCard
            viewModel={toRegionScoreChart(data)}
            empty={{ reason: 'NO_DATA', message: '표시할 region score가 없습니다.' }}
          />
          <DescriptionCard description={data.description} />
          <ChecklistCard checklist={data.checklist} />
          <RelatedResultsCard
            relatedResults={data.relatedResults}
            onDetail={(nextResultId) => navigate(`/results/${nextResultId}`)}
          />
        </div>
      </div>
    </div>
  )
}
