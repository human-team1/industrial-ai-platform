import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'

export function useInactiveAccountNotice() {
  const [searchParams, setSearchParams] = useSearchParams()
  const inactive = searchParams.get('inactive') === '1'
  const [show, setShow] = useState(false)

  useEffect(() => {
    if (!inactive) return
    setShow(true)
    const next = new URLSearchParams(searchParams)
    next.delete('inactive')
    setSearchParams(next, { replace: true })
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [inactive])

  const dismiss = () => setShow(false)

  return { show, dismiss }
}
