import { useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import {
  removeDocument,
  submitDocumentMetadata,
  submitDocumentVersion,
  useDocumentDetail,
} from '../features/document/model'
import { indexingStatusLabel } from '../features/document/ui'

export function DocumentEditPage() {
  const navigate = useNavigate()
  const documentId = Number(useParams().documentId)
  const { detail, loading, error, reload } = useDocumentDetail(documentId)
  const [title, setTitle] = useState('')
  const [newFile, setNewFile] = useState<File | null>(null)
  const [saving, setSaving] = useState(false)

  if (loading) return <div className="page-panel">상세를 불러오는 중입니다.</div>
  if (error || !detail) return <div className="page-panel text-red-600">{error ?? '문서를 찾을 수 없습니다.'}</div>

  const onSave = async () => {
    setSaving(true)
    try {
      await submitDocumentMetadata(documentId, {
        title: title.trim() || detail.title,
      })
      if (newFile) {
        const form = new FormData()
        form.append('file', newFile)
        form.append('changeReason', 'manual update')
        await submitDocumentVersion(documentId, form)
      }
      await reload()
      alert('저장되었습니다.')
    } finally {
      setSaving(false)
    }
  }

  const onDelete = async () => {
    if (!window.confirm('문서를 삭제하시겠습니까?')) return
    await removeDocument(documentId)
    navigate('/documents')
  }

  return (
    <section className="space-y-4">
      <div className="page-panel">
        <h1 className="text-2xl font-semibold text-slate-900">문서 수정</h1>
        <p className="mt-2 text-sm text-slate-600">
          저장 가능한 메타데이터는 문서명뿐입니다. 새 파일을 선택하면 버전이 추가됩니다.
        </p>
      </div>
      <div className="page-panel space-y-3">
        <p>현재 파일: {detail.latestVersion?.fileName ?? '-'}</p>
        <p>인덱싱 상태: {indexingStatusLabel(detail.latestVersion?.indexingStatus)}</p>
        <input
          className="control"
          defaultValue={detail.title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="문서명"
        />
        <input type="file" accept=".pdf,.docx,.txt" onChange={(e) => setNewFile(e.target.files?.[0] ?? null)} />
        <div className="flex gap-2">
          <button type="button" className="btn-secondary" onClick={() => navigate('/documents')}>목록으로</button>
          <button type="button" className="btn-secondary" onClick={() => void onDelete()}>문서 삭제</button>
          <button type="button" className="btn-primary" disabled={saving} onClick={() => void onSave()}>저장</button>
        </div>
      </div>
    </section>
  )
}
