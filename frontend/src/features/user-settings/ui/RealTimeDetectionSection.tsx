import { FLOAT_EPSILON } from '../../../entities/user-settings'
import { SettingsSectionCard, SettingsSelectField } from './primitives'
import {
  INTERVAL_OPTIONS,
  SENSITIVITY_OPTIONS,
  THRESHOLD_PRESET_OPTIONS,
  type DetectionView,
} from '../types'

export function RealTimeDetectionSection({
  settings,
  onChange,
  thresholdMin,
  thresholdMax,
  effectiveThresholdMin,
  lowConfidenceThreshold,
  hasValidThresholdInputRange,
  thresholdInputError,
}: {
  settings: DetectionView
  onChange: <K extends keyof DetectionView>(key: K, value: DetectionView[K]) => void
  thresholdMin: number
  thresholdMax: number
  effectiveThresholdMin: number
  lowConfidenceThreshold: number
  hasValidThresholdInputRange: boolean
  thresholdInputError: string | null
}) {
  function handleThresholdInput(e: React.ChangeEvent<HTMLInputElement>) {
    const raw = e.target.value
    if (raw === '') {
      onChange('threshold', Number.NaN)
      return
    }
    const n = Number(raw)
    onChange('threshold', n)
  }

  const displayValue = Number.isFinite(settings.threshold)
    ? String(settings.threshold)
    : ''
  const hasError = Boolean(thresholdInputError)

  return (
    <SettingsSectionCard title="실시간 탐지 설정" description="탐지 알고리즘의 동작 방식을 설정합니다.">
      <div className="flex flex-col gap-3">
        <div>
          <label
            htmlFor="detection-threshold"
            className="block text-[#374151] text-[11px] mb-1.5"
          >
            탐지 임계값
          </label>
          <input
            id="detection-threshold"
            type="number"
            inputMode="decimal"
            step={0.01}
            min={effectiveThresholdMin}
            max={thresholdMax}
            value={displayValue}
            onChange={handleThresholdInput}
            className={`w-full px-3 py-2 bg-[#fefefe] border rounded-[3px] text-[#4b5563] text-xs outline-none transition-colors ${
              hasError
                ? 'border-[#e58a85] focus:border-[#cf504a]'
                : 'border-[#f0f1f5] focus:border-[#4a90e2]'
            }`}
          />
          <div className="mt-1 flex items-center justify-between gap-2">
            <div className="flex flex-col">
              {hasValidThresholdInputRange ? (
                <>
                  <span className="text-[#6b7280] text-[10px]">
                    저장 가능 범위: {effectiveThresholdMin.toFixed(2)} ~ {thresholdMax.toFixed(2)}
                  </span>
                  <span className="text-[#9ba3af] text-[10px]">
                    시스템 허용 범위 {thresholdMin.toFixed(2)}~{thresholdMax.toFixed(2)} 중,
                    저신뢰 기준 {lowConfidenceThreshold.toFixed(2)}보다 큰 값만 저장됩니다.
                  </span>
                </>
              ) : (
                <span className="text-[#cf504a] text-[10px]">
                  저장 가능한 임계값 범위가 없습니다 (저신뢰 기준 {lowConfidenceThreshold.toFixed(2)}이
                  허용 최대값 {thresholdMax.toFixed(2)} 이상).
                </span>
              )}
            </div>
            <div className="flex items-center gap-1 shrink-0">
              <span className="text-[#6b7280] text-[10px]">빠른 선택</span>
              {THRESHOLD_PRESET_OPTIONS.map((preset) => {
                const active =
                  Number.isFinite(settings.threshold) &&
                  Math.abs(settings.threshold - preset.numericValue) < FLOAT_EPSILON
                // 부동소수점 산술 오차 보정: a-b > EPSILON 일 때만 진짜 초과로 본다.
                const presetOutOfRange =
                  effectiveThresholdMin - preset.numericValue > FLOAT_EPSILON ||
                  preset.numericValue - thresholdMax > FLOAT_EPSILON
                const disabled = !hasValidThresholdInputRange || presetOutOfRange
                return (
                  <button
                    key={preset.value}
                    type="button"
                    onClick={() => onChange('threshold', preset.numericValue)}
                    disabled={disabled}
                    className={`px-2 py-0.5 rounded-[3px] border text-[10px] transition-colors ${
                      active
                        ? 'bg-[#1166e0] border-[#1166e0] text-white'
                        : 'bg-[#fdfdfd] border-[#e3e1e0] text-[#4b5563] hover:bg-[#f5f6fa]'
                    } disabled:opacity-40 disabled:cursor-not-allowed disabled:hover:bg-[#fdfdfd]`}
                    title={`${preset.label} (${preset.numericValue.toFixed(2)})`}
                  >
                    {preset.label.split(' ')[0]}
                  </button>
                )
              })}
            </div>
          </div>
          {hasError && (
            <div className="mt-1 text-[#cf504a] text-[10px]">{thresholdInputError}</div>
          )}
        </div>

        <SettingsSelectField
          id="detection-sensitivity"
          label="민감도"
          value={settings.sensitivity}
          options={SENSITIVITY_OPTIONS}
          onChange={(v) => onChange('sensitivity', v)}
        />
        <SettingsSelectField
          id="detection-interval"
          label="탐지 주기"
          value={settings.interval}
          options={INTERVAL_OPTIONS}
          onChange={(v) => onChange('interval', v)}
        />
      </div>
    </SettingsSectionCard>
  )
}
