export type PageResponse<T> = {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export type SystemStatus = {
  snapshotId?: number | null
  cpuUsage?: number | null
  memoryUsage?: number | null
  diskUsage?: number | null
  responseTimeMs?: number | null
  overallStatus?: string | null
  createdAt?: string | null
}

export type SystemComponentStatus = {
  componentStatusId: number
  componentType: string
  componentName: string
  status: string
  message?: string | null
  cpuUsage?: number | null
  memoryUsage?: number | null
  diskUsage?: number | null
  hostName?: string | null
  instanceId?: string | null
  responseTimeMs?: number | null
  checkedAt?: string | null
  createdAt?: string | null
}

export type OperationLog = {
  operationLogId: number
  eventType?: string | null
  eventStatus?: string | null
  logLevel?: string | null
  sourceComponent?: string | null
  requestId?: string | null
  actorUserId?: number | null
  detailMessage?: string | null
  relatedPath?: string | null
  createdAt?: string | null
}

export type AsyncJob = {
  jobId: number
  jobType?: string | null
  jobStatus?: string | null
  targetType?: string | null
  targetId?: number | null
  errorMessage?: string | null
  createdAt?: string | null
  completedAt?: string | null
}

export type OperationPolicy = {
  operationPolicyId: number
  policyCategory?: string | null
  policyKey?: string | null
  policyName?: string | null
  policyValue: string
  valueType?: 'NUMBER' | 'BOOLEAN' | 'STRING' | 'JSON' | string | null
  description?: string | null
  isActive?: boolean | null
  updatedAt?: string | null
  updatedBy?: number | null
}
