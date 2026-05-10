export type ChatRole = 'USER' | 'ASSISTANT'

export type DocumentScope = 'ALL' | 'SELECTED'

export type ChatMessageStatus = 'SUCCESS' | 'FAILED'

export type ChatAnswerStatus =
  | 'ANSWERED'
  | 'NO_RELEVANT_SOURCE'
  | 'LLM_FAILED'
  | 'VECTOR_STORE_FAILED'
  | 'DOCUMENT_SCOPE_FORBIDDEN'
  | 'VALIDATION_FAILED'

export interface ChatSource {
  chatSourceId: number
  messageId?: number
  sourceType: string
  sourceId?: number | null
  documentId?: number | null
  documentTitle?: string | null
  documentType?: string | null
  chunkId?: number | null
  page?: number | null
  section?: string | null
  sourceSnippet?: string | null
  score?: number | null
  createdAt?: string
}

export interface ChatMessage {
  messageId: number
  conversationId?: number
  role: ChatRole
  messageText: string
  messageStatus?: ChatMessageStatus
  answerStatus?: ChatAnswerStatus | null
  errorCode?: string | null
  modelName?: string | null
  createdAt: string
  updatedAt?: string | null
  sources?: ChatSource[]
}

export interface ChatConversationSummary {
  conversationId: number
  title: string
  lastMessagePreview: string
  messageCount: number
  sourceCount: number
  createdAt: string
  updatedAt: string
}

export interface ChatConversationDetail {
  conversationId: number
  title: string
  createdAt: string
  updatedAt: string
  messages: ChatMessage[]
}
