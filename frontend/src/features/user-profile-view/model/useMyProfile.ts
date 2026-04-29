import { useEffect, useState } from 'react'
import { getMyProfile } from '../api'
import type { Affiliation, UserProfile } from '../../../entities/user/types'
import { mapUserProfile } from './mapUserProfile'
import { mapAffiliation } from './mapAffiliation'

export function useMyProfile(): {
  profile: UserProfile
  affiliation: Affiliation
  isLoading: boolean
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

  return { profile, affiliation, isLoading }
}
