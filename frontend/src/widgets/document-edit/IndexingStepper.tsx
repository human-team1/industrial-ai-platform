import type { IndexingStepperProps } from './types'

export const INDEXING_STEPS = [
  '업로드 완료',
  '텍스트 추출',
  '청크 분할',
  '임베딩 저장',
  '인덱싱 완료',
] as const

export function IndexingStepper({ activeStep, failed }: IndexingStepperProps) {
  return (
    <ol className="flex items-center justify-between gap-1">
      {INDEXING_STEPS.map((label, index) => {
        const stepNo = index + 1
        const isDone =
          stepNo < activeStep ||
          (!failed && stepNo === activeStep && activeStep === INDEXING_STEPS.length)
        const isCurrent = stepNo === activeStep && !isDone
        const tone =
          failed && isCurrent
            ? 'bg-rose-500 text-white border-rose-500'
            : isDone
              ? 'bg-emerald-500 text-white border-emerald-500'
              : isCurrent
                ? 'bg-sky-500 text-white border-sky-500'
                : 'bg-white text-slate-400 border-slate-300'
        return (
          <li key={label} className="flex flex-1 flex-col items-center gap-1 text-[11px]">
            <div className="flex w-full items-center">
              <span
                className={`mx-auto flex h-7 w-7 items-center justify-center rounded-full border ${tone} text-xs font-semibold`}
              >
                {isDone ? '✓' : stepNo}
              </span>
            </div>
            <span className={`text-center ${isCurrent ? 'font-semibold text-slate-800' : 'text-slate-500'}`}>
              {label}
            </span>
          </li>
        )
      })}
    </ol>
  )
}
