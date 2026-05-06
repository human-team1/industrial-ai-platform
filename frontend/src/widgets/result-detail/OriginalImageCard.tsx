import type { ResultImage } from '../../entities/result/model/types'
import { useResultFilePreview } from '../../features/result/model/useResultFilePreview'
import { InfoCard } from './InfoCard'
import { PreviewImage } from './PreviewImage'

type Props = {
  images: ResultImage[]
}

export function OriginalImageCard({ images }: Props) {
  const original = images.find((image) => image.imageRole === 'ORIGINAL') ?? images[0]
  const preview = useResultFilePreview(original?.fileId)
  return (
    <InfoCard title="원본 이미지">
      <PreviewImage
        previewUrl={preview.previewUrl}
        isLoading={preview.isLoading}
        error={preview.error}
        emptyText="원본 이미지가 없습니다."
      />
    </InfoCard>
  )
}
