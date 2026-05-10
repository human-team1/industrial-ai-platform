export type ChartPoint = {
  label: string
  value: number
}

export type BarChartViewModel = {
  title: string
  unit?: string
  points: ChartPoint[]
  /** 차트 우측 상단 보조 설명 */
  caption?: string
}

export type TimeSeriesPoint = {
  time: string
  [metric: string]: string | number | null
}

export type LineChartSeries = {
  key: string
  label: string
  unit?: string
}

export type LineChartViewModel = {
  title: string
  xKey: string
  series: LineChartSeries[]
  points: TimeSeriesPoint[]
  caption?: string
  yMax?: number
}

export type EmptyChartState = {
  reason: 'NO_DATA' | 'API_ERROR' | 'NOT_APPLICABLE'
  message: string
}

const MAX_LABEL_LENGTH = 32

export function truncateChartLabel(value: string) {
  const normalized = value.replace(/\s+/g, ' ').trim()
  return normalized.length > MAX_LABEL_LENGTH ? `${normalized.slice(0, MAX_LABEL_LENGTH)}...` : normalized
}

export function countBy<T>(items: T[], getKey: (item: T) => string | null | undefined): ChartPoint[] {
  const counts = new Map<string, number>()
  items.forEach((item) => {
    const key = getKey(item)
    if (!key) return
    counts.set(key, (counts.get(key) ?? 0) + 1)
  })
  return Array.from(counts.entries()).map(([label, value]) => ({ label: truncateChartLabel(label), value }))
}

export function compactNumber(value: number | null | undefined) {
  return typeof value === 'number' && Number.isFinite(value) ? value : null
}
