import type { DragEvent, ReactNode } from 'react'
import { useRef, useState } from 'react'
import type {
  AnalysisTargetOption,
  CameraSource,
  InspectionEvent,
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
  isRunning,
  isStarting,
  isStopping,
  canStart,
  elapsedSeconds,
  runStatus,
  onStart,
  onStop,
  onSnapshot,
}: {
  isRunning: boolean
  isStarting: boolean
  isStopping: boolean
  canStart: boolean
  elapsedSeconds: number
  runStatus: string
  onStart: () => void
  onStop: () => void
  onSnapshot: () => void
}) {
  const statusLabel = isRunning ? '라이브 스트리밍' : runStatus === 'STOPPED' ? '중지됨' : '대기 중'

  return (
    <section className="flex flex-wrap items-center justify-between gap-3 rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <div className="flex flex-wrap items-center gap-4">
        <span className="inline-flex items-center gap-2 text-sm font-semibold text-slate-700">
          <span className={`h-2.5 w-2.5 rounded-full ${isRunning ? 'bg-emerald-500' : 'bg-slate-400'}`} />
          {statusLabel}
        </span>
        <span className="font-mono text-sm text-slate-500">{formatElapsed(elapsedSeconds)}</span>
        {isRunning ? <Badge tone="blue">PROCESSING</Badge> : null}
      </div>
      <div className="flex gap-2">
        <button type="button" className="btn-blue" disabled={!canStart || isStarting || isRunning} onClick={onStart}>
          {isStarting ? '시작 중...' : '시작'}
        </button>
        <button type="button" className="btn-secondary" disabled={!isRunning || isStopping} onClick={onStop}>
          {isStopping ? '중지 중...' : '중지'}
        </button>
        <button type="button" className="btn-secondary" onClick={onSnapshot} disabled>
          스냅샷
        </button>
      </div>
    </section>
  )
}

export function LiveStreamPanel({
  selectedCamera,
  isRunning,
}: {
  selectedCamera: CameraSource | null
  isRunning: boolean
}) {
  const streamUrl = selectedCamera?.streamUrl
  const canRenderMedia = Boolean(streamUrl?.startsWith('http://') || streamUrl?.startsWith('https://'))

  return (
    <Card title="라이브 영상" className="min-h-[520px]">
      <div className="relative flex min-h-[440px] items-center justify-center overflow-hidden rounded-lg bg-slate-950 text-slate-300">
        {isRunning && canRenderMedia ? (
          <video src={streamUrl} controls autoPlay muted className="h-full max-h-[520px] w-full object-contain" />
        ) : (
          <div className="px-6 text-center">
            <p className="text-base font-semibold">
              {!selectedCamera
                ? '카메라를 선택하고 시작 버튼을 눌러주세요.'
                : isRunning
                  ? '실시간 스트림 연결 대기 중입니다.'
                  : '실시간 탐지를 시작하려면 카메라를 선택하고 시작 버튼을 눌러주세요.'}
            </p>
            {isRunning ? <p className="mt-3 text-sm text-blue-200">AI 분석 연동 준비 중</p> : null}
          </div>
        )}

        {isRunning ? (
          <div className="absolute left-4 top-4">
            <Badge tone="green">탐지 결과 대기 중</Badge>
          </div>
        ) : null}

        <div className="absolute inset-x-0 bottom-0 flex items-center justify-between bg-black/55 px-4 py-3 text-xs text-slate-200">
          <div className="flex gap-4">
            <span>{isRunning ? 'LIVE' : 'READY'}</span>
            <span>FPS -</span>
            <span>해상도 -</span>
          </div>
          <span>{new Date().toLocaleTimeString('ko-KR')}</span>
        </div>
      </div>
    </Card>
  )
}

export function CameraList({
  cameras,
  selectedCameraId,
  loading,
  onSelect,
}: {
  cameras: CameraSource[]
  selectedCameraId: number | null
  loading: boolean
  onSelect: (cameraId: number) => void
}) {
  return (
    <Card title="카메라 목록">
      {loading ? (
        <p className="text-sm text-slate-500">카메라 목록을 불러오는 중입니다.</p>
      ) : cameras.length === 0 ? (
        <p className="rounded-md bg-slate-50 px-4 py-8 text-center text-sm text-slate-500">
          등록된 카메라가 없습니다.
        </p>
      ) : (
        <div className="flex gap-3 overflow-x-auto pb-2">
          {cameras.map((camera) => {
            const selected = camera.cameraId === selectedCameraId
            return (
              <button
                key={camera.cameraId}
                type="button"
                onClick={() => onSelect(camera.cameraId)}
                className={`min-w-[210px] rounded-lg border bg-white p-3 text-left transition ${
                  selected ? 'border-blue-500 ring-2 ring-blue-100' : 'border-slate-200 hover:border-blue-200'
                }`}
              >
                <div className="mb-3 flex h-24 items-center justify-center rounded-md bg-slate-100 text-slate-400">
                  <VideoIcon />
                </div>
                <p className="truncate text-sm font-semibold text-slate-900">{camera.cameraName}</p>
                <p className="mt-1 truncate text-xs text-slate-500">{camera.streamUrl ?? '-'}</p>
                <span className="mt-3 inline-flex items-center gap-1.5 text-xs text-slate-600">
                  <span className={`h-2 w-2 rounded-full ${isCameraActive(camera) ? 'bg-emerald-500' : 'bg-slate-400'}`} />
                  {camera.status ?? '-'}
                </span>
              </button>
            )
          })}
          <div className="flex min-w-[160px] items-center justify-center rounded-lg border border-dashed border-slate-300 p-3 text-sm text-slate-400">
            카메라 추가 준비 중
          </div>
        </div>
      )}
    </Card>
  )
}

export function CurrentDetectionResultCard({
  isRunning,
  runStatus,
}: {
  isRunning: boolean
  runStatus: string
}) {
  return (
    <Card title="현재 탐지 결과">
      <div className="space-y-4 text-sm">
        <InfoRow label="이상점수" value={isRunning ? '분석 대기' : runStatus === 'STOPPED' ? '결과 없음' : '-'} />
        <InfoRow label="판정 결과" value={isRunning ? 'PROCESSING' : runStatus === 'STOPPED' ? '결과 없음' : '대기'} />
        <InfoRow label="신뢰도" value="-" />
        <div className="rounded-md border border-dashed border-slate-300 bg-slate-50 p-3 text-xs text-slate-500">
          실제 AI 결과 API 연결 후 score, confidence, decisionCode를 표시합니다.
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
      action={<button type="button" className="text-xs font-semibold text-blue-600" onClick={onRefresh}>새로고침</button>}
    >
      {events.length === 0 ? (
        <p className="rounded-md bg-slate-50 px-4 py-8 text-center text-sm text-slate-500">
          최근 탐지 이벤트가 없습니다.
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

export function EquipmentInfoCard({ selectedCamera }: { selectedCamera: CameraSource | null }) {
  return (
    <Card title="검사 중 설비 정보">
      <div className="space-y-3 text-sm">
        <InfoRow label="설비명" value={selectedCamera?.cameraName ?? '-'} />
        <InfoRow label="설비ID" value={selectedCamera ? String(selectedCamera.cameraId) : '-'} />
        <InfoRow label="설비 유형" value="-" />
        <InfoRow label="위치" value="-" />
        <InfoRow label="상태" value={selectedCamera?.status ?? '-'} />
        <InfoRow label="최근 점검일" value="-" />
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
    <Card title="1. 파일 업로드" className="xl:col-span-7">
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
              파일을 드래그하거나 클릭하여 업로드하세요
            </p>
            <p className="mt-2 text-sm text-slate-500">
              이미지 (JPG, PNG) 또는 비디오 (MP4, MOV, AVI)
            </p>
            <p className="mt-1 text-xs text-slate-400">최대 2GB</p>
          </div>
          <button type="button" className="btn-blue mt-2" disabled={uploading}>
            파일 선택
          </button>
          <input
            ref={inputRef}
            className="hidden"
            type="file"
            accept=".jpg,.jpeg,.png,.mp4,.mov,.avi,image/jpeg,image/png,video/mp4,video/quicktime,video/x-msvideo"
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
        <h3 className="text-sm font-semibold text-slate-800">
          선택된 파일 ({selectedFile ? 1 : 0})
        </h3>
        <button
          type="button"
          className="text-xs font-medium text-slate-500 hover:text-red-600 disabled:cursor-not-allowed disabled:opacity-40"
          disabled={!selectedFile || uploading}
          onClick={onClear}
        >
          모두 삭제
        </button>
      </div>

      {!selectedFile ? (
        <p className="mt-10 rounded-md bg-slate-50 px-4 py-8 text-center text-sm text-slate-500">
          선택된 파일이 없습니다.
        </p>
      ) : (
        <div className="mt-4 space-y-4">
          <div className="h-32 overflow-hidden rounded-md border border-slate-200 bg-slate-100">
            {selectedFile.fileKind === 'image' ? (
              <img
                src={selectedFile.previewUrl}
                alt="선택된 파일 썸네일"
                className="h-full w-full object-cover"
              />
            ) : (
              <div className="flex h-full items-center justify-center text-slate-400">
                <VideoIcon />
              </div>
            )}
          </div>
          <div className="min-w-0 space-y-2 text-sm">
            <p className="truncate font-semibold text-slate-900" title={selectedFile.file.name}>
              {selectedFile.file.name}
            </p>
            <Badge tone="blue">{selectedFile.fileKind === 'image' ? '이미지' : '비디오'}</Badge>
            <InfoRow label="해상도" value={formatResolution(selectedFile)} />
            <InfoRow label="크기" value={formatFileSize(selectedFile.file.size)} />
            <InfoRow label="업로드 시간" value={formatDateTime(selectedFile.selectedAt)} />
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
      className="xl:col-span-5"
      action={selectedFile ? <Badge tone="blue">{selectedFile.fileKind === 'image' ? '이미지' : '비디오'}</Badge> : null}
    >
      <div className="relative flex min-h-[360px] items-center justify-center overflow-hidden rounded-lg border border-slate-200 bg-slate-50">
        {!selectedFile ? (
          <p className="text-sm text-slate-500">파일을 선택하면 미리보기가 표시됩니다.</p>
        ) : selectedFile.fileKind === 'image' ? (
          <img src={selectedFile.previewUrl} alt="검사 파일 미리보기" className="max-h-[420px] w-full object-contain" />
        ) : (
          <video src={selectedFile.previewUrl} controls className="max-h-[420px] w-full" />
        )}

        {uploadResult ? (
          <div className="absolute left-4 top-4 flex gap-2">
            <Badge tone="green">분석 요청 접수됨</Badge>
            <Badge tone="blue">결과 생성 대기 중</Badge>
          </div>
        ) : null}
      </div>

      <div className="mt-3 flex items-center justify-end gap-2 text-xs text-slate-500">
        <span className="rounded border border-slate-200 px-2 py-1">100%</span>
        {/* TODO: 실제 줌/전체화면 동작은 결과 뷰어 구현 시 연결한다. */}
        <button type="button" className="icon-button" disabled>−</button>
        <button type="button" className="icon-button" disabled>+</button>
        <button type="button" className="icon-button" disabled>⛶</button>
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
    <Card title="3. 탐지 실행" className="xl:col-span-4">
      <div className="space-y-4">
        <Field label="탐지모델">
          <div className="flex gap-2">
            <select className="control min-w-0 flex-1" value={selectedModel} onChange={(event) => onModelChange(event.target.value)} disabled={uploading}>
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
            <option value="">검사 대상 선택 안 함</option>
            {targetOptions.map((target) => (
              <option key={target.id} value={target.id}>{target.name}</option>
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
            {thresholdOptions.filter((threshold) => threshold.id).map((threshold) => (
              <option key={threshold.id} value={threshold.id}>{threshold.name}</option>
            ))}
          </select>
          {!thresholdSelectable && thresholdOptions[0] ? (
            <p className="mt-1 text-xs text-slate-500">
              현재 조회된 임계값은 ID가 없어 업로드 요청에는 포함하지 않습니다.
            </p>
          ) : null}
        </Field>

        <button
          type="button"
          className="h-12 w-full rounded-md bg-blue-600 text-sm font-semibold text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-50"
          disabled={!selectedFile || uploading}
          onClick={onSubmit}
        >
          {uploading ? '요청 중...' : '탐지 실행'}
        </button>

        <div className="space-y-1 rounded-md bg-slate-50 p-3 text-xs leading-5 text-slate-500">
          <p>모델 유형: 이미지 분류 + 객체 탐지</p>
          <p>예상 요청 시간: 5~10초</p>
          <p>분석 완료 시간은 후속 처리 상태에 따라 달라질 수 있습니다.</p>
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
  const steps = [
    getStep('파일 업로드', !selectedFile ? '대기' : uploading ? '진행 중' : uploadResult ? '완료' : '준비'),
    getStep('전처리', uploadResult ? '처리 예정' : '대기'),
    getStep('모델추론', uploadResult ? '처리 예정' : '대기'),
    getStep('결과분석', uploadResult ? '처리 예정' : '대기'),
  ]

  return (
    <Card title="4. 진행 상태" className="xl:col-span-4">
      <div className="flex items-center gap-5">
        <div className="flex h-24 w-24 shrink-0 items-center justify-center rounded-full border-[10px] border-blue-100 bg-white text-center">
          <div>
            <p className="text-lg font-bold text-blue-700">{uploadResult ? '25%' : uploading ? '...' : '0%'}</p>
            <p className="text-[11px] text-slate-500">{uploadResult ? '접수 완료' : '대기'}</p>
          </div>
        </div>
        <div className="min-w-0 flex-1 space-y-3">
          {steps.map((step) => (
            <div key={step.label} className="flex items-center gap-3">
              <span className={`h-3 w-3 rounded-full ${step.color}`} />
              <div className="flex min-w-0 flex-1 items-center justify-between gap-2 text-sm">
                <span className="font-medium text-slate-700">{step.label}</span>
                <span className="text-xs text-slate-500">{step.status}</span>
              </div>
            </div>
          ))}
          <p className="pt-2 text-xs text-slate-500">
            요청 소요 시간: {requestDurationMs == null ? '-' : `${(requestDurationMs / 1000).toFixed(2)}초`}
          </p>
          {uploadResult ? <Badge tone="blue">PROCESSING</Badge> : null}
        </div>
      </div>
    </Card>
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
    <Card title="5. 탐지 결과 요약" className="xl:col-span-4">
      {!uploadResult ? (
        <Placeholder
          title="아직 탐지 결과가 없습니다."
          description="파일을 업로드하고 탐지 실행을 눌러주세요."
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
            결과 생성 대기 중입니다. AI 분석 결과는 처리 완료 후 결과 화면에서 확인할 수 있습니다.
          </div>
          <div className="flex flex-wrap gap-2">
            <button type="button" className="btn-secondary" onClick={onGoResults}>결과 목록으로 이동</button>
            <button type="button" className="btn-blue" onClick={onReset}>새 검사</button>
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
    <Card title="6. 주요 분석 인사이트" className="xl:col-span-4">
      {!uploadResult ? (
        <Placeholder
          title="분석 인사이트는 검사 결과 생성 후 표시됩니다."
          description="현재 화면에서는 실제 분석 수치나 이상 항목을 임의로 표시하지 않습니다."
        />
      ) : (
        <div className="space-y-3 text-sm text-slate-600">
          <Alert variant="success">검사 요청이 접수되었습니다.</Alert>
          <p>현재 AI 분석 결과 생성 대기 중입니다.</p>
          <p>결과가 생성되면 이상 확률, 탐지 항목, 관련 인사이트를 표시할 예정입니다.</p>
          <div className="flex flex-wrap gap-2 pt-2">
            <button type="button" className="btn-secondary" disabled>결과 다운로드</button>
            <button type="button" className="btn-blue" onClick={onGoResults}>탐지이력 보기</button>
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
  const color = variant === 'success' ? 'border-emerald-200 bg-emerald-50 text-emerald-700' : 'border-red-200 bg-red-50 text-red-700'
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

function getStep(label: string, status: string) {
  const color = status === '완료' ? 'bg-emerald-500' : status === '진행 중' ? 'bg-blue-500' : status === '준비' ? 'bg-amber-400' : 'bg-slate-300'
  return { label, status, color }
}

function formatResolution(selectedFile: SelectedInspectionFile) {
  if (selectedFile.fileKind !== 'image') return '-'
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

function formatElapsed(seconds: number) {
  const hour = Math.floor(seconds / 3600)
  const minute = Math.floor((seconds % 3600) / 60)
  const second = seconds % 60
  return [hour, minute, second].map((value) => String(value).padStart(2, '0')).join(':')
}

function isCameraActive(camera: CameraSource) {
  const status = camera.status?.toUpperCase()
  return status === 'ACTIVE' || status === 'ONLINE'
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

function VideoIcon() {
  return (
    <svg className="h-12 w-12" fill="none" viewBox="0 0 48 48" stroke="currentColor" strokeWidth={1.6}>
      <rect x="9" y="12" width="30" height="24" rx="3" />
      <path strokeLinejoin="round" d="m22 19 10 5-10 5z" />
    </svg>
  )
}
