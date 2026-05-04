export type ResultListItemViewModel = {
  resultId: number
  inspectionId: number
  inspectedAt: string
  targetLine: string
  runTypeLabel: string
  decisionCode: string | null | undefined
  finalDecisionCode: string | null | undefined
  score: number | null | undefined
  statusLabel: string
}
