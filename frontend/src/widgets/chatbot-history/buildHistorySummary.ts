import type { ChatConversationSummary } from '../../entities/chatbot/model/types'
import type { ChatHistoryRecentItem, ChatHistorySummaryView } from './types'

export function buildHistorySummary(
  items: ChatConversationSummary[],
  totalElements: number,
): ChatHistorySummaryView {
  const pageMessageCount = items.reduce((acc, item) => acc + (item.messageCount ?? 0), 0)
  const pageRelatedSourceCount = items.reduce((acc, item) => acc + (item.sourceCount ?? 0), 0)
  const lastActivityAt = items.reduce<string | null>((latest, item) => {
    if (!item.updatedAt) return latest
    if (!latest) return item.updatedAt
    return Date.parse(item.updatedAt) > Date.parse(latest) ? item.updatedAt : latest
  }, null)

  return {
    totalConversations: totalElements,
    favoriteCount: 0,
    lastActivityAt,
    pageMessageCount,
    pageRelatedSourceCount,
  }
}

export function buildRecentItems(
  items: ChatConversationSummary[],
  limit = 3,
): ChatHistoryRecentItem[] {
  return items.slice(0, limit).map((item) => ({
    conversationId: item.conversationId,
    title: item.title,
    updatedAt: item.updatedAt,
  }))
}
