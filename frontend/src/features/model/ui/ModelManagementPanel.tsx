import { useMemo, useState, type ReactNode } from 'react'
import type {
  DeploymentScope,
  Model,
  ModelCategory,
  ModelDeployment,
  ModelDeployStatus,
  ModelProfile,
  UploadModelVersionForm,
} from '../../../entities/model'
import { useModelManagement } from '../model/useModelManagement'

const EMPTY_VERSION_FORM: UploadModelVersionForm = {
  versionName: '',
  modelCategory: 'OBJECT',
  modelProfile: 'PERFORMANCE',
  framework: '',
  inputSize: '',
  thresholdDefault: '',
  accuracy: '',
  precisionScore: '',
  recallScore: '',
  f1Score: '',
  aurocScore: '',
  ckptFile: null,
  configFile: null,
  memoryBankFile: null,
  labelsFile: null,
}

const CATEGORY_OPTIONS: { value: ModelCategory; label: string }[] = [
  { value: 'OBJECT', label: 'OBJECT' },
  { value: 'TEXTURE', label: 'TEXTURE' },
]

const PROFILE_OPTIONS: { value: ModelProfile; label: string }[] = [
  { value: 'SPEED', label: 'SPEED' },
  { value: 'PERFORMANCE', label: 'PERFORMANCE' },
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
    submitUploadVersion,
    submitActivateVersion,
    submitDeprecateVersion,
    submitDeployVersion,
    submitDeactivateDeployment,
    submitRollbackDeployment,
  } = useModelManagement()

  const [modelName, setModelName] = useState('')
  const [modelType, setModelType] = useState('PATCHCORE')
  const [description, setDescription] = useState('')
  const [versionForm, setVersionForm] = useState<UploadModelVersionForm>(EMPTY_VERSION_FORM)
  const [organizationId, setOrganizationId] = useState('')
  const [targetId, setTargetId] = useState('')
  const [deploymentScope, setDeploymentScope] = useState<DeploymentScope>('ORGANIZATION')
  const [deployVersionId, setDeployVersionId] = useState('')
  const [deployReason, setDeployReason] = useState('')
  const [rollbackToId, setRollbackToId] = useState<Record<number, string>>({})
  const [fileInputKey, setFileInputKey] = useState(0)

  const selectedModel = useMemo(
    () => models.find((model) => model.modelId === selectedModelId) ?? null,
    [models, selectedModelId],
  )

  const deployableVersions = useMemo(
    () => versions.filter((version) => version.isActive === true && version.deployStatus !== 'DEPRECATED'),
    [versions],
  )

  const filteredDeployments = useMemo(
    () => deployments.filter((deployment) => (selectedModel ? deployment.modelId === selectedModel.modelId : true)),
    [deployments, selectedModel],
  )

  const activeDeployments = useMemo(
    () => filteredDeployments.filter((deployment) => deployment.isActive === true),
    [filteredDeployments],
  )

  const canDeploy =
    !!selectedModel &&
    deployableVersions.length > 0 &&
    !!deployVersionId &&
    !!organizationId.trim() &&
    (deploymentScope === 'ORGANIZATION' || !!targetId.trim()) &&
    !busy.deploy

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

  async function handleUploadVersion() {
    if (!selectedModel) {
      window.alert('모델을 먼저 선택하세요.')
      return
    }
    if (!versionForm.versionName.trim()) {
      window.alert('버전명은 필수입니다.')
      return
    }
    if (!versionForm.ckptFile) {
      window.alert('모델 가중치 파일을 선택하세요.')
      return
    }
    if (!versionForm.configFile) {
      window.alert('모델 설정 파일을 선택하세요.')
      return
    }
    if (!versionForm.memoryBankFile) {
      window.alert('메모리뱅크 파일을 선택하세요.')
      return
    }
    if (versionForm.thresholdDefault) {
      const threshold = Number(versionForm.thresholdDefault)
      if (Number.isNaN(threshold) || threshold < 0 || threshold > 1) {
        window.alert('기본 임계값은 0~1 범위로 입력하세요.')
        return
      }
    }

    await submitUploadVersion(versionForm)
    setVersionForm(EMPTY_VERSION_FORM)
    setFileInputKey((prev) => prev + 1)
  }

  async function handleDeploy() {
    if (!deployVersionId || !organizationId.trim()) {
      window.alert('배포 버전과 조직 ID는 필수입니다.')
      return
    }
    if (deploymentScope === 'TARGET' && !targetId.trim()) {
      window.alert('검사대상 단위 배포에는 검사대상 ID가 필요합니다.')
      return
    }

    await submitDeployVersion(Number(deployVersionId), {
      organizationId: Number(organizationId),
      targetId: deploymentScope === 'TARGET' ? Number(targetId) : null,
      deploymentScope,
      reason: deployReason.trim(),
    })

    setDeployReason('')
    if (deploymentScope === 'ORGANIZATION') {
      setTargetId('')
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
          <h2 className="text-xl font-bold text-slate-900">모델 관리</h2>
          <p className="mt-1 text-sm text-slate-500">
            운영자가 모델을 등록하고, 버전을 업로드한 뒤 활성화하고 조직 또는 검사대상에 순서대로 배포합니다.
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
        <SummaryItem label="선택된 모델" value={selectedModel?.modelName ?? '미선택'} />
        <SummaryItem label="선택된 모델 ID" value={selectedModel ? String(selectedModel.modelId) : '-'} />
        <SummaryItem label="배포 가능 버전 / 활성 배포" value={`${deployableVersions.length} / ${activeDeployments.length}`} />
      </section>

      <StepSection
        step="1"
        title="모델 등록"
        description="모델의 기본 정보를 먼저 등록합니다. 실제 파일은 다음 단계의 모델 버전 업로드에서 등록합니다."
      >
        <div className="grid gap-3 lg:grid-cols-[1fr_1fr_1.3fr_auto] lg:items-end">
          <Input label="모델명" value={modelName} onChange={setModelName} placeholder="PatchCore Texture Detector" />
          <Input label="모델 타입" value={modelType} onChange={setModelType} placeholder="PATCHCORE" />
          <TextArea label="설명" value={description} onChange={setDescription} placeholder="운영 메모를 입력합니다." rows={2} />
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

      <StepSection step="2" title="모델 선택" description="버전을 업로드하거나 배포할 모델을 선택합니다.">
        {loading ? (
          <Empty text="모델 목록을 불러오는 중입니다." />
        ) : models.length === 0 ? (
          <Empty text="등록된 모델이 없습니다. 먼저 모델을 등록하세요." />
        ) : (
          <div className="grid gap-3 lg:grid-cols-2">
            {models.map((model) => {
              const active = model.modelId === selectedModelId
              return (
                <button
                  key={model.modelId}
                  type="button"
                  onClick={() => setSelectedModelId(model.modelId)}
                  className={`rounded border p-4 text-left transition ${
                    active
                      ? 'border-[#109498] bg-[#109498]/5 ring-1 ring-[#109498]/20'
                      : 'border-slate-200 bg-slate-50 hover:border-slate-300'
                  }`}
                >
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <p className="font-semibold text-slate-900">{model.modelName}</p>
                      <p className="mt-1 text-xs font-medium text-slate-500">{model.modelType}</p>
                    </div>
                    <span
                      className={`rounded-full px-2.5 py-1 text-xs font-semibold ${
                        active ? 'bg-[#109498] text-white' : 'bg-white text-slate-500'
                      }`}
                    >
                      {active ? '선택됨' : `모델 ID ${model.modelId}`}
                    </span>
                  </div>
                  <p className="mt-3 text-sm text-slate-600">{model.description || '설명이 없습니다.'}</p>
                </button>
              )
            })}
          </div>
        )}
      </StepSection>

      <StepSection
        step="3"
        title="모델 버전 업로드"
        description="이미 생성된 model.ckpt, config.json, memory bank 파일을 선택한 모델의 새 버전으로 등록합니다."
        disabled={!selectedModel}
      >
        <SelectedModelNotice model={selectedModel} emptyText="모델을 먼저 선택하세요." />
        {!selectedModel ? (
          <Empty text="모델을 먼저 선택하면 버전 업로드와 배포를 진행할 수 있습니다." />
        ) : (
          <div className="grid gap-3">
            <p className="rounded border border-dashed border-slate-200 bg-slate-50 p-3 text-sm text-slate-500">
              모델 가중치 파일, 설정 파일, 메모리뱅크 파일을 함께 등록합니다.
            </p>

            <div className="grid gap-3 md:grid-cols-2">
              <Input
                label="버전명"
                value={versionForm.versionName}
                onChange={(value) => setVersionForm((prev) => ({ ...prev, versionName: value }))}
                placeholder="v1.0.0-texture-performance"
              />
              <Input
                label="프레임워크"
                value={versionForm.framework ?? ''}
                onChange={(value) => setVersionForm((prev) => ({ ...prev, framework: value }))}
                placeholder="PyTorch"
              />
            </div>

            <div className="grid gap-3 md:grid-cols-2">
              <Select
                label="카테고리"
                value={versionForm.modelCategory}
                onChange={(value) => setVersionForm((prev) => ({ ...prev, modelCategory: value as ModelCategory }))}
                options={CATEGORY_OPTIONS}
              />
              <Select
                label="프로필"
                value={versionForm.modelProfile}
                onChange={(value) => setVersionForm((prev) => ({ ...prev, modelProfile: value as ModelProfile }))}
                options={PROFILE_OPTIONS}
              />
            </div>

            <div className="grid gap-3 md:grid-cols-2">
              <Input
                label="입력 크기"
                value={versionForm.inputSize ?? ''}
                onChange={(value) => setVersionForm((prev) => ({ ...prev, inputSize: value }))}
                placeholder="256x256"
              />
              <Input
                label="기본 임계값"
                value={versionForm.thresholdDefault ?? ''}
                onChange={(value) => setVersionForm((prev) => ({ ...prev, thresholdDefault: value }))}
                placeholder="0.65"
              />
            </div>

            <div className="grid gap-3 md:grid-cols-3">
              <Input
                label="정확도"
                value={versionForm.accuracy ?? ''}
                onChange={(value) => setVersionForm((prev) => ({ ...prev, accuracy: value }))}
                placeholder="0.98"
              />
              <Input
                label="정밀도"
                value={versionForm.precisionScore ?? ''}
                onChange={(value) => setVersionForm((prev) => ({ ...prev, precisionScore: value }))}
                placeholder="0.97"
              />
              <Input
                label="재현율"
                value={versionForm.recallScore ?? ''}
                onChange={(value) => setVersionForm((prev) => ({ ...prev, recallScore: value }))}
                placeholder="0.96"
              />
            </div>

            <div className="grid gap-3 md:grid-cols-2">
              <Input
                label="F1 점수"
                value={versionForm.f1Score ?? ''}
                onChange={(value) => setVersionForm((prev) => ({ ...prev, f1Score: value }))}
                placeholder="0.96"
              />
              <Input
                label="AUROC"
                value={versionForm.aurocScore ?? ''}
                onChange={(value) => setVersionForm((prev) => ({ ...prev, aurocScore: value }))}
                placeholder="0.99"
              />
            </div>

            <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-4">
              <FileInput
                key={`ckpt-${fileInputKey}`}
                label="모델 가중치 파일 *"
                onChange={(file) => setVersionForm((prev) => ({ ...prev, ckptFile: file }))}
              />
              <FileInput
                key={`config-${fileInputKey}`}
                label="모델 설정 파일 *"
                onChange={(file) => setVersionForm((prev) => ({ ...prev, configFile: file }))}
              />
              <FileInput
                key={`memory-${fileInputKey}`}
                label="메모리뱅크 파일 *"
                description="PatchCore 추론에 사용하는 정상 feature memory bank 파일입니다. (.pt, .pth, .npy, .npz 등)"
                onChange={(file) => setVersionForm((prev) => ({ ...prev, memoryBankFile: file }))}
              />
              <FileInput
                key={`labels-${fileInputKey}`}
                label="라벨 파일(선택)"
                onChange={(file) => setVersionForm((prev) => ({ ...prev, labelsFile: file }))}
              />
            </div>

            <div className="flex justify-end">
              <button
                type="button"
                disabled={busy.upload}
                onClick={() => void handleUploadVersion()}
                className="h-10 rounded bg-[#109498] px-4 text-sm font-semibold text-white disabled:cursor-not-allowed disabled:bg-slate-300"
              >
                {busy.upload ? '업로드 중...' : '버전 업로드'}
              </button>
            </div>
          </div>
        )}
      </StepSection>

      <StepSection
        step="4"
        title="버전 활성화"
        description="업로드된 버전을 검증 완료 상태로 전환합니다. CKPT, CONFIG, MEMORY_BANK가 모두 등록된 버전만 활성화할 수 있습니다."
        disabled={!selectedModel}
      >
        <SelectedModelNotice model={selectedModel} emptyText="모델을 먼저 선택하세요." />
        {!selectedModel ? (
          <Empty text="모델을 먼저 선택하면 버전 업로드와 배포를 진행할 수 있습니다." />
        ) : versionsLoading ? (
          <Empty text="버전 목록을 불러오는 중입니다." />
        ) : versions.length === 0 ? (
          <Empty text="등록된 모델 버전이 없습니다. 먼저 버전을 업로드하세요." />
        ) : (
          <div className="space-y-3">
            {versions.map((version) => {
              const deployStatus = version.deployStatus ?? 'REGISTERED'
              const canActivate =
                deployStatus !== 'DEPRECATED' &&
                version.isActive !== true &&
                deployStatus !== 'VALIDATED' &&
                deployStatus !== 'DEPLOYED'
              const canDeprecate = deployStatus !== 'DEPRECATED'

              return (
                <article key={version.modelVersionId} className="rounded border border-slate-200 bg-slate-50 p-4">
                  <div className="flex flex-col gap-3 lg:flex-row lg:items-start lg:justify-between">
                    <div className="space-y-2">
                      <div className="flex flex-wrap items-center gap-2">
                        <h4 className="text-base font-semibold text-slate-900">{version.versionName}</h4>
                        <StatusBadge tone={deployStatusTone(deployStatus)}>{deployStatusLabel(deployStatus)}</StatusBadge>
                        <StatusBadge tone={version.isActive ? 'success' : 'muted'}>
                          {version.isActive ? 'ACTIVE' : 'INACTIVE'}
                        </StatusBadge>
                      </div>
                      <dl className="grid gap-2 text-sm text-slate-600 md:grid-cols-2 xl:grid-cols-4">
                        <InfoRow label="카테고리" value={String(version.modelCategory)} />
                        <InfoRow label="프로필" value={String(version.modelProfile)} />
                        <InfoRow label="프레임워크" value={version.framework || '-'} />
                        <InfoRow label="입력 크기" value={version.inputSize || '-'} />
                        <InfoRow label="기본 임계값" value={version.thresholdDefault != null ? String(version.thresholdDefault) : '-'} />
                        <InfoRow label="생성일" value={formatDateTime(version.createdAt)} />
                      </dl>
                    </div>

                    <div className="flex flex-wrap gap-2">
                      <button
                        type="button"
                        disabled={!canActivate || busy.actionId === `activate-${version.modelVersionId}`}
                        onClick={() => void submitActivateVersion(version.modelVersionId, '관리자 활성화')}
                        className="h-10 rounded border border-slate-300 bg-white px-4 text-sm font-semibold text-slate-700 disabled:cursor-not-allowed disabled:bg-slate-100 disabled:text-slate-400"
                      >
                        활성화
                      </button>
                      <button
                        type="button"
                        disabled={!canDeprecate || busy.actionId === `deprecate-${version.modelVersionId}`}
                        onClick={() => void submitDeprecateVersion(version.modelVersionId, '관리자 사용 중단')}
                        className="h-10 rounded border border-slate-300 bg-white px-4 text-sm font-semibold text-slate-700 disabled:cursor-not-allowed disabled:bg-slate-100 disabled:text-slate-400"
                      >
                        사용 중단
                      </button>
                    </div>
                  </div>
                </article>
              )
            })}
          </div>
        )}
      </StepSection>

      <StepSection
        step="5"
        title="모델 배포"
        description="활성화된 모델 버전을 조직 전체 또는 특정 검사대상에 적용합니다."
        disabled={!selectedModel}
      >
        <SelectedModelNotice
          model={selectedModel}
          emptyText="모델을 먼저 선택하세요."
          extraText={selectedModel ? `배포 가능 버전 수: ${deployableVersions.length}` : undefined}
        />

        {!selectedModel ? (
          <Empty text="모델을 먼저 선택하면 버전 업로드와 배포를 진행할 수 있습니다." />
        ) : (
          <div className="grid gap-3">
            <p className="rounded border border-dashed border-slate-200 bg-slate-50 p-3 text-sm text-slate-500">
              조직/검사대상 선택 UI는 추후 드롭다운으로 교체 예정입니다. 현재는 ID를 직접 입력합니다.
            </p>

            <div className="grid gap-3 md:grid-cols-2">
              <Select
                label="배포 버전"
                value={deployVersionId}
                onChange={setDeployVersionId}
                options={deployableVersions.map((version) => ({
                  value: String(version.modelVersionId),
                  label: `${version.versionName} / ${version.modelCategory} / ${version.modelProfile} / ${version.deployStatus ?? '-'}`,
                }))}
                placeholder={deployableVersions.length === 0 ? '배포 가능한 버전이 없습니다.' : '배포할 버전을 선택하세요.'}
                disabled={deployableVersions.length === 0}
              />
              <Select
                label="배포 범위"
                value={deploymentScope}
                onChange={handleScopeChange}
                options={[
                  { value: 'ORGANIZATION', label: '조직 전체' },
                  { value: 'TARGET', label: '검사대상 단위' },
                ]}
              />
            </div>

            <div className="grid gap-3 md:grid-cols-2">
              <Input label="조직 ID" value={organizationId} onChange={setOrganizationId} placeholder="1001" />
              {deploymentScope === 'TARGET' ? (
                <Input label="검사대상 ID" value={targetId} onChange={setTargetId} placeholder="95001" />
              ) : (
                <Input label="검사대상 ID" value="" onChange={() => undefined} placeholder="조직 전체 배포에서는 사용하지 않습니다." disabled />
              )}
            </div>

            <TextArea label="배포 사유" value={deployReason} onChange={setDeployReason} placeholder="배포 사유를 입력합니다." rows={3} />

            {deployableVersions.length === 0 ? (
              <Empty text="배포 가능한 버전이 없습니다. 버전을 업로드한 뒤 활성화하세요." />
            ) : null}

            <div className="flex justify-end">
              <button
                type="button"
                disabled={!canDeploy}
                onClick={() => void handleDeploy()}
                className="h-10 rounded bg-[#109498] px-4 text-sm font-semibold text-white disabled:cursor-not-allowed disabled:bg-slate-300"
              >
                {busy.deploy ? '배포 중...' : '버전 배포'}
              </button>
            </div>
          </div>
        )}
      </StepSection>

      <StepSection
        step="6"
        title="배포 이력"
        description="조직 또는 검사대상에 적용된 모델 배포 상태를 확인하고 비활성화하거나 롤백합니다."
        disabled={!selectedModel}
      >
        {!selectedModel ? (
          <Empty text="모델을 먼저 선택하면 버전 업로드와 배포를 진행할 수 있습니다." />
        ) : deploymentsLoading ? (
          <Empty text="배포 이력을 불러오는 중입니다." />
        ) : filteredDeployments.length === 0 ? (
          <Empty text="배포 이력이 없습니다. 활성화된 버전을 먼저 배포하세요." />
        ) : (
          <div className="space-y-3">
            {filteredDeployments.map((deployment) => {
              const isActive = deployment.isActive === true
              return (
                <article
                  key={deployment.deploymentId}
                  className={`rounded border p-4 ${isActive ? 'border-[#109498] bg-[#109498]/5' : 'border-slate-200 bg-slate-50'}`}
                >
                  <div className="flex flex-col gap-3 lg:flex-row lg:items-start lg:justify-between">
                    <div className="space-y-2">
                      <div className="flex flex-wrap items-center gap-2">
                        <h4 className="text-base font-semibold text-slate-900">
                          {deployment.modelName || '-'} / {deployment.versionName || '-'}
                        </h4>
                        <StatusBadge tone={deploymentStatusTone(deployment)}>{deployment.deployStatus ?? '-'}</StatusBadge>
                        <StatusBadge tone={isActive ? 'success' : 'muted'}>{isActive ? 'ACTIVE' : 'INACTIVE'}</StatusBadge>
                      </div>
                      <dl className="grid gap-2 text-sm text-slate-600 md:grid-cols-2 xl:grid-cols-4">
                        <InfoRow label="배포 범위" value={deployment.deploymentScope === 'TARGET' ? '검사대상 단위' : '조직 전체'} />
                        <InfoRow label="조직 ID" value={String(deployment.organizationId)} />
                        <InfoRow label="검사대상 ID" value={deployment.targetId != null ? String(deployment.targetId) : '-'} />
                        <InfoRow label="배포 시각" value={formatDateTime(deployment.deployedAt)} />
                        <InfoRow label="배포자" value={deployment.deployedBy != null ? String(deployment.deployedBy) : '-'} />
                        <InfoRow label="현재 deploymentId" value={String(deployment.deploymentId)} />
                        <InfoRow label="롤백 원본" value={deployment.rollbackFromDeploymentId != null ? String(deployment.rollbackFromDeploymentId) : '-'} />
                        <InfoRow label="사유" value={deployment.reason || '-'} />
                      </dl>
                    </div>

                    <div className="grid gap-2 lg:min-w-[240px]">
                      <Input
                        label="롤백 대상 배포 ID"
                        value={rollbackToId[deployment.deploymentId] ?? ''}
                        onChange={(value) => setRollbackToId((prev) => ({ ...prev, [deployment.deploymentId]: value }))}
                        placeholder="이전 deploymentId 입력"
                        disabled={!isActive}
                      />
                      <p className="text-xs text-slate-500">같은 조직/검사대상/범위의 이전 배포 ID만 입력할 수 있습니다.</p>
                      <div className="flex gap-2">
                        <button
                          type="button"
                          disabled={!isActive || busy.actionId === `deployment-off-${deployment.deploymentId}`}
                          onClick={() => void submitDeactivateDeployment(deployment.deploymentId, '운영 비활성화')}
                          className="h-10 flex-1 rounded border border-slate-300 bg-white px-4 text-sm font-semibold text-slate-700 disabled:cursor-not-allowed disabled:bg-slate-100 disabled:text-slate-400"
                        >
                          비활성화
                        </button>
                        <button
                          type="button"
                          disabled={!isActive || busy.actionId === `rollback-${deployment.deploymentId}`}
                          onClick={() => {
                            const target = Number(rollbackToId[deployment.deploymentId] ?? '')
                            if (!target) {
                              window.alert('롤백 대상 배포 ID를 입력하세요.')
                              return
                            }
                            void submitRollbackDeployment(deployment.deploymentId, target, '운영 롤백')
                          }}
                          className="h-10 flex-1 rounded border border-slate-300 bg-white px-4 text-sm font-semibold text-slate-700 disabled:cursor-not-allowed disabled:bg-slate-100 disabled:text-slate-400"
                        >
                          롤백
                        </button>
                      </div>
                    </div>
                  </div>
                </article>
              )
            })}
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

function SummaryItem({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded border border-slate-200 bg-white p-3">
      <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">{label}</p>
      <p className="mt-1 text-sm font-semibold text-slate-900">{value}</p>
    </div>
  )
}

function SelectedModelNotice({
  model,
  emptyText,
  extraText,
}: {
  model: Model | null
  emptyText: string
  extraText?: string
}) {
  return (
    <div className="mb-4 rounded border border-dashed border-slate-200 bg-slate-50 p-3 text-sm text-slate-600">
      <p>
        선택된 모델: <span className="font-semibold text-slate-900">{model?.modelName ?? emptyText}</span>
      </p>
      {model ? <p className="mt-1 text-xs text-slate-500">모델 ID {model.modelId} / 타입 {model.modelType}</p> : null}
      {extraText ? <p className="mt-1 text-xs text-slate-500">{extraText}</p> : null}
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

function StatusBadge({ tone, children }: { tone: 'success' | 'warning' | 'danger' | 'muted'; children: ReactNode }) {
  const toneClass =
    tone === 'success'
      ? 'bg-emerald-100 text-emerald-700'
      : tone === 'warning'
        ? 'bg-amber-100 text-amber-700'
        : tone === 'danger'
          ? 'bg-rose-100 text-rose-700'
          : 'bg-slate-100 text-slate-700'

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
  placeholder,
  disabled,
}: {
  label: string
  value: string
  onChange: (value: string) => void
  options: { value: string; label: string }[]
  placeholder?: string
  disabled?: boolean
}) {
  return (
    <label className="flex flex-col gap-1 text-sm font-medium text-slate-700">
      {label}
      <select
        value={value}
        onChange={(event) => onChange(event.target.value)}
        disabled={disabled}
        className="h-10 rounded border border-slate-200 bg-white px-3 text-sm disabled:cursor-not-allowed disabled:bg-slate-100 disabled:text-slate-400"
      >
        {placeholder ? <option value="">{placeholder}</option> : null}
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
  description,
  onChange,
}: {
  label: string
  description?: string
  onChange: (file: File | null) => void
}) {
  return (
    <label className="flex flex-col gap-1 text-sm font-medium text-slate-700">
      {label}
      <input
        type="file"
        onChange={(event) => onChange(event.target.files?.[0] ?? null)}
        className="rounded border border-slate-200 bg-white px-3 py-2 text-sm"
      />
      {description ? <span className="text-xs font-normal text-slate-500">{description}</span> : null}
    </label>
  )
}

function deployStatusLabel(status: string) {
  switch (status) {
    case 'REGISTERED':
      return '등록됨'
    case 'VALIDATED':
      return '검증 완료'
    case 'DEPLOYED':
      return '배포됨'
    case 'DEPRECATED':
      return '사용 중단'
    default:
      return status
  }
}

function deployStatusTone(status: string): 'success' | 'warning' | 'danger' | 'muted' {
  switch (status as ModelDeployStatus) {
    case 'VALIDATED':
    case 'DEPLOYED':
      return 'success'
    case 'REGISTERED':
      return 'warning'
    case 'DEPRECATED':
      return 'danger'
    default:
      return 'muted'
  }
}

function deploymentStatusTone(deployment: ModelDeployment): 'success' | 'warning' | 'danger' | 'muted' {
  if (deployment.isActive) return 'success'
  if (deployment.deployStatus === 'ROLLED_BACK') return 'warning'
  if (deployment.deployStatus === 'DEACTIVATED') return 'muted'
  return 'muted'
}

function formatDateTime(value?: string | null) {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString('ko-KR')
}
