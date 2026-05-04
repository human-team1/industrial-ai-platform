import { useMemo } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useResultDetail } from '../features/result/model'
import { toRegionScoreChart } from '../features/result/model/chartViewModels'
import {
  ChecklistCard,
  DescriptionCard,
  DetectionInfoCard,
  EventLogCard,
  ModelInfoCard,
  OriginalImageCard,
  ProbabilityDecisionCard,
  RelatedResultsCard,
  VisualizationCard,
  type EventLogItem,
  type RelatedResult,
  type ResultChecklistItem,
  type ResultDescriptionView,
} from '../widgets/result-detail'
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

  // 후속 PR에서 inspection events / explanation API 연동 시 교체.
  // 현재는 ResultDetail 응답에 해당 필드가 없으므로 빈 fallback으로만 처리.
  const eventLogs: EventLogItem[] = []
  const checklist: ResultChecklistItem[] = []
  const relatedResults: RelatedResult[] = []
  const description: ResultDescriptionView | null = null

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
          <EventLogCard events={eventLogs} />
        </div>

        <div className="space-y-5">
          <DetectionInfoCard
            target={data.target}
            inspection={data.inspection}
            result={data.result}
            originalImage={originalImage}
          />
          <ModelInfoCard model={data.model ?? null} result={data.result} />
          <ProbabilityDecisionCard result={data.result} />
          <BarChartCard
            viewModel={toRegionScoreChart(data)}
            empty={{ reason: 'NO_DATA', message: '표시할 region score가 없습니다.' }}
          />
          <DescriptionCard description={description} />
          <ChecklistCard checklist={checklist} />
          <RelatedResultsCard
            relatedResults={relatedResults}
            onDetail={(nextResultId) => navigate(`/results/${nextResultId}`)}
          />
        </div>
      </div>
    </div>
  )
}
