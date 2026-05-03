import { useState } from 'react'
import { useRealtimeInspection } from '../features/inspection/model'
import {
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
          <h1 className="mt-2 text-2xl font-semibold text-slate-900">카메라 단건 검사</h1>
          <p className="mt-2 text-sm text-slate-600">
            현재 시연 환경에서는 실제 설비 센서가 없으므로, 검사 버튼 클릭이 센서 트리거를 대체합니다. 버튼을 누른 순간의 카메라 프레임 1장을 캡처해 기존 이미지 업로드 검사 API로 처리합니다.
          </p>
          <p className="mt-3 text-sm font-medium text-slate-700">{realtime.statusMessage}</p>
        </div>
        <button
          type="button"
          className="btn-secondary"
          onClick={realtime.refreshDevices}
          disabled={realtime.isCameraLoading}
        >
          장치 새로고침
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
        isCameraReady={realtime.isCameraReady}
        isPreparing={realtime.isCameraLoading}
        isCapturing={realtime.isCapturing}
        canCapture={Boolean(realtime.selectedDeviceId)}
        requestDurationMs={realtime.requestDurationMs}
        onCapture={() => {
          setSnapshotMessage('현재 화면을 캡처해 검사 요청을 전송합니다.')
          void realtime.captureFrame()
        }}
      />

      <div className="grid grid-cols-1 gap-5 xl:grid-cols-[minmax(0,1fr)_360px]">
        <div className="space-y-5">
          <LiveStreamPanel
            videoRef={realtime.videoRef}
            devices={realtime.devices}
            selectedDeviceId={realtime.selectedDeviceId}
            isCameraReady={realtime.isCameraReady}
            isCameraLoading={realtime.isCameraLoading}
            onSelectDevice={realtime.setSelectedDeviceId}
          />
        </div>
        <aside className="space-y-5">
          <CurrentDetectionResultCard uploadResult={realtime.uploadResult} />
          <RecentDetectionEventsCard events={realtime.events} onRefresh={realtime.refreshEvents} />
          <EquipmentInfoCard
            selectedDeviceLabel={
              realtime.devices.find((device) => device.deviceId === realtime.selectedDeviceId)?.label ?? null
            }
          />
        </aside>
      </div>
    </section>
  )
}
