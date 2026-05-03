import { useEffect, useState } from 'react'
import type {
  CreateModelRequest,
  DeployModelVersionRequest,
  Model,
  ModelDeployment,
  ModelVersion,
  UploadModelVersionForm,
} from '../../../entities/model'
import {
  activateModelVersion,
  createModel,
  deactivateModelDeployment,
  deployModelVersion,
  deprecateModelVersion,
  fetchModelDeployments,
  fetchModelVersions,
  fetchModels,
  rollbackModelDeployment,
  uploadModelVersion,
} from '../api'

type BusyState = {
  create: boolean
  upload: boolean
  deploy: boolean
  refresh: boolean
  actionId?: string | null
}

export function useModelManagement() {
  const [models, setModels] = useState<Model[]>([])
  const [versions, setVersions] = useState<ModelVersion[]>([])
  const [deployments, setDeployments] = useState<ModelDeployment[]>([])
  const [selectedModelId, setSelectedModelId] = useState<number | null>(null)
  const [loading, setLoading] = useState(true)
  const [versionsLoading, setVersionsLoading] = useState(false)
  const [deploymentsLoading, setDeploymentsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [message, setMessage] = useState<string | null>(null)
  const [busy, setBusy] = useState<BusyState>({
    create: false,
    upload: false,
    deploy: false,
    refresh: false,
    actionId: null,
  })

  async function loadModels() {
    const response = await fetchModels()
    setModels(response.content)
    if (response.content.length === 0) {
      setSelectedModelId(null)
      return
    }

    setSelectedModelId((current) => {
      const exists = current != null && response.content.some((model) => model.modelId === current)
      return exists ? current : response.content[0].modelId
    })
  }

  async function loadVersions(modelId: number | null) {
    if (!modelId) {
      setVersions([])
      return
    }

    setVersionsLoading(true)
    try {
      const response = await fetchModelVersions(modelId)
      setVersions(response.content)
    } finally {
      setVersionsLoading(false)
    }
  }

  async function loadDeployments() {
    setDeploymentsLoading(true)
    try {
      const response = await fetchModelDeployments()
      setDeployments(response.content)
    } finally {
      setDeploymentsLoading(false)
    }
  }

  async function reloadAll() {
    setBusy((prev) => ({ ...prev, refresh: true }))
    setLoading(true)
    setError(null)
    try {
      await Promise.all([loadModels(), loadDeployments()])
    } catch (nextError) {
      setError(nextError instanceof Error ? nextError.message : '모델 관리 데이터를 불러오지 못했습니다.')
    } finally {
      setBusy((prev) => ({ ...prev, refresh: false }))
      setLoading(false)
    }
  }

  useEffect(() => {
    void reloadAll()
  }, [])

  useEffect(() => {
    void loadVersions(selectedModelId).catch((nextError) => {
      setError(nextError instanceof Error ? nextError.message : '모델 버전 목록을 불러오지 못했습니다.')
    })
  }, [selectedModelId])

  async function submitCreateModel(payload: CreateModelRequest) {
    setBusy((prev) => ({ ...prev, create: true }))
    setError(null)
    try {
      const created = await createModel(payload)
      setMessage(`모델 "${created.modelName}"을 등록했습니다.`)
      await loadModels()
      setSelectedModelId(created.modelId)
    } catch (nextError) {
      setError(nextError instanceof Error ? nextError.message : '모델 등록에 실패했습니다.')
    } finally {
      setBusy((prev) => ({ ...prev, create: false }))
    }
  }

  async function submitUploadVersion(form: UploadModelVersionForm) {
    if (!selectedModelId) return

    setBusy((prev) => ({ ...prev, upload: true }))
    setError(null)
    try {
      await uploadModelVersion(selectedModelId, form)
      setMessage(`버전 "${form.versionName}" 업로드를 완료했습니다.`)
      await loadVersions(selectedModelId)
    } catch (nextError) {
      setError(nextError instanceof Error ? nextError.message : '모델 버전 업로드에 실패했습니다.')
    } finally {
      setBusy((prev) => ({ ...prev, upload: false }))
    }
  }

  async function submitActivateVersion(versionId: number, reason: string) {
    setBusy((prev) => ({ ...prev, actionId: `activate-${versionId}` }))
    setError(null)
    try {
      await activateModelVersion(versionId, reason)
      setMessage('모델 버전을 활성화했습니다.')
      await loadVersions(selectedModelId)
    } catch (nextError) {
      setError(nextError instanceof Error ? nextError.message : '모델 버전 활성화에 실패했습니다.')
    } finally {
      setBusy((prev) => ({ ...prev, actionId: null }))
    }
  }

  async function submitDeprecateVersion(versionId: number, reason: string) {
    setBusy((prev) => ({ ...prev, actionId: `deprecate-${versionId}` }))
    setError(null)
    try {
      await deprecateModelVersion(versionId, reason)
      setMessage('모델 버전을 사용 중단했습니다.')
      await loadVersions(selectedModelId)
      await loadDeployments()
    } catch (nextError) {
      setError(nextError instanceof Error ? nextError.message : '모델 버전 사용 중단에 실패했습니다.')
    } finally {
      setBusy((prev) => ({ ...prev, actionId: null }))
    }
  }

  async function submitDeployVersion(versionId: number, payload: DeployModelVersionRequest) {
    setBusy((prev) => ({ ...prev, deploy: true }))
    setError(null)
    try {
      await deployModelVersion(versionId, payload)
      setMessage('모델 배포를 완료했습니다.')
      await Promise.all([loadVersions(selectedModelId), loadDeployments()])
    } catch (nextError) {
      setError(nextError instanceof Error ? nextError.message : '모델 배포에 실패했습니다.')
    } finally {
      setBusy((prev) => ({ ...prev, deploy: false }))
    }
  }

  async function submitDeactivateDeployment(deploymentId: number, reason: string) {
    setBusy((prev) => ({ ...prev, actionId: `deployment-off-${deploymentId}` }))
    setError(null)
    try {
      await deactivateModelDeployment(deploymentId, reason)
      setMessage('배포를 비활성화했습니다.')
      await loadDeployments()
    } catch (nextError) {
      setError(nextError instanceof Error ? nextError.message : '배포 비활성화에 실패했습니다.')
    } finally {
      setBusy((prev) => ({ ...prev, actionId: null }))
    }
  }

  async function submitRollbackDeployment(deploymentId: number, rollbackToDeploymentId: number, reason: string) {
    setBusy((prev) => ({ ...prev, actionId: `rollback-${deploymentId}` }))
    setError(null)
    try {
      await rollbackModelDeployment(deploymentId, rollbackToDeploymentId, reason)
      setMessage('롤백을 완료했습니다.')
      await loadDeployments()
    } catch (nextError) {
      setError(nextError instanceof Error ? nextError.message : '롤백에 실패했습니다.')
    } finally {
      setBusy((prev) => ({ ...prev, actionId: null }))
    }
  }

  return {
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
  }
}
