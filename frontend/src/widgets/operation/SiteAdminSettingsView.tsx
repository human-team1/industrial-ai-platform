import { useState } from 'react'
import type { OperationPolicy } from '../../entities/operation/model/types'
import { formatDateTime } from '../../shared/lib/date'

const categories = ['', 'SYSTEM', 'INSPECTION', 'NOTIFICATION', 'SECURITY', 'RETENTION']

export function SiteAdminSettingsView({
  category,
  setCategory,
  activeOnly,
  setActiveOnly,
  policies,
  savingId,
  savePolicy,
}: {
  category: string
  setCategory: (value: string) => void
  activeOnly: boolean
  setActiveOnly: (value: boolean) => void
  policies: OperationPolicy[]
  savingId: number | null
  savePolicy: (policy: OperationPolicy, policyValue: string, isActive: boolean) => void
}) {
  return (
    <section className="rounded border border-slate-200 bg-white p-5 shadow-sm">
      <div className="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
        <div className="flex gap-2">
          {['운영 정책', '판정/검사 기준', '알림 기준', '보안 기준', '보존 정책'].map((tab) => (
            <span key={tab} className="rounded border border-slate-200 px-3 py-2 text-xs font-medium text-slate-600">{tab}</span>
          ))}
        </div>
        <div className="flex items-center gap-3">
          <select className="h-9 rounded border border-slate-200 px-2 text-sm" value={category} onChange={(event) => setCategory(event.target.value)}>
            {categories.map((item) => <option key={item} value={item}>{item || '전체 카테고리'}</option>)}
          </select>
          <label className="flex items-center gap-2 text-sm text-slate-600">
            <input type="checkbox" checked={activeOnly} onChange={(event) => setActiveOnly(event.target.checked)} />
            활성만
          </label>
        </div>
      </div>

      <div className="mt-5 space-y-3">
        {policies.length === 0 ? <p className="py-8 text-center text-sm text-slate-500">표시할 정책이 없습니다.</p> : null}
        {policies.map((policy) => (
          <PolicyRow key={policy.operationPolicyId} policy={policy} saving={savingId === policy.operationPolicyId} onSave={savePolicy} />
        ))}
      </div>
    </section>
  )
}

function PolicyRow({
  policy,
  saving,
  onSave,
}: {
  policy: OperationPolicy
  saving: boolean
  onSave: (policy: OperationPolicy, policyValue: string, isActive: boolean) => void
}) {
  const [value, setValue] = useState(policy.policyValue)
  const [active, setActive] = useState(Boolean(policy.isActive))
  const type = policy.valueType ?? 'STRING'

  return (
    <div className="rounded border border-slate-100 bg-slate-50 p-4">
      <div className="grid grid-cols-1 gap-4 lg:grid-cols-[1fr_280px_90px_92px] lg:items-center">
        <div>
          <p className="font-semibold text-slate-900">{policy.policyName ?? policy.policyKey}</p>
          <p className="mt-1 text-xs text-slate-500">{policy.policyCategory} · {policy.policyKey} · {type}</p>
          <p className="mt-2 text-sm text-slate-600">{policy.description ?? '-'}</p>
          <p className="mt-2 text-xs text-slate-400">수정 {formatDateTime(policy.updatedAt ?? undefined)}</p>
        </div>
        <PolicyInput type={type} value={value} onChange={setValue} />
        <label className="flex items-center gap-2 text-sm text-slate-600">
          <input type="checkbox" checked={active} onChange={(event) => setActive(event.target.checked)} />
          활성
        </label>
        <button
          type="button"
          disabled={saving}
          onClick={() => onSave(policy, value, active)}
          className="h-9 rounded bg-[#109498] px-3 text-sm font-semibold text-white disabled:opacity-50"
        >
          {saving ? '저장 중' : '저장'}
        </button>
      </div>
    </div>
  )
}

function PolicyInput({ type, value, onChange }: { type: string; value: string; onChange: (value: string) => void }) {
  if (type === 'BOOLEAN') {
    return (
      <select className="h-10 rounded border border-slate-200 bg-white px-3 text-sm" value={value} onChange={(event) => onChange(event.target.value)}>
        <option value="true">true</option>
        <option value="false">false</option>
      </select>
    )
  }
  if (type === 'JSON') {
    return <textarea className="min-h-[72px] rounded border border-slate-200 bg-white px-3 py-2 text-sm" value={value} onChange={(event) => onChange(event.target.value)} />
  }
  return <input type={type === 'NUMBER' ? 'number' : 'text'} className="h-10 rounded border border-slate-200 bg-white px-3 text-sm" value={value} onChange={(event) => onChange(event.target.value)} />
}
