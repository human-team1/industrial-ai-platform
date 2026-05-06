import type { DragEvent, ReactNode, RefObject } from 'react'
import { useRef, useState } from 'react'
import { formatElapsedTime } from '../../../shared/lib/date'
import { computeProgressSteps } from '../model'
import type {
  AnalysisTargetOption,
  BrowserCameraDevice,
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
  isCameraReady,
  isPreparing,
  isCapturing,
  canCapture,
  requestDurationMs,
  onCapture,
}: {
  isCameraReady: boolean
  isPreparing: boolean
  isCapturing: boolean
  canCapture: boolean
  requestDurationMs: number | null
  onCapture: () => void
}) {
  const statusLabel = isCapturing
    ? '캡처 검사 진행 중'
    : isCameraReady
      ? '카메라 준비 완료'
      : isPreparing
        ? '카메라 연결 중'
        : '대기 중'

  return (
    <section className="flex flex-wrap items-center justify-between gap-3 rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <div className="flex flex-wrap items-center gap-4">
        <span className="inline-flex items-center gap-2 text-sm font-semibold text-slate-700">
          <span className={`h-2.5 w-2.5 rounded-full ${isCameraReady ? 'bg-emerald-500' : 'bg-slate-400'}`} />
          {statusLabel}
        </span>
        <span className="text-sm text-slate-500">
          최근 요청 시간: {requestDurationMs == null ? '-' : `${(requestDurationMs / 1000).toFixed(2)}초`}
        </span>
      </div>
      <div className="flex gap-2">
        <button
          type="button"
          className="btn-blue"
          disabled={!canCapture || isPreparing || isCapturing}
          onClick={onCapture}
        >
          {isCapturing ? '검사 요청 중...' : '현재 화면 검사'}
        </button>
      </div>
    </section>
  )
}

export function LiveStreamPanel({
  videoRef,
  devices,
  selectedDeviceId,
  isCameraReady,
  isCameraLoading,
  onSelectDevice,
}: {
  videoRef: RefObject<HTMLVideoElement>
  devices: BrowserCameraDevice[]
  selectedDeviceId: string | null
  isCameraReady: boolean
  isCameraLoading: boolean
  onSelectDevice: (deviceId: string | null) => void
}) {
  return (
    <Card title="카메라 프리뷰" className="min-h-[520px]">
      <div className="mb-4 grid gap-3 md:grid-cols-[minmax(0,1fr)_240px]">
        <label className="block">
          <span className="mb-1.5 block text-sm font-medium text-slate-700">브라우저 카메라</span>
          <select
            className="control w-full"
            value={selectedDeviceId ?? ''}
            onChange={(event) => onSelectDevice(event.target.value || null)}
            disabled={isCameraLoading || devices.length === 0}
          >
            <option value="">카메라를 선택해 주세요</option>
            {devices.map((device) => (
              <option key={device.deviceId} value={device.deviceId}>
                {device.label}
              </option>
            ))}
          </select>
        </label>
        <div className="rounded-lg border border-blue-100 bg-blue-50 px-4 py-3 text-sm text-blue-800">
          지속 스트리밍 업로드는 이번 MVP에서 사용하지 않습니다. 버튼을 누른 순간의 프레임 1장만 캡처합니다.
        </div>
      </div>

      <div className="relative flex min-h-[440px] items-center justify-center overflow-hidden rounded-lg bg-slate-950 text-slate-300">
        <video ref={videoRef} autoPlay muted playsInline className="h-full max-h-[520px] w-full object-contain" />

        <div className="pointer-events-none absolute inset-[18%_22%] rounded-2xl border-2 border-dashed border-amber-300/90 shadow-[0_0_0_9999px_rgba(15,23,42,0.28)]">
          <div className="absolute -top-7 left-0 rounded-full bg-amber-300 px-3 py-1 text-[11px] font-semibold text-slate-900">
            중앙 고정 영역
          </div>
        </div>

        {!isCameraReady ? (
          <div className="pointer-events-none absolute inset-0 flex items-center justify-center bg-slate-950/75 px-6 text-center">
            <p className="text-base font-semibold">
              {isCameraLoading
                ? '카메라 프리뷰를 준비하고 있습니다.'
                : '브라우저 카메라 권한을 허용하면 프리뷰가 표시됩니다.'}
            </p>
          </div>
        ) : null}

        <div className="absolute left-4 top-4">
          <Badge tone={isCameraReady ? 'green' : 'blue'}>
            {isCameraReady ? 'PREVIEW READY' : 'WAITING'}
          </Badge>
        </div>

        <div className="absolute inset-x-0 bottom-0 flex items-center justify-between bg-black/55 px-4 py-3 text-xs text-slate-200">
          <div className="flex gap-4">
            <span>{isCameraReady ? 'LIVE PREVIEW' : 'READY'}</span>
            <span>캡처 방식: 단건 이미지</span>
          </div>
          <span>{new Date().toLocaleTimeString('ko-KR')}</span>
        </div>
      </div>

      <div className="mt-4 rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-900">
        ROI UI가 아직 없어 이번 MVP에서는 <code>FULL_FRAME</code>으로 전송합니다. 검사 대상이 중앙 고정 영역 안에 오도록 배치해 주세요.
      </div>
    </Card>
  )
}

export function CurrentDetectionResultCard({
  uploadResult,
}: {
  uploadResult: UploadInspectionResponse | null
}) {
  return (
    <Card title="현재 검사 상태">
      <div className="space-y-4 text-sm">
        <InfoRow label="입력 출처" value={uploadResult ? '카메라 캡처' : '-'} />
        <InfoRow label="inspectionId" value={uploadResult ? String(uploadResult.inspectionId) : '-'} />
        <InfoRow label="runStatus" value={uploadResult?.runStatus ?? '대기'} />
        <div className="rounded-md border border-dashed border-slate-300 bg-slate-50 p-3 text-xs text-slate-500">
          품질 실패나 경계 구간 응답은 결과 화면에서 재검사 필요로 표시됩니다.
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
      title="최근 검사 이벤트"
      action={
        <button type="button" className="text-xs font-semibold text-blue-600" onClick={onRefresh}>
          새로고침
        </button>
      }
    >
      {events.length === 0 ? (
        <p className="rounded-md bg-slate-50 px-4 py-8 text-center text-sm text-slate-500">
          최근 검사 이벤트가 없습니다.
        </p>
      ) : (
        <div className="space-y-3">
          {events.slice().reverse().slice(0, 5).map((event) => (
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

export function EquipmentInfoCard({ selectedDeviceLabel }: { selectedDeviceLabel: string | null }) {
  return (
    <Card title="입력 정보">
      <div className="space-y-3 text-sm">
        <InfoRow label="입력 방식" value="브라우저 카메라 캡처" />
        <InfoRow label="선택 카메라" value={selectedDeviceLabel ?? '-'} />
        <InfoRow label="ROI 모드" value="FULL_FRAME" />
        <InfoRow label="품질 게이트" value="사용" />
        <InfoRow label="프레임 수" value="1" />
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
            <p className="text-base font-semibold text-slate-800">
              이미지를 드래그하거나 클릭해서 업로드해 주세요
            </p>
            <p className="mt-2 text-sm text-slate-500">JPG, PNG, WEBP 이미지 파일만 지원합니다.</p>
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

        <SelectedFilePanel
          selectedFile={selectedFile}
          uploading={uploading}
          onClear={() => onFileSelect(null)}
        />
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
        <h3 className="text-sm font-semibold text-slate-800">선택된 파일</h3>
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
          선택된 이미지가 없습니다.
        </p>
      ) : (
        <div className="mt-4 space-y-4">
          <div className="h-32 overflow-hidden rounded-md border border-slate-200 bg-slate-100">
            <img
              src={selectedFile.previewUrl}
              alt="선택한 이미지 미리보기"
              className="h-full w-full object-cover"
            />
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
    <Card
      title="2. 미리보기"
      action={selectedFile ? <Badge tone="blue">이미지</Badge> : null}
    >
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
  selectedTargetId,
  selectedThresholdId,
  selectedModel,
  loadingOptions,
  uploading,
  onTargetChange,
  onThresholdChange,
  onModelChange,
  onSubmit,
  onSettingClick,
}: {
  selectedFile: SelectedInspectionFile | null
  targetOptions: AnalysisTargetOption[]
  thresholdOptions: ThresholdOption[]
  selectedTargetId: number | null
  selectedThresholdId: number | null
  selectedModel: string
  loadingOptions: boolean
  uploading: boolean
  onTargetChange: (value: number | null) => void
  onThresholdChange: (value: number | null) => void
  onModelChange: (value: string) => void
  onSubmit: () => void
  onSettingClick: () => void
}) {
  const thresholdSelectable = thresholdOptions.some((option) => option.id)

  return (
    <Card title="3. 검사 실행">
      <div className="grid grid-cols-2 gap-4">
        <div className="space-y-4">
          <Field label="검사 모델">
            <div className="flex gap-2">
              <select
                className="control min-w-0 flex-1"
                value={selectedModel}
                onChange={(event) => onModelChange(event.target.value)}
                disabled={uploading}
              >
                <option value="default">기본 이상 탐지 모델</option>
                <option value="conveyor">컨베이어 벨트 이상 탐지 모델</option>
              </select>
              <button type="button" className="btn-secondary" onClick={onSettingClick} disabled={uploading}>
                설정
              </button>
            </div>
          </Field>

          <Field label="검사 대상">
            <select
              className="control w-full"
              value={selectedTargetId ?? ''}
              onChange={(event) => onTargetChange(event.target.value ? Number(event.target.value) : null)}
              disabled={uploading || loadingOptions}
            >
              <option value="">검사 대상을 선택해 주세요</option>
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

        <div className="flex items-center justify-center">
          <button
            type="button"
            className="h-12 w-full rounded-md bg-blue-600 text-sm font-semibold text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-50"
            disabled={!selectedFile || uploading}
            onClick={onSubmit}
          >
            {uploading ? '요청 중...' : '검사 실행'}
          </button>
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
  const completedCount = steps.filter((s) => s.state === 'complete').length
  const percent = Math.round((completedCount / steps.length) * 100)
  const elapsed = formatElapsedTime(requestDurationMs)
  const summaryLabel = uploadResult ? '분석 완료' : uploading ? '분석 중' : '대기 중'

  return (
    <Card title="4. 진행 상태">
      <div className="flex items-stretch gap-5">
        <ol className="min-w-0 flex-1 space-y-0">
          {steps.map((step, i) => (
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
              {i < steps.length - 1 ? (
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
        <svg className="h-3 w-3" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={3} strokeLinecap="round" strokeLinejoin="round">
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
            AI 분석이 완료되면 결과 목록과 상세 화면에서 판정, 시각화, 설명을 확인할 수 있습니다.
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
