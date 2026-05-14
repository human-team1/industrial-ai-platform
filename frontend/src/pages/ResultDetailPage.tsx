import { useEffect, useMemo, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { fetchAvailableInspectionModels, rerunReviewInspection } from '../features/inspection/api'
import type { AvailableInspectionModel } from '../features/inspection/types'
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
  const [reviewModelOptions, setReviewModelOptions] = useState<AvailableInspectionModel[]>([])
  const [selectedDeploymentId, setSelectedDeploymentId] = useState<number | null>(null)
  const [rerunLoading, setRerunLoading] = useState(false)
  const [rerunMessage, setRerunMessage] = useState<string | null>(null)
  const [rerunError, setRerunError] = useState<string | null>(null)

  const regions = useMemo(
    () =>
      data?.images
        .filter((image) => String(image.imageRole).toUpperCase() !== 'ORIGINAL')
        .flatMap((image) => image.regions) ?? [],
    [data],
  )
  const originalImage = useMemo(
    () => data?.images.find((image) => String(image.imageRole).toUpperCase() === 'ORIGINAL'),
    [data],
  )

  const eventLogs: EventLogItem[] = []
  const checklist: ResultChecklistItem[] = []
  const relatedResults: RelatedResult[] = []
  const description: ResultDescriptionView | null = null

  useEffect(() => {
    if (!data?.review.reviewQueueId) {
      setReviewModelOptions([])
      setSelectedDeploymentId(null)
      return
    }

    const controller = new AbortController()
    setRerunError(null)

    void fetchAvailableInspectionModels(
      {
        targetId: data.target.targetId,
        inspectionType: 'REVIEW',
        modelCategory:
          data.model?.modelCategory === 'OBJECT' || data.model?.modelCategory === 'TEXTURE'
            ? data.model.modelCategory
            : undefined,
      },
      controller.signal,
    )
      .then((models) => {
        setReviewModelOptions(models)
        setSelectedDeploymentId(models[0]?.deploymentId ?? null)
      })
      .catch((nextError) => {
        if (!controller.signal.aborted) {
          setReviewModelOptions([])
          setSelectedDeploymentId(null)
          setRerunError(
            nextError instanceof Error ? nextError.message : '재검사 가능 모델을 조회하지 못했습니다.',
          )
        }
      })

    return () => controller.abort()
  }, [data?.model?.modelCategory, data?.review.reviewQueueId, data?.target.targetId])

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
            다시 시도
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
          <p className="mb-2 text-xs font-semibold text-slate-500">검사 이력 &gt; 결과 상세</p>
          <h1 className="text-2xl font-semibold text-slate-900">결과 상세</h1>
        </div>
        <div className="flex gap-2">
          <button
            type="button"
            onClick={() => navigate(`/chatbot?resultId=${data.resultId}`)}
            className="btn-primary"
          >
            이 결과로 챗봇 질문
          </button>
          <button type="button" onClick={goBack} className="btn-secondary">
            목록으로
          </button>
        </div>
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

          {data.review.reviewQueueId ? (
            <section className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
              <h2 className="mb-4 text-base font-semibold text-slate-950">재검사 실행</h2>
              <div className="space-y-3">
                <label className="block">
                  <span className="mb-1.5 block text-sm font-medium text-slate-700">재검사 모델</span>
                  <select
                    className="control w-full"
                    value={selectedDeploymentId ?? ''}
                    onChange={(event) =>
                      setSelectedDeploymentId(event.target.value ? Number(event.target.value) : null)
                    }
                    disabled={rerunLoading || reviewModelOptions.length === 0}
                  >
                    <option value="">
                      {reviewModelOptions.length === 0
                        ? '사용 가능한 배포 모델이 없습니다.'
                        : '배포 모델을 선택해 주세요.'}
                    </option>
                    {reviewModelOptions.map((model) => (
                      <option key={model.deploymentId} value={model.deploymentId}>
                        {model.displayName}
                      </option>
                    ))}
                  </select>
                </label>
                <p className="text-xs text-slate-500">
                  현재 결과와 같은 조직/대상 범위에서 활성 배포된 모델만 재검사 대상으로 표시합니다.
                </p>
                {rerunMessage ? (
                  <p className="rounded-md border border-blue-200 bg-blue-50 px-3 py-2 text-sm text-blue-700">
                    {rerunMessage}
                  </p>
                ) : null}
                {rerunError ? (
                  <p className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
                    {rerunError}
                  </p>
                ) : null}
                <button
                  type="button"
                  className="btn-primary"
                  disabled={!selectedDeploymentId || rerunLoading}
                  onClick={async () => {
                    if (!data.review.reviewQueueId || !selectedDeploymentId) return
                    setRerunLoading(true)
                    setRerunError(null)
                    setRerunMessage(null)
                    try {
                      const response = await rerunReviewInspection(
                        data.review.reviewQueueId,
                        selectedDeploymentId,
                      )
                      setRerunMessage(
                        `재검사 요청이 접수되었습니다. inspectionId=${response.inspectionId}, runStatus=${response.runStatus}`,
                      )
                    } catch (nextError) {
                      setRerunError(
                        nextError instanceof Error ? nextError.message : '재검사 요청에 실패했습니다.',
                      )
                    } finally {
                      setRerunLoading(false)
                    }
                  }}
                >
                  {rerunLoading ? '재검사 요청 중...' : '재검사 실행'}
                </button>
              </div>
            </section>
          ) : null}

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
