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

      <div className="page-panel space-y-8 pb-1">
        <DocumentFileUpload
          mode={mode}
          file={file}
          latestVersion={detail?.latestVersion ?? null}
          errorMessage={fieldErrors.file}
          onFileChange={onFileChange}
        />
        <DocumentMetadataFields values={values} fieldErrors={fieldErrors} onChange={onChange} />
        <DocumentIndexingStatus mode={mode} latestVersion={detail?.latestVersion ?? null} />
        <DocumentPreviewPanel
          mode={mode}
          latestVersion={detail?.latestVersion ?? null}
          loading={previewLoading}
          errorMessage={previewErrorMessage}
          onPreviewOpen={onPreviewOpen}
        />

        {errorMessage ? (
          <p className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
            {errorMessage}
          </p>
        ) : null}

        <div className="flex flex-wrap justify-end gap-3 border-t border-slate-100 pt-6">
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
            className="btn-primary inline-flex min-w-[5.5rem] items-center justify-center whitespace-nowrap px-5"
            disabled={saving}
            onClick={onSubmit}
          >
            {saving ? '저장 중...' : submitLabel}
          </button>
        </div>
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

  const status = latestVersion?.indexingStatus

  return (
    <section className="space-y-3">
      <h2 className="text-sm font-semibold text-slate-800">인덱싱 상태</h2>
      <div className="grid grid-cols-1 gap-4 rounded-lg border border-slate-200 bg-white p-5 text-sm md:grid-cols-2">
        <Info label="상태" value={<StatusBadge status={status} />} />
        <Info label="청크 수" value={formatChunkCount(latestVersion?.indexedChunkCount)} />
        <Info label="반영 시각" value={formatDateTime(latestVersion?.indexedAt)} />
        {status === 'FAILED' ? (
          <Info label="실패 사유" value={latestVersion?.indexErrorMessage ?? '실패 사유가 제공되지 않았습니다.'} />
        ) : null}
      </div>
    </section>
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

function formatChunkCount(value?: number | null) {
  if (value == null) return '-'
  return `${value.toLocaleString()}개`
}

function formatDateTime(value?: string | null) {
  if (!value) return '-'
  return value.slice(0, 16).replace('T', ' ')
}
