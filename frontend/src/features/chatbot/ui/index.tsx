import type { FormEvent } from 'react'
import { useState } from 'react'
import { formatDateTime } from '../../../shared/lib/date'
import type { ChatSendStatus } from '../model/sendStatus'
import { assistantDeliveryLabel } from '../model/mapper'
import { STATIC_RECOMMENDED_CHAT_QUESTIONS } from '../model/recommendedQuestions'
import type { ChatConversationSummary, ChatMessage, ChatSource, DocumentScope } from '../types'

const roleLabel: Record<ChatMessage['role'], string> = {
  USER: '사용자',
  ASSISTANT: 'AI',
}

export function ChatInput({
  sendStatus,
  documentScope,
  onScopeChange,
  onSubmit,
  value: controlledValue,
  onChange: controlledOnChange,
}: {
  sendStatus: ChatSendStatus
  documentScope: DocumentScope
  onScopeChange: (scope: DocumentScope) => void
  onSubmit: (question: string) => void
  value?: string
  onChange?: (value: string) => void
}) {
  const [internal, setInternal] = useState('')
  const value = controlledValue !== undefined ? controlledValue : internal
  const setValue = controlledOnChange ?? setInternal
  const trimmed = value.trim()
  const pending = sendStatus === 'pending'

  const submit = (event: FormEvent) => {
    event.preventDefault()
    if (!trimmed || pending) return
    onSubmit(trimmed)
    setValue('')
  }

  return (
    <form className="space-y-3" onSubmit={submit}>
      <div className="flex flex-col gap-2 sm:flex-row sm:flex-wrap sm:items-center">
        <select
          className="control max-w-xs"
          value={documentScope}
          onChange={(event) => onScopeChange(event.target.value as DocumentScope)}
        >
          <option value="ALL">전체문서</option>
        </select>
        <div className="flex items-center gap-2 text-xs text-slate-500">
          <button
            type="button"
            disabled
            className="cursor-not-allowed rounded-md border border-slate-200 bg-slate-100 px-3 py-1.5 text-slate-400"
            aria-disabled="true"
          >
            첨부
          </button>
          <span>문서 첨부 기능은 추후 지원 예정입니다.</span>
        </div>
      </div>
      <div className="flex gap-2">
        <textarea
          className="min-h-[88px] flex-1 rounded-md border border-slate-300 bg-white px-3 py-2 text-sm outline-none focus:border-slate-500"
          maxLength={1000}
          placeholder="문서를 기반으로 궁금한 내용을 질문해 주세요."
          value={value}
          onChange={(event) => setValue(event.target.value)}
          disabled={pending}
        />
        <button className="btn-primary h-auto min-w-[84px]" disabled={!trimmed || pending} type="submit">
          전송
        </button>
      </div>
      <div className="text-right text-xs text-slate-500">{trimmed.length}/1000</div>
    </form>
  )
}

export function ChatMessageList({
  messages,
  detailLoading,
  sendStatus,
}: {
  messages: ChatMessage[]
  detailLoading: boolean
  sendStatus: ChatSendStatus
}) {
  if (detailLoading && messages.length === 0) {
    return (
      <div className="flex min-h-[260px] items-center justify-center rounded-md border border-slate-200 bg-slate-50 text-sm text-slate-600">
        대화를 불러오는 중입니다.
      </div>
    )
  }

  if (messages.length === 0 && !detailLoading) {
    return (
      <div className="flex min-h-[260px] items-center justify-center rounded-md border border-dashed border-slate-300 bg-slate-50 text-sm text-slate-600">
        아직 대화가 없습니다. 문서 기반 질문을 시작해 보세요.
      </div>
    )
  }

  const lastRole = messages.length > 0 ? messages[messages.length - 1]?.role : null
  const showTyping = sendStatus === 'pending' && lastRole === 'USER'

  return (
    <div className="space-y-3">
      {messages.map((message) => (
        <ChatMessageItem key={message.messageId} message={message} />
      ))}
      {showTyping ? (
        <div className="rounded-md border border-slate-200 bg-white p-4 text-sm text-slate-600">
          답변을 생성하는 중입니다.
        </div>
      ) : null}
    </div>
  )
}

export function ChatMessageItem({ message }: { message: ChatMessage }) {
  const isUser = message.role === 'USER'
  const isFailed = message.messageStatus === 'FAILED'
  const delivery = !isUser ? assistantDeliveryLabel(message) : ''

  const copyAnswer = async () => {
    await navigator.clipboard.writeText(message.messageText)
  }

  return (
    <article
      className={`rounded-md border p-4 ${
        isUser ? 'border-slate-300 bg-slate-900 text-white' : isFailed ? 'border-red-200 bg-red-50' : 'border-slate-200 bg-white'
      }`}
    >
      <div className="mb-2 flex items-center justify-between gap-2">
        <span className={`text-xs font-semibold ${isUser ? 'text-slate-200' : isFailed ? 'text-red-700' : 'text-slate-500'}`}>
          {roleLabel[message.role]}
          {!isUser && delivery ? ` · ${delivery}` : ''}
          {message.modelName ? ` · ${message.modelName}` : ''}
        </span>
        {!isUser && message.messageText?.trim() ? (
          <button className="btn-secondary h-8" type="button" onClick={copyAnswer}>
            복사
          </button>
        ) : null}
      </div>
      <p className="whitespace-pre-wrap text-sm leading-6">{message.messageText || (isFailed ? '(내용 없음)' : '')}</p>
      {isFailed ? <p className="mt-2 text-xs text-red-700">아래 안내 또는 재시도를 확인해 주세요.</p> : null}
      {!isUser ? <ChatSourceList sources={message.sources ?? []} /> : null}
    </article>
  )
}

export function ChatSourceList({ sources }: { sources: ChatSource[] }) {
  if (sources.length === 0) {
    return (
      <p className="mt-3 text-xs text-slate-500">
        표시할 출처가 없습니다. (RAG·AI 서버 연동 후 제공될 수 있습니다.)
      </p>
    )
  }

  return (
    <div className="mt-4 space-y-2">
      <p className="text-xs font-semibold text-slate-600">출처 문서</p>
      {sources.map((source) => (
        <div key={source.chatSourceId} className="rounded-md border border-slate-200 bg-slate-50 p-3">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <div>
              <p className="text-sm font-semibold text-slate-900">{source.documentTitle ?? '문서 출처'}</p>
              <p className="text-xs text-slate-500">
                {source.documentType ?? source.sourceType}
                {source.page ? ` · ${source.page}페이지` : ''}
                {source.section ? ` · ${source.section}` : ''}
                {source.score != null ? ` · score ${source.score}` : ''}
              </p>
            </div>
            {source.documentId ? (
              <a className="btn-secondary inline-flex items-center" href={`/documents/${source.documentId}/edit`}>
                열기
              </a>
            ) : (
              <a className="btn-secondary inline-flex items-center" href="/documents">
                문서 목록
              </a>
            )}
          </div>
          {source.sourceSnippet ? (
            <p className="mt-2 line-clamp-3 text-xs leading-5 text-slate-600">{source.sourceSnippet}</p>
          ) : null}
        </div>
      ))}
    </div>
  )
}

export function RecommendedQuestions({
  onSend,
  onFill,
  disabled,
}: {
  onSend: (question: string) => void
  onFill?: (question: string) => void
  disabled?: boolean
}) {
  return (
    <ul className="space-y-2 text-sm">
      {STATIC_RECOMMENDED_CHAT_QUESTIONS.map((question) => (
        <li key={question} className="rounded-md border border-slate-200 bg-white p-2">
          <p className="text-slate-800">{question}</p>
          <div className="mt-2 flex flex-wrap gap-2">
            <button
              className="btn-primary py-1 text-xs"
              type="button"
              disabled={disabled}
              onClick={() => onSend(question)}
            >
              바로 전송
            </button>
            {onFill ? (
              <button
                className="btn-secondary py-1 text-xs"
                type="button"
                disabled={disabled}
                onClick={() => onFill(question)}
              >
                입력창에 넣기
              </button>
            ) : null}
          </div>
        </li>
      ))}
    </ul>
  )
}

export function ChatConversationList({
  items,
  selectedId,
  loading,
  emptyMessage,
  onSelect,
  onDelete,
}: {
  items: ChatConversationSummary[]
  selectedId?: number
  loading: boolean
  emptyMessage: string
  onSelect: (conversationId: number) => void
  onDelete: (conversationId: number) => void
}) {
  if (loading) {
    return (
      <div className="page-panel flex min-h-[200px] items-center justify-center text-sm text-slate-600">
        챗봇 이력을 불러오는 중입니다.
      </div>
    )
  }

  if (items.length === 0) {
    return (
      <div className="page-panel flex min-h-[200px] items-center justify-center text-center text-sm text-slate-600">
        {emptyMessage}
      </div>
    )
  }

  return (
    <div className="space-y-3">
      {items.map((item) => (
        <div
          key={item.conversationId}
          className={`flex w-full items-stretch overflow-hidden rounded-md border transition-colors ${
            selectedId === item.conversationId ? 'border-slate-900 bg-slate-50' : 'border-slate-200 bg-white hover:border-slate-300'
          }`}
        >
          <button
            type="button"
            className="min-w-0 flex-1 px-4 py-4 text-left"
            onClick={() => onSelect(item.conversationId)}
          >
            <p className="truncate text-sm font-semibold text-slate-900">{item.title}</p>
            <p className="mt-1.5 line-clamp-2 text-xs leading-5 text-slate-600">{item.lastMessagePreview}</p>
            <p className="mt-2.5 text-xs text-slate-500">
              메시지 {item.messageCount}개 · 출처 {item.sourceCount}개 · {formatDateTime(item.updatedAt)}
            </p>
          </button>
          <div className="flex shrink-0 items-center border-l border-slate-100 px-3">
            <button
              type="button"
              className="btn-secondary inline-flex items-center justify-center whitespace-nowrap px-4"
              onClick={() => onDelete(item.conversationId)}
            >
              삭제
            </button>
          </div>
        </div>
      ))}
    </div>
  )
}

export function ChatDetailPanel({
  detail,
  loading,
}: {
  detail: { title: string; messages: ChatMessage[] } | null
  loading: boolean
}) {
  if (loading) {
    return (
      <div className="page-panel flex min-h-[360px] flex-col items-center justify-center text-center text-sm text-slate-600">
        대화 상세를 불러오는 중입니다.
      </div>
    )
  }
  if (!detail) {
    return (
      <div className="page-panel flex min-h-[360px] flex-col items-center justify-center px-6 text-center text-sm leading-relaxed text-slate-600">
        왼쪽 목록에서 대화를 선택해 주세요.
      </div>
    )
  }
  return (
    <div className="page-panel flex min-h-[360px] flex-col space-y-4">
      <h2 className="text-lg font-semibold text-slate-900">{detail.title}</h2>
      <div className="min-h-0 flex-1">
        <ChatMessageList messages={detail.messages} detailLoading={false} sendStatus="idle" />
      </div>
    </div>
  )
}

