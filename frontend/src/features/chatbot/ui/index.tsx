import type { FormEvent } from 'react'
import { useState } from 'react'
import type { ChatConversationSummary, ChatMessage, ChatSource, DocumentScope } from '../types'

const roleLabel: Record<ChatMessage['role'], string> = {
  USER: '사용자',
  ASSISTANT: 'AI',
}

export function ChatInput({
  loading,
  documentScope,
  onScopeChange,
  onSubmit,
}: {
  loading: boolean
  documentScope: DocumentScope
  onScopeChange: (scope: DocumentScope) => void
  onSubmit: (question: string) => void
}) {
  const [value, setValue] = useState('')
  const trimmed = value.trim()

  const submit = (event: FormEvent) => {
    event.preventDefault()
    if (!trimmed || loading) return
    onSubmit(trimmed)
    setValue('')
  }

  return (
    <form className="space-y-3" onSubmit={submit}>
      <div className="flex flex-wrap items-center gap-2">
        <select
          className="control"
          value={documentScope}
          onChange={(event) => onScopeChange(event.target.value as DocumentScope)}
        >
          <option value="ALL">전체문서</option>
        </select>
        <button className="btn-secondary" type="button" disabled title="첨부 기능은 문서 업로드 화면을 사용합니다.">
          첨부
        </button>
      </div>
      <div className="flex gap-2">
        <textarea
          className="min-h-[88px] flex-1 rounded-md border border-slate-300 bg-white px-3 py-2 text-sm outline-none focus:border-slate-500"
          maxLength={1000}
          placeholder="문서를 기반으로 궁금한 내용을 질문해 주세요."
          value={value}
          onChange={(event) => setValue(event.target.value)}
        />
        <button className="btn-primary h-auto min-w-[84px]" disabled={!trimmed || loading} type="submit">
          전송
        </button>
      </div>
      <div className="text-right text-xs text-slate-500">{trimmed.length}/1000</div>
    </form>
  )
}

export function ChatMessageList({ messages, loading }: { messages: ChatMessage[]; loading: boolean }) {
  if (messages.length === 0 && !loading) {
    return (
      <div className="flex min-h-[260px] items-center justify-center rounded-md border border-dashed border-slate-300 bg-slate-50 text-sm text-slate-600">
        아직 대화가 없습니다. 문서 기반 질문을 시작해 보세요.
      </div>
    )
  }

  return (
    <div className="space-y-3">
      {messages.map((message) => (
        <ChatMessageItem key={message.messageId} message={message} />
      ))}
      {loading ? (
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
  const isNoSource = message.answerStatus === 'NO_RELEVANT_SOURCE'

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
          {isFailed ? ' · 실패' : ''}
          {isNoSource ? ' · 검색 결과 없음' : ''}
          {message.modelName ? ` · ${message.modelName}` : ''}
        </span>
        {!isUser ? (
          <button className="btn-secondary h-8" type="button" onClick={copyAnswer}>
            복사
          </button>
        ) : null}
      </div>
      <p className="whitespace-pre-wrap text-sm leading-6">{message.messageText}</p>
      {isFailed ? <p className="mt-2 text-xs text-red-700">재시도하거나 질문을 더 구체적으로 입력해 주세요.</p> : null}
      {!isUser ? <ChatSourceList sources={message.sources ?? []} /> : null}
    </article>
  )
}

export function ChatSourceList({ sources }: { sources: ChatSource[] }) {
  if (sources.length === 0) {
    return <p className="mt-3 text-xs text-slate-500">참조 가능한 문서를 찾지 못했습니다.</p>
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

export function RecommendedQuestions({ onPick }: { onPick: (question: string) => void }) {
  const questions = [
    '모터 과부하 경보 발생 시 조치 절차는?',
    '베어링 교체 주기 기준은 어떻게 되나요?',
    '문서 업로드 방법을 알려줘',
    '정기 점검 체크리스트는 어디서 확인하나요?',
  ]

  return (
    <div className="flex flex-wrap gap-2">
      {questions.map((question) => (
        <button key={question} className="btn-secondary h-auto py-2 text-left" type="button" onClick={() => onPick(question)}>
          {question}
        </button>
      ))}
    </div>
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
    return <div className="page-panel text-sm text-slate-600">챗봇 이력을 불러오는 중입니다.</div>
  }

  if (items.length === 0) {
    return <div className="page-panel text-sm text-slate-600">{emptyMessage}</div>
  }

  return (
    <div className="space-y-2">
      {items.map((item) => (
        <button
          key={item.conversationId}
          className={`w-full rounded-md border p-4 text-left ${
            selectedId === item.conversationId ? 'border-slate-900 bg-slate-50' : 'border-slate-200 bg-white'
          }`}
          type="button"
          onClick={() => onSelect(item.conversationId)}
        >
          <div className="flex items-start justify-between gap-3">
            <div className="min-w-0">
              <p className="truncate text-sm font-semibold text-slate-900">{item.title}</p>
              <p className="mt-1 line-clamp-2 text-xs text-slate-600">{item.lastMessagePreview}</p>
              <p className="mt-2 text-xs text-slate-500">
                메시지 {item.messageCount}개 · 출처 {item.sourceCount}개 · {formatDateTime(item.updatedAt)}
              </p>
            </div>
            <span
              className="btn-secondary inline-flex items-center"
              role="button"
              tabIndex={0}
              onClick={(event) => {
                event.stopPropagation()
                onDelete(item.conversationId)
              }}
              onKeyDown={(event) => {
                if (event.key === 'Enter') {
                  event.stopPropagation()
                  onDelete(item.conversationId)
                }
              }}
            >
              삭제
            </span>
          </div>
        </button>
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
    return <div className="page-panel text-sm text-slate-600">대화 상세를 불러오는 중입니다.</div>
  }
  if (!detail) {
    return <div className="page-panel text-sm text-slate-600">왼쪽 목록에서 대화를 선택해 주세요.</div>
  }
  return (
    <div className="page-panel space-y-4">
      <h2 className="text-lg font-semibold text-slate-900">{detail.title}</h2>
      <ChatMessageList messages={detail.messages} loading={false} />
    </div>
  )
}

export function formatDateTime(value: string) {
  if (!value) return '-'
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value))
}
