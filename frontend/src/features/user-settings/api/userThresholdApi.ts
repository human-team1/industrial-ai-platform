import { AxiosError } from 'axios'
import { apiClient } from '../../../shared/api/client'
import {
  DEFAULT_USER_THRESHOLD,
  type UserThreshold,
} from '../../../entities/user-settings'
import { DEFAULT_THRESHOLD_CHANGE_REASON, type SaveMyThresholdRequest } from './types'

type ApiResponse<T> = {
  success: boolean
  data: T
  message?: string
}

const ENDPOINT = '/users/me/thresholds'

// GET /users/me/thresholds
// 서버 계약상 단일 객체 또는 빈 응답을 가정. 배열 응답이 와도 첫 활성 항목을 사용한다.
//
// TODO(backend-contract): "사용자 threshold 미존재" 전용 errorCode가 확정되면
//   해당 코드 매칭 시에만 null로 변환하고, 그 외 404(라우트 누락/배포 누락/장애)는 throw한다.
//   현재는 코드가 없어 모든 404를 임시로 null 처리하므로 라우트 누락이 사용자 미존재로
//   잘못 표시될 수 있다.
const THRESHOLD_NOT_FOUND_ERROR_CODES = new Set<string>([
  'USER_THRESHOLD_NOT_FOUND',
  'USER_THRESHOLD_MISSING',
])

export async function getMyThreshold(signal?: AbortSignal): Promise<UserThreshold | null> {
  try {
    const response = await apiClient.get<ApiResponse<unknown>>(ENDPOINT, { signal })
    return parseThreshold(response.data.data)
  } catch (error) {
    if (error instanceof AxiosError && error.response?.status === 404) {
      const errorCode = extractErrorCode(error)
      if (errorCode && THRESHOLD_NOT_FOUND_ERROR_CODES.has(errorCode)) {
        return null
      }
      // TODO(backend-contract): errorCode가 미정인 동안에만 임시로 모든 404를 null 처리한다.
      // 계약 확정 후에는 위 if 블록만 남기고 아래 임시 fallback을 제거한다.
      return null
    }
    throw new Error(getThresholdErrorMessage(error, 'load'))
  }
}

function extractErrorCode(error: AxiosError): string | undefined {
  const data = error.response?.data as
    | { errorCode?: string; code?: string }
    | undefined
  return data?.errorCode ?? data?.code
}

// 생성/수정 추상화. UI는 이 함수만 호출하여 thresholdId 의존을 숨긴다.
//
// 백엔드 계약이 upsert PATCH면 PATCH /users/me/thresholds 한 번으로 끝난다.
// 백엔드가 생성/수정 분리 구조면 thresholdId 유무로 POST/PATCH 분기한다.
//
// TODO(backend-contract): 실제 계약 확정 후 분기 로직을 한쪽으로 정리한다.
//   - upsert PATCH: tryUpsertPatch만 사용
//   - 생성/수정 분리: createThreshold + patchThreshold만 사용
export async function saveMyThreshold(
  payload: SaveMyThresholdRequest,
  currentThresholdId: number | null,
): Promise<UserThreshold> {
  const body = {
    ...payload,
    changeReason: payload.changeReason ?? DEFAULT_THRESHOLD_CHANGE_REASON,
  }

  if (currentThresholdId == null) {
    return createThreshold(body)
  }
  return patchThreshold(currentThresholdId, body)
}

async function createThreshold(body: SaveMyThresholdRequest): Promise<UserThreshold> {
  try {
    const response = await apiClient.post<ApiResponse<unknown>>(ENDPOINT, body)
    return parseThreshold(response.data.data) ?? hydrateFromRequest(body, null)
  } catch (error) {
    throw new Error(getThresholdErrorMessage(error, 'save'))
  }
}

async function patchThreshold(
  thresholdId: number,
  body: SaveMyThresholdRequest,
): Promise<UserThreshold> {
  try {
    const response = await apiClient.patch<ApiResponse<unknown>>(`${ENDPOINT}/${thresholdId}`, body)
    return parseThreshold(response.data.data) ?? hydrateFromRequest(body, thresholdId)
  } catch (error) {
    throw new Error(getThresholdErrorMessage(error, 'save'))
  }
}

// ─── 내부 유틸 ────────────────────────────────────────────────────────────
function parseThreshold(raw: unknown): UserThreshold | null {
  const single = pickFirst(raw)
  if (!single) return null

  const id = toFiniteNumber(single.thresholdId ?? single.id)
  const anomalyThreshold = toFiniteNumber(single.anomalyThreshold)
  const lowConfidenceThreshold = toFiniteNumber(single.lowConfidenceThreshold)
  if (anomalyThreshold == null) return null

  return {
    thresholdId: id ?? null,
    anomalyThreshold,
    lowConfidenceThreshold:
      lowConfidenceThreshold ?? DEFAULT_USER_THRESHOLD.lowConfidenceThreshold,
    minAllowed: toFiniteNumber(single.minAllowed) ?? DEFAULT_USER_THRESHOLD.minAllowed,
    maxAllowed: toFiniteNumber(single.maxAllowed) ?? DEFAULT_USER_THRESHOLD.maxAllowed,
    applyScope: typeof single.applyScope === 'string' ? single.applyScope : 'DEFAULT',
    isActive: typeof single.isActive === 'boolean' ? single.isActive : true,
    updatedAt: typeof single.updatedAt === 'string' ? single.updatedAt : undefined,
    thresholdVersion: toFiniteNumber(single.thresholdVersion) ?? undefined,
  }
}

function hydrateFromRequest(
  body: SaveMyThresholdRequest,
  thresholdId: number | null,
): UserThreshold {
  return {
    ...DEFAULT_USER_THRESHOLD,
    thresholdId,
    anomalyThreshold: body.anomalyThreshold,
    lowConfidenceThreshold: body.lowConfidenceThreshold,
    applyScope: body.applyScope ?? DEFAULT_USER_THRESHOLD.applyScope,
  }
}

function pickFirst(raw: unknown): Record<string, unknown> | null {
  if (!raw) return null
  if (Array.isArray(raw)) {
    const active = raw.find(
      (item): item is Record<string, unknown> =>
        isRecord(item) && (item.isActive === true || item.isActive == null),
    )
    return active ?? (isRecord(raw[0]) ? raw[0] : null)
  }
  if (isRecord(raw)) {
    if (Array.isArray((raw as Record<string, unknown>).items)) {
      return pickFirst((raw as Record<string, unknown>).items)
    }
    if (Array.isArray((raw as Record<string, unknown>).content)) {
      return pickFirst((raw as Record<string, unknown>).content)
    }
    return raw
  }
  return null
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null
}

function toFiniteNumber(value: unknown): number | null {
  const n = Number(value)
  return Number.isFinite(n) ? n : null
}

function getThresholdErrorMessage(error: unknown, kind: 'load' | 'save'): string {
  if (error instanceof AxiosError) {
    const status = error.response?.status
    const data = error.response?.data as
      | { detail?: string; message?: string; errorCode?: string }
      | undefined
    const serverMessage = data?.detail ?? data?.message ?? data?.errorCode
    if (serverMessage) return String(serverMessage)
    if (status === 401) return '인증이 만료되었습니다. 다시 로그인해주세요.'
    if (status === 403) return '임계값에 접근할 권한이 없습니다.'
    if (status === 404) return '임계값 정보를 찾을 수 없습니다.'
    if (status === 422) return '임계값 범위가 올바르지 않습니다.'
    if (status && status >= 500) return '서버 오류로 임계값을 처리하지 못했습니다.'
    if (error.code === 'ERR_NETWORK') return '네트워크 상태를 확인해주세요.'
  }
  if (error instanceof Error) return error.message
  return kind === 'load' ? '임계값을 불러오지 못했습니다.' : '임계값을 저장하지 못했습니다.'
}
