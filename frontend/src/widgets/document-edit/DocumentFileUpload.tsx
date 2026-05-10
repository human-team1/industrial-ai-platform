import { useState, type DragEventHandler } from 'react'
import type { DocumentVersion } from '../../entities/document/model/types'
import { Info } from './Info'
import type { DocumentFileUploadProps } from './types'

export function DocumentFileUpload({
  mode,
  file,
  latestVersion,
  errorMessage,
  onFileChange,
}: DocumentFileUploadProps) {
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
