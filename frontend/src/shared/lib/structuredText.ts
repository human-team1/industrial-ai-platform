export type SourceFormat = 'json' | 'csv' | 'markdown-table' | 'key-value'

export type ParseResult<T> =
  | { ok: true; data: T; sourceFormat: SourceFormat }
  | { ok: false; reason: string; originalText: string }

export type ParsedCell = string | number | null

export type ParsedTable = {
  columns: string[]
  rows: Array<Record<string, ParsedCell>>
}

export type KeyValueRecord = Record<string, number>

export type ChartPoint = {
  label: string
  value: number
}

export type ChartSeries = {
  name: string
  points: ChartPoint[]
}

const MAX_LABEL_LENGTH = 40

export function parseJsonText(text: string): ParseResult<unknown> {
  const originalText = text
  const trimmed = text.trim()
  if (!trimmed) return fail('빈 문자열입니다.', originalText)

  try {
    return { ok: true, data: JSON.parse(trimmed) as unknown, sourceFormat: 'json' }
  } catch {
    return fail('JSON 형식이 아닙니다.', originalText)
  }
}

export function parseCsvText(text: string): ParseResult<ParsedTable> {
  const originalText = text
  const lines = text.trim().split(/\r?\n/).filter(Boolean)
  if (lines.length < 2) return fail('CSV는 헤더와 1개 이상의 행이 필요합니다.', originalText)

  const delimiter = lines[0].includes('\t') ? '\t' : ','
  const columns = splitDelimitedLine(lines[0], delimiter).map((column) => column.trim()).filter(Boolean)
  if (columns.length < 2) return fail('CSV 컬럼이 부족합니다.', originalText)

  const rows = lines.slice(1).map((line) => {
    const cells = splitDelimitedLine(line, delimiter)
    return columns.reduce<Record<string, ParsedCell>>((row, column, index) => {
      row[column] = parseCell(cells[index] ?? '')
      return row
    }, {})
  })

  if (rows.length === 0) return fail('CSV 행이 없습니다.', originalText)
  return { ok: true, data: { columns, rows }, sourceFormat: 'csv' }
}

export function parseMarkdownTable(text: string): ParseResult<ParsedTable> {
  const originalText = text
  const lines = text.trim().split(/\r?\n/).map((line) => line.trim()).filter(Boolean)
  if (lines.length < 3 || !lines[0].startsWith('|') || !lines[1].includes('---')) {
    return fail('Markdown table 형식이 아닙니다.', originalText)
  }

  const columns = splitMarkdownRow(lines[0]).filter(Boolean)
  if (columns.length < 2) return fail('Markdown table 컬럼이 부족합니다.', originalText)

  const rows = lines.slice(2).map((line) => {
    const cells = splitMarkdownRow(line)
    return columns.reduce<Record<string, ParsedCell>>((row, column, index) => {
      row[column] = parseCell(cells[index] ?? '')
      return row
    }, {})
  })

  if (rows.length === 0) return fail('Markdown table 행이 없습니다.', originalText)
  return { ok: true, data: { columns, rows }, sourceFormat: 'markdown-table' }
}

export function parseKeyValueText(text: string): ParseResult<KeyValueRecord> {
  const originalText = text
  const lines = text.trim().split(/\r?\n/).map((line) => line.trim()).filter(Boolean)
  if (lines.length === 0) return fail('key-value 행이 없습니다.', originalText)

  const pairs: KeyValueRecord = {}
  for (const line of lines) {
    const match = line.match(/^([^:=]+)\s*[:=]\s*(-?\d+(?:\.\d+)?)\s*%?$/)
    if (!match) return fail('숫자 key-value 형식이 아닙니다.', originalText)
    pairs[truncateLabel(match[1].trim())] = Number(match[2])
  }

  return { ok: true, data: pairs, sourceFormat: 'key-value' }
}

export function parseStructuredText(text: string): ParseResult<unknown | ParsedTable | KeyValueRecord> {
  const trimmed = text.trim()
  if (!trimmed) return fail('빈 문자열입니다.', text)

  if (looksLikeJson(trimmed)) {
    const json = parseJsonText(trimmed)
    if (json.ok) return json
  }

  if (looksLikeMarkdownTable(trimmed)) {
    const markdown = parseMarkdownTable(trimmed)
    if (markdown.ok) return markdown
  }

  if (looksLikeDelimitedTable(trimmed)) {
    const csv = parseCsvText(trimmed)
    if (csv.ok) return csv
  }

  return parseKeyValueText(trimmed)
}

export function jsonToParsedTable(value: unknown): ParsedTable | null {
  if (Array.isArray(value)) {
    const rows = value.filter(isRecord)
    if (rows.length === 0) return null
    const columns = Array.from(new Set(rows.flatMap((row) => Object.keys(row))))
    return {
      columns,
      rows: rows.map((row) => normalizeRecord(row, columns)),
    }
  }

  if (isRecord(value)) {
    return {
      columns: ['항목', '값'],
      rows: Object.entries(value).map(([key, rawValue]) => ({
        항목: truncateLabel(key),
        값: normalizeCell(rawValue),
      })),
    }
  }

  return null
}

export function toChartSeries(parsed: ParsedTable | KeyValueRecord, name = 'series'): ChartSeries | null {
  if (isParsedTable(parsed)) {
    const labelColumn = parsed.columns[0]
    const numericColumn = parsed.columns.find((column) =>
      parsed.rows.some((row) => typeof row[column] === 'number'),
    )
    if (!labelColumn || !numericColumn) return null

    const points = parsed.rows
      .map((row) => ({
        label: truncateLabel(String(row[labelColumn] ?? '-')),
        value: row[numericColumn],
      }))
      .filter((point): point is ChartPoint => typeof point.value === 'number')

    return points.length > 0 ? { name: numericColumn, points } : null
  }

  const points = Object.entries(parsed).map(([label, value]) => ({ label: truncateLabel(label), value }))
  return points.length > 0 ? { name, points } : null
}

export function toTableRows(parsed: ParsedTable): ParsedTable {
  return {
    columns: parsed.columns.map(truncateLabel),
    rows: parsed.rows.map((row) =>
      Object.fromEntries(
        Object.entries(row).map(([key, value]) => [truncateLabel(key), value]),
      ),
    ),
  }
}

function splitDelimitedLine(line: string, delimiter: string) {
  return line.split(delimiter).map((cell) => cell.trim().replace(/^"|"$/g, ''))
}

function splitMarkdownRow(line: string) {
  return line.replace(/^\|/, '').replace(/\|$/, '').split('|').map((cell) => cell.trim())
}

function parseCell(value: string): ParsedCell {
  const trimmed = value.trim()
  if (!trimmed) return null
  const normalized = trimmed.replace(/,/g, '')
  if (/^-?\d+(?:\.\d+)?%?$/.test(normalized)) return Number(normalized.replace('%', ''))
  return truncateLabel(trimmed)
}

function normalizeRecord(row: Record<string, unknown>, columns: string[]): Record<string, ParsedCell> {
  return columns.reduce<Record<string, ParsedCell>>((acc, column) => {
    acc[column] = normalizeCell(row[column])
    return acc
  }, {})
}

function normalizeCell(value: unknown): ParsedCell {
  if (value == null) return null
  if (typeof value === 'number') return Number.isFinite(value) ? value : null
  if (typeof value === 'boolean') return value ? 'true' : 'false'
  if (typeof value === 'string') return parseCell(value)
  return truncateLabel(JSON.stringify(value))
}

function looksLikeJson(text: string) {
  return (text.startsWith('{') && text.endsWith('}')) || (text.startsWith('[') && text.endsWith(']'))
}

function looksLikeMarkdownTable(text: string) {
  const lines = text.split(/\r?\n/)
  return lines.length >= 3 && lines[0].trim().startsWith('|') && lines[1].includes('---')
}

function looksLikeDelimitedTable(text: string) {
  const lines = text.split(/\r?\n/)
  return lines.length >= 2 && (lines[0].includes(',') || lines[0].includes('\t'))
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value)
}

function isParsedTable(value: ParsedTable | KeyValueRecord): value is ParsedTable {
  return 'columns' in value && 'rows' in value
}

function truncateLabel(value: string) {
  const normalized = value.replace(/\s+/g, ' ').trim()
  return normalized.length > MAX_LABEL_LENGTH ? `${normalized.slice(0, MAX_LABEL_LENGTH)}...` : normalized
}

function fail(reason: string, originalText: string): ParseResult<never> {
  return { ok: false, reason, originalText }
}
