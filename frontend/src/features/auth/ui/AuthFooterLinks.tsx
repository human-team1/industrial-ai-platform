import { useState } from 'react'
import { PrivacyPolicyModal } from '../../../shared/ui/legal/PrivacyPolicyModal'
import { TermsOfServiceModal } from '../../../shared/ui/legal/TermsOfServiceModal'
import { HelpModal } from '../../../shared/ui/legal/HelpModal'

type FooterLinkKey = 'terms' | 'privacy' | 'help'

const FOOTER_LINKS: { key: FooterLinkKey; label: string }[] = [
  { key: 'terms', label: '이용약관' },
  { key: 'privacy', label: '개인정보처리방침' },
  { key: 'help', label: '도움말' },
]

export function AuthFooterLinks() {
  const [openModal, setOpenModal] = useState<FooterLinkKey | null>(null)
  const close = () => setOpenModal(null)

  return (
    <>
      <div className="flex items-center justify-center gap-4 mt-6">
        {FOOTER_LINKS.map((link, idx) => (
          <span key={link.key} className="flex items-center gap-4">
            <button
              type="button"
              onClick={() => setOpenModal(link.key)}
              className="text-[#1f2937] text-[12px] font-medium hover:text-[#1166e0] transition-colors"
              style={{ textShadow: '0 0 8px rgba(255,255,255,0.8)' }}
            >
              {link.label}
            </button>
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

      <TermsOfServiceModal open={openModal === 'terms'} onClose={close} />
      <PrivacyPolicyModal open={openModal === 'privacy'} onClose={close} />
      <HelpModal open={openModal === 'help'} onClose={close} />
    </>
  )
}
