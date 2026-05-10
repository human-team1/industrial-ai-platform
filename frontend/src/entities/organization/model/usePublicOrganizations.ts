import { useEffect, useState } from 'react'
import { getPublicOrganizations } from '../api/getPublicOrganizations'
import type { PublicOrganization } from '../types'

type UsePublicOrganizationsResult = {
  organizations: PublicOrganization[]
  loading: boolean
  error: string | null
}

export function usePublicOrganizations(): UsePublicOrganizationsResult {
  const [organizations, setOrganizations] = useState<PublicOrganization[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false

    setLoading(true)
    setError(null)

    getPublicOrganizations()
      .then((list) => {
        if (!cancelled) setOrganizations(list)
      })
      .catch(() => {
        if (!cancelled) setError('조직 목록을 불러오지 못했습니다.')
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [])

  return { organizations, loading, error }
}
