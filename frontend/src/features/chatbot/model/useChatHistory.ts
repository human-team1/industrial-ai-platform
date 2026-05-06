import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import {
  deleteChatConversation,
  getChatConversation,
  getChatConversations,
  getChatMessages,
} from '../api'
import type { ChatConversationPage, ChatHistoryQuery } from '../api/types'
import type { ChatConversationDetail } from '../../../entities/chatbot/model/types'

const DEFAULT_HISTORY_QUERY: ChatHistoryQuery = { page: 0, size: 20 }

export function useChatHistory() {
  const [query, setQuery] = useState<ChatHistoryQuery>(DEFAULT_HISTORY_QUERY)
  const [data, setData] = useState<ChatConversationPage | null>(null)
  const [selected, setSelected] = useState<ChatConversationDetail | null>(null)
  const [loading, setLoading] = useState(false)
  const [detailLoading, setDetailLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const requestGeneration = useRef(0)

  const load = useCallback(async (signal?: AbortSignal) => {
    const gen = ++requestGeneration.current
    try {
      setLoading(true)
      setError(null)
      const page = await getChatConversations(query, signal)
      if (gen !== requestGeneration.current || signal?.aborted) return
      setData(page)
    } catch (err) {
      if (signal?.aborted) return
      setError(err instanceof Error ? err.message : '챗봇 이력을 불러오지 못했습니다.')
    } finally {
      if (gen === requestGeneration.current) {
        setLoading(false)
      }
    }
  }, [query])

  useEffect(() => {
    const controller = new AbortController()
    void load(controller.signal)
    return () => controller.abort()
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
      try {
        await deleteChatConversation(targetConversationId)
        if (selected?.conversationId === targetConversationId) {
          setSelected(null)
        }
        await load()
      } catch (err) {
        window.alert(err instanceof Error ? err.message : '대화를 삭제하지 못했습니다.')
      }
    },
    [load, selected?.conversationId],
  )

  const reload = useCallback(() => {
    const controller = new AbortController()
    return load(controller.signal)
  }, [load])

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
    reload,
    selectConversation,
    removeConversation,
    isSearchResultEmpty,
  }
}
