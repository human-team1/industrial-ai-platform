import type { DocumentListItem, DocumentSummary } from '../types'
import type { BarChartViewModel } from '../../../shared/lib/chartViewModel'
import { countBy } from '../../../shared/lib/chartViewModel'
import { indexingStatusLabel } from '../lib/indexingStatusLabel'

export function toDocumentIndexingStatusChart(summary: DocumentSummary | null): BarChartViewModel {
  return {
    title: '문서 인덱싱 상태 분포',
    unit: '건',
    points: summary
      ? [
          { label: '반영완료', value: summary.completedCount },
          { label: '처리중', value: summary.processingCount },
          { label: '반영실패', value: summary.failedCount },
          { label: '대기/기타', value: Math.max(0, summary.totalCount - summary.completedCount - summary.processingCount - summary.failedCount) },
        ].filter((point) => point.value > 0)
      : [],
  }
}

export function toDocumentTypeChart(items: DocumentListItem[]): BarChartViewModel {
  return {
    title: '현재 페이지 문서 유형 분포',
    unit: '건',
    caption: '현재 페이지 기준',
    points: countBy(items, (item) => item.documentType),
  }
}

export function toDocumentPageStatusChart(items: DocumentListItem[]): BarChartViewModel {
  return {
    title: '현재 페이지 반영 상태',
    unit: '건',
    caption: '현재 페이지 기준',
    points: countBy(items, (item) => indexingStatusLabel(item.indexingStatus)),
  }
}
