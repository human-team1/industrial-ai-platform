import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  askChat,
  createChatConversation,
  deleteChatConversation,
  getChatConversation,
  getChatConversations,
  getChatMessages,
} from '../api'
import type {
  ChatConversationDetail,
  ChatConversationPage,
  ChatHistoryQuery,
  ChatMessage,
  DocumentScope,
} from '../types'

const DEFAULT_HISTORY_QUERY: ChatHistoryQuery = { page: 0, size: 10 }

export function useChatbot(initialConversationId?: number | null) {
  const [conversationId, setConversationId] = useState<number | null>(initialConversationId ?? null)
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [loading, setLoading] = useState(false)
  const [detailLoading, setDetailLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [documentScope, setDocumentScope] = useState<DocumentScope>('ALL')

  const loadConversation = useCallback(async (targetId: number) => {
    try {
      setDetailLoading(true)
      setError(null)
      const detail = await getChatConversation(targetId)
      const loadedMessages = detail.messages?.length ? detail.messages : await getChatMessages(targetId)
      setConversationId(detail.conversationId)
      setMessages(loadedMessages)
    } catch (err) {
      setError(err instanceof Error ? err.message : '챗봇 대화를 불러오지 못했습니다.')
    } finally {
      setDetailLoading(false)
    }
  }, [])

  useEffect(() => {
    if (initialConversationId && initialConversationId > 0) {
      void loadConversation(initialConversationId)
    }
  }, [initialConversationId, loadConversation])

  const sendQuestion = useCallback(
    async (question: string) => {
      const trimmed = question.trim()
      if (!trimmed || loading) return

      const tempUser: ChatMessage = {
        messageId: Date.now() * -1,
        role: 'USER',
        messageText: trimmed,
        messageStatus: 'SUCCESS',
        createdAt: new Date().toISOString(),
        sources: [],
      }
      setMessages((prev) => [...prev, tempUser])

      try {
        setLoading(true)
        setError(null)
        const targetConversationId = conversationId ?? (await createChatConversation(trimmed)).conversationId
        const result = await askChat(targetConversationId, {
          messageText: trimmed,
          context: {
            documentScope,
            documentIds: [],
            resultId: null,
          },
        })
        setConversationId(result.conversationId)
        setMessages((prev) => [
          ...prev.filter((message) => message.messageId !== tempUser.messageId),
          result.userMessage,
          result.assistantMessage,
        ])
      } catch (err) {
        setError(err instanceof Error ? err.message : '챗봇 답변 생성에 실패했습니다.')
        setMessages((prev) => [
          ...prev.filter((message) => message.messageId !== tempUser.messageId),
          tempUser,
          {
            messageId: Date.now() * -1 - 1,
            role: 'ASSISTANT',
            messageText: '답변을 생성하지 못했습니다. 잠시 후 다시 시도해 주세요.',
            messageStatus: 'FAILED',
            answerStatus: 'LLM_FAILED',
            createdAt: new Date().toISOString(),
            sources: [],
          },
        ])
      } finally {
        setLoading(false)
      }
    },
    [conversationId, documentScope, loading],
  )

  const startNew = useCallback(() => {
    setConversationId(null)
    setMessages([])
    setError(null)
  }, [])

  return {
    conversationId,
    messages,
    loading,
    detailLoading,
    error,
    documentScope,
    setDocumentScope,
    sendQuestion,
    loadConversation,
    startNew,
  }
}

export function useChatHistory() {
  const [query, setQuery] = useState<ChatHistoryQuery>(DEFAULT_HISTORY_QUERY)
  const [data, setData] = useState<ChatConversationPage | null>(null)
  const [selected, setSelected] = useState<ChatConversationDetail | null>(null)
  const [loading, setLoading] = useState(false)
  const [detailLoading, setDetailLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const load = useCallback(async () => {
    try {
      setLoading(true)
      setError(null)
      setData(await getChatConversations(query))
    } catch (err) {
      setError(err instanceof Error ? err.message : '챗봇 이력을 불러오지 못했습니다.')
    } finally {
      setLoading(false)
    }
  }, [query])

  useEffect(() => {
    void load()
  }, [load])

  const selectConversation = useCallback(async (targetConversationId: number) => {
    try {
      setDetailLoading(true)
      setError(null)
      const detail = await getChatConversation(targetConversationId)
      const messages = detail.messages?.length ? detail.messages : await getChatMessages(targetConversationId)
      setSelected({ ...detail, messages })
    } catch (err) {
      setError(err instanceof Error ? err.message : '챗봇 대화 상세를 불러오지 못했습니다.')
    } finally {
      setDetailLoading(false)
    }
  }, [])

  const removeConversation = useCallback(
    async (targetConversationId: number) => {
      await deleteChatConversation(targetConversationId)
      if (selected?.conversationId === targetConversationId) {
        setSelected(null)
      }
      await load()
    },
    [load, selected?.conversationId],
  )

  const isSearchResultEmpty = useMemo(
    () => Boolean((query.keyword || query.from || query.to) && data && data.content.length === 0),
    [data, query],
  )

  return {
    query,
    setQuery,
    data,
    selected,
    loading,
    detailLoading,
    error,
    reload: load,
    selectConversation,
    removeConversation,
    isSearchResultEmpty,
  }
}
