import type { AccountSettingItem } from '../types'

export const ACCOUNT_SETTINGS: AccountSettingItem[] = [
  { id: 'password', title: '비밀번호 변경', description: '주기적으로 비밀번호를 변경하세요.' },
  { id: '2fa', title: '2단계 인증', description: '계정 보안을 강화하세요.', status: '사용중' },
  { id: 'login-history', title: '로그인 기록', description: '최근 로그인 기록을 확인하세요.' },
  { id: 'session', title: '세션 관리', description: '활성 세션을 관리하고 로그아웃하세요.' },
]
