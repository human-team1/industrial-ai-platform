import type { DashboardOverview } from '../../../entities/dashboard/model/types'
import type { BarChartViewModel, LineChartViewModel } from '../../../shared/lib/chartViewModel'
import { countBy, truncateChartLabel } from '../../../shared/lib/chartViewModel'

export function toDashboardTrendChart(data: DashboardOverview): LineChartViewModel {
  return {
    title: '이상 탐지 추이',
    xKey: 'time',
    caption: '기간 API 기준',
    series: [
      { key: 'normalCount', label: '정상', unit: '건' },
      { key: 'anomalyCount', label: '이상', unit: '건' },
      { key: 'recheckCount', label: '재검사', unit: '건' },
    ],
    points: data.trend.map((row) => ({
      time: row.date.slice(5),
      normalCount: row.normalCount,
      anomalyCount: row.anomalyCount,
      recheckCount: row.recheckCount,
    })),
  }
}

export function toDashboardEquipmentChart(data: DashboardOverview): BarChartViewModel {
  return {
    title: '설비별 이상 탐지율',
    unit: '%',
    caption: '상위 설비',
    points: data.topEquipmentAnomalyRates.slice(0, 5).map((row) => ({
      label: truncateChartLabel(row.equipmentName),
      value: row.anomalyRate,
    })),
  }
}

export function toDashboardDecisionChart(data: DashboardOverview): BarChartViewModel {
  return {
    title: '최근 결과 판정 분포',
    unit: '건',
    caption: '최근 결과 기준',
    points: countBy(data.recentResults, (row) => row.decisionLabel ?? row.decision),
  }
}
