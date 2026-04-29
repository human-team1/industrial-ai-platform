import type { DragEventHandler } from 'react'
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { submitNewDocument } from '../features/document/model'

export function DocumentNewPage() {
  const navigate = useNavigate()
  const [file, setFile] = useState<File | null>(null)
  const [title, setTitle] = useState('')
  const [category, setCategory] = useState('')
  const [equipmentType, setEquipmentType] = useState('')
  const [description, setDescription] = useState('')
  const [tags, setTags] = useState('')
  const [saving, setSaving] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [dragOver, setDragOver] = useState(false)

  const onSubmit = async () => {
    setErrorMessage(null)
    if (!file || !title.trim()) {
      setErrorMessage('파일과 문서명은 필수 입력입니다.')
      return
    }
    const ext = file.name.split('.').pop()?.toLowerCase()
    if (!ext || !['pdf', 'docx', 'md'].includes(ext)) {
      setErrorMessage('허용 파일 형식은 PDF, DOCX, MD 입니다.')
      return
    }
    setSaving(true)
    try {
      const formData = new FormData()
      formData.append('file', file)
      formData.append('title', title.trim())
      formData.append('category', category.trim())
      formData.append('equipmentType', equipmentType.trim())
      formData.append('description', description.trim())
      formData.append('tags', tags.trim())
      await submitNewDocument(formData)
      navigate('/documents')
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : '문서 저장에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

  const onFileChange = (nextFile: File | null) => {
    setFile(nextFile)
    if (nextFile) {
      setErrorMessage(null)
    }
  }

  const onDropFile: DragEventHandler<HTMLLabelElement> = (event) => {
    event.preventDefault()
    setDragOver(false)
    onFileChange(event.dataTransfer.files?.[0] ?? null)
  }

  return (
    <section className="mx-auto w-full max-w-5xl space-y-4">
      <div className="page-panel">
        <h1 className="text-2xl font-semibold text-slate-900">문서 등록</h1>
        <p className="mt-2 text-sm text-slate-600">파일과 문서 메타데이터를 함께 등록합니다.</p>
      </div>
      <div className="page-panel space-y-8">
        <div className="space-y-3">
          <p className="text-sm font-semibold text-slate-800">파일 업로드 *</p>
          <label
            className={`flex cursor-pointer flex-col items-center justify-center gap-3 rounded-lg border-2 border-dashed p-6 text-center transition ${
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
            <span className="btn-secondary inline-flex items-center">파일 선택</span>
            <p className="text-xs text-slate-500">
              {file ? `선택된 파일: ${file.name}` : '선택된 파일이 없습니다.'}
            </p>
            <input
              className="hidden"
              type="file"
              accept=".pdf,.docx,.md"
              onChange={(e) => onFileChange(e.target.files?.[0] ?? null)}
            />
          </label>
        </div>

        <div className="space-y-3">
          <p className="text-sm font-semibold text-slate-800">문서 정보</p>
          <p className="text-xs text-slate-500">입력 폼</p>
          <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
            <label className="space-y-1">
              <span className="text-sm font-medium text-slate-700">문서명 *</span>
              <input
                className="control w-full"
                placeholder="문서명을 입력하세요"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
              />
            </label>
            <label className="space-y-1">
              <span className="text-sm font-medium text-slate-700">카테고리</span>
              <input
                className="control w-full"
                placeholder="예: 점검 매뉴얼"
                value={category}
                onChange={(e) => setCategory(e.target.value)}
              />
            </label>
            <label className="space-y-1">
              <span className="text-sm font-medium text-slate-700">설비유형</span>
              <input
                className="control w-full"
                placeholder="예: 프레스 설비"
                value={equipmentType}
                onChange={(e) => setEquipmentType(e.target.value)}
              />
            </label>
            <label className="space-y-1">
              <span className="text-sm font-medium text-slate-700">태그</span>
              <input
                className="control w-full"
                placeholder="쉼표(,)로 구분"
                value={tags}
                onChange={(e) => setTags(e.target.value)}
              />
            </label>
            <label className="space-y-1 md:col-span-2">
              <span className="text-sm font-medium text-slate-700">설명</span>
              <textarea
                className="control min-h-[140px] w-full py-2"
                placeholder="문서 설명을 입력하세요"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
              />
            </label>
          </div>
        </div>

        {errorMessage ? (
          <p className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">{errorMessage}</p>
        ) : null}

        <div className="flex justify-end gap-2">
          <button type="button" className="btn-secondary" onClick={() => navigate('/documents')} disabled={saving}>
            취소
          </button>
          <button type="button" className="btn-primary" disabled={saving} onClick={() => void onSubmit()}>
            {saving ? '저장 중...' : '저장'}
          </button>
        </div>
      </div>
    </section>
  )
}
