export function AuthLoginHeader() {
  return (
    <header
      aria-label="산업 이상 탐지 시스템 상단 헤더"
      className="h-[58px] bg-[#1e2333] flex items-center justify-between px-6 shrink-0"
    >
      <div className="flex items-center gap-3 text-[#a5b0c4] text-lg font-bold leading-none">
        <img src="/icons/logo_icon.png" alt="" aria-hidden="true" className="w-[60px] h-[60px] shrink-0" />
        산업 이상 탐지 시스템
      </div>
    </header>
  )
}
