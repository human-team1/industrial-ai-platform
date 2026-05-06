import type { ChatMessage } from '../../../entities/chatbot/model/types'
import type { DocumentScope } from '../../../entities/chatbot/model/types'

export interface AskChatRequest {
  messageText: string
  context: {
    resultId?: number | null
    documentIds?: number[]
    documentScope?: DocumentScope
  }
}

export interface AskChatResponse {
  conversationId: number
  userMessage: ChatMessage
  assistantMessage: ChatMessage
}

export interface ChatConversationResult {
  conversationId: number
  title: string
  createdAt: string
  updatedAt: string
}

export interface ChatConversationPage {
  content: import('../../../entities/chatbot/model/types').ChatConversationSummary[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface ChatHistoryQuery {
  page: number
  size: number
  keyword?: string
  from?: string
  to?: string
}
