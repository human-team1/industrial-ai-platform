import { useNavigate } from 'react-router-dom'
import type { SignupStatus } from '../types/signupStatus'

type StatusConfig = {
  icon: JSX.Element
  title: string
  description: JSX.Element
  badgeClass: string
  badgeText: string
}

const STATUS_CONFIG: Record<SignupStatus, StatusConfig> = {
  pending: {
    icon: (
      <svg className="w-14 h-14 text-[#1166e0]" fill="none" viewBox="0 0 56 56" stroke="currentColor" strokeWidth={1.3}>
        <circle cx="28" cy="28" r="24" />
        <path strokeLinecap="round" d="M28 16v12l7 5" />
      </svg>
    ),
    title: '가입 승인 대기 중',
    description: (
      <>
        가입 신청이 접수되었습니다.<br />
        관리자 승인 후 서비스를 이용하실 수 있습니다.<br />
        승인 완료 시 등록하신 이메일로 안내드립니다.
      </>
    ),
    badgeClass: 'bg-blue-50 text-[#1166e0] border border-blue-100',
    badgeText: 'PENDING',
  },
  rejected: {
    icon: (
      <svg className="w-14 h-14 text-[#dc3545]" fill="none" viewBox="0 0 56 56" stroke="currentColor" strokeWidth={1.3}>
        <circle cx="28" cy="28" r="24" />
        <path strokeLinecap="round" strokeLinejoin="round" d="M20 20l16 16M36 20L20 36" />
      </svg>
    ),
    title: '가입이 거절되었습니다',
    description: (
      <>
        가입 신청이 승인되지 않았습니다.<br />
        자세한 사유는 관리자에게 문의해주세요.
      </>
    ),
    badgeClass: 'bg-red-50 text-[#dc3545] border border-red-100',
    badgeText: 'REJECTED',
  },
}

type Props = {
  status: SignupStatus
}

export function SignupStatusCard({ status }: Props) {
  const navigate = useNavigate()
  const { icon, title, description, badgeClass, badgeText } = STATUS_CONFIG[status]

  return (
    <div className="bg-white rounded-xl shadow-[0_4px_24px_rgba(0,0,0,0.08)] px-10 py-12 w-full max-w-[400px] flex flex-col items-center text-center">
      <div className="mb-5">{icon}</div>

      <span className={`text-[11px] font-medium px-2.5 py-0.5 rounded-full mb-3 ${badgeClass}`}>
        {badgeText}
      </span>

      <h2 className="text-[#1f2937] text-xl font-bold mb-3">{title}</h2>

      <p className="text-[#6c757d] text-sm leading-relaxed mb-8">{description}</p>

      <button
        type="button"
        onClick={() => navigate('/auth')}
        className="px-6 py-2.5 bg-transparent text-[#6c757d] border border-[#dee2e6] rounded-md text-sm hover:bg-[#f8f9fa] transition-colors"
      >
        로그인 화면으로
      </button>
    </div>
  )
}
