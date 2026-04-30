import { apiClient, normalizeApiError } from '../../../shared/api/client'
import type {
  AskChatRequest,
  AskChatResponse,
  ChatConversationDetail,
  ChatConversationPage,
  ChatConversationResult,
  ChatHistoryQuery,
  ChatMessage,
  ChatSource,
} from '../types'

type ApiResponse<T> = {
  success: boolean
  data: T
  message?: string
}

export async function createChatConversation(title?: string): Promise<ChatConversationResult> {
  try {
    const response = await apiClient.post<ApiResponse<ChatConversationResult>>('/chat-conversations', {
      title,
    })
    return response.data.data
  } catch (error) {
    throw normalizeApiError(error)
  }
}

export async function askChat(conversationId: number, body: AskChatRequest): Promise<AskChatResponse> {
  try {
    const response = await apiClient.post<ApiResponse<AskChatResponse>>(
      `/chat-conversations/${conversationId}/messages`,
      body,
    )
    return response.data.data
  } catch (error) {
    throw normalizeApiError(error)
  }
}

export async function getChatConversations(
  query: ChatHistoryQuery,
  signal?: AbortSignal,
): Promise<ChatConversationPage> {
  try {
    const response = await apiClient.get<ApiResponse<ChatConversationPage>>('/chat-conversations', {
      params: query,
      signal,
    })
    return response.data.data
  } catch (error) {
    throw normalizeApiError(error)
  }
}

export async function getChatConversation(conversationId: number): Promise<ChatConversationDetail> {
  try {
    const response = await apiClient.get<ApiResponse<ChatConversationDetail>>(
      `/chat-conversations/${conversationId}`,
    )
    return response.data.data
  } catch (error) {
    throw normalizeApiError(error)
  }
}

export async function getChatMessages(conversationId: number): Promise<ChatMessage[]> {
  try {
    const response = await apiClient.get<ApiResponse<ChatMessage[]>>(
      `/chat-conversations/${conversationId}/messages`,
    )
    return response.data.data
  } catch (error) {
    throw normalizeApiError(error)
  }
}

export async function getChatMessageSources(messageId: number): Promise<ChatSource[]> {
  try {
    const response = await apiClient.get<ApiResponse<ChatSource[]>>(`/chat-messages/${messageId}/sources`)
    return response.data.data
  } catch (error) {
    throw normalizeApiError(error)
  }
}

export async function deleteChatConversation(conversationId: number): Promise<void> {
  try {
    await apiClient.delete(`/chat-conversations/${conversationId}`)
  } catch (error) {
    throw normalizeApiError(error)
  }
}
