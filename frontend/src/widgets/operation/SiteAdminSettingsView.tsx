import { useEffect, useState } from 'react'
import type { OperationPolicy } from '../../entities/operation/model/types'
import { toPolicyActiveChart, toPolicyCategoryChart, toPolicyValueTypeChart } from '../../features/operation/model/policyCharts'
import { policyCategoryLabel } from '../../shared/lib/operationDisplay'
import { formatDateTime } from '../../shared/lib/date'
import { BarChartCard } from '../../shared/ui/chart/BarChartCard'

const CATEGORY_TABS: { category: string; label: string }[] = [
  { category: '', label: '전체' },
  { category: 'INSPECTION', label: '판정/검사 기준' },
  { category: 'NOTIFICATION', label: '알림 기준' },
  { category: 'SECURITY', label: '보안 기준' },
  { category: 'RETENTION', label: '보존 정책' },
  { category: 'SYSTEM', label: '운영 정책' },
]

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
  savePolicy: (policy: OperationPolicy, policyValue: string, isActive: boolean) => Promise<boolean>
}) {
  return (
    <section className="rounded border border-slate-200 bg-white p-5 shadow-sm">
      <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
        <div className="flex flex-wrap gap-2">
          {CATEGORY_TABS.map((tab) => {
            const active = category === tab.category
            return (
              <button
                key={tab.category || 'all'}
                type="button"
                onClick={() => setCategory(tab.category)}
                className={`rounded border px-3 py-2 text-xs font-medium transition-colors ${
                  active
                    ? 'border-[#109498] bg-[#109498]/10 text-[#0d7a7d]'
                    : 'border-slate-200 bg-white text-slate-600 hover:bg-slate-50'
                }`}
              >
                {tab.label}
              </button>
            )
          })}
        </div>
        <label className="flex items-center gap-2 text-sm text-slate-600">
          <input type="checkbox" checked={activeOnly} onChange={(event) => setActiveOnly(event.target.checked)} />
          활성만
        </label>
      </div>

      <div className="mt-5 grid grid-cols-1 gap-5 lg:grid-cols-3">
        <BarChartCard viewModel={toPolicyCategoryChart(policies)} empty={{ reason: 'NO_DATA', message: '정책 데이터가 없습니다.' }} />
        <BarChartCard viewModel={toPolicyValueTypeChart(policies)} empty={{ reason: 'NO_DATA', message: '정책 데이터가 없습니다.' }} />
        <BarChartCard viewModel={toPolicyActiveChart(policies)} empty={{ reason: 'NO_DATA', message: '정책 데이터가 없습니다.' }} />
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
  onSave: (policy: OperationPolicy, policyValue: string, isActive: boolean) => Promise<boolean>
}) {
  const [value, setValue] = useState(policy.policyValue)
  const [active, setActive] = useState(Boolean(policy.isActive))
  const type = (policy.valueType ?? 'STRING').toUpperCase()

  useEffect(() => {
    setValue(policy.policyValue)
    setActive(Boolean(policy.isActive))
  }, [policy.operationPolicyId, policy.policyValue, policy.isActive])

  const title = policy.policyName?.trim() || policy.policyKey || '-'
  const descriptionText =
    policy.description != null && String(policy.description).trim() !== ''
      ? policy.description
      : '설명이 등록되지 않았습니다.'

  async function handleSave() {
    const v = type === 'NUMBER' ? String(value).trim() : String(value)
    const ok = await onSave(policy, v, active)
    if (!ok) {
      setValue(policy.policyValue)
      setActive(Boolean(policy.isActive))
    }
  }

  return (
    <div className="rounded border border-slate-100 bg-slate-50 p-4">
      <div className="grid grid-cols-1 gap-4 lg:grid-cols-[1fr_280px_90px_92px] lg:items-center">
        <div>
          <p className="font-semibold text-slate-900">{title}</p>
          <p className="mt-1 text-xs text-slate-500">
            {policyCategoryLabel(policy.policyCategory)} · {policy.policyKey ?? '-'} · {type}
          </p>
          <p className="mt-2 text-sm text-slate-600">{descriptionText}</p>
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
          onClick={() => void handleSave()}
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
  if (type === 'NUMBER') {
    return (
      <input
        type="number"
        step="any"
        className="h-10 rounded border border-slate-200 bg-white px-3 text-sm"
        value={value}
        onChange={(event) => onChange(event.target.value)}
      />
    )
  }
  return <input type="text" className="h-10 rounded border border-slate-200 bg-white px-3 text-sm" value={value} onChange={(event) => onChange(event.target.value)} />
}
