import { useSearchParams } from 'react-router-dom'
import { ChatbotWidget } from '../widgets/chatbot/ChatbotWidget'

export function ChatbotPage() {
  const [searchParams] = useSearchParams()
  const conversationId = Number(searchParams.get('conversationId'))
  const resultId = Number(searchParams.get('resultId'))

  return (
    <ChatbotWidget
      initialConversationId={Number.isFinite(conversationId) && conversationId > 0 ? conversationId : null}
      resultId={Number.isFinite(resultId) && resultId > 0 ? resultId : null}
    />
  )
}
