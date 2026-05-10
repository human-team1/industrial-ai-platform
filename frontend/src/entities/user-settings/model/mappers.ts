// 임계값 빠른 선택 preset.
// 기존 LOW/MEDIUM/HIGH dropdown은 제거되고, 직접 입력 방식이 되었다.
// preset은 자주 쓰는 값을 한 번에 선택할 수 있게 도와주는 버튼 용도로만 남는다.
export type ThresholdPreset = 'LOW' | 'MEDIUM' | 'HIGH'

export const THRESHOLD_PRESET_VALUE = {
  LOW: 0.6,
  MEDIUM: 0.75,
  HIGH: 0.9,
} as const

export const THRESHOLD_PRESETS: ThresholdPreset[] = ['LOW', 'MEDIUM', 'HIGH']

// 서버 DECIMAL(5,4) 정밀도 비교 허용 오차 (변경 감지용 — 0.0001 단위 차이는 무시)
const EPS = 1e-3

// 부동소수점 산술 오차 보정용 epsilon.
// UI 정밀도(0.01)보다 충분히 작아 산술 오차만 보정하고 의미 있는 차이는 보존한다.
// 사용처: 범위 비교(<= / >=) 시 a-b <= FLOAT_EPSILON 형태로 사용.
export const FLOAT_EPSILON = 1e-9

export function isThresholdValueChanged(a: number, b: number): boolean {
  return Math.abs(a - b) > EPS
}

// UI는 step=0.01로 입력받지만 서버는 DECIMAL(5,4) 정밀도. 저장 직전 4자리로 고정.
export function normalizeThresholdValue(value: number): number {
  if (!Number.isFinite(value)) return value
  return Number(value.toFixed(4))
}
