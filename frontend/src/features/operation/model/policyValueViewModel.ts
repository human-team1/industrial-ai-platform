import type { OperationPolicy } from '../../../entities/operation/model/types'
import {
  jsonToParsedTable,
  parseStructuredText,
  toChartSeries,
  toTableRows,
  type KeyValueRecord,
  type ParsedTable,
} from '../../../shared/lib/structuredText'
import type { StructuredTextPreviewViewModel } from '../../../shared/ui/chart/StructuredTextPreview'

export function toPolicyValuePreview(policy: OperationPolicy, value: string): StructuredTextPreviewViewModel {
  const title = policy.policyName ?? policy.policyKey ?? '정책 값'
  const trimmed = value.trim()
  if (!trimmed) return { kind: 'empty', title }

  if (policy.valueType === 'NUMBER' || policy.valueType === 'BOOLEAN') {
    return { kind: 'empty', title }
  }

  const parsed = parseStructuredText(trimmed)
  if (!parsed.ok) {
    return {
      kind: 'fallback',
      title,
      reason: parsed.reason,
      text: trimmed,
    }
  }

  const table = parsed.sourceFormat === 'json'
    ? jsonToParsedTable(parsed.data)
    : parsed.data && isParsedTableOrKeyValue(parsed.data)
      ? parsed.data
      : null

  if (!table) {
    return {
      kind: 'fallback',
      title,
      reason: '객체, 배열, 표, 숫자 key-value 형식만 시각화할 수 있습니다.',
      text: trimmed,
    }
  }

  const normalizedTable = isParsedTable(table) ? toTableRows(table) : undefined
  const chart = toChartSeries(table, title)

  return {
    kind: 'parsed',
    title,
    sourceFormat: parsed.sourceFormat,
    table: normalizedTable,
    chart: chart ?? undefined,
  }
}

function isParsedTable(value: ParsedTable | KeyValueRecord): value is ParsedTable {
  return 'columns' in value && 'rows' in value
}

function isParsedTableOrKeyValue(value: unknown): value is ParsedTable | KeyValueRecord {
  return typeof value === 'object' && value !== null
}
