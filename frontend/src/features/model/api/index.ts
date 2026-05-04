import { apiClient, normalizeApiError } from '../../../shared/api/client'
import type {
  CreateModelRequest,
  DeployModelVersionRequest,
  GenerateModelVersionsForm,
  GenerateModelVersionsResponse,
  Model,
  ModelArtifact,
  ModelDeployment,
  ModelVersion,
  ModelVersionDetail,
  PagedResponse,
  UploadModelVersionForm,
} from '../../../entities/model'

type ApiResponse<T> = {
  success: boolean
  data: T
  message?: string
}

export async function fetchModels(): Promise<PagedResponse<Model>> {
  try {
    const response = await apiClient.get<ApiResponse<PagedResponse<Model>>>('/models')
    return response.data.data
  } catch (error) {
    throw normalizeApiError(error)
  }
}

export async function createModel(payload: CreateModelRequest): Promise<Model> {
  try {
    const response = await apiClient.post<ApiResponse<Model>>('/models', payload)
    return response.data.data
  } catch (error) {
    throw normalizeApiError(error)
  }
}

export async function fetchModel(modelId: number): Promise<Model> {
  try {
    const response = await apiClient.get<ApiResponse<Model>>(`/models/${modelId}`)
    return response.data.data
  } catch (error) {
    throw normalizeApiError(error)
  }
}

export async function fetchModelVersions(modelId: number): Promise<PagedResponse<ModelVersion>> {
  try {
    const response = await apiClient.get<ApiResponse<PagedResponse<ModelVersion>>>(`/models/${modelId}/versions`)
    return response.data.data
  } catch (error) {
    throw normalizeApiError(error)
  }
}

export async function uploadModelVersion(modelId: number, form: UploadModelVersionForm): Promise<ModelVersionDetail> {
  try {
    const formData = new FormData()
    formData.append('versionName', form.versionName)
    formData.append('modelCategory', form.modelCategory)
    formData.append('modelProfile', form.modelProfile)
    if (form.framework) formData.append('framework', form.framework)
    if (form.inputSize) formData.append('inputSize', form.inputSize)
    if (form.thresholdDefault) formData.append('thresholdDefault', form.thresholdDefault)
    if (form.accuracy) formData.append('accuracy', form.accuracy)
    if (form.precisionScore) formData.append('precisionScore', form.precisionScore)
    if (form.recallScore) formData.append('recallScore', form.recallScore)
    if (form.f1Score) formData.append('f1Score', form.f1Score)
    if (form.aurocScore) formData.append('aurocScore', form.aurocScore)
    if (form.ckptFile) formData.append('ckptFile', form.ckptFile)
    if (form.configFile) formData.append('configFile', form.configFile)
    if (form.memoryBankFile) formData.append('memoryBankFile', form.memoryBankFile)
    if (form.labelsFile) formData.append('labelsFile', form.labelsFile)
    const response = await apiClient.post<ApiResponse<ModelVersionDetail>>(`/models/${modelId}/versions`, formData)
    return response.data.data
  } catch (error) {
    throw normalizeApiError(error)
  }
}

export async function generateModelVersionsFromNormalImages(
  modelId: number,
  form: GenerateModelVersionsForm,
): Promise<GenerateModelVersionsResponse> {
  try {
    const formData = new FormData()
    form.normalImages.forEach((file) => formData.append('normalImages', file))
    formData.append('modelCategory', form.modelCategory)
    formData.append('organizationId', form.organizationId)
    formData.append('deploymentScope', form.deploymentScope)
    if (form.deploymentScope === 'TARGET' && form.targetId) formData.append('targetId', form.targetId)
    if (form.thresholdDefault) formData.append('thresholdDefault', form.thresholdDefault)
    if (form.reason) formData.append('reason', form.reason)
    const response = await apiClient.post<ApiResponse<GenerateModelVersionsResponse>>(
      `/models/${modelId}/versions/from-normal-images`,
      formData,
    )
    return response.data.data
  } catch (error) {
    throw normalizeApiError(error)
  }
}

export async function fetchModelVersion(versionId: number): Promise<ModelVersionDetail> {
  try {
    const response = await apiClient.get<ApiResponse<ModelVersionDetail>>(`/model-versions/${versionId}`)
    return response.data.data
  } catch (error) {
    throw normalizeApiError(error)
  }
}

export async function fetchModelArtifacts(versionId: number): Promise<ModelArtifact[]> {
  try {
    const response = await apiClient.get<ApiResponse<ModelArtifact[]>>(`/model-versions/${versionId}/artifacts`)
    return response.data.data
  } catch (error) {
    throw normalizeApiError(error)
  }
}

export async function activateModelVersion(versionId: number, reason: string): Promise<ModelVersionDetail> {
  try {
    const response = await apiClient.patch<ApiResponse<ModelVersionDetail>>(`/model-versions/${versionId}/activate`, { reason })
    return response.data.data
  } catch (error) {
    throw normalizeApiError(error)
  }
}

export async function deprecateModelVersion(versionId: number, reason: string): Promise<ModelVersionDetail> {
  try {
    const response = await apiClient.patch<ApiResponse<ModelVersionDetail>>(`/model-versions/${versionId}/deprecate`, { reason })
    return response.data.data
  } catch (error) {
    throw normalizeApiError(error)
  }
}

export async function fetchModelDeployments(): Promise<PagedResponse<ModelDeployment>> {
  try {
    const response = await apiClient.get<ApiResponse<PagedResponse<ModelDeployment>>>('/model-deployments')
    return response.data.data
  } catch (error) {
    throw normalizeApiError(error)
  }
}

export async function deployModelVersion(versionId: number, payload: DeployModelVersionRequest): Promise<ModelDeployment> {
  try {
    const response = await apiClient.post<ApiResponse<ModelDeployment>>(`/model-versions/${versionId}/deployments`, payload)
    return response.data.data
  } catch (error) {
    throw normalizeApiError(error)
  }
}

export async function deactivateModelDeployment(deploymentId: number, reason: string): Promise<ModelDeployment> {
  try {
    const response = await apiClient.patch<ApiResponse<ModelDeployment>>(`/model-deployments/${deploymentId}/deactivate`, { reason })
    return response.data.data
  } catch (error) {
    throw normalizeApiError(error)
  }
}

export async function rollbackModelDeployment(deploymentId: number, rollbackToDeploymentId: number, reason: string): Promise<ModelDeployment> {
  try {
    const response = await apiClient.patch<ApiResponse<ModelDeployment>>(`/model-deployments/${deploymentId}/rollback`, {
      rollbackToDeploymentId,
      reason,
    })
    return response.data.data
  } catch (error) {
    throw normalizeApiError(error)
  }
}
