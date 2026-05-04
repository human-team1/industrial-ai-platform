import type { DragEventHandler, ReactNode } from 'react'
import { useState } from 'react'
import type { DocumentFormMode, DocumentFormValues } from '../model'
import type { DocumentDetail, DocumentVersion, IndexingStatus } from '../../document/types'
import { indexingStatusLabel } from '../../document/lib/indexingStatusLabel'

type DocumentFormProps = {
  mode: DocumentFormMode
  title: string
  description: string
  submitLabel: string
  values: DocumentFormValues
  file: File | null
  detail: DocumentDetail | null
  loading: boolean
  saving: boolean
  previewLoading: boolean
  previewErrorMessage: string | null
  errorMessage: string | null
  fieldErrors: Partial<Record<keyof DocumentFormValues | 'file', string>>
  onChange: (name: keyof DocumentFormValues, value: string) => void
  onFileChange: (file: File | null) => void
  onCancel: () => void
  onPreviewOpen: () => void
  onSubmit: () => void
}

export function DocumentForm({
  mode,
  title,
  description,
  submitLabel,
  values,
  file,
  detail,
  loading,
  saving,
  previewLoading,
  previewErrorMessage,
  errorMessage,
  fieldErrors,
  onChange,
  onFileChange,
  onCancel,
  onPreviewOpen,
  onSubmit,
}: DocumentFormProps) {
  if (loading) return <div className="page-panel">문서 정보를 불러오는 중입니다.</div>

  if (mode === 'edit' && errorMessage && !detail) {
    return <div className="page-panel text-sm text-red-600">{errorMessage}</div>
  }

  return (
    <section className="mx-auto w-full max-w-[min(100%,72rem)] space-y-5">
      <div className="page-panel">
        <h1 className="text-2xl font-semibold text-slate-900">{title}</h1>
        <p className="mt-2 text-sm leading-relaxed text-slate-600">{description}</p>
      </div>

      <div className="grid grid-cols-1 gap-5 xl:grid-cols-2">
        <div className="space-y-5">
          <div className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
            <DocumentFileUpload
              mode={mode}
              file={file}
              latestVersion={detail?.latestVersion ?? null}
              errorMessage={fieldErrors.file}
              onFileChange={onFileChange}
            />
          </div>
          <div className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
            <DocumentMetadataFields values={values} fieldErrors={fieldErrors} onChange={onChange} />
          </div>
        </div>

        <div className="space-y-5">
          <div className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
            <DocumentIndexingStatus mode={mode} latestVersion={detail?.latestVersion ?? null} />
          </div>
          <div className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
            <DocumentPreviewPanel
              mode={mode}
              latestVersion={detail?.latestVersion ?? null}
              loading={previewLoading}
              errorMessage={previewErrorMessage}
              onPreviewOpen={onPreviewOpen}
            />
          </div>
        </div>
      </div>

      {errorMessage ? (
        <p className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
          {errorMessage}
        </p>
      ) : null}

      <div className="flex flex-wrap items-center justify-between gap-3">
        <button
          type="button"
          className="btn-secondary inline-flex min-w-[5rem] items-center justify-center whitespace-nowrap px-5"
          onClick={onCancel}
          disabled={saving}
        >
          취소
        </button>
        <button
          type="button"
          className="btn-primary inline-flex min-w-[8rem] items-center justify-center whitespace-nowrap px-6"
          disabled={saving}
          onClick={onSubmit}
        >
          {saving ? '저장 중...' : submitLabel}
        </button>
        <button
          type="button"
          className="inline-flex min-w-[5.5rem] items-center justify-center whitespace-nowrap rounded border border-rose-200 bg-white px-5 py-2 text-sm font-semibold text-rose-600 hover:bg-rose-50 disabled:opacity-50"
          disabled={saving || mode !== 'edit'}
          onClick={() => window.alert('문서 삭제 기능은 준비 중입니다. (UI 전용)')}
          title={mode === 'edit' ? '문서 삭제 (준비 중)' : '신규 등록 화면에서는 사용할 수 없습니다.'}
        >
          문서 삭제
        </button>
      </div>
    </section>
  )
}

function DocumentFileUpload({
  mode,
  file,
  latestVersion,
  errorMessage,
  onFileChange,
}: {
  mode: DocumentFormMode
  file: File | null
  latestVersion: DocumentVersion | null
  errorMessage?: string
  onFileChange: (file: File | null) => void
}) {
  const [dragOver, setDragOver] = useState(false)

  const onDropFile: DragEventHandler<HTMLLabelElement> = (event) => {
    event.preventDefault()
    setDragOver(false)
    onFileChange(event.dataTransfer.files?.[0] ?? null)
  }

  return (
    <section className="space-y-4">
      <div>
        <h2 className="text-sm font-semibold text-slate-800">파일 업로드</h2>
        {mode === 'edit' ? (
          <p className="mt-1 text-xs text-slate-500">
            새 파일을 선택하면 기존 파일의 새 버전으로 등록됩니다.
          </p>
        ) : null}
      </div>

      {mode === 'edit' ? <ExistingFileInfo latestVersion={latestVersion} /> : null}

      <label
        className={`flex min-h-[176px] cursor-pointer flex-col items-center justify-center gap-4 rounded-lg border-2 border-dashed px-6 py-8 text-center transition ${
          dragOver ? 'border-slate-500 bg-slate-50' : 'border-slate-300 bg-slate-50/60'
        }`}
        onDragOver={(event) => {
          event.preventDefault()
          setDragOver(true)
        }}
        onDragLeave={() => setDragOver(false)}
        onDrop={onDropFile}
      >
        <p className="text-sm text-slate-600">PDF, DOCX, MD 문서를 업로드하세요</p>
        <span className="btn-secondary inline-flex items-center justify-center whitespace-nowrap px-5">파일 선택</span>
        <p className="text-xs text-slate-500">
          {file ? `선택된 파일: ${file.name}` : '선택된 파일이 없습니다.'}
        </p>
        <input
          className="hidden"
          type="file"
          accept=".pdf,.docx,.md"
          onChange={(event) => onFileChange(event.target.files?.[0] ?? null)}
        />
      </label>

      {errorMessage ? <p className="text-sm text-red-600">{errorMessage}</p> : null}
    </section>
  )
}

function ExistingFileInfo({ latestVersion }: { latestVersion: DocumentVersion | null }) {
  if (!latestVersion) {
    return (
      <div className="rounded-lg border border-slate-200 bg-slate-50 p-4 text-sm text-slate-600">
        기존 파일 정보가 없습니다.
      </div>
    )
  }

  return (
    <div className="grid grid-cols-1 gap-3 rounded-lg border border-slate-200 bg-slate-50 p-4 text-sm md:grid-cols-2">
      <Info label="파일명" value={latestVersion.fileName} />
      <Info label="파일 형식" value={latestVersion.fileExt ?? latestVersion.mimeType} />
      <Info label="파일 크기" value={formatFileSize(latestVersion.fileSize)} />
      <Info label="최신 버전 생성일" value={formatDateTime(latestVersion.createdAt)} />
    </div>
  )
}

function DocumentMetadataFields({
  values,
  fieldErrors,
  onChange,
}: {
  values: DocumentFormValues
  fieldErrors: Partial<Record<keyof DocumentFormValues | 'file', string>>
  onChange: (name: keyof DocumentFormValues, value: string) => void
}) {
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

const INDEXING_STEPS = ['업로드 완료', '텍스트 추출', '청크 분할', '임베딩 저장', '인덱싱 완료'] as const

function activeStepFromStatus(status: IndexingStatus | null | undefined): number {
  if (!status) return 0
  if (status === 'PENDING') return 1
  if (status === 'PROCESSING') return 3
  if (status === 'COMPLETED') return INDEXING_STEPS.length
  if (status === 'FAILED') return 2
  return 0
}

function progressPercentFromStatus(status: IndexingStatus | null | undefined): number {
  if (!status) return 0
  if (status === 'PENDING') return 15
  if (status === 'PROCESSING') return 78
  if (status === 'COMPLETED') return 100
  if (status === 'FAILED') return 40
  return 0
}

function DocumentIndexingStatus({
  mode,
  latestVersion,
}: {
  mode: DocumentFormMode
  latestVersion: DocumentVersion | null
}) {
  if (mode === 'create') {
    return (
      <section className="space-y-3">
        <h2 className="text-sm font-semibold text-slate-800">인덱싱 상태</h2>
        <div className="flex min-h-[100px] items-center rounded-lg border border-slate-200 bg-slate-50 px-5 py-4 text-sm leading-relaxed text-slate-600">
          문서 저장 후 인덱싱 상태가 표시됩니다.
        </div>
      </section>
    )
  }

  const status = latestVersion?.indexingStatus ?? null
  const activeStep = activeStepFromStatus(status)
  const percent = progressPercentFromStatus(status)
  const totalChunks = latestVersion?.indexedChunkCount ?? 0
  // 와이어프레임 시각 placeholder (백엔드 응답에 부분 통계 필드가 없을 경우 표시 전용)
  const processed = totalChunks > 0 ? Math.round(totalChunks * (percent / 100)) : 0
  const queued = totalChunks > 0 ? Math.max(0, totalChunks - processed) : 0

  return (
    <section className="space-y-4">
      <h2 className="text-sm font-semibold text-slate-800">인덱싱 상태</h2>

      <IndexingStepper activeStep={activeStep} failed={status === 'FAILED'} />

      <div className="grid grid-cols-1 gap-4 rounded-lg border border-slate-200 bg-white p-5 md:grid-cols-[auto_minmax(0,1fr)]">
        <CircularProgress percent={percent} status={status} />
        <div className="space-y-2 text-sm">
          <div className="flex items-center gap-2">
            <StatusBadge status={status} />
            <span className="text-xs text-slate-500">{formatDateTime(latestVersion?.indexedAt) || '시각 정보 없음'}</span>
          </div>
          <dl className="grid grid-cols-3 gap-3 pt-2 text-xs">
            <div>
              <dt className="text-slate-500">전체 청크</dt>
              <dd className="mt-0.5 text-base font-semibold text-slate-900">{totalChunks.toLocaleString()}</dd>
            </div>
            <div>
              <dt className="text-slate-500">처리 완료</dt>
              <dd className="mt-0.5 text-base font-semibold text-slate-900">{processed.toLocaleString()}</dd>
            </div>
            <div>
              <dt className="text-slate-500">대기/실패</dt>
              <dd className="mt-0.5 text-base font-semibold text-slate-900">{queued.toLocaleString()}</dd>
            </div>
          </dl>
        </div>
      </div>

      {status === 'FAILED' ? (
        <p className="rounded border border-rose-200 bg-rose-50 px-3 py-2 text-xs text-rose-700">
          실패 사유: {latestVersion?.indexErrorMessage ?? '제공되지 않았습니다.'}
        </p>
      ) : null}

      <p className="text-[11px] text-slate-400">
        ※ 단계 진행/원형 진행률/처리 통계는 시각 placeholder이며, 실제 단계별 상태 API 연동 후 정합화됩니다.
      </p>
    </section>
  )
}

function IndexingStepper({ activeStep, failed }: { activeStep: number; failed: boolean }) {
  return (
    <ol className="flex items-center justify-between gap-1">
      {INDEXING_STEPS.map((label, index) => {
        const stepNo = index + 1
        const isDone = stepNo < activeStep || (!failed && stepNo === activeStep && activeStep === INDEXING_STEPS.length)
        const isCurrent = stepNo === activeStep && !isDone
        const tone = failed && isCurrent
          ? 'bg-rose-500 text-white border-rose-500'
          : isDone
            ? 'bg-emerald-500 text-white border-emerald-500'
            : isCurrent
              ? 'bg-sky-500 text-white border-sky-500'
              : 'bg-white text-slate-400 border-slate-300'
        return (
          <li key={label} className="flex flex-1 flex-col items-center gap-1 text-[11px]">
            <div className="flex w-full items-center">
              <span className={`mx-auto flex h-7 w-7 items-center justify-center rounded-full border ${tone} text-xs font-semibold`}>
                {isDone ? '✓' : stepNo}
              </span>
            </div>
            <span className={`text-center ${isCurrent ? 'font-semibold text-slate-800' : 'text-slate-500'}`}>{label}</span>
          </li>
        )
      })}
    </ol>
  )
}

function CircularProgress({ percent, status }: { percent: number; status: IndexingStatus | null }) {
  const clamped = Math.max(0, Math.min(100, percent))
  const color = status === 'FAILED' ? '#f43f5e' : status === 'COMPLETED' ? '#10b981' : '#0ea5e9'
  const bg = `conic-gradient(${color} ${clamped * 3.6}deg, #e2e8f0 0deg)`
  return (
    <div className="flex h-24 w-24 shrink-0 items-center justify-center rounded-full" style={{ background: bg }}>
      <div className="flex h-[4.5rem] w-[4.5rem] flex-col items-center justify-center rounded-full bg-white">
        <span className="text-lg font-bold text-slate-900">{clamped}%</span>
        <span className="text-[10px] text-slate-500">진행률</span>
      </div>
    </div>
  )
}

function DocumentPreviewPanel({
  mode,
  latestVersion,
  loading,
  errorMessage,
  onPreviewOpen,
}: {
  mode: DocumentFormMode
  latestVersion: DocumentVersion | null
  loading: boolean
  errorMessage: string | null
  onPreviewOpen: () => void
}) {
  if (mode === 'create') {
    return (
      <section className="space-y-3">
        <h2 className="text-sm font-semibold text-slate-800">문서 미리보기</h2>
        <div className="flex min-h-[100px] items-center rounded-lg border border-slate-200 bg-slate-50 px-5 py-4 text-sm leading-relaxed text-slate-600">
          저장 후 미리보기를 확인할 수 있습니다.
        </div>
      </section>
    )
  }

  const canPreview = Boolean(latestVersion?.fileId)

  return (
    <section className="space-y-3">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h2 className="text-sm font-semibold text-slate-800">문서 미리보기</h2>
        {canPreview ? (
          <button
            type="button"
            className="btn-secondary inline-flex shrink-0 items-center justify-center whitespace-nowrap px-4"
            onClick={onPreviewOpen}
            disabled={loading}
          >
            {loading ? '불러오는 중...' : '새 탭에서 열기'}
          </button>
        ) : null}
      </div>
      <div className="min-h-[128px] max-h-72 overflow-y-auto rounded-lg border border-slate-200 bg-slate-50 p-5 text-sm leading-6 text-slate-600">
        {errorMessage ??
          (canPreview
          ? '백엔드가 텍스트 미리보기 필드를 제공하지 않아 원문은 새 탭에서 확인할 수 있습니다.'
          : '미리보기 데이터를 제공하는 API가 없습니다.')}
      </div>
    </section>
  )
}

function Field({
  label,
  required,
  errorMessage,
  className = '',
  children,
}: {
  label: string
  required?: boolean
  errorMessage?: string
  className?: string
  children: ReactNode
}) {
  return (
    <label className={`block space-y-2 ${className}`}>
      <span className="block text-sm font-medium text-slate-700">
        {label}
        {required ? <span className="text-red-500"> *</span> : null}
      </span>
      {children}
      {errorMessage ? <span className="block text-sm text-red-600">{errorMessage}</span> : null}
    </label>
  )
}

function Info({ label, value }: { label: string; value?: ReactNode }) {
  return (
    <div>
      <p className="text-xs font-medium text-slate-500">{label}</p>
      <div className="mt-1 text-sm text-slate-800">{value || '-'}</div>
    </div>
  )
}

function StatusBadge({ status }: { status?: IndexingStatus | null }) {
  const color =
    status === 'PENDING'
      ? 'bg-slate-100 text-slate-700'
      : status === 'PROCESSING'
        ? 'bg-blue-100 text-blue-700'
        : status === 'COMPLETED'
          ? 'bg-emerald-100 text-emerald-700'
          : 'bg-red-100 text-red-700'

  return (
    <span className={`inline-flex rounded-full px-2 py-1 text-xs font-semibold ${color}`}>
      {indexingStatusLabel(status)}
    </span>
  )
}

function formatFileSize(value?: number | null) {
  if (!value) return '-'
  if (value < 1024) return `${value} B`
  if (value < 1024 * 1024) return `${(value / 1024).toFixed(1)} KB`
  return `${(value / 1024 / 1024).toFixed(1)} MB`
}

function formatDateTime(value?: string | null) {
  if (!value) return '-'
  return value.slice(0, 16).replace('T', ' ')
}
