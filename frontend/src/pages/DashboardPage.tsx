import { useAppStore } from '../shared/lib/useAppStore'

export function DashboardPage() {
  const appName = useAppStore((state) => state.appName)

  return (
    <section className="page-panel">
      <p className="text-uppercase text-secondary small mb-2">Dashboard</p>
      <h1 className="h3 mb-3">{appName}</h1>
      <p className="mb-0">
        산업 이상 탐지 플랫폼의 주요 상태를 표시할 초기 화면입니다.
      </p>
    </section>
  )
}
