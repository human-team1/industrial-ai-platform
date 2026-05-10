import { useId } from 'react'

type PhoneFieldProps = {
  value: string
  onChange: (value: string) => void
  disabled?: boolean
  error?: string
}

export function PhoneField({ value, onChange, disabled = false, error }: PhoneFieldProps) {
  const phoneId = useId()
  const errorId = `${phoneId}-error`

  return (
    <div>
      <label htmlFor={phoneId} className="block text-[#374151] text-sm mb-1.5">
        휴대전화<span className="text-red-400 ml-0.5">*</span>
      </label>
      <input
        id={phoneId}
        name="phone"
        type="tel"
        inputMode="tel"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder="010-0000-0000"
        required
        disabled={disabled}
        aria-invalid={Boolean(error)}
        aria-describedby={error ? errorId : undefined}
        className="w-full px-3 py-2 bg-[#fdfdfd] border border-[#eef0f4] rounded text-[#1f2937] text-[13px] outline-none focus:border-[#4a90e2] transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
      />
      {error && (
        <p id={errorId} className="text-red-500 text-xs mt-1">
          {error}
        </p>
      )}
    </div>
  )
}
