import type { DragEvent, ReactNode } from 'react'
import { useRef, useState } from 'react'
import { formatElapsedTime } from '../../../shared/lib/date'
import { computeProgressSteps } from '../model'
import type {
  AnalysisTargetOption,
  AvailableInspectionModel,
  AvailableRealtimeCamera,
  InspectionEvent,
  ProgressStepState,
  SelectedInspectionFile,
  ThresholdOption,
  UploadInspectionResponse,
} from '../types'

type FileProps = {
  selectedFile: SelectedInspectionFile | null
  uploading: boolean
  errorMessage: string | null
  noticeMessage: string | null
  onFileSelect: (file: File | null) => void
}

export function RealtimeControlBar({
  isStarting,
  canStart,
  requestDurationMs,
  onStart,
}: {
  isStarting: boolean
  canStart: boolean
  requestDurationMs: number | null
  onStart: () => void
}) {
  return (
    <section className="flex flex-wrap items-center justify-between gap-3 rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <div className="flex flex-wrap items-center gap-4">
        <span className="inline-flex items-center gap-2 text-sm font-semibold text-slate-700">
          <span className={`h-2.5 w-2.5 rounded-full ${isStarting ? 'bg-blue-500' : 'bg-emerald-500'}`} />
          {isStarting ? '실시간 탐지 시작 중' : '실시간 탐지 준비 완료'}
        </span>
        <span className="text-sm text-slate-500">
          최근 요청 시간: {requestDurationMs == null ? '-' : `${(requestDurationMs / 1000).toFixed(2)}초`}
        </span>
      </div>
      <button type="button" className="btn-blue" disabled={!canStart} onClick={onStart}>
        {isStarting ? '시작 요청 중...' : '실시간 탐지 시작'}
      </button>
    </section>
  )
}

export function LiveStreamPanel({
  targetOptions,
  selectedTargetId,
  cameraOptions,
  selectedCameraId,
  modelOptions,
  selectedDeploymentId,
  thresholdOptions,
  selectedThresholdId,
  loadingOptions,
  loadingCameras,
  loadingModels,
  onTargetChange,
  onCameraChange,
  onModelChange,
  onThresholdChange,
}: {
  targetOptions: AnalysisTargetOption[]
  selectedTargetId: number | null
  cameraOptions: AvailableRealtimeCamera[]
  selectedCameraId: number | null
  modelOptions: AvailableInspectionModel[]
  selectedDeploymentId: number | null
  thresholdOptions: ThresholdOption[]
  selectedThresholdId: number | null
  loadingOptions: boolean
  loadingCameras: boolean
  loadingModels: boolean
  onTargetChange: (value: number | null) => void
  onCameraChange: (value: number | null) => void
  onModelChange: (value: number | null) => void
  onThresholdChange: (value: number | null) => void
}) {
  return (
    <Card title="실시간 탐지 설정" className="min-h-[520px]">
      <div className="grid gap-4 md:grid-cols-2">
        <Field label="검사 대상">
          <select
            className="control w-full"
            value={selectedTargetId ?? ''}
            onChange={(event) => onTargetChange(event.target.value ? Number(event.target.value) : null)}
            disabled={loadingOptions}
          >
            <option value="">조직 전체</option>
            {targetOptions.map((target) => (
              <option key={target.id} value={target.id}>
                {target.name}
              </option>
            ))}
          </select>
        </Field>

        <Field label="임계값">
          <select
            className="control w-full"
            value={selectedThresholdId ?? ''}
            onChange={(event) => onThresholdChange(event.target.value ? Number(event.target.value) : null)}
            disabled={loadingOptions}
          >
            <option value="">기본 임계값 사용</option>
            {thresholdOptions
              .filter((threshold) => threshold.id)
              .map((threshold) => (
                <option key={threshold.id} value={threshold.id}>
                  {threshold.name}
                </option>
              ))}
          </select>
        </Field>

        <Field label="카메라">
          <select
            className="control w-full"
            value={selectedCameraId ?? ''}
            onChange={(event) => onCameraChange(event.target.value ? Number(event.target.value) : null)}
            disabled={loadingCameras}
          >
            <option value="">
              {loadingCameras ? '카메라 목록을 불러오는 중입니다.' : '카메라를 선택해 주세요.'}
            </option>
            {cameraOptions.map((camera) => (
              <option key={camera.cameraId} value={camera.cameraId}>
                {camera.displayName}
              </option>
            ))}
          </select>
          {cameraOptions.length === 0 && !loadingCameras ? (
            <p className="mt-1 text-xs text-slate-500">사용 가능한 활성 카메라가 없습니다.</p>
          ) : null}
        </Field>

        <Field label="배포 모델">
          <select
            className="control w-full"
            value={selectedDeploymentId ?? ''}
            onChange={(event) => onModelChange(event.target.value ? Number(event.target.value) : null)}
            disabled={loadingModels}
          >
            <option value="">
              {loadingModels ? '배포 모델 목록을 불러오는 중입니다.' : '배포 모델을 선택해 주세요.'}
            </option>
            {modelOptions.map((model) => (
              <option key={model.deploymentId} value={model.deploymentId}>
                {model.displayName}
              </option>
            ))}
          </select>
          {modelOptions.length === 0 && !loadingModels ? (
            <p className="mt-1 text-xs text-slate-500">
              사용 가능한 배포 모델이 없습니다. 모델 생성과 배포 상태를 확인해 주세요.
            </p>
          ) : null}
        </Field>
      </div>

      <div className="mt-6 rounded-lg border border-blue-100 bg-blue-50 px-4 py-4 text-sm text-blue-900">
        실시간 탐지는 선택한 <strong>cameraId</strong>와 <strong>deploymentId</strong>를 backend에 전달해
        세션을 시작합니다. 실행 중 설정을 바꾸려면 현재 세션을 중지한 뒤 다시 시작해 주세요.
      </div>

      <div className="mt-6 rounded-lg border border-dashed border-slate-300 bg-slate-50 px-4 py-6 text-sm text-slate-600">
        현재 화면에서는 세션 시작과 상태 추적을 제공합니다. 실제 실시간 추론 파이프라인은 별도 backend/FastAPI
        구현 상태에 따라 동작합니다.
      </div>
    </Card>
  )
}

export function CurrentDetectionResultCard({
  uploadResult,
  modelName,
  cameraName,
}: {
  uploadResult: UploadInspectionResponse | null
  modelName: string | null
  cameraName: string | null
}) {
  return (
    <Card title="현재 탐지 상태">
      <div className="space-y-4 text-sm">
        <InfoRow label="inspectionId" value={uploadResult ? String(uploadResult.inspectionId) : '-'} />
        <InfoRow label="runStatus" value={uploadResult?.runStatus ?? '대기'} />
        <InfoRow label="선택 모델" value={modelName ?? '-'} />
        <InfoRow label="선택 카메라" value={cameraName ?? '-'} />
        <div className="rounded-md border border-dashed border-slate-300 bg-slate-50 p-3 text-xs text-slate-500">
          세션 시작 이후 실제 탐지 이벤트와 세부 상태는 아래 이벤트 로그와 결과 상세 화면에서 확인할 수 있습니다.
        </div>
      </div>
    </Card>
  )
}

export function RecentDetectionEventsCard({
  events,
  onRefresh,
}: {
  events: InspectionEvent[]
  onRefresh: () => void
}) {
  return (
    <Card
      title="최근 탐지 이벤트"
      action={
        <button type="button" className="text-xs font-semibold text-blue-600" onClick={onRefresh}>
          새로고침
        </button>
      }
    >
      {events.length === 0 ? (
        <p className="rounded-md bg-slate-50 px-4 py-8 text-center text-sm text-slate-500">
          최근 탐지 이벤트가 없습니다.
        </p>
      ) : (
        <div className="space-y-3">
          {events
            .slice()
            .reverse()
            .slice(0, 5)
            .map((event) => (
              <div key={event.eventId} className="rounded-md border border-slate-200 p-3">
                <p className="text-sm font-semibold text-slate-800">{event.eventType}</p>
                <p className="mt-1 text-xs text-slate-500">{event.message ?? '-'}</p>
                <p className="mt-2 text-xs text-slate-400">{formatStringDateTime(event.createdAt)}</p>
              </div>
            ))}
        </div>
      )}
    </Card>
  )
}

export function EquipmentInfoCard({
  selectedCameraLabel,
  selectedModelLabel,
}: {
  selectedCameraLabel: string | null
  selectedModelLabel: string | null
}) {
  return (
    <Card title="선택 정보">
      <div className="space-y-3 text-sm">
        <InfoRow label="입력 방식" value="서버 카메라 스트림" />
        <InfoRow label="선택 카메라" value={selectedCameraLabel ?? '-'} />
        <InfoRow label="선택 모델" value={selectedModelLabel ?? '-'} />
        <InfoRow label="세션 시작 방식" value="cameraId + deploymentId" />
      </div>
    </Card>
  )
}

export function FileUploadCard({
  selectedFile,
  uploading,
  errorMessage,
  noticeMessage,
  onFileSelect,
}: FileProps) {
  const inputRef = useRef<HTMLInputElement | null>(null)
  const [dragOver, setDragOver] = useState(false)

  const pickFile = () => {
    if (!uploading) inputRef.current?.click()
  }

  const handleDrop = (event: DragEvent<HTMLDivElement>) => {
    event.preventDefault()
    setDragOver(false)
    if (uploading) return
    onFileSelect(event.dataTransfer.files?.[0] ?? null)
  }

  return (
    <Card title="1. 이미지 업로드">
      <div className="grid gap-4 lg:grid-cols-[minmax(0,1.2fr)_minmax(260px,0.8fr)]">
        <div
          role="button"
          tabIndex={0}
          aria-disabled={uploading}
          onClick={pickFile}
          onKeyDown={(event) => {
            if (event.key === 'Enter' || event.key === ' ') pickFile()
          }}
          onDragOver={(event) => {
            event.preventDefault()
            if (!uploading) setDragOver(true)
          }}
          onDragLeave={() => setDragOver(false)}
          onDrop={handleDrop}
          className={`flex min-h-[280px] flex-col items-center justify-center gap-3 rounded-lg border-2 border-dashed p-6 text-center transition ${
            dragOver ? 'border-blue-400 bg-blue-50' : 'border-slate-300 bg-slate-50'
          } ${uploading ? 'cursor-not-allowed opacity-60' : 'cursor-pointer hover:border-blue-300 hover:bg-blue-50/60'}`}
        >
          <UploadIcon />
          <div>
            <p className="text-base font-semibold text-slate-800">이미지를 드래그하거나 클릭해서 업로드해 주세요.</p>
            <p className="mt-2 text-sm text-slate-500">JPG, PNG, WEBP 이미지 파일만 지원합니다.</p>
            <p className="mt-1 text-xs text-slate-400">영상 파일과 수동 프레임 업로드는 MVP 범위에서 제외됩니다.</p>
          </div>
          <button type="button" className="btn-blue mt-2" disabled={uploading}>
            이미지 선택
          </button>
          <input
            ref={inputRef}
            className="hidden"
            type="file"
            accept="image/jpeg,image/png,image/webp"
            disabled={uploading}
            onChange={(event) => onFileSelect(event.target.files?.[0] ?? null)}
          />
        </div>

        <SelectedFilePanel selectedFile={selectedFile} uploading={uploading} onClear={() => onFileSelect(null)} />
      </div>

      {noticeMessage ? <Alert variant="success">{noticeMessage}</Alert> : null}
      {errorMessage ? <Alert variant="error">{errorMessage}</Alert> : null}
    </Card>
  )
}

function SelectedFilePanel({
  selectedFile,
  uploading,
  onClear,
}: {
  selectedFile: SelectedInspectionFile | null
  uploading: boolean
  onClear: () => void
}) {
  return (
    <aside className="rounded-lg border border-slate-200 bg-white p-4">
      <div className="flex items-center justify-between gap-2">
        <h3 className="text-sm font-semibold text-slate-800">선택한 파일</h3>
        <button
          type="button"
          className="text-xs font-medium text-slate-500 hover:text-red-600 disabled:cursor-not-allowed disabled:opacity-40"
          disabled={!selectedFile || uploading}
          onClick={onClear}
        >
          제거
        </button>
      </div>

      {!selectedFile ? (
        <p className="mt-10 rounded-md bg-slate-50 px-4 py-8 text-center text-sm text-slate-500">
          선택한 이미지가 없습니다.
        </p>
      ) : (
        <div className="mt-4 space-y-4">
          <div className="h-32 overflow-hidden rounded-md border border-slate-200 bg-slate-100">
            <img src={selectedFile.previewUrl} alt="선택한 이미지 미리보기" className="h-full w-full object-cover" />
          </div>
          <div className="min-w-0 space-y-2 text-sm">
            <p className="truncate font-semibold text-slate-900" title={selectedFile.file.name}>
              {selectedFile.file.name}
            </p>
            <Badge tone="blue">이미지</Badge>
            <InfoRow label="해상도" value={formatResolution(selectedFile)} />
            <InfoRow label="크기" value={formatFileSize(selectedFile.file.size)} />
            <InfoRow label="선택 시각" value={formatDateTime(selectedFile.selectedAt)} />
          </div>
        </div>
      )}
    </aside>
  )
}

export function PreviewCard({
  selectedFile,
  uploadResult,
}: {
  selectedFile: SelectedInspectionFile | null
  uploadResult: UploadInspectionResponse | null
}) {
  return (
    <Card title="2. 미리보기" action={selectedFile ? <Badge tone="blue">이미지</Badge> : null}>
      <div className="relative flex min-h-[360px] items-center justify-center overflow-hidden rounded-lg border border-slate-200 bg-slate-50">
        {!selectedFile ? (
          <p className="text-sm text-slate-500">이미지를 선택하면 미리보기가 표시됩니다.</p>
        ) : (
          <img src={selectedFile.previewUrl} alt="검사 이미지 미리보기" className="max-h-[420px] w-full object-contain" />
        )}

        {uploadResult ? (
          <div className="absolute left-4 top-4 flex gap-2">
            <Badge tone="green">요청 접수 완료</Badge>
            <Badge tone="blue">PROCESSING</Badge>
          </div>
        ) : null}
      </div>
    </Card>
  )
}

export function InspectionRunCard({
  selectedFile,
  targetOptions,
  thresholdOptions,
  modelOptions,
  selectedTargetId,
  selectedThresholdId,
  selectedDeploymentId,
  loadingOptions,
  loadingModels,
  uploading,
  onTargetChange,
  onThresholdChange,
  onDeploymentChange,
  onSubmit,
  onSettingClick,
}: {
  selectedFile: SelectedInspectionFile | null
  targetOptions: AnalysisTargetOption[]
  thresholdOptions: ThresholdOption[]
  modelOptions: AvailableInspectionModel[]
  selectedTargetId: number | null
  selectedThresholdId: number | null
  selectedDeploymentId: number | null
  loadingOptions: boolean
  loadingModels: boolean
  uploading: boolean
  onTargetChange: (value: number | null) => void
  onThresholdChange: (value: number | null) => void
  onDeploymentChange: (value: number | null) => void
  onSubmit: () => void
  onSettingClick: () => void
}) {
  const thresholdSelectable = thresholdOptions.some((option) => option.id)

  return (
    <Card title="3. 검사 실행">
      <div className="grid grid-cols-2 gap-4">
        <div className="space-y-4">
          <Field label="배포 모델">
            <div className="flex gap-2">
              <select
                className="control min-w-0 flex-1"
                value={selectedDeploymentId ?? ''}
                onChange={(event) => onDeploymentChange(event.target.value ? Number(event.target.value) : null)}
                disabled={uploading || loadingModels}
              >
                <option value="">
                  {loadingModels ? '배포 모델 목록을 불러오는 중입니다.' : '배포 모델을 선택해 주세요.'}
                </option>
                {modelOptions.map((model) => (
                  <option key={model.deploymentId} value={model.deploymentId}>
                    {model.displayName}
                  </option>
                ))}
              </select>
              <button type="button" className="btn-secondary" onClick={onSettingClick} disabled={uploading}>
                설정
              </button>
            </div>
            {modelOptions.length === 0 && !loadingModels ? (
              <p className="mt-1 text-xs text-slate-500">
                사용 가능한 배포 모델이 없습니다. 모델 생성 후 배포 상태를 확인해 주세요.
              </p>
            ) : null}
          </Field>

          <Field label="검사 대상">
            <select
              className="control w-full"
              value={selectedTargetId ?? ''}
              onChange={(event) => onTargetChange(event.target.value ? Number(event.target.value) : null)}
              disabled={uploading || loadingOptions}
            >
              <option value="">검사 대상을 선택해 주세요.</option>
              {targetOptions.map((target) => (
                <option key={target.id} value={target.id}>
                  {target.name}
                </option>
              ))}
            </select>
          </Field>

          <Field label="임계값">
            <select
              className="control w-full"
              value={selectedThresholdId ?? ''}
              onChange={(event) => onThresholdChange(event.target.value ? Number(event.target.value) : null)}
              disabled={uploading || loadingOptions || !thresholdSelectable}
            >
              <option value="">기본 임계값 사용</option>
              {thresholdOptions
                .filter((threshold) => threshold.id)
                .map((threshold) => (
                  <option key={threshold.id} value={threshold.id}>
                    {threshold.name}
                  </option>
                ))}
            </select>
            {!thresholdSelectable && thresholdOptions[0] ? (
              <p className="mt-1 text-xs text-slate-500">
                조회된 임계값 중 선택 가능한 항목이 없어 기본 정책으로 요청합니다.
              </p>
            ) : null}
          </Field>
        </div>

        <div className="flex flex-col justify-between gap-4">
          <button
            type="button"
            className="h-12 w-full rounded-md bg-blue-600 text-sm font-semibold text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-50"
            disabled={!selectedFile || !selectedDeploymentId || uploading}
            onClick={onSubmit}
          >
            {uploading ? '요청 중...' : '검사 실행'}
          </button>

          <div className="space-y-1 rounded-md bg-slate-50 p-3 text-xs leading-5 text-slate-500">
            <p>입력 모드: IMAGE</p>
            <p>입력 출처: 이미지 업로드</p>
            <p>선택 값: deploymentId 기반 모델 해석</p>
            <p>ROI 모드: FULL_FRAME</p>
          </div>
        </div>
      </div>
    </Card>
  )
}

export function ProgressStatusCard({
  selectedFile,
  uploading,
  uploadResult,
  requestDurationMs,
}: {
  selectedFile: SelectedInspectionFile | null
  uploading: boolean
  uploadResult: UploadInspectionResponse | null
  requestDurationMs: number | null
}) {
  const steps = computeProgressSteps({ selectedFile, uploading, uploadResult })
  const completedCount = steps.filter((step) => step.state === 'complete').length
  const percent = Math.round((completedCount / steps.length) * 100)
  const elapsed = formatElapsedTime(requestDurationMs)
  const summaryLabel = uploadResult ? '분석 완료' : uploading ? '분석 중' : '대기 중'

  return (
    <Card title="4. 진행 상태">
      <div className="flex items-stretch gap-5">
        <ol className="min-w-0 flex-1 space-y-0">
          {steps.map((step, index) => (
            <li key={step.label} className="relative">
              <div className="flex items-center gap-3">
                <ProgressStepCircle state={step.state} />
                <span
                  className={`text-sm font-medium ${
                    step.state === 'pending' ? 'text-slate-400' : 'text-slate-700'
                  }`}
                >
                  {step.label}
                </span>
              </div>
              {index < steps.length - 1 ? (
                <span
                  aria-hidden="true"
                  className={`ml-[11px] block h-5 w-px ${
                    step.state === 'complete' ? 'bg-emerald-300' : 'bg-slate-200'
                  }`}
                />
              ) : null}
            </li>
          ))}
        </ol>

        <div className="flex shrink-0 flex-col items-center justify-center gap-2">
          <div className="flex h-24 w-24 items-center justify-center rounded-full border-[10px] border-blue-100 bg-white">
            <p className="text-lg font-bold text-blue-700">{percent}%</p>
          </div>
          <div className="text-center">
            <p className="text-xs text-slate-500">{summaryLabel}</p>
            <p className="font-mono text-sm font-semibold text-slate-700">{elapsed}</p>
          </div>
        </div>
      </div>
    </Card>
  )
}

function ProgressStepCircle({ state }: { state: ProgressStepState }) {
  if (state === 'complete') {
    return (
      <span className="flex h-[22px] w-[22px] shrink-0 items-center justify-center rounded-full bg-emerald-500 text-white">
        <svg
          className="h-3 w-3"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth={3}
          strokeLinecap="round"
          strokeLinejoin="round"
        >
          <path d="M5 12l5 5L20 7" />
        </svg>
      </span>
    )
  }
  if (state === 'progress') {
    return (
      <span className="flex h-[22px] w-[22px] shrink-0 items-center justify-center rounded-full border-2 border-blue-500 bg-blue-50">
        <span className="h-2 w-2 rounded-full bg-blue-500" />
      </span>
    )
  }
  return (
    <span className="flex h-[22px] w-[22px] shrink-0 items-center justify-center rounded-full border-2 border-slate-200 bg-white" />
  )
}

export function ResultSummaryCard({
  uploadResult,
  selectedThreshold,
  onGoResults,
  onReset,
}: {
  uploadResult: UploadInspectionResponse | null
  selectedThreshold: ThresholdOption | null
  onGoResults: () => void
  onReset: () => void
}) {
  return (
    <Card title="5. 검사 요청 요약">
      {!uploadResult ? (
        <Placeholder
          title="아직 검사 요청이 없습니다."
          description="이미지를 업로드하고 검사 실행 버튼을 눌러 주세요."
        />
      ) : (
        <div className="space-y-4">
          <Alert variant="success">검사 요청이 접수되었습니다.</Alert>
          <div className="grid gap-3 text-sm">
            <InfoRow label="inspectionId" value={String(uploadResult.inspectionId)} />
            <InfoRow label="runStatus" value={uploadResult.runStatus} />
            <InfoRow label="임계값" value={formatThreshold(selectedThreshold)} />
          </div>
          <div className="rounded-md border border-dashed border-slate-300 bg-slate-50 p-4 text-sm text-slate-500">
            AI 분석이 완료되면 결과 목록과 상세 화면에서 점수, 판정, 설명을 확인할 수 있습니다.
          </div>
          <div className="flex flex-wrap gap-2">
            <button type="button" className="btn-secondary" onClick={onGoResults}>
              결과 목록으로 이동
            </button>
            <button type="button" className="btn-blue" onClick={onReset}>
              새 검사
            </button>
          </div>
        </div>
      )}
    </Card>
  )
}

export function AnalysisInsightCard({
  uploadResult,
  onGoResults,
}: {
  uploadResult: UploadInspectionResponse | null
  onGoResults: () => void
}) {
  return (
    <Card title="6. 분석 안내">
      {!uploadResult ? (
        <Placeholder
          title="분석 안내는 검사 요청 후 표시됩니다."
          description="현재 화면에서는 업로드 가능한 이미지 범위와 요청 흐름만 안내합니다."
        />
      ) : (
        <div className="space-y-3 text-sm text-slate-600">
          <Alert variant="success">검사 요청이 접수되었습니다.</Alert>
          <p>현재 AI 분석 결과 생성 대기 중입니다.</p>
          <p>결과가 생성되면 이상 점수, 판정 결과, 근거 설명을 결과 화면에서 확인할 수 있습니다.</p>
          <div className="flex flex-wrap gap-2 pt-2">
            <button type="button" className="btn-blue" onClick={onGoResults}>
              검사 이력 보기
            </button>
          </div>
        </div>
      )}
    </Card>
  )
}

function Card({
  title,
  action,
  className = '',
  children,
}: {
  title: string
  action?: ReactNode
  className?: string
  children: ReactNode
}) {
  return (
    <section className={`rounded-lg border border-slate-200 bg-white p-5 shadow-sm ${className}`}>
      <div className="mb-4 flex items-center justify-between gap-3">
        <h2 className="text-base font-semibold text-slate-900">{title}</h2>
        {action}
      </div>
      {children}
    </section>
  )
}

function Field({ label, children }: { label: string; children: ReactNode }) {
  return (
    <label className="block">
      <span className="mb-1.5 block text-sm font-medium text-slate-700">{label}</span>
      {children}
    </label>
  )
}

function Alert({ variant, children }: { variant: 'success' | 'error'; children: ReactNode }) {
  const color =
    variant === 'success'
      ? 'border-emerald-200 bg-emerald-50 text-emerald-700'
      : 'border-red-200 bg-red-50 text-red-700'
  return <p className={`mt-4 rounded-md border px-3 py-2 text-sm ${color}`}>{children}</p>
}

function Placeholder({ title, description }: { title: string; description: string }) {
  return (
    <div className="rounded-md border border-dashed border-slate-300 bg-slate-50 px-4 py-8 text-center">
      <p className="font-medium text-slate-700">{title}</p>
      <p className="mt-2 text-sm text-slate-500">{description}</p>
      <div className="mt-5 space-y-2">
        <div className="h-2 rounded bg-slate-200" />
        <div className="h-2 w-2/3 rounded bg-slate-200" />
      </div>
    </div>
  )
}

function Badge({ tone, children }: { tone: 'blue' | 'green'; children: ReactNode }) {
  const color = tone === 'green' ? 'bg-emerald-100 text-emerald-700' : 'bg-blue-100 text-blue-700'
  return <span className={`inline-flex rounded-full px-2.5 py-1 text-xs font-semibold ${color}`}>{children}</span>
}

function InfoRow({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex items-center justify-between gap-3">
      <span className="text-slate-500">{label}</span>
      <span className="min-w-0 truncate text-right font-medium text-slate-800">{value}</span>
    </div>
  )
}

function formatResolution(selectedFile: SelectedInspectionFile) {
  if (!selectedFile.width || !selectedFile.height) return '-'
  return `${selectedFile.width} x ${selectedFile.height}`
}

function formatFileSize(size: number) {
  if (size < 1024) return `${size}B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)}KB`
  if (size < 1024 * 1024 * 1024) return `${(size / 1024 / 1024).toFixed(2)}MB`
  return `${(size / 1024 / 1024 / 1024).toFixed(2)}GB`
}

function formatDateTime(value: Date) {
  return value.toLocaleString('ko-KR', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function formatThreshold(threshold: ThresholdOption | null) {
  if (!threshold) return '기본 임계값 사용'
  const detail = threshold.anomalyThreshold == null ? '' : ` (${threshold.anomalyThreshold})`
  return `${threshold.name}${detail}`
}

function formatStringDateTime(value?: string) {
  if (!value) return '-'
  return value.slice(0, 16).replace('T', ' ')
}

function UploadIcon() {
  return (
    <svg className="h-12 w-12 text-blue-500" fill="none" viewBox="0 0 48 48" stroke="currentColor" strokeWidth={1.6}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M24 31V12m0 0-7 7m7-7 7 7" />
      <path strokeLinecap="round" d="M12 30v4a4 4 0 0 0 4 4h16a4 4 0 0 0 4-4v-4" />
    </svg>
  )
}
