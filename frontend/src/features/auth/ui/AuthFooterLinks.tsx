const FOOTER_LINKS = [
  { label: '이용약관', href: '#' },       // TODO: 이용약관 페이지 내용 추가
  { label: '개인정보처리방침', href: '#' }, // TODO: 개인정보처리방침 페이지 내용 추가
  { label: '도움말', href: '#' },          // TODO: 도움말 페이지 내용 추가
]

export function AuthFooterLinks() {
  return (
    <>
      <div className="flex items-center justify-center gap-4 mt-6">
        {FOOTER_LINKS.map((link, idx) => (
          <span key={link.label} className="flex items-center gap-4">
            <a
              href={link.href}
              className="text-[#1f2937] text-[12px] font-medium hover:text-[#1166e0] transition-colors"
              style={{ textShadow: '0 0 8px rgba(255,255,255,0.8)' }}
            >
              {link.label}
            </a>
            {idx < FOOTER_LINKS.length - 1 && (
              <span className="text-[#6c757d] text-[11px]">|</span>
            )}
          </span>
        ))}
      </div>

      <p
        className="text-center text-[#374151] text-[11px] mt-3 font-medium"
        style={{ textShadow: '0 0 8px rgba(255,255,255,0.8)' }}
      >
        © 2026 Industrial AI Platform. All rights reserved.
      </p>
    </>
  )
}
