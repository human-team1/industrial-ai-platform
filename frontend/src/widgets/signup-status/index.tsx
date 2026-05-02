import { SignupStatusCard } from '../../features/auth/ui'
import type { SignupStatus } from '../../features/auth/types/signupStatus'

type Props = {
  status: SignupStatus
}

export function SignupStatusWidget({ status }: Props) {
  return (
    <div className="min-h-screen flex items-center justify-center bg-[#f8f9fa]">
      <SignupStatusCard status={status} />
    </div>
  )
}
