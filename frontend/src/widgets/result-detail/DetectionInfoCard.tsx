import type {
  ResultDecisionInfo,
  ResultImage,
  ResultInspection,
  ResultTarget,
} from '../../entities/result/model/types'
import { formatDateMinute, labelRunType } from '../../entities/result/model/labels'
import { InfoCard } from './InfoCard'
import { InfoGrid } from './InfoGrid'

type Props = {
  target: ResultTarget
  inspection: ResultInspection
  result: ResultDecisionInfo
  originalImage?: ResultImage
}

export function DetectionInfoCard({ target, inspection, result, originalImage }: Props) {
  return (
    <InfoCard title="탐지 결과 정보">
      <InfoGrid
        items={[
          ['설비명', target.equipmentName],
          ['검사유형', labelRunType(inspection.runType)],
          ['탐지일시', formatDateMinute(inspection.startedAt ?? result.createdAt)],
          ['위치', target.targetName],
          ['모델버전', result.modelVersionId],
          [
            '이미지ID / 파일ID',
            originalImage
              ? `${originalImage.imageId != null ? originalImage.imageId : '-'} / ${display(originalImage.fileId)}`
              : '-',
          ],
        ]}
      />
    </InfoCard>
  )
}

function display(value: number | null | undefined) {
  return value === null || value === undefined ? '-' : value
}
