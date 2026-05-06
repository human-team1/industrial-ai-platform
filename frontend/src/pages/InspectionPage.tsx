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

  return (
    <section className="space-y-5">
      <div className="flex flex-wrap items-start justify-between gap-3 rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
        <div>
          <p className="text-xs font-semibold uppercase tracking-wide text-blue-600">Realtime Inspection</p>
          <h1 className="mt-2 text-2xl font-semibold text-slate-900">실시간 탐지</h1>
          <p className="mt-2 text-sm text-slate-600">
            조직에 등록된 활성 카메라와 배포 모델을 선택해 실시간 탐지 세션을 시작합니다. 선택값은
            <code className="mx-1 rounded bg-slate-100 px-1.5 py-0.5 text-xs">cameraId</code>와
            <code className="mx-1 rounded bg-slate-100 px-1.5 py-0.5 text-xs">deploymentId</code>로 backend에 전달됩니다.
          </p>
          <p className="mt-3 text-sm font-medium text-slate-700">{realtime.statusMessage}</p>
        </div>
      </div>

      {realtime.noticeMessage ? (
        <p className="rounded-md border border-blue-200 bg-blue-50 px-3 py-2 text-sm text-blue-700">
          {realtime.noticeMessage}
        </p>
      ) : null}
      {realtime.errorMessage ? (
        <p className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
          {realtime.errorMessage}
        </p>
      ) : null}

      <RealtimeControlBar
        isStarting={realtime.isStarting}
        canStart={realtime.canStart}
        requestDurationMs={realtime.requestDurationMs}
        onStart={() => {
          void realtime.start()
        }}
      />

      <div className="grid grid-cols-1 gap-5 xl:grid-cols-[minmax(0,1fr)_360px]">
        <div className="space-y-5">
          <LiveStreamPanel
            targetOptions={realtime.targetOptions}
            selectedTargetId={realtime.selectedTargetId}
            cameraOptions={realtime.cameraOptions}
            selectedCameraId={realtime.selectedCameraId}
            modelOptions={realtime.modelOptions}
            selectedDeploymentId={realtime.selectedDeploymentId}
            thresholdOptions={realtime.thresholdOptions}
            selectedThresholdId={realtime.selectedThresholdId}
            loadingOptions={realtime.loadingOptions}
            loadingCameras={realtime.loadingCameras}
            loadingModels={realtime.loadingModels}
            onTargetChange={realtime.setSelectedTargetId}
            onCameraChange={realtime.setSelectedCameraId}
            onModelChange={realtime.setSelectedDeploymentId}
            onThresholdChange={realtime.setSelectedThresholdId}
          />
        </div>
        <aside className="space-y-5">
          <CurrentDetectionResultCard
            uploadResult={realtime.uploadResult}
            modelName={realtime.selectedModel?.displayName ?? null}
            cameraName={realtime.selectedCamera?.displayName ?? null}
          />
          <RecentDetectionEventsCard events={realtime.events} onRefresh={realtime.refreshEvents} />
          <EquipmentInfoCard
            selectedCameraLabel={realtime.selectedCamera?.displayName ?? null}
            selectedModelLabel={realtime.selectedModel?.displayName ?? null}
          />
        </aside>
      </div>
    </section>
  )
}
