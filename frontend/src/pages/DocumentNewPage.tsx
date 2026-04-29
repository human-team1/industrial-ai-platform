import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { submitNewDocument } from '../features/document/model'

export function DocumentNewPage() {
  const navigate = useNavigate()
  const [file, setFile] = useState<File | null>(null)
  const [title, setTitle] = useState('')
  const [saving, setSaving] = useState(false)

  const onSubmit = async () => {
    if (!file || !title.trim()) {
      alert('파일과 문서명을 입력해 주세요.')
      return
    }
    const ext = file.name.split('.').pop()?.toLowerCase()
    if (!ext || !['pdf', 'docx', 'txt'].includes(ext)) {
      alert('허용 파일 형식은 pdf, docx, txt 입니다.')
      return
    }
    setSaving(true)
    try {
      const formData = new FormData()
      formData.append('file', file)
      formData.append('title', title.trim())
      await submitNewDocument(formData)
      navigate('/documents')
    } finally {
      setSaving(false)
    }
  }

  return (
    <section className="space-y-4">
      <div className="page-panel">
        <h1 className="text-2xl font-semibold text-slate-900">문서 등록</h1>
        <p className="mt-2 text-sm text-slate-600">
          현재 DB 스키마에는 카테고리·설비·설명·태그 컬럼이 없어, 문서명과 파일만 저장됩니다.
        </p>
      </div>
      <div className="page-panel space-y-3">
        <input type="file" accept=".pdf,.docx,.txt" onChange={(e) => setFile(e.target.files?.[0] ?? null)} />
        <input className="control" placeholder="문서명" value={title} onChange={(e) => setTitle(e.target.value)} />
        <div className="flex gap-2">
          <button type="button" className="btn-secondary" onClick={() => navigate('/documents')}>취소</button>
          <button type="button" className="btn-primary" disabled={saving} onClick={() => void onSubmit()}>저장</button>
        </div>
      </div>
    </section>
  )
}
