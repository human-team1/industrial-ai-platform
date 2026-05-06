export type EventLogItem = {
  eventId?: number
  eventType: string
  message: string
  createdAt: string
}

export type ResultChecklistItem = {
  title: string
  description?: string | null
  priority: 'REQUIRED' | 'RECOMMENDED' | 'OPTIONAL' | string
}

export type RelatedResult = {
  resultId: number
  createdAt?: string | null
  score?: number | null
  decisionCode?: string | null
  location?: string | null
}

export type ResultDescriptionView = {
  summary?: string
  cause?: string
  action?: string
}
