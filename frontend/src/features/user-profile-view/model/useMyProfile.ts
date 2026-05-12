import { useCallback, useEffect, useState } from 'react'
import { getMyProfile } from '../api'
import type { UserMeResponse } from '../types'
import type { Affiliation, UserProfile } from '../../../entities/user/types'
import { mapUserProfile } from './mapUserProfile'
import { mapAffiliation } from './mapAffiliation'

export function useMyProfile(): {
  profile: UserProfile
  affiliation: Affiliation
  isLoading: boolean
  applyServerProfile: (res: UserMeResponse) => void
} {
  const [profile, setProfile] = useState<UserProfile>(() => mapUserProfile(null))
  const [affiliation, setAffiliation] = useState<Affiliation>(() => mapAffiliation(null))
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    let cancelled = false
    getMyProfile()
      .then((res) => {
        if (cancelled) return
        setProfile(mapUserProfile(res))
        setAffiliation(mapAffiliation(res))
      })
      .catch(() => {
        if (cancelled) return
        setProfile(mapUserProfile(null))
        setAffiliation(mapAffiliation(null))
      })
      .finally(() => {
        if (!cancelled) setIsLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [])

  const applyServerProfile = useCallback((res: UserMeResponse) => {
    setProfile(mapUserProfile(res))
    setAffiliation(mapAffiliation(res))
  }, [])

  return { profile, affiliation, isLoading, applyServerProfile }
}
