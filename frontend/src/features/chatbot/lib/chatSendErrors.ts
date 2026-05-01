import { AxiosError } from 'axios'
import { normalizeApiError } from '../../../shared/api/client'

/** Spring만 동작하고 AI(FastAPI) 미기동 등으로 인한 오류에 대한 안내 */
export function chatSendFailureMessage(error: unknown): string {
  if (error instanceof AxiosError) {
    const status = error.response?.status
    if (status === 500 || status === 502 || status === 503 || status === 504) {
      return '현재 답변 생성 서버가 준비되지 않았습니다. 잠시 후 다시 시도해 주세요.'
    }
  }
  return normalizeApiError(error).message
}
