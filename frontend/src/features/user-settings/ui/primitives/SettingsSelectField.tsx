export type SettingsSelectOption = {
  value: string
  label: string
}

type SettingsSelectFieldProps = {
  id: string
  label: string
  value: string
  options: SettingsSelectOption[]
  onChange: (v: string) => void
}

export function SettingsSelectField({
  id,
  label,
  value,
  options,
  onChange,
}: SettingsSelectFieldProps) {
  return (
    <div>
      <label htmlFor={id} className="block text-[#374151] text-[11px] mb-1.5">
        {label}
      </label>
      <div className="relative">
        <select
          id={id}
          value={value}
          onChange={(e) => onChange(e.target.value)}
          className="w-full appearance-none px-3 py-2 bg-[#fefefe] border border-[#f0f1f5] rounded-[3px] text-[#4b5563] text-xs outline-none focus:border-[#4a90e2] transition-colors pr-7 cursor-pointer"
        >
          {options.map((o) => (
            <option key={o.value} value={o.value}>
              {o.label}
            </option>
          ))}
        </select>
        <svg
          className="absolute right-2.5 top-1/2 -translate-y-1/2 w-2.5 h-1.5 pointer-events-none text-[#c3c7d2]"
          fill="none"
          viewBox="0 0 10 6"
          stroke="currentColor"
          strokeWidth={1.5}
        >
          <path strokeLinecap="round" strokeLinejoin="round" d="M1 1l4 4 4-4" />
        </svg>
      </div>
    </div>
  )
}
