import { useSearchParams } from 'react-router-dom'
import { ChatbotWidget } from '../widgets/chatbot/ChatbotWidget'

export function ChatbotPage() {
  const [searchParams] = useSearchParams()
  const conversationId = Number(searchParams.get('conversationId'))

  return <ChatbotWidget initialConversationId={Number.isFinite(conversationId) && conversationId > 0 ? conversationId : null} />
}
