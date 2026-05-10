type SettingsToggleSwitchProps = {
  id: string
  checked: boolean
  onChange: (v: boolean) => void
}

export function SettingsToggleSwitch({ id, checked, onChange }: SettingsToggleSwitchProps) {
  return (
    <button
      id={id}
      type="button"
      role="switch"
      aria-checked={checked}
      onClick={() => onChange(!checked)}
      className={`relative w-9 h-5 rounded-full transition-colors shrink-0 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#1166e0] ${
        checked ? 'bg-[#1166e0]' : 'bg-[#e2e4ea]'
      }`}
    >
      <span
        className={`absolute top-0.5 w-4 h-4 bg-white rounded-full shadow transition-transform ${
          checked ? 'translate-x-[18px]' : 'translate-x-0.5'
        }`}
      />
    </button>
  )
}
