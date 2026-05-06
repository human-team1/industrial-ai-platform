import { Field } from './Field'
import type { DocumentMetadataFieldsProps } from './types'

export function DocumentMetadataFields({
  values,
  fieldErrors,
  onChange,
}: DocumentMetadataFieldsProps) {
  return (
    <section className="space-y-4">
      <h2 className="text-sm font-semibold text-slate-800">문서 정보</h2>
      <div className="grid grid-cols-1 gap-x-6 gap-y-5 md:grid-cols-2">
        <Field label="문서명" required errorMessage={fieldErrors.title}>
          <input
            className="control w-full"
            value={values.title}
            onChange={(event) => onChange('title', event.target.value)}
            placeholder="문서명을 입력하세요"
          />
        </Field>
        <Field label="카테고리">
          <input
            className="control w-full"
            value={values.category}
            onChange={(event) => onChange('category', event.target.value)}
            placeholder="예: 정비 매뉴얼"
          />
        </Field>
        <Field label="설비유형">
          <input
            className="control w-full"
            value={values.equipmentType}
            onChange={(event) => onChange('equipmentType', event.target.value)}
            placeholder="예: 프레스 설비"
          />
        </Field>
        <Field label="태그">
          <input
            className="control w-full"
            value={values.tags}
            onChange={(event) => onChange('tags', event.target.value)}
            placeholder="쉼표(,)로 구분"
          />
        </Field>
        <Field label="설명" className="md:col-span-2">
          <textarea
            className="control min-h-[168px] w-full resize-y py-2.5 leading-relaxed"
            value={values.description}
            onChange={(event) => onChange('description', event.target.value)}
            placeholder="문서 설명을 입력하세요"
          />
        </Field>
      </div>
    </section>
  )
}
