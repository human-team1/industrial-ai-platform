import type { OperationPolicy } from '../../../entities/operation/model/types'
import type { BarChartViewModel } from '../../../shared/lib/chartViewModel'
import { countBy } from '../../../shared/lib/chartViewModel'
import { statusBadgeLabel } from '../../../shared/lib/operationDisplay'

function mapChartLabels(points: { label: string; value: number }[], map: Record<string, string>): BarChartViewModel['points'] {
  return points.map((p) => ({
    label: map[p.label] ?? p.label,
    value: p.value,
  }))
}

export function toPolicyCategoryChart(policies: OperationPolicy[]): BarChartViewModel {
  const raw = countBy(policies, (policy) => policy.policyCategory ?? 'UNKNOWN')
  const labelMap: Record<string, string> = {
    UNKNOWN: '미분류',
    INSPECTION: '판정/검사 기준',
    NOTIFICATION: '알림 기준',
    SECURITY: '보안 기준',
    RETENTION: '보존 정책',
    SYSTEM: '운영 정책',
  }
  return {
    title: '정책 카테고리 분포',
    unit: '개',
    caption: '현재 조회된 목록 기준',
    points: mapChartLabels(raw, labelMap),
  }
}

export function toPolicyValueTypeChart(policies: OperationPolicy[]): BarChartViewModel {
  return {
    title: '정책 값 타입 분포',
    unit: '개',
    caption: '현재 조회된 목록 기준',
    points: countBy(policies, (policy) => policy.valueType ?? 'UNKNOWN'),
  }
}

export function toPolicyActiveChart(policies: OperationPolicy[]): BarChartViewModel {
  const raw = countBy(policies, (policy) => (policy.isActive ? 'ACTIVE' : 'INACTIVE'))
  return {
    title: '정책 활성 상태',
    unit: '개',
    caption: '현재 조회된 목록 기준',
    points: raw.map((p) => ({
      label: statusBadgeLabel(p.label),
      value: p.value,
    })),
  }
}
