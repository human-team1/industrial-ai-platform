import type { AnomalyRegion, ResultArtifact } from '../../entities/result/model/types'
import { useResultFilePreview } from '../../features/result/model/useResultFilePreview'
import { InfoCard } from './InfoCard'
import { PreviewImage } from './PreviewImage'

type Props = {
  artifacts: ResultArtifact[]
  regions: AnomalyRegion[]
}

const VISUALIZATION_PRIORITY = ['HEATMAP', 'ANOMALY_MAP', 'BOUNDING_BOX_IMAGE', 'VISUALIZATION'] as const

export function VisualizationCard({ artifacts, regions }: Props) {
  const artifact = pickVisualizationArtifact(artifacts)
  const preview = useResultFilePreview(artifact?.fileId)

  return (
    <InfoCard title="이상 탐지 시각화">
      <PreviewImage
        previewUrl={preview.previewUrl}
        isLoading={preview.isLoading}
        error={preview.error}
        emptyText="시각화 이미지가 없습니다."
      />
      {regions.length > 0 ? (
        <div className="mt-4 space-y-2">
          {regions.map((region) => (
            <div
              key={region.regionId}
              className="flex items-center justify-between rounded border border-slate-200 px-3 py-2 text-sm"
            >
              <span>{display(region.labelCode)}</span>
              <span className="text-slate-500">score {formatPercent(region.score)}</span>
            </div>
          ))}
        </div>
      ) : null}
    </InfoCard>
  )
}

function pickVisualizationArtifact(artifacts: ResultArtifact[]) {
  for (const t of VISUALIZATION_PRIORITY) {
    const hit = artifacts.find((item) => String(item.artifactType).toUpperCase() === t)
    if (hit?.fileId) return hit
  }
  return undefined
}

function display(value: string | null | undefined) {
  return value === null || value === undefined || value === '' ? '-' : value
}

function formatPercent(value?: number | null) {
  if (value === null || value === undefined) return '-'
  return `${Math.max(0, Math.min(100, Number(value) * 100)).toFixed(1)}%`
}
