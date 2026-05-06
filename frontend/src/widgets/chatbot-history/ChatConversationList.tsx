import { formatDateTime } from '../../shared/lib/date'
import type { ChatConversationListProps } from './types'

export function ChatConversationList({
  items,
  selectedId,
  loading,
  emptyMessage,
  onSelect,
}: ChatConversationListProps) {
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
        <button
          key={item.conversationId}
          type="button"
          className={`flex w-full flex-col gap-1.5 overflow-hidden rounded-md border px-4 py-4 text-left transition-colors ${
            selectedId === item.conversationId
              ? 'border-slate-900 bg-slate-50'
              : 'border-slate-200 bg-white hover:border-slate-300'
          }`}
          onClick={() => onSelect(item.conversationId)}
        >
          <p className="truncate text-sm font-semibold text-slate-900">{item.title}</p>
          <p className="line-clamp-2 text-xs leading-5 text-slate-600">{item.lastMessagePreview}</p>
          <p className="mt-1 text-xs text-slate-500">
            메시지 {item.messageCount}개 · 출처 {item.sourceCount}개 · {formatDateTime(item.updatedAt)}
          </p>
        </button>
      ))}
    </div>
  )
}
