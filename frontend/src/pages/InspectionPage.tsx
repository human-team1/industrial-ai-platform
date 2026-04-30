import { useState } from 'react'
import { useRealtimeInspection } from '../features/inspection/model'
import {
  CameraList,
  CurrentDetectionResultCard,
  EquipmentInfoCard,
  LiveStreamPanel,
  RealtimeControlBar,
  RecentDetectionEventsCard,
} from '../features/inspection/ui'

export function InspectionPage() {
  const realtime = useRealtimeInspection()
  const [snapshotMessage, setSnapshotMessage] = useState<string | null>(null)

  return (
    <section className="space-y-5">
      <div className="flex flex-wrap items-start justify-between gap-3 rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
        <div>
          <p className="text-xs font-semibold uppercase tracking-wide text-blue-600">Realtime Inspection</p>
          <h1 className="mt-2 text-2xl font-semibold text-slate-900">실시간 탐지</h1>
          <p className="mt-2 text-sm text-slate-600">
            현장 설비의 실시간 영상을 분석하여 이상을 탐지합니다.
          </p>
          <p className="mt-3 text-sm font-medium text-slate-700">{realtime.statusMessage}</p>
        </div>
        <button type="button" className="btn-secondary" onClick={realtime.refreshCameras} disabled={realtime.loadingCameras}>
          새로고침
        </button>
      </div>

      {realtime.noticeMessage ? (
        <p className="rounded-md border border-blue-200 bg-blue-50 px-3 py-2 text-sm text-blue-700">
          {realtime.noticeMessage}
        </p>
      ) : null}
      {snapshotMessage ? (
        <p className="rounded-md border border-slate-200 bg-white px-3 py-2 text-sm text-slate-600">
          {snapshotMessage}
        </p>
      ) : null}
      {realtime.errorMessage ? (
        <p className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
          {realtime.errorMessage}
        </p>
      ) : null}

      <RealtimeControlBar
        isRunning={realtime.isRunning}
        isStarting={realtime.isStarting}
        isStopping={realtime.isStopping}
        canStart={Boolean(realtime.selectedCameraId)}
        elapsedSeconds={realtime.elapsedSeconds}
        runStatus={realtime.runStatus}
        onStart={realtime.start}
        onStop={realtime.stop}
        onSnapshot={() => setSnapshotMessage('스냅샷 기능은 준비 중입니다.')}
      />

      <div className="grid grid-cols-1 gap-5 xl:grid-cols-[minmax(0,1fr)_360px]">
        <div className="space-y-5">
          <LiveStreamPanel selectedCamera={realtime.selectedCamera} isRunning={realtime.isRunning} />
          <CameraList
            cameras={realtime.cameras}
            selectedCameraId={realtime.selectedCameraId}
            loading={realtime.loadingCameras}
            onSelect={realtime.setSelectedCameraId}
          />
        </div>
        <aside className="space-y-5">
          <CurrentDetectionResultCard isRunning={realtime.isRunning} runStatus={realtime.runStatus} />
          <RecentDetectionEventsCard events={realtime.events} onRefresh={realtime.refreshEvents} />
          <EquipmentInfoCard selectedCamera={realtime.selectedCamera} />
        </aside>
      </div>
    </section>
  )
}
