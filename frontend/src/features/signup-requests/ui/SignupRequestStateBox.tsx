type Props = {
  loading: boolean
  error: string | null
}

export function SignupRequestStateBox({ loading, error }: Props) {
  if (loading) return <p>불러오는 중...</p>
  if (error) return <p style={{ color: 'red' }}>{error}</p>
  return null
}
