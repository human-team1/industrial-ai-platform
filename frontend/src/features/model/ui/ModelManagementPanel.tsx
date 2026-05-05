import { useMemo, useState, type ReactNode } from 'react'
import type {
  CreatedModelVersion,
  DeploymentScope,
  GenerateModelVersionsForm,
  Model,
  ModelCategory,
  ModelDeployment,
  ModelProfile,
} from '../../../entities/model'
import { useModelManagement } from '../model/useModelManagement'

const CATEGORY_OPTIONS: { value: ModelCategory; label: string }[] = [
  { value: 'OBJECT', label: 'Object' },
  { value: 'TEXTURE', label: 'Texture' },
]

const IMAGE_MIME_TYPES = new Set(['image/jpeg', 'image/png', 'image/webp'])
const PROFILE_OPTIONS: { value: 'ALL' | ModelProfile; label: string }[] = [
  { value: 'ALL', label: '전체 생성' },
  { value: 'SPEED', label: 'SPEED만 생성' },
  { value: 'PERFORMANCE', label: 'PERFORMANCE만 생성' },
]

export function ModelManagementPanel() {
  const {
    models,
    versions,
    deployments,
    selectedModelId,
    setSelectedModelId,
    loading,
    versionsLoading,
    deploymentsLoading,
    error,
    message,
    busy,
    reloadAll,
    submitCreateModel,
    submitGenerateFromNormalImages,
    submitDeactivateDeployment,
    submitRollbackDeployment,
  } = useModelManagement()

  const [modelName, setModelName] = useState('')
  const [modelType, setModelType] = useState('PATCHCORE')
  const [description, setDescription] = useState('')
  const [modelCategory, setModelCategory] = useState<ModelCategory>('TEXTURE')
  const [modelProfile, setModelProfile] = useState<'ALL' | ModelProfile>('ALL')
  const [normalImages, setNormalImages] = useState<File[]>([])
  const [organizationId, setOrganizationId] = useState('')
  const [targetId, setTargetId] = useState('')
  const [deploymentScope, setDeploymentScope] = useState<DeploymentScope>('TARGET')
  const [thresholdDefault, setThresholdDefault] = useState('')
  const [reason, setReason] = useState('')
  const [createdVersions, setCreatedVersions] = useState<CreatedModelVersion[]>([])
  const [fileInputKey, setFileInputKey] = useState(0)
  const [rollbackToId, setRollbackToId] = useState<Record<number, string>>({})

  const selectedModel = useMemo(
    () => models.find((model) => model.modelId === selectedModelId) ?? null,
    [models, selectedModelId],
  )

  const filteredDeployments = useMemo(
    () => deployments.filter((deployment) => (selectedModel ? deployment.modelId === selectedModel.modelId : true)),
    [deployments, selectedModel],
  )

  async function handleCreateModel() {
    if (!modelName.trim() || !modelType.trim()) {
      window.alert('모델명과 모델 타입은 필수입니다.')
      return
    }
    await submitCreateModel({
      modelName: modelName.trim(),
      modelType: modelType.trim(),
      description: description.trim(),
    })
    setModelName('')
    setDescription('')
  }

  async function handleGenerate() {
    if (!selectedModel) {
      window.alert('모델을 먼저 선택하세요.')
      return
    }
    if (normalImages.length < 100) {
      window.alert('정상 이미지는 최소 100장 이상 업로드해야 합니다.')
      return
    }
    const invalidFile = normalImages.find((file) => !IMAGE_MIME_TYPES.has(file.type))
    if (invalidFile) {
      window.alert('jpg, jpeg, png, webp 이미지만 업로드할 수 있습니다.')
      return
    }
    if (!organizationId.trim()) {
      window.alert('조직 ID는 필수입니다.')
      return
    }
    if (deploymentScope === 'TARGET' && !targetId.trim()) {
      window.alert('검사대상 배포에는 targetId가 필요합니다.')
      return
    }
    if (thresholdDefault.trim()) {
      const threshold = Number(thresholdDefault)
      if (Number.isNaN(threshold) || threshold < 0 || threshold > 1) {
        window.alert('기본 임계값은 0~1 범위로 입력하세요.')
        return
      }
    }

    const payload: GenerateModelVersionsForm = {
      normalImages,
      modelCategory,
      modelProfile: modelProfile === 'ALL' ? undefined : modelProfile,
      organizationId: organizationId.trim(),
      targetId: deploymentScope === 'TARGET' ? targetId.trim() : undefined,
      deploymentScope,
      thresholdDefault: thresholdDefault.trim(),
      reason: reason.trim(),
    }
    const result = await submitGenerateFromNormalImages(payload)
    if (result) {
      setCreatedVersions(result.createdVersions)
      setNormalImages([])
      setFileInputKey((current) => current + 1)
    }
  }

  function handleScopeChange(value: string) {
    const nextScope = value as DeploymentScope
    setDeploymentScope(nextScope)
    if (nextScope === 'ORGANIZATION') {
      setTargetId('')
    }
  }

  return (
    <section className="space-y-5 rounded border border-slate-200 bg-white p-5 shadow-sm">
      <div className="flex flex-col gap-3 lg:flex-row lg:items-start lg:justify-between">
        <div>
          <h2 className="text-xl font-bold text-slate-900">모델 운영 관리</h2>
          <p className="mt-1 text-sm text-slate-500">
            운영자는 정상 이미지셋과 모델 유형만 선택합니다. ckpt, config, backbone, layer 설정은 Spring 고정 프로필을 사용합니다.
          </p>
        </div>
        <button
          type="button"
          onClick={() => void reloadAll()}
          disabled={busy.refresh}
          className="h-10 rounded border border-slate-200 px-4 text-sm font-semibold text-slate-700 disabled:cursor-not-allowed disabled:bg-slate-100 disabled:text-slate-400"
        >
          {busy.refresh ? '새로고침 중...' : '새로고침'}
        </button>
      </div>

      {message ? <Notice tone="success">{message}</Notice> : null}
      {error ? <Notice tone="error">{error}</Notice> : null}

      <section className="grid gap-3 rounded border border-slate-200 bg-slate-50 p-4 md:grid-cols-3">
        <SummaryItem label="선택 모델" value={selectedModel?.modelName ?? '미선택'} />
        <SummaryItem label="모델 ID" value={selectedModel ? String(selectedModel.modelId) : '-'} />
        <SummaryItem label="배포 수" value={`${filteredDeployments.filter((deployment) => deployment.isActive).length} active`} />
      </section>

      <StepSection step="1" title="모델 등록" description="정상 이미지셋을 연결할 기본 모델을 등록합니다.">
        <div className="grid gap-3 lg:grid-cols-[1fr_1fr_1.3fr_auto] lg:items-end">
          <Input label="모델명" value={modelName} onChange={setModelName} placeholder="PatchCore Texture Detector" />
          <Input label="모델 타입" value={modelType} onChange={setModelType} placeholder="PATCHCORE" />
          <TextArea label="설명" value={description} onChange={setDescription} placeholder="운영 메모" rows={2} />
          <button
            type="button"
            disabled={busy.create}
            onClick={() => void handleCreateModel()}
            className="h-10 rounded bg-[#109498] px-4 text-sm font-semibold text-white disabled:cursor-not-allowed disabled:bg-slate-300"
          >
            {busy.create ? '등록 중...' : '모델 등록'}
          </button>
        </div>
      </StepSection>

      <StepSection step="2" title="모델 선택" description="정상 이미지셋 기반 버전을 생성할 모델을 선택합니다.">
        {loading ? (
          <Empty text="모델 목록을 불러오는 중입니다." />
        ) : models.length === 0 ? (
          <Empty text="등록된 모델이 없습니다. 먼저 모델을 등록하세요." />
        ) : (
          <div className="grid gap-3 lg:grid-cols-2">
            {models.map((model) => (
              <button
                key={model.modelId}
                type="button"
                onClick={() => setSelectedModelId(model.modelId)}
                className={`rounded border p-4 text-left transition ${
                  model.modelId === selectedModelId
                    ? 'border-[#109498] bg-[#109498]/5 ring-1 ring-[#109498]/20'
                    : 'border-slate-200 bg-slate-50 hover:border-slate-300'
                }`}
              >
                <p className="font-semibold text-slate-900">{model.modelName}</p>
                <p className="mt-1 text-xs font-medium text-slate-500">{model.modelType}</p>
                <p className="mt-3 text-sm text-slate-600">{model.description || '설명이 없습니다.'}</p>
              </button>
            ))}
          </div>
        )}
      </StepSection>

      <StepSection
        step="3"
        title="정상 이미지셋 기반 모델 생성"
        description="modelProfile은 기본 전송하지 않습니다. Spring이 SPEED와 PERFORMANCE 버전을 함께 생성하고 배포합니다."
        disabled={!selectedModel}
      >
        <SelectedModelNotice model={selectedModel} />
        <div className="grid gap-3">
          <p className="rounded border border-dashed border-slate-200 bg-slate-50 p-3 text-sm text-slate-500">
            정상 이미지셋을 업로드하세요. 최소 개수 기준은 운영 설정을 따릅니다.
          </p>
          <div className="grid gap-3 md:grid-cols-2">
            <Select label="모델 유형" value={modelCategory} onChange={(value) => setModelCategory(value as ModelCategory)} options={CATEGORY_OPTIONS} />
            <Select label="생성 프로필" value={modelProfile} onChange={(value) => setModelProfile(value as 'ALL' | ModelProfile)} options={PROFILE_OPTIONS} />
            <Select
              label="배포 범위"
              value={deploymentScope}
              onChange={handleScopeChange}
              options={[
                { value: 'ORGANIZATION', label: '조직 전체' },
                { value: 'TARGET', label: '검사대상' },
              ]}
            />
          </div>
          <div className="grid gap-3 md:grid-cols-2">
            <Input label="조직 ID" value={organizationId} onChange={setOrganizationId} placeholder="1001" />
            {deploymentScope === 'TARGET' ? (
              <Input label="검사대상 ID" value={targetId} onChange={setTargetId} placeholder="10" />
            ) : (
              <Input label="검사대상 ID" value="" onChange={() => undefined} placeholder="조직 전체 배포에서는 사용하지 않습니다." disabled />
            )}
          </div>
          <div className="grid gap-3 md:grid-cols-2">
            <Input label="기본 임계값 선택" value={thresholdDefault} onChange={setThresholdDefault} placeholder="0.7500" />
            <FileInput
              key={fileInputKey}
              label="정상 이미지셋"
              multiple
              accept=".jpg,.jpeg,.png,.webp,image/jpeg,image/png,image/webp"
              onChange={setNormalImages}
            />
          </div>
          <TextArea label="생성 및 배포 사유" value={reason} onChange={setReason} placeholder="정상 이미지셋 기준 모델 생성" rows={3} />
          <div className="flex justify-end">
            <button
              type="button"
              disabled={!selectedModel || busy.generate}
              onClick={() => void handleGenerate()}
              className="h-10 rounded bg-[#109498] px-4 text-sm font-semibold text-white disabled:cursor-not-allowed disabled:bg-slate-300"
            >
              {busy.generate ? '생성 중...' : '모델 생성 및 배포'}
            </button>
          </div>
        </div>

        {createdVersions.length > 0 ? (
          <div className="mt-4 rounded border border-emerald-200 bg-emerald-50 p-4">
            <p className="text-sm font-semibold text-emerald-800">정상 이미지셋 기반 모델 생성이 완료되었습니다.</p>
            <p className="mt-1 text-sm text-emerald-700">
              속도형 모델은 실시간 검사에 사용됩니다. 성능형 모델은 업로드 검사와 재검토 큐에 사용됩니다.
            </p>
            <div className="mt-3 grid gap-2 md:grid-cols-2">
              {createdVersions.map((version) => (
                <div key={version.modelVersionId} className="rounded border border-emerald-200 bg-white p-3 text-sm">
                  <p className="font-semibold text-slate-900">{profileLabel(version.modelProfile)} 모델</p>
                  <p className="mt-1 text-slate-600">versionId: {version.modelVersionId}</p>
                  <p className="text-slate-600">deploymentId: {version.deploymentId}</p>
                  <p className="text-slate-600">status: {version.deployStatus}</p>
                </div>
              ))}
            </div>
          </div>
        ) : null}
      </StepSection>

      <StepSection step="4" title="생성된 모델 버전" description="고정 프로필 설정으로 생성된 모델 버전을 확인합니다." disabled={!selectedModel}>
        {!selectedModel ? (
          <Empty text="모델을 먼저 선택하세요." />
        ) : versionsLoading ? (
          <Empty text="버전 목록을 불러오는 중입니다." />
        ) : versions.length === 0 ? (
          <Empty text="등록된 모델 버전이 없습니다." />
        ) : (
          <div className="space-y-3">
            {versions.map((version) => (
              <article key={version.modelVersionId} className="rounded border border-slate-200 bg-slate-50 p-4">
                <div className="flex flex-wrap items-center gap-2">
                  <h4 className="text-base font-semibold text-slate-900">{version.versionName}</h4>
                  <StatusBadge tone={version.deployStatus === 'DEPLOYED' ? 'success' : 'muted'}>{version.deployStatus ?? '-'}</StatusBadge>
                  <StatusBadge tone={version.isActive ? 'success' : 'muted'}>{version.isActive ? 'ACTIVE' : 'INACTIVE'}</StatusBadge>
                </div>
                <dl className="mt-3 grid gap-2 text-sm text-slate-600 md:grid-cols-2 xl:grid-cols-4">
                  <InfoRow label="유형" value={String(version.modelCategory)} />
                  <InfoRow label="프로필" value={`${String(version.modelProfile)} (${profileLabel(String(version.modelProfile))})`} />
                  <InfoRow label="프레임워크" value={version.framework || '-'} />
                  <InfoRow label="입력 크기" value={version.inputSize || '-'} />
                </dl>
              </article>
            ))}
          </div>
        )}
      </StepSection>

      <StepSection step="5" title="배포 이력" description="활성 배포와 이전 배포를 확인하고 필요 시 비활성화 또는 롤백합니다." disabled={!selectedModel}>
        {!selectedModel ? (
          <Empty text="모델을 먼저 선택하세요." />
        ) : deploymentsLoading ? (
          <Empty text="배포 이력을 불러오는 중입니다." />
        ) : filteredDeployments.length === 0 ? (
          <Empty text="배포 이력이 없습니다." />
        ) : (
          <div className="space-y-3">
            {filteredDeployments.map((deployment) => (
              <DeploymentCard
                key={deployment.deploymentId}
                deployment={deployment}
                rollbackToId={rollbackToId[deployment.deploymentId] ?? ''}
                onRollbackToIdChange={(value) => setRollbackToId((prev) => ({ ...prev, [deployment.deploymentId]: value }))}
                actionId={busy.actionId}
                onDeactivate={() => void submitDeactivateDeployment(deployment.deploymentId, '운영 비활성화')}
                onRollback={() => {
                  const target = Number(rollbackToId[deployment.deploymentId] ?? '')
                  if (!target) {
                    window.alert('롤백 대상 배포 ID를 입력하세요.')
                    return
                  }
                  void submitRollbackDeployment(deployment.deploymentId, target, '운영 롤백')
                }}
              />
            ))}
          </div>
        )}
      </StepSection>
    </section>
  )
}

function StepSection({
  step,
  title,
  description,
  disabled,
  children,
}: {
  step: string
  title: string
  description: string
  disabled?: boolean
  children: ReactNode
}) {
  return (
    <section className={`rounded border p-4 ${disabled ? 'border-slate-200 bg-slate-50/80' : 'border-slate-200 bg-white'}`}>
      <div className="mb-4 flex items-start gap-3">
        <div className={`flex h-9 w-9 items-center justify-center rounded-full text-sm font-bold ${disabled ? 'bg-slate-200 text-slate-500' : 'bg-[#109498] text-white'}`}>
          {step}
        </div>
        <div>
          <h3 className="text-base font-semibold text-slate-900">
            {step}. {title}
          </h3>
          <p className="mt-1 text-sm text-slate-500">{description}</p>
        </div>
      </div>
      <div className={disabled ? 'opacity-80' : ''}>{children}</div>
    </section>
  )
}

function DeploymentCard({
  deployment,
  rollbackToId,
  onRollbackToIdChange,
  actionId,
  onDeactivate,
  onRollback,
}: {
  deployment: ModelDeployment
  rollbackToId: string
  onRollbackToIdChange: (value: string) => void
  actionId?: string | null
  onDeactivate: () => void
  onRollback: () => void
}) {
  const isActive = deployment.isActive === true
  return (
    <article className={`rounded border p-4 ${isActive ? 'border-[#109498] bg-[#109498]/5' : 'border-slate-200 bg-slate-50'}`}>
      <div className="flex flex-col gap-3 lg:flex-row lg:items-start lg:justify-between">
        <div className="space-y-2">
          <div className="flex flex-wrap items-center gap-2">
            <h4 className="text-base font-semibold text-slate-900">
              {deployment.modelName || '-'} / {deployment.versionName || '-'}
            </h4>
            <StatusBadge tone={isActive ? 'success' : 'muted'}>{isActive ? 'ACTIVE' : 'INACTIVE'}</StatusBadge>
            <StatusBadge tone={deployment.deployStatus === 'DEPLOYED' ? 'success' : 'muted'}>{deployment.deployStatus ?? '-'}</StatusBadge>
          </div>
          <dl className="grid gap-2 text-sm text-slate-600 md:grid-cols-2 xl:grid-cols-4">
            <InfoRow label="범위" value={deployment.deploymentScope === 'TARGET' ? '검사대상' : '조직 전체'} />
            <InfoRow label="조직 ID" value={String(deployment.organizationId)} />
            <InfoRow label="검사대상 ID" value={deployment.targetId != null ? String(deployment.targetId) : '-'} />
            <InfoRow label="deploymentId" value={String(deployment.deploymentId)} />
          </dl>
        </div>
        <div className="grid gap-2 lg:min-w-[240px]">
          <Input label="롤백 대상 배포 ID" value={rollbackToId} onChange={onRollbackToIdChange} placeholder="이전 deploymentId" disabled={!isActive} />
          <div className="flex gap-2">
            <button
              type="button"
              disabled={!isActive || actionId === `deployment-off-${deployment.deploymentId}`}
              onClick={onDeactivate}
              className="h-10 flex-1 rounded border border-slate-300 bg-white px-4 text-sm font-semibold text-slate-700 disabled:cursor-not-allowed disabled:bg-slate-100 disabled:text-slate-400"
            >
              비활성화
            </button>
            <button
              type="button"
              disabled={!isActive || actionId === `rollback-${deployment.deploymentId}`}
              onClick={onRollback}
              className="h-10 flex-1 rounded border border-slate-300 bg-white px-4 text-sm font-semibold text-slate-700 disabled:cursor-not-allowed disabled:bg-slate-100 disabled:text-slate-400"
            >
              롤백
            </button>
          </div>
        </div>
      </div>
    </article>
  )
}

function SummaryItem({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded border border-slate-200 bg-white p-3">
      <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">{label}</p>
      <p className="mt-1 text-sm font-semibold text-slate-900">{value}</p>
    </div>
  )
}

function SelectedModelNotice({ model }: { model: Model | null }) {
  return (
    <div className="mb-4 rounded border border-dashed border-slate-200 bg-slate-50 p-3 text-sm text-slate-600">
      선택 모델: <span className="font-semibold text-slate-900">{model?.modelName ?? '모델을 먼저 선택하세요.'}</span>
      {model ? <span className="ml-2 text-xs text-slate-500">ID {model.modelId} / {model.modelType}</span> : null}
    </div>
  )
}

function Notice({ tone, children }: { tone: 'success' | 'error'; children: ReactNode }) {
  const className =
    tone === 'success'
      ? 'rounded border border-emerald-200 bg-emerald-50 p-3 text-sm text-emerald-700'
      : 'rounded border border-rose-200 bg-rose-50 p-3 text-sm text-rose-700'
  return <div className={className}>{children}</div>
}

function Empty({ text }: { text: string }) {
  return <p className="rounded border border-dashed border-slate-200 bg-white p-6 text-center text-sm text-slate-500">{text}</p>
}

function StatusBadge({ tone, children }: { tone: 'success' | 'muted'; children: ReactNode }) {
  const toneClass = tone === 'success' ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-100 text-slate-700'
  return <span className={`rounded-full px-2.5 py-1 text-xs font-semibold ${toneClass}`}>{children}</span>
}

function InfoRow({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <dt className="text-xs font-semibold uppercase tracking-wide text-slate-500">{label}</dt>
      <dd className="mt-1 text-sm text-slate-700">{value}</dd>
    </div>
  )
}

function Input({
  label,
  value,
  onChange,
  placeholder,
  disabled,
}: {
  label: string
  value: string
  onChange: (value: string) => void
  placeholder?: string
  disabled?: boolean
}) {
  return (
    <label className="flex flex-col gap-1 text-sm font-medium text-slate-700">
      {label}
      <input
        value={value}
        onChange={(event) => onChange(event.target.value)}
        placeholder={placeholder}
        disabled={disabled}
        className="h-10 rounded border border-slate-200 bg-white px-3 text-sm disabled:cursor-not-allowed disabled:bg-slate-100 disabled:text-slate-400"
      />
    </label>
  )
}

function TextArea({
  label,
  value,
  onChange,
  placeholder,
  rows = 3,
}: {
  label: string
  value: string
  onChange: (value: string) => void
  placeholder?: string
  rows?: number
}) {
  return (
    <label className="flex flex-col gap-1 text-sm font-medium text-slate-700">
      {label}
      <textarea
        value={value}
        onChange={(event) => onChange(event.target.value)}
        placeholder={placeholder}
        rows={rows}
        className="rounded border border-slate-200 bg-white px-3 py-2 text-sm"
      />
    </label>
  )
}

function Select({
  label,
  value,
  onChange,
  options,
}: {
  label: string
  value: string
  onChange: (value: string) => void
  options: { value: string; label: string }[]
}) {
  return (
    <label className="flex flex-col gap-1 text-sm font-medium text-slate-700">
      {label}
      <select value={value} onChange={(event) => onChange(event.target.value)} className="h-10 rounded border border-slate-200 bg-white px-3 text-sm">
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
    </label>
  )
}

function FileInput({
  label,
  multiple,
  accept,
  onChange,
}: {
  label: string
  multiple?: boolean
  accept?: string
  onChange: (files: File[]) => void
}) {
  return (
    <label className="flex flex-col gap-1 text-sm font-medium text-slate-700">
      {label}
      <input
        type="file"
        multiple={multiple}
        accept={accept}
        onChange={(event) => onChange(Array.from(event.target.files ?? []))}
        className="rounded border border-slate-200 bg-white px-3 py-2 text-sm"
      />
    </label>
  )
}

function profileLabel(profile: string) {
  if (profile === 'SPEED') return '속도형'
  if (profile === 'PERFORMANCE') return '성능형'
  return profile
}
