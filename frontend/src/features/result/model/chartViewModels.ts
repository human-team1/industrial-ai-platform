import type { AnomalyRegion } from '../../../entities/result/model/types'
import type { ResultDetail, ResultPageResponse } from '../api/types'
import type { BarChartViewModel } from '../../../shared/lib/chartViewModel'
import { countBy, truncateChartLabel } from '../../../shared/lib/chartViewModel'
import { labelDecision } from '../../../entities/result/model/labels'

export function toResultDecisionChart(data: ResultPageResponse | null): BarChartViewModel {
  const rows = data?.content ?? []
  return {
    title: '현재 페이지 판정 분포',
    unit: '건',
    caption: '현재 페이지 기준',
    points: countBy(rows, (row) => labelDecision(row.finalDecisionCode ?? row.decisionCode ?? 'UNKNOWN')),
  }
}

export function toResultScoreRangeChart(data: ResultPageResponse | null): BarChartViewModel {
  const rows = data?.content ?? []
  const buckets = [
    { label: '0~20%', min: 0, max: 0.2, count: 0 },
    { label: '20~40%', min: 0.2, max: 0.4, count: 0 },
    { label: '40~60%', min: 0.4, max: 0.6, count: 0 },
    { label: '60~80%', min: 0.6, max: 0.8, count: 0 },
    { label: '80~100%', min: 0.8, max: 1.01, count: 0 },
  ]

  rows.forEach((row) => {
    const score = row.score
    if (typeof score !== 'number') return
    const bucket = buckets.find((item) => score >= item.min && score < item.max)
    if (bucket) bucket.count += 1
  })

  return {
    title: '현재 페이지 score 분포',
    unit: '건',
    caption: '현재 페이지 기준',
    points: buckets.map((bucket) => ({ label: bucket.label, value: bucket.count })).filter((point) => point.value > 0),
  }
}

export function toRegionScoreChart(detail: ResultDetail): BarChartViewModel {
  const regions = detail.images.flatMap((image) => image.regions)
  return {
    title: 'Region score',
    unit: '%',
    caption: '결과 상세 기준',
    points: regionsToPoints(regions),
  }
}

function regionsToPoints(regions: AnomalyRegion[]) {
  return regions
    .filter((region) => typeof region.score === 'number')
    .map((region) => ({
      label: truncateChartLabel(region.labelCode ?? `region-${region.regionId}`),
      value: Math.round((region.score ?? 0) * 1000) / 10,
    }))
}
