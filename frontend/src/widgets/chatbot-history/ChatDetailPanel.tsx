import { formatDateTime } from '../../shared/lib/date'
import type { ChatMessage } from '../../entities/chatbot/model/types'
import type { ChatDetailPanelProps } from './types'

const ROLE_LABEL: Record<ChatMessage['role'], string> = {
  USER: '사용자',
  ASSISTANT: 'AI',
}

export function ChatDetailPanel({ detail, loading, onOpenInChatbot }: ChatDetailPanelProps) {
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
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <h2 className="truncate text-lg font-semibold text-slate-900">{detail.title}</h2>
          <p className="mt-1 text-xs text-slate-500">{formatDateTime(detail.updatedAt)}</p>
        </div>
        {onOpenInChatbot ? (
          <button
            type="button"
            className="btn-secondary inline-flex shrink-0 items-center justify-center whitespace-nowrap px-4"
            onClick={() => onOpenInChatbot(detail.conversationId)}
          >
            대화 열기
          </button>
        ) : null}
      </div>

      <div className="min-h-0 flex-1 space-y-3 overflow-y-auto">
        {detail.messages.length === 0 ? (
          <div className="flex min-h-[200px] items-center justify-center rounded-md border border-dashed border-slate-300 bg-slate-50 text-sm text-slate-600">
            메시지가 없습니다.
          </div>
        ) : (
          detail.messages.map((message) => (
            <article
              key={message.messageId}
              className={`rounded-md border p-4 ${
                message.role === 'USER'
                  ? 'border-slate-300 bg-slate-900 text-white'
                  : message.messageStatus === 'FAILED'
                    ? 'border-red-200 bg-red-50'
                    : 'border-slate-200 bg-white'
              }`}
            >
              <div className="mb-2 flex items-center justify-between gap-2">
                <span
                  className={`text-xs font-semibold ${
                    message.role === 'USER'
                      ? 'text-slate-200'
                      : message.messageStatus === 'FAILED'
                        ? 'text-red-700'
                        : 'text-slate-500'
                  }`}
                >
                  {ROLE_LABEL[message.role]}
                  {message.modelName ? ` · ${message.modelName}` : ''}
                </span>
                <span
                  className={`text-[11px] ${
                    message.role === 'USER' ? 'text-slate-300' : 'text-slate-400'
                  }`}
                >
                  {formatDateTime(message.createdAt)}
                </span>
              </div>
              <p className="whitespace-pre-wrap text-sm leading-6">
                {message.messageText || (message.messageStatus === 'FAILED' ? '(내용 없음)' : '')}
              </p>
            </article>
          ))
        )}
      </div>
    </div>
  )
}
