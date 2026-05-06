type Props = {
  previewUrl: string | null
  isLoading: boolean
  error: string | null
  emptyText: string
}

export function PreviewImage({ previewUrl, isLoading, error, emptyText }: Props) {
  if (error) return <ImageEmpty text="이미지가 없습니다." />
  if (isLoading && !previewUrl) return <ImageEmpty text="이미지를 불러오는 중입니다." />
  if (!previewUrl) return <ImageEmpty text={emptyText} />
  return <img src={previewUrl} alt="" className="h-72 w-full rounded-lg bg-slate-100 object-contain" />
}

function ImageEmpty({ text }: { text: string }) {
  return (
    <div className="flex h-72 items-center justify-center rounded-lg bg-slate-100 text-sm text-slate-500">
      {text}
    </div>
  )
}
