import { IconChevronDown } from '../icons/IconChevronDown'

export type SelectOption<T extends string | number = string> = {
  value: T
  label: string
}

type SelectFieldProps<T extends string | number> = {
  id?: string
  value: T
  onChange: (value: T) => void
  options: SelectOption<T>[]
  className?: string
  'aria-label'?: string
  parseValue?: (raw: string) => T
}

export function SelectField<T extends string | number = string>({
  id,
  value,
  onChange,
  options,
  className = '',
  'aria-label': ariaLabel,
  parseValue,
}: SelectFieldProps<T>) {
  return (
    <div className="relative">
      <select
        id={id}
        value={String(value)}
        onChange={(e) => {
          const raw = e.target.value
          onChange(parseValue ? parseValue(raw) : (raw as T))
        }}
        aria-label={ariaLabel}
        className={`w-full appearance-none pr-7 ${className}`}
      >
        {options.map((o) => (
          <option key={String(o.value)} value={String(o.value)}>
            {o.label}
          </option>
        ))}
      </select>
      <span className="absolute right-2.5 top-1/2 -translate-y-1/2 pointer-events-none text-[#c3c7d2]">
        <IconChevronDown />
      </span>
    </div>
  )
}
