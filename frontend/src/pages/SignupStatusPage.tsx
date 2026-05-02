import type { SignupStatus } from '../features/auth/types/signupStatus'
import { SignupStatusWidget } from '../widgets/signup-status'

type Props = {
  status: SignupStatus
}

export function SignupStatusPage({ status }: Props) {
  return <SignupStatusWidget status={status} />
}
