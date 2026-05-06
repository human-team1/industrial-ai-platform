import type { ResultDecisionInfo, ResultModelInfo } from '../../entities/result/model/types'

type Props = {
  model?: ResultModelInfo | null
  result: ResultDecisionInfo
}

export function ModelInfoCard({ model, result }: Props) {
  return (
    <section className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
      <h2 className="mb-4 text-base font-semibold text-slate-950">사용 모델</h2>
      {model ? (
        <dl className="grid grid-cols-1 gap-3 sm:grid-cols-2">
          <Row label="모델명" value={model.modelName} />
          <Row label="버전명" value={model.versionName} />
          <Row label="카테고리" value={model.modelCategory} />
          <Row label="프로필" value={model.modelProfile} />
          <Row label="modelVersionId" value={result.modelVersionId} />
        </dl>
      ) : (
        <p className="text-sm text-slate-500">모델 정보 없음</p>
      )}
    </section>
  )
}

function Row({ label, value }: { label: string; value: string | number | null | undefined }) {
  return (
    <div>
      <dt className="text-xs font-semibold uppercase text-slate-500">{label}</dt>
      <dd className="mt-1 text-sm text-slate-900">{value ?? '-'}</dd>
    </div>
  )
}
