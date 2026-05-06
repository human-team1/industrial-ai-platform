import type {
  ChatConversationDetail,
  ChatConversationSummary,
} from '../../entities/chatbot/model/types'

export type ChatHistorySummaryView = {
  /** 조회 조건 전체 대화 수 (data.totalElements) */
  totalConversations: number
  /** 즐겨찾기 수 (백엔드 미지원 — placeholder 0) */
  favoriteCount: number
  /** 가장 최근 활동 시각 (현재 페이지 기준) */
  lastActivityAt: string | null
  /** 현재 페이지 누적 메시지 수 */
  pageMessageCount: number
  /** 현재 페이지 누적 관련 문서 수 (sourceCount 합계) */
  pageRelatedSourceCount: number
}

export type ChatHistoryRecentItem = {
  conversationId: number
  title: string
  updatedAt: string
}

export type ChatHistoryHeaderProps = {
  onStartNew: () => void
}

export type ChatHistoryFilterBarProps = {
  keyword: string
  from: string
  to: string
  onKeywordChange: (value: string) => void
  onFromChange: (value: string) => void
  onToChange: (value: string) => void
  onSearch?: () => void
}

export type ChatConversationListProps = {
  items: ChatConversationSummary[]
  selectedId?: number
  loading: boolean
  emptyMessage: string
  onSelect: (conversationId: number) => void
}

export type ChatDetailPanelProps = {
  detail: ChatConversationDetail | null
  loading: boolean
  onOpenInChatbot?: (conversationId: number) => void
}

export type ChatHistorySidebarProps = {
  summary: ChatHistorySummaryView | null
  recent: ChatHistoryRecentItem[]
}
