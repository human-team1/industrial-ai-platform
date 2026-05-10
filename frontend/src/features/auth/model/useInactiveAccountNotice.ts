import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'

export function useInactiveAccountNotice() {
  const [searchParams, setSearchParams] = useSearchParams()
  const inactive = searchParams.get('inactive') === '1'
  const expired = searchParams.get('expired') === '1'
  const [showInactive, setShowInactive] = useState(false)
  const [showExpired, setShowExpired] = useState(false)

  useEffect(() => {
    if (!inactive && !expired) return
    if (inactive) setShowInactive(true)
    if (expired) setShowExpired(true)
    const next = new URLSearchParams(searchParams)
    next.delete('inactive')
    next.delete('expired')
    setSearchParams(next, { replace: true })
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [inactive, expired])

  const dismissInactive = () => setShowInactive(false)
  const dismissExpired = () => setShowExpired(false)

  return { showInactive, showExpired, dismissInactive, dismissExpired }
}
