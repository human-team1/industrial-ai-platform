import {
  formatDateMinute,
  formatResultStatus,
  labelRunType,
  labelSourceType,
} from '../../../entities/result/model/labels'
import type { ResultSummary } from '../api/types'
import type { ResultListItemViewModel } from './types'

export function mapResultListRow(item: ResultSummary): ResultListItemViewModel {
  const targetLine =
    [item.equipmentName, item.targetName ?? item.location].filter(Boolean).join(' / ') || '-'
  const sourceTypeLabel = labelSourceType(item.sourceType)

  return {
    resultId: item.resultId,
    inspectionId: item.inspectionId,
    inspectedAt: formatDateMinute(item.startedAt ?? item.createdAt),
    targetLine,
    runTypeLabel: `${labelRunType(item.runType)} · ${sourceTypeLabel}`,
    decisionCode: item.decisionCode,
    finalDecisionCode: item.finalDecisionCode,
    score: item.score,
    statusLabel: formatResultStatus(item.resultStatus),
  }
}

export {
  formatDateMinute,
  formatResultStatus,
  labelRunType,
  labelSourceType,
} from '../../../entities/result/model/labels'
export { labelDecision } from '../../../entities/result/model/labels'
