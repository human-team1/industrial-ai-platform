import { InfoCard } from './InfoCard'
import type { ResultDescriptionView } from './types'

type Props = {
  description: ResultDescriptionView | null
}

export function DescriptionCard({ description }: Props) {
  const hasContent = Boolean(description?.summary || description?.cause || description?.action)
  return (
    <InfoCard title="이상 설명">
      {hasContent ? (
        <div className="space-y-3 text-sm leading-6 text-slate-700">
          {description?.summary ? <p>{description.summary}</p> : null}
          {description?.cause ? (
            <p className="rounded bg-slate-50 p-3 text-slate-700">원인: {description.cause}</p>
          ) : null}
          {description?.action ? (
            <p className="rounded bg-slate-50 p-3 font-medium text-slate-900">
              권고 조치: {description.action}
            </p>
          ) : null}
        </div>
      ) : (
        <p className="text-sm text-slate-500">이상 설명이 없습니다.</p>
      )}
    </InfoCard>
  )
}
