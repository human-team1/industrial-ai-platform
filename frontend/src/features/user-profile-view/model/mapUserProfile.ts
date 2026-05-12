import { formatDate, formatDateTime } from '../../../shared/lib/date'
import { PLACEHOLDER } from '../../../entities/user/model/constants'
import type { UserProfile } from '../../../entities/user/types'
import type { UserMeResponse } from '../types'

export function mapUserProfile(profile: UserMeResponse | null): UserProfile {
  return {
    name: profile?.name ?? PLACEHOLDER,
    email: profile?.email ?? PLACEHOLDER,
    phone: profile?.phone ?? '',
    role: profile?.role ?? PLACEHOLDER,
    joinedAt: formatDate(profile?.createdAt),
    lastLoginAt: formatDateTime(profile?.lastLoginAt),
    accountStatus: profile?.status ?? '정상',
  }
}
