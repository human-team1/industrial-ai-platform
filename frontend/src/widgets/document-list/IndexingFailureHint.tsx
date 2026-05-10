type Props = {
  documentId: number
  onClick?: (documentId: number) => void
}

export function IndexingFailureHint({ documentId, onClick }: Props) {
  return (
    <p className="text-xs text-red-600">
      반영 실패 ·{' '}
      <button
        type="button"
        className="font-semibold underline"
        onClick={() => onClick?.(documentId)}
      >
        사유 확인
      </button>
    </p>
  )
}
