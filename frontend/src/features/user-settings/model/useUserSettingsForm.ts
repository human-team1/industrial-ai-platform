import { useMemo, useRef, useState } from 'react'
import {
  DEFAULT_USER_PREFERENCES,
  DEFAULT_USER_SETTINGS,
  FLOAT_EPSILON,
  isThresholdValueChanged,
  normalizeThresholdValue,
} from '../../../entities/user-settings'
import type {
  DashboardView,
  DetectionView,
  NotificationView,
  SecurityView,
  SummaryView,
} from '../types'
import type { PatchMySettingsRequest, SaveMyThresholdRequest } from '../api/types'
import { useUserPreferences } from './useUserPreferences'
import { useUserSettings } from './useUserSettings'
import { useUserThreshold } from './useUserThreshold'

// 저장 흐름 상태 — UI 옵션이 아니라 form 훅의 결과 상태이므로 훅 파일 내부에 둔다.
export type SaveStatus =
  | { kind: 'idle' }
  | { kind: 'noop' } // 변경된 설정이 없어 저장이 수행되지 않음
  | { kind: 'success' }
  | { kind: 'partial'; failed: Array<'settings' | 'threshold'>; messages: string[] }
  | { kind: 'error'; messages: string[] }

export type UseUserSettingsFormResult = {
  loading: boolean
  saving: boolean
  loadError: string | null
  saveStatus: SaveStatus
  validationError: string | null

  // 임계값 직접 입력 관련 도움말/검증
  thresholdMin: number
  thresholdMax: number
  effectiveThresholdMin: number
  lowConfidenceThreshold: number
  hasValidThresholdInputRange: boolean
  thresholdInputError: string | null

  dashboard: DashboardView
  notifications: NotificationView
  detection: DetectionView
  security: SecurityView
  summary: SummaryView

  updateDashboard: <K extends keyof DashboardView>(key: K, value: DashboardView[K]) => void
  updateNotifications: <K extends keyof NotificationView>(
    key: K,
    value: NotificationView[K],
  ) => void
  updateDetection: <K extends keyof DetectionView>(key: K, value: DetectionView[K]) => void
  updateSecurity: <K extends keyof SecurityView>(key: K, value: SecurityView[K]) => void

  handleSave: () => Promise<void>
  handleCancel: () => void
  handleReset: () => void
}

export function useUserSettingsForm(): UseUserSettingsFormResult {
  const settingsHook = useUserSettings()
  const thresholdHook = useUserThreshold()
  const preferencesHook = useUserPreferences()

  const [saving, setSaving] = useState(false)
  const [saveStatus, setSaveStatus] = useState<SaveStatus>({ kind: 'idle' })
  const [validationError, setValidationError] = useState<string | null>(null)

  // handleCancel/handleReset에서 마지막 서버/저장된 값으로 되돌릴 수 있도록 보관.
  // settings/threshold는 hook 내부 state를 다시 setLocal로 덮어쓰는 방식.
  const lastServerSettingsRef = useRef(settingsHook.settings)
  const lastServerThresholdRef = useRef(thresholdHook.threshold)
  const lastPersistedPreferencesRef = useRef(preferencesHook.preferences)
  // 서버 응답을 받을 때마다 baseline 갱신
  if (settingsHook.loaded && lastServerSettingsRef.current !== settingsHook.settings) {
    // 페이지 진입 후 첫 로드 또는 외부에서 reload된 경우 baseline 동기화
    lastServerSettingsRef.current = settingsHook.settings
  }
  if (thresholdHook.loaded && lastServerThresholdRef.current !== thresholdHook.threshold) {
    lastServerThresholdRef.current = thresholdHook.threshold
  }

  const loading = !settingsHook.loaded || !thresholdHook.loaded
  const loadError = settingsHook.loadError ?? thresholdHook.loadError

  // ─── View-Model 조립 ────────────────────────────────────────────────────
  const dashboard: DashboardView = useMemo(
    () => ({
      entryPage: preferencesHook.preferences.entryPage,
      refreshCycle: preferencesHook.preferences.refreshCycle,
      chartRange: settingsHook.settings.defaultDashboardRange,
      theme: preferencesHook.preferences.theme,
    }),
    [
      preferencesHook.preferences.entryPage,
      preferencesHook.preferences.refreshCycle,
      preferencesHook.preferences.theme,
      settingsHook.settings.defaultDashboardRange,
    ],
  )

  const notifications: NotificationView = useMemo(
    () => ({
      anomalyAlert: settingsHook.settings.notificationEnabled,
      systemAlert: preferencesHook.preferences.systemAlert,
      reportAlert: preferencesHook.preferences.reportAlert,
      emailAlert: preferencesHook.preferences.emailAlert,
    }),
    [
      settingsHook.settings.notificationEnabled,
      preferencesHook.preferences.systemAlert,
      preferencesHook.preferences.reportAlert,
      preferencesHook.preferences.emailAlert,
    ],
  )

  const detection: DetectionView = useMemo(
    () => ({
      threshold: thresholdHook.threshold.anomalyThreshold,
      sensitivity: preferencesHook.preferences.sensitivity,
      interval: preferencesHook.preferences.interval,
    }),
    [
      thresholdHook.threshold.anomalyThreshold,
      preferencesHook.preferences.sensitivity,
      preferencesHook.preferences.interval,
    ],
  )

  const security: SecurityView = useMemo(
    () => ({
      autoLogout: preferencesHook.preferences.autoLogout,
      sessionKeep: preferencesHook.preferences.sessionKeep,
    }),
    [preferencesHook.preferences.autoLogout, preferencesHook.preferences.sessionKeep],
  )

  const summary: SummaryView = useMemo(
    () => ({
      entryPage: dashboard.entryPage,
      refreshCycle: dashboard.refreshCycle,
      chartRange: dashboard.chartRange,
      theme: dashboard.theme,
      anomalyAlert: notifications.anomalyAlert,
      emailAlert: notifications.emailAlert,
      threshold: detection.threshold,
      autoLogout: security.autoLogout,
    }),
    [dashboard, notifications, detection, security],
  )

  // ─── 임계값 inline 검증 ────────────────────────────────────────────────
  // 정밀도 정책:
  //   - UI 입력/증감 단위(step): 0.01  → 사용자가 직접 조정 가능한 정밀도는 소수 2자리.
  //   - 서버 저장 정규화: Number(value.toFixed(4)) → DECIMAL(5,4) 호환.
  //     입력 정밀도(2자리)와 저장 정규화(4자리)는 목적이 다르다.
  //     JS number는 trailing zero를 보존하지 않으므로 0.7500 표기는 JSON에서 0.75로 전송될 수 있음.
  //
  // anomalyThreshold만 직접 입력. lowConfidenceThreshold는 서버 값 유지(UI 미노출).
  // 사용자가 lowConfidence보다 작은 값을 입력해 차단되는 혼동을 줄이기 위해
  // "저장 가능 범위"를 effectiveMin = max(minAllowed, lowConfidence + step)으로 표시한다.
  const thresholdMin = thresholdHook.threshold.minAllowed
  const thresholdMax = thresholdHook.threshold.maxAllowed
  const lowConfidenceThreshold = thresholdHook.threshold.lowConfidenceThreshold
  const THRESHOLD_INPUT_STEP = 0.01
  const effectiveThresholdMin = useMemo(() => {
    // lowConfidence + step을 소수 2자리로 정규화 (UI step과 동일 단위)
    const candidate = Number((lowConfidenceThreshold + THRESHOLD_INPUT_STEP).toFixed(2))
    return Math.max(thresholdMin, candidate)
  }, [thresholdMin, lowConfidenceThreshold])

  // edge case: lowConfidence + step > maxAllowed → 저장 가능한 anomalyThreshold 값이 없다.
  // 예) lowConfidence=1.00, max=1.00 → effectiveMin=1.01 > max → 저장 불가.
  // 비교 기준: 산술 오차(FLOAT_EPSILON)를 초과하는 진짜 초과만 invalid로 판단.
  const hasValidThresholdInputRange =
    effectiveThresholdMin - thresholdMax <= FLOAT_EPSILON

  const thresholdInputError = useMemo<string | null>(() => {
    if (!hasValidThresholdInputRange) {
      return '저장 가능한 임계값 범위가 없습니다. 저신뢰 기준 또는 허용 범위 설정을 확인해 주세요.'
    }
    const v = detection.threshold
    if (!Number.isFinite(v)) return '숫자 값을 입력해주세요.'
    // 1) 시스템 허용 범위(minAllowed/maxAllowed) 검증 — 프로젝트 정책상 저장 차단 사유
    if (v < thresholdMin || v > thresholdMax) {
      return `허용 범위(${thresholdMin.toFixed(2)} ~ ${thresholdMax.toFixed(2)}) 안의 값을 입력해주세요.`
    }
    // 2) 저신뢰 기준 검증 — 판정 구간 정합 (low < anomaly)
    if (lowConfidenceThreshold >= v) {
      return `이상 임계값은 저신뢰 기준 ${lowConfidenceThreshold.toFixed(2)}보다 커야 합니다.`
    }
    return null
  }, [
    detection.threshold,
    thresholdMin,
    thresholdMax,
    lowConfidenceThreshold,
    hasValidThresholdInputRange,
  ])

  // ─── 변경 핸들러 ────────────────────────────────────────────────────────
  function clearStatus() {
    if (saveStatus.kind !== 'idle') setSaveStatus({ kind: 'idle' })
    if (validationError) setValidationError(null)
  }

  function updateDashboard<K extends keyof DashboardView>(key: K, value: DashboardView[K]) {
    clearStatus()
    if (key === 'chartRange') {
      settingsHook.setLocal('defaultDashboardRange', value as string)
    } else {
      preferencesHook.setLocal(key as 'entryPage' | 'refreshCycle' | 'theme', value as string)
    }
  }

  function updateNotifications<K extends keyof NotificationView>(
    key: K,
    value: NotificationView[K],
  ) {
    clearStatus()
    if (key === 'anomalyAlert') {
      settingsHook.setLocal('notificationEnabled', value as boolean)
    } else {
      preferencesHook.setLocal(
        key as 'systemAlert' | 'reportAlert' | 'emailAlert',
        value as boolean,
      )
    }
  }

  function updateDetection<K extends keyof DetectionView>(key: K, value: DetectionView[K]) {
    clearStatus()
    if (key === 'threshold') {
      thresholdHook.setLocalAnomaly(value as number)
    } else {
      preferencesHook.setLocal(key as 'sensitivity' | 'interval', value as string)
    }
  }

  function updateSecurity<K extends keyof SecurityView>(key: K, value: SecurityView[K]) {
    clearStatus()
    if (key === 'autoLogout') {
      preferencesHook.setLocal('autoLogout', value as string)
    } else {
      preferencesHook.setLocal('sessionKeep', value as boolean)
    }
  }

  // ─── 저장 흐름 ──────────────────────────────────────────────────────────
  function buildSettingsPatch(): PatchMySettingsRequest | null {
    const baseline = lastServerSettingsRef.current ?? DEFAULT_USER_SETTINGS
    const current = settingsHook.settings
    const patch: PatchMySettingsRequest = {}
    if (current.notificationEnabled !== baseline.notificationEnabled) {
      patch.notificationEnabled = current.notificationEnabled
    }
    if (current.defaultDashboardRange !== baseline.defaultDashboardRange) {
      patch.defaultDashboardRange = current.defaultDashboardRange
    }
    if (current.defaultCameraId !== baseline.defaultCameraId) {
      patch.defaultCameraId = current.defaultCameraId
    }
    return Object.keys(patch).length > 0 ? patch : null
  }

  function buildThresholdPayload(): SaveMyThresholdRequest | null {
    const baseline = lastServerThresholdRef.current
    const current = thresholdHook.threshold
    // 서버 값이 한 번도 없었던 경우(hasServerValue=false): 변경된 값이 있으면 신규 저장
    if (!thresholdHook.hasServerValue) {
      // 디폴트와 비교해 둘 다 안 바뀌었으면 굳이 저장하지 않는다.
      const anomalyChanged = isThresholdValueChanged(
        current.anomalyThreshold,
        baseline.anomalyThreshold,
      )
      const lowChanged = isThresholdValueChanged(
        current.lowConfidenceThreshold,
        baseline.lowConfidenceThreshold,
      )
      if (!anomalyChanged && !lowChanged) return null
    } else {
      const anomalyChanged = isThresholdValueChanged(
        current.anomalyThreshold,
        baseline.anomalyThreshold,
      )
      const lowChanged = isThresholdValueChanged(
        current.lowConfidenceThreshold,
        baseline.lowConfidenceThreshold,
      )
      if (!anomalyChanged && !lowChanged) return null
    }
    // 서버 DECIMAL(5,4)와 호환되도록 저장 직전 4자리로 정규화한다.
    return {
      anomalyThreshold: normalizeThresholdValue(current.anomalyThreshold),
      lowConfidenceThreshold: normalizeThresholdValue(current.lowConfidenceThreshold),
      applyScope: current.applyScope,
    }
  }

  // 임계값 입력은 number input 직접 입력 + preset 버튼 방식이므로
  // 사용자가 잘못된 값(범위 초과 / lowConfidence 이상)을 만들 수 있다.
  // 이 검증은 (1) 사용자 입력 차단 + (2) 서버 응답 비정상 보호 두 역할을 한다.
  function validateThresholdRange(payload: SaveMyThresholdRequest): string | null {
    const { minAllowed, maxAllowed } = thresholdHook.threshold
    const { anomalyThreshold, lowConfidenceThreshold } = payload
    // 1) anomaly/lowConfidence 모두 minAllowed..maxAllowed 범위 검증
    if (anomalyThreshold < minAllowed || anomalyThreshold > maxAllowed) {
      return `이상 임계값은 ${minAllowed} ~ ${maxAllowed} 범위여야 합니다.`
    }
    if (lowConfidenceThreshold < minAllowed || lowConfidenceThreshold > maxAllowed) {
      return `저신뢰 임계값은 ${minAllowed} ~ ${maxAllowed} 범위여야 합니다.`
    }
    // 2) lowConfidenceThreshold >= anomalyThreshold 인 경우 저장 전 차단.
    //    TODO(backend-policy): 백엔드/AI Server 판정 정책과의 최종 정합 확인 필요.
    //    현재는 프론트 1차 검증으로만 사용하고, 서버 422 응답이 더 정확하다고 간주한다.
    if (lowConfidenceThreshold >= anomalyThreshold) {
      return '저신뢰 임계값은 이상 임계값보다 작아야 합니다.'
    }
    return null
  }

  async function handleSave(): Promise<void> {
    if (saving) return
    setValidationError(null)

    const settingsPatch = buildSettingsPatch()
    const thresholdPayload = buildThresholdPayload()

    // edge case: lowConfidence + step > maxAllowed → 저장 가능한 anomaly 값이 없는 상황.
    // threshold를 변경하려는 시도가 있다면 API 호출 전 차단한다.
    if (thresholdPayload && !hasValidThresholdInputRange) {
      const err =
        '저장 가능한 임계값 범위가 없습니다. 저신뢰 기준 또는 허용 범위 설정을 확인해 주세요.'
      setValidationError(err)
      setSaveStatus({ kind: 'error', messages: [err] })
      return
    }

    // 사전 검증 — 통과하지 못하면 서버 호출도, preferences 영속도 하지 않는다.
    if (thresholdPayload) {
      const err = validateThresholdRange(thresholdPayload)
      if (err) {
        setValidationError(err)
        setSaveStatus({ kind: 'error', messages: [err] })
        return
      }
    }

    // 서버/클라 변경 사항이 모두 없으면 noop으로 안내
    const hasServerWork = Boolean(settingsPatch || thresholdPayload)
    const preferencesChanged =
      JSON.stringify(preferencesHook.preferences) !==
      JSON.stringify(lastPersistedPreferencesRef.current)
    if (!hasServerWork && !preferencesChanged) {
      setSaveStatus({ kind: 'noop' })
      return
    }

    // 서버 호출이 없으면 preferences만 즉시 영속 후 종료
    if (!hasServerWork) {
      preferencesHook.persist()
      lastPersistedPreferencesRef.current = preferencesHook.preferences
      setSaveStatus({ kind: 'success' })
      return
    }

    setSaving(true)
    try {
      const tasks: Array<Promise<unknown>> = []
      const taskNames: Array<'settings' | 'threshold'> = []

      if (settingsPatch) {
        tasks.push(settingsHook.saveToServer(settingsPatch))
        taskNames.push('settings')
      }
      if (thresholdPayload) {
        tasks.push(thresholdHook.saveToServer(thresholdPayload))
        taskNames.push('threshold')
      }

      const results = await Promise.allSettled(tasks)
      const failed: Array<'settings' | 'threshold'> = []
      const messages: string[] = []
      results.forEach((r, idx) => {
        if (r.status === 'rejected') {
          failed.push(taskNames[idx])
          const msg =
            r.reason instanceof Error
              ? r.reason.message
              : taskNames[idx] === 'settings'
                ? '설정 저장 실패'
                : '임계값 저장 실패'
          messages.push(`${taskNames[idx] === 'settings' ? '설정' : '임계값'}: ${msg}`)
        }
      })

      // preferences 영속 시점 정책:
      //   - 모두 성공: persist (성공 토스트)
      //   - 서버 호출이 1개뿐이고 그 호출이 실패: 전체 실패로 처리 → persist 보류 (error 토스트)
      //   - 서버 호출이 2개이고 일부만 실패: 부분 실패로 처리 → persist 수행 + 안내 메시지 추가
      //   - 모두 실패(2/2): persist 보류 (error 토스트)
      //
      // 의도: 사용자 관점에서 "저장 자체가 실패"한 케이스(전체 실패)에는
      // 클라 항목(localStorage)도 보존되지 않도록 정합을 맞춘다.
      if (failed.length === 0) {
        lastServerSettingsRef.current = settingsHook.settings
        lastServerThresholdRef.current = thresholdHook.threshold
        if (preferencesChanged) {
          preferencesHook.persist()
          lastPersistedPreferencesRef.current = preferencesHook.preferences
        }
        setSaveStatus({ kind: 'success' })
      } else if (failed.length === results.length) {
        // 전체 실패 — preferences는 영속하지 않는다.
        setSaveStatus({ kind: 'error', messages })
      } else {
        // 부분 실패 — preferences는 영속하되, 안내 문구에 "이 브라우저에 저장된 항목은 보존됨"을 포함.
        if (preferencesChanged) {
          preferencesHook.persist()
          lastPersistedPreferencesRef.current = preferencesHook.preferences
          messages.push(
            '이 브라우저의 화면 환경 설정(테마/주기/알림 일부 등)은 저장되었습니다.',
          )
        }
        setSaveStatus({ kind: 'partial', failed, messages })
      }
    } finally {
      setSaving(false)
    }
  }

  function handleCancel() {
    // 마지막 서버 값/마지막 영속 preferences로 되돌림. 서버 호출은 하지 않는다.
    settingsHook.applyServerResponse(lastServerSettingsRef.current)
    // threshold도 baseline으로 되돌림 — 직접 setLocalAnomaly로 적용
    thresholdHook.setLocalAnomaly(lastServerThresholdRef.current.anomalyThreshold)
    thresholdHook.setLocalLowConfidence(lastServerThresholdRef.current.lowConfidenceThreshold)
    // preferences 되돌리기
    const prev = lastPersistedPreferencesRef.current
    ;(Object.keys(prev) as Array<keyof typeof prev>).forEach((k) => {
      preferencesHook.setLocal(k, prev[k])
    })
    setSaveStatus({ kind: 'idle' })
    setValidationError(null)
  }

  function handleReset() {
    // 모든 항목을 코드 기본값으로 되돌림. 서버 호출은 하지 않는다.
    settingsHook.applyServerResponse(DEFAULT_USER_SETTINGS)
    thresholdHook.setLocalAnomaly(lastServerThresholdRef.current.anomalyThreshold)
    thresholdHook.setLocalLowConfidence(lastServerThresholdRef.current.lowConfidenceThreshold)
    ;(Object.keys(DEFAULT_USER_PREFERENCES) as Array<keyof typeof DEFAULT_USER_PREFERENCES>).forEach(
      (k) => preferencesHook.setLocal(k, DEFAULT_USER_PREFERENCES[k]),
    )
    setSaveStatus({ kind: 'idle' })
    setValidationError(null)
  }

  return {
    loading,
    saving,
    loadError,
    saveStatus,
    validationError,
    thresholdMin,
    thresholdMax,
    effectiveThresholdMin,
    lowConfidenceThreshold,
    hasValidThresholdInputRange,
    thresholdInputError,
    dashboard,
    notifications,
    detection,
    security,
    summary,
    updateDashboard,
    updateNotifications,
    updateDetection,
    updateSecurity,
    handleSave,
    handleCancel,
    handleReset,
  }
}
