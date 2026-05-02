import { useOperationPolicies } from '../features/operation/model/useOperationPolicies'
import { SiteAdminSettingsView } from '../widgets/operation/SiteAdminSettingsView'

export function SiteAdminSettingsPage() {
  const {
    category,
    setCategory,
    activeOnly,
    setActiveOnly,
    policies,
    loading,
    savingId,
    error,
    message,
    reload,
    savePolicy,
  } = useOperationPolicies()

  return (
    <div className="space-y-5">
      <div className="flex flex-col gap-3 lg:flex-row lg:items-end lg:justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">사이트 관리자 설정</h1>
          <p className="mt-2 text-sm text-slate-500">운영 기준, 알림 기준, 보안 기준, 보존 정책을 관리합니다.</p>
        </div>
        <button type="button" onClick={reload} className="h-10 rounded border border-slate-200 bg-white px-4 text-sm font-semibold text-slate-700">새로고침</button>
      </div>

      {message ? <div className="rounded border border-emerald-200 bg-emerald-50 p-3 text-sm font-medium text-emerald-700">{message}</div> : null}
      {error ? <div className="rounded border border-rose-200 bg-rose-50 p-3 text-sm font-medium text-rose-700">{error}</div> : null}
      {loading ? <div className="h-72 animate-pulse rounded border border-slate-200 bg-white" /> : (
        <SiteAdminSettingsView
          category={category}
          setCategory={setCategory}
          activeOnly={activeOnly}
          setActiveOnly={setActiveOnly}
          policies={policies}
          savingId={savingId}
          savePolicy={savePolicy}
        />
      )}
    </div>
  )
}
