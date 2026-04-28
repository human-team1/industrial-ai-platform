import { PLACEHOLDER } from '../../../entities/user/model/constants'
import type { Affiliation } from '../../../entities/user/types'
import type { UserMeResponse } from '../types'

export function mapAffiliation(profile: UserMeResponse | null): Affiliation {
  return {
    organization: profile?.organizationName ?? PLACEHOLDER,
    team: PLACEHOLDER,
    role: profile?.role ?? PLACEHOLDER,
    permissions: [],
  }
}
