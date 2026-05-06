import { AxiosError } from 'axios'
import { apiClient, normalizeApiError } from '../../../shared/api/client'
import type {
  AnalysisTargetOption,
  AvailableInspectionModel,
  AvailableInspectionModelsParams,
  AvailableRealtimeCamera,
  AvailableRealtimeCamerasParams,
  InspectionDetail,
  InspectionEvent,
  StartRealtimeInspectionPayload,
  ThresholdOption,
  UploadInspectionPayload,
  UploadInspectionResponse,
} from '../types'

type ApiResponse<T> = {
  success: boolean
  data: T
  message?: string
}

type ProblemDetails = {
  detail?: string
  title?: string
  errorCode?: string
  message?: string
}

export async function uploadInspection(
  payload: UploadInspectionPayload,
): Promise<UploadInspectionResponse> {
  try {
    const formData = new FormData()
    formData.append('file', payload.file)
    formData.append('deploymentId', String(payload.deploymentId))
    if (payload.targetId) formData.append('targetId', String(payload.targetId))
    if (payload.thresholdId) formData.append('thresholdId', String(payload.thresholdId))
    if (payload.inputMode) formData.append('inputMode', payload.inputMode)
    if (payload.sourceType) formData.append('sourceType', payload.sourceType)
    if (payload.roiMode) formData.append('roiMode', payload.roiMode)
    if (payload.qualityGateEnabled !== undefined) {
      formData.append('qualityGateEnabled', String(payload.qualityGateEnabled))
    }
    if (payload.idempotencyKey) formData.append('idempotencyKey', payload.idempotencyKey)

    const response = await apiClient.post<ApiResponse<UploadInspectionResponse>>(
      '/inspections/upload',
      formData,
      {
        headers: payload.idempotencyKey
          ? {
              'Idempotency-Key': payload.idempotencyKey,
            }
          : undefined,
      },
    )
    return response.data.data
  } catch (error) {
    throw new Error(getInspectionErrorMessage(error))
  }
}

export async function fetchAvailableInspectionModels(
  params: AvailableInspectionModelsParams,
  signal?: AbortSignal,
): Promise<AvailableInspectionModel[]> {
  try {
    const response = await apiClient.get<ApiResponse<{ items: AvailableInspectionModel[] }>>(
      '/inspection-models/available',
      {
        params: compactParams(params),
        signal,
      },
    )
    return response.data.data?.items ?? []
  } catch (error) {
    throw normalizeApiError(error)
  }
}

export async function fetchAvailableRealtimeCameras(
  params: AvailableRealtimeCamerasParams,
  signal?: AbortSignal,
): Promise<AvailableRealtimeCamera[]> {
  try {
    const response = await apiClient.get<ApiResponse<{ items: AvailableRealtimeCamera[] }>>(
      '/realtime/cameras/available',
      {
        params: compactParams(params),
        signal,
      },
    )
    return response.data.data?.items ?? []
  } catch (error) {
    throw normalizeApiError(error)
  }
}

export async function startRealtimeInspection(
  payload: StartRealtimeInspectionPayload,
): Promise<UploadInspectionResponse> {
  try {
    const response = await apiClient.post<ApiResponse<UploadInspectionResponse>>(
      '/inspections/realtime',
      payload,
    )
    return response.data.data
  } catch (error) {
    throw normalizeApiError(error)
  }
}

export async function rerunReviewInspection(
  reviewQueueId: number,
  deploymentId: number,
): Promise<UploadInspectionResponse> {
  try {
    const response = await apiClient.post<ApiResponse<UploadInspectionResponse>>(
      `/reviews/${reviewQueueId}/rerun`,
      { deploymentId },
    )
    return response.data.data
  } catch (error) {
    throw normalizeApiError(error)
  }
}

export async function getAnalysisTargets(signal?: AbortSignal): Promise<AnalysisTargetOption[]> {
  try {
    const response = await apiClient.get<ApiResponse<unknown>>('/analysis-targets', { signal })
    return toAnalysisTargetOptions(response.data.data)
  } catch (error) {
    throw new Error(getInspectionErrorMessage(error))
  }
}

export async function getMyThresholds(signal?: AbortSignal): Promise<ThresholdOption[]> {
  try {
    const response = await apiClient.get<ApiResponse<unknown>>('/users/me/thresholds', { signal })
    return toThresholdOptions(response.data.data)
  } catch (error) {
    throw new Error(getInspectionErrorMessage(error))
  }
}

export async function getInspectionEvents(
  inspectionId: number,
  signal?: AbortSignal,
): Promise<InspectionEvent[]> {
  try {
    const response = await apiClient.get<ApiResponse<InspectionEvent[]>>(
      `/inspections/${inspectionId}/events`,
      { signal },
    )
    return response.data.data ?? []
  } catch (error) {
    throw new Error(getInspectionErrorMessage(error))
  }
}

export async function getInspectionDetail(
  inspectionId: number,
  signal?: AbortSignal,
): Promise<InspectionDetail> {
  try {
    const response = await apiClient.get<ApiResponse<InspectionDetail>>(
      `/inspections/${inspectionId}`,
      { signal },
    )
    return response.data.data
  } catch (error) {
    throw new Error(getInspectionErrorMessage(error))
  }
}

function getInspectionErrorMessage(error: unknown) {
  if (error instanceof AxiosError) {
    const status = error.response?.status
    const data = error.response?.data as ProblemDetails | undefined
    const serverMessage = data?.detail ?? data?.title ?? data?.errorCode ?? data?.message

    if (serverMessage) return String(serverMessage)
    if (status === 401) return '인증이 만료되었거나 로그인 정보가 올바르지 않습니다.'
    if (status === 403) return '검사 요청 권한이 없습니다.'
    if (status === 404) return '요청한 검사 정보를 찾을 수 없습니다.'
    if (status === 409) return '중복 요청이 감지되었거나 현재 상태에서 처리할 수 없습니다.'
    if (status === 422) return '검사 요청 값이 올바르지 않습니다.'
    if (status && status >= 500) return '서버 오류로 검사 요청에 실패했습니다.'
    if (error.code === 'ERR_NETWORK') return '네트워크 상태를 확인해 주세요.'
    return '검사 요청에 실패했습니다. 파일 형식과 네트워크 상태를 확인해 주세요.'
  }

  if (error instanceof Error) return error.message
  return '검사 요청에 실패했습니다. 파일 형식과 네트워크 상태를 확인해 주세요.'
}

function toAnalysisTargetOptions(data: unknown): AnalysisTargetOption[] {
  const items = extractList(data)
  return items
    .map((item): AnalysisTargetOption | null => {
      if (!isRecord(item)) return null
      const rawId = item.id ?? item.targetId ?? item.analysisTargetId
      const id = Number(rawId)
      if (!Number.isFinite(id)) return null

      return {
        id,
        name: String(item.name ?? item.targetName ?? item.equipmentName ?? `검사 대상 #${id}`),
        type: item.type ? String(item.type) : undefined,
        status: item.status ? String(item.status) : undefined,
      }
    })
    .filter((item): item is AnalysisTargetOption => Boolean(item))
}

function toThresholdOptions(data: unknown): ThresholdOption[] {
  const items = extractList(data)
  if (items.length > 0) {
    return items
      .map((item, index): ThresholdOption | null => {
        if (!isRecord(item)) return null
        const rawId = item.id ?? item.thresholdId
        const id = rawId == null ? undefined : Number(rawId)
        return {
          id: Number.isFinite(id) ? id : undefined,
          name: String(item.name ?? item.thresholdName ?? `임계값 ${index + 1}`),
          anomalyThreshold: toOptionalNumber(item.anomalyThreshold),
          lowConfidenceThreshold: toOptionalNumber(item.lowConfidenceThreshold),
          source: item.source ? String(item.source) : undefined,
        }
      })
      .filter((item): item is ThresholdOption => Boolean(item))
  }

  if (isRecord(data) && ('anomalyThreshold' in data || 'lowConfidenceThreshold' in data)) {
    return [
      {
        name: data.source === 'SYSTEM_DEFAULT' ? '기본 임계값' : '임계값',
        anomalyThreshold: toOptionalNumber(data.anomalyThreshold),
        lowConfidenceThreshold: toOptionalNumber(data.lowConfidenceThreshold),
        source: data.source ? String(data.source) : undefined,
      },
    ]
  }

  return []
}

function extractList(data: unknown): unknown[] {
  if (Array.isArray(data)) return data
  if (!isRecord(data)) return []
  if (Array.isArray(data.items)) return data.items
  if (Array.isArray(data.content)) return data.content
  if (Array.isArray(data.data)) return data.data
  return []
}

function compactParams(params: Record<string, unknown>) {
  return Object.fromEntries(
    Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== ''),
  )
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null
}

function toOptionalNumber(value: unknown) {
  const numberValue = Number(value)
  return Number.isFinite(numberValue) ? numberValue : undefined
}
