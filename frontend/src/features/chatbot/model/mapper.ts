import type { ChatMessage } from '../types'

export function assistantDeliveryLabel(message: ChatMessage): string {
  if (message.role !== 'ASSISTANT') return ''
  if (message.messageStatus === 'FAILED') return '실패'
  if (message.answerStatus === 'NO_RELEVANT_SOURCE') return '답변 없음'
  if (message.answerStatus && message.answerStatus !== 'ANSWERED') return '실패'
  if (!message.messageText?.trim()) return '답변 없음'
  return '성공'
}
