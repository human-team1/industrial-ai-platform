import type { OperationPolicy } from '../../../entities/operation/model/types'
import type { BarChartViewModel } from '../../../shared/lib/chartViewModel'
import { countBy } from '../../../shared/lib/chartViewModel'

export function toPolicyCategoryChart(policies: OperationPolicy[]): BarChartViewModel {
  return {
    title: '정책 카테고리 분포',
    unit: '개',
    points: countBy(policies, (policy) => policy.policyCategory ?? 'UNKNOWN'),
  }
}

export function toPolicyValueTypeChart(policies: OperationPolicy[]): BarChartViewModel {
  return {
    title: '정책 값 타입 분포',
    unit: '개',
    points: countBy(policies, (policy) => policy.valueType ?? 'UNKNOWN'),
  }
}

export function toPolicyActiveChart(policies: OperationPolicy[]): BarChartViewModel {
  return {
    title: '정책 활성 상태',
    unit: '개',
    points: countBy(policies, (policy) => policy.isActive ? 'ACTIVE' : 'INACTIVE'),
  }
}
