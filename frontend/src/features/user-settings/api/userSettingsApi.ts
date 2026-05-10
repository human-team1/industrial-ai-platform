import { AxiosError } from 'axios'
import { apiClient } from '../../../shared/api/client'
import {
  DEFAULT_USER_SETTINGS,
  type UserSettings,
} from '../../../entities/user-settings'
import type { GetMySettingsResponse, PatchMySettingsRequest } from './types'

type ApiResponse<T> = {
  success: boolean
  data: T
  message?: string
}

const ENDPOINT = '/users/me/settings'

// 신규 사용자 설정(USER_SETTING row 없음)에 한해 DEFAULT로 정상화한다.
//
// ⚠ 리스크: 백엔드 errorCode 계약 미확정으로, 현재는 errorCode 없는 404도 임시로 DEFAULT 처리한다.
//   이 임시 fallback은 다음 케이스를 사용자에게 "정상 기본값"처럼 숨길 수 있다.
//     - API 라우트 누락 / API prefix 오류
//     - 백엔드 미구현 / 배포 누락
//     - 인증 라우팅이 잘못되어 404 응답이 떨어지는 경우
//
// TODO(backend-contract): errorCode 계약 확정 후, 화이트리스트 매칭만 남기고 임시 fallback은 제거한다.
const SETTINGS_NOT_FOUND_ERROR_CODES = new Set<string>([
  'USER_SETTING_NOT_FOUND',
  'USER_SETTING_MISSING',
])

export async function getMySettings(signal?: AbortSignal): Promise<UserSettings> {
  try {
    const response = await apiClient.get<ApiResponse<GetMySettingsResponse | null>>(ENDPOINT, {
      signal,
    })
    return mergeWithDefaults(response.data.data)
  } catch (error) {
    // 신규 사용자(USER_SETTING row 없음) 또는 endpoint 미구현으로 인한 404를
    // 에러로 띄우면 페이지 진입 자체가 실패한 것처럼 보인다. DEFAULT로 정상화.
    if (error instanceof AxiosError && error.response?.status === 404) {
      const errorCode = extractErrorCode(error)
      if (errorCode && SETTINGS_NOT_FOUND_ERROR_CODES.has(errorCode)) {
        return mergeWithDefaults(null)
      }
      // TODO(backend-contract): errorCode 미정인 동안에만 임시 fallback.
      // 계약 확정 후에는 위 if 블록만 남기고 아래 임시 fallback을 제거한다.
      return mergeWithDefaults(null)
    }
    throw new Error(getSettingsErrorMessage(error, 'load'))
  }
}

function extractErrorCode(error: AxiosError): string | undefined {
  const data = error.response?.data as { errorCode?: string; code?: string } | undefined
  return data?.errorCode ?? data?.code
}

export async function patchMySettings(
  payload: PatchMySettingsRequest,
): Promise<UserSettings> {
  try {
    const response = await apiClient.patch<ApiResponse<GetMySettingsResponse>>(ENDPOINT, payload)
    return mergeWithDefaults(response.data.data)
  } catch (error) {
    throw new Error(getSettingsErrorMessage(error, 'save'))
  }
}

function mergeWithDefaults(raw: GetMySettingsResponse | null | undefined): UserSettings {
  if (!raw) return { ...DEFAULT_USER_SETTINGS }
  return {
    notificationEnabled:
      typeof raw.notificationEnabled === 'boolean'
        ? raw.notificationEnabled
        : DEFAULT_USER_SETTINGS.notificationEnabled,
    defaultDashboardRange:
      typeof raw.defaultDashboardRange === 'string' && raw.defaultDashboardRange
        ? raw.defaultDashboardRange
        : DEFAULT_USER_SETTINGS.defaultDashboardRange,
    defaultCameraId:
      typeof raw.defaultCameraId === 'number' && Number.isFinite(raw.defaultCameraId)
        ? raw.defaultCameraId
        : null,
  }
}

function getSettingsErrorMessage(error: unknown, kind: 'load' | 'save'): string {
  if (error instanceof AxiosError) {
    const status = error.response?.status
    const data = error.response?.data as
      | { detail?: string; message?: string; errorCode?: string }
      | undefined
    const serverMessage = data?.detail ?? data?.message ?? data?.errorCode
    if (serverMessage) return String(serverMessage)
    if (status === 401) return '인증이 만료되었습니다. 다시 로그인해주세요.'
    if (status === 403) return '설정에 접근할 권한이 없습니다.'
    if (status === 404) return '설정 정보를 찾을 수 없습니다.'
    if (status === 422) return '설정 값이 올바르지 않습니다.'
    if (status && status >= 500) return '서버 오류로 설정을 처리하지 못했습니다.'
    if (error.code === 'ERR_NETWORK') return '네트워크 상태를 확인해주세요.'
  }
  if (error instanceof Error) return error.message
  return kind === 'load' ? '설정을 불러오지 못했습니다.' : '설정을 저장하지 못했습니다.'
}
