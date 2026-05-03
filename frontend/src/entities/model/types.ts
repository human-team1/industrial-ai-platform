export type Model = {
  modelId: number
  modelName: string
  modelType: string
  description?: string | null
  createdAt?: string | null
}

export type ModelCategory = 'OBJECT' | 'TEXTURE'

export type ModelProfile = 'SPEED' | 'PERFORMANCE'

export type ModelDeployStatus = 'REGISTERED' | 'VALIDATED' | 'DEPLOYED' | 'DEPRECATED'

export type ModelArtifactType = 'CKPT' | 'CONFIG' | 'LABELS' | 'EXTRA'

export type DeploymentScope = 'ORGANIZATION' | 'TARGET'

export type DeploymentStatus = 'DEPLOYED' | 'ROLLED_BACK' | 'DEACTIVATED'

export type ModelArtifact = {
  modelArtifactId: number
  modelVersionId: number
  fileId?: number | null
  artifactType?: ModelArtifactType | string | null
  fileName?: string | null
  objectKey?: string | null
  checksum?: string | null
  createdAt?: string | null
}

export type ModelVersion = {
  modelVersionId: number
  modelId: number
  modelName?: string | null
  versionName: string
  modelCategory: ModelCategory | string
  modelProfile: ModelProfile | string
  framework?: string | null
  inputSize?: string | null
  thresholdDefault?: number | null
  accuracy?: number | null
  precisionScore?: number | null
  recallScore?: number | null
  f1Score?: number | null
  aurocScore?: number | null
  deployStatus?: ModelDeployStatus | string | null
  isActive?: boolean | null
  validatedAt?: string | null
  validatedBy?: number | null
  createdAt?: string | null
}

export type ModelVersionDetail = {
  version: ModelVersion
  artifacts: ModelArtifact[]
}

export type ModelDeployment = {
  deploymentId: number
  organizationId: number
  targetId?: number | null
  modelVersionId: number
  modelId?: number | null
  modelName?: string | null
  versionName?: string | null
  deploymentScope: DeploymentScope | string
  deployStatus?: DeploymentStatus | string | null
  isActive?: boolean | null
  deployedAt?: string | null
  deployedBy?: number | null
  rollbackFromDeploymentId?: number | null
  reason?: string | null
}

export type PagedResponse<T> = {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export type CreateModelRequest = {
  modelName: string
  modelType: string
  description?: string
}

export type UploadModelVersionForm = {
  versionName: string
  modelCategory: ModelCategory
  modelProfile: ModelProfile
  framework?: string
  inputSize?: string
  thresholdDefault?: string
  accuracy?: string
  precisionScore?: string
  recallScore?: string
  f1Score?: string
  aurocScore?: string
  ckptFile: File | null
  configFile: File | null
  labelsFile: File | null
}

export type DeployModelVersionRequest = {
  organizationId: number
  targetId?: number | null
  deploymentScope: DeploymentScope
  reason?: string
}
