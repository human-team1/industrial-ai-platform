import { useCallback, useEffect, useRef, useState } from 'react'
import {
  askChat,
  createChatConversation,
  getChatConversation,
  getChatMessages,
} from '../api'
import { chatSendFailureMessage } from '../lib/chatSendErrors'
import type { ChatSendStatus } from './sendStatus'
import type { ChatMessage, DocumentScope } from '../../../entities/chatbot/model/types'

type RetrySnapshot = {
  pendingTempMessageId: number
  conversationId: number | null
  text: string
}

export function useChatbot(initialConversationId?: number | null, resultId?: number | null) {
  const [conversationId, setConversationId] = useState<number | null>(initialConversationId ?? null)
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [loading, setLoading] = useState(false)
  const [detailLoading, setDetailLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [sendStatus, setSendStatus] = useState<ChatSendStatus>('idle')
  const [documentScope, setDocumentScope] = useState<DocumentScope>('ALL')
  const retryRef = useRef<RetrySnapshot | null>(null)

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

  const runAsk = useCallback(
    async (
      trimmed: string,
      tempId: number,
      existingUserBubble: boolean,
      overrideConversationId?: number | null,
    ) => {
      let cid: number | null =
        overrideConversationId !== undefined ? overrideConversationId : conversationId
      try {
        setLoading(true)
        setSendStatus('pending')
        setError(null)
        if (cid == null) {
          const created = await createChatConversation(trimmed)
          cid = created.conversationId
          setConversationId(cid)
        }
        const result = await askChat(cid, {
          messageText: trimmed,
          context: {
            documentScope,
            documentIds: [],
            resultId: resultId ?? null,
          },
        })
        setConversationId(result.conversationId)
        setMessages((prev) => [
          ...prev.filter((m) => m.messageId !== tempId),
          result.userMessage,
          result.assistantMessage,
        ])
        setSendStatus('success')
        retryRef.current = null
        queueMicrotask(() => setSendStatus('idle'))
      } catch (err) {
        setSendStatus('failed')
        setError(chatSendFailureMessage(err))
        if (!existingUserBubble) {
          const tempUser: ChatMessage = {
            messageId: tempId,
            role: 'USER',
            messageText: trimmed,
            messageStatus: 'SUCCESS',
            createdAt: new Date().toISOString(),
            sources: [],
          }
          setMessages((prev) => [...prev.filter((m) => m.messageId !== tempId), tempUser])
        }
        retryRef.current = { pendingTempMessageId: tempId, conversationId: cid, text: trimmed }
      } finally {
        setLoading(false)
      }
    },
    [conversationId, documentScope, resultId],
  )

  const sendQuestion = useCallback(
    async (question: string) => {
      const trimmed = question.trim()
      if (!trimmed || loading || sendStatus === 'pending') return

      const tempId = -Math.abs(Date.now())
      setMessages((prev) => [
        ...prev,
        {
          messageId: tempId,
          role: 'USER',
          messageText: trimmed,
          messageStatus: 'SUCCESS',
          createdAt: new Date().toISOString(),
          sources: [],
        },
      ])
      await runAsk(trimmed, tempId, false)
    },
    [loading, runAsk, sendStatus],
  )

  const retryLastQuestion = useCallback(async () => {
    const snap = retryRef.current
    if (!snap || loading || sendStatus === 'pending') return
    await runAsk(snap.text, snap.pendingTempMessageId, true, snap.conversationId)
  }, [loading, runAsk, sendStatus])

  const startNew = useCallback(() => {
    retryRef.current = null
    setConversationId(null)
    setMessages([])
    setError(null)
    setSendStatus('idle')
  }, [])

  return {
    conversationId,
    messages,
    loading,
    sendStatus,
    detailLoading,
    error,
    documentScope,
    setDocumentScope,
    sendQuestion,
    retryLastQuestion,
    loadConversation,
    startNew,
  }
}
