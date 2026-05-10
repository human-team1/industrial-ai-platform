import { useInactiveAccountNotice } from '../../features/auth/model/useInactiveAccountNotice'
import { AuthFooterLinks, AuthLoginCard, AuthLoginHeader } from '../../features/auth/ui'
import { NoticeBanner } from '../../shared/ui/feedback/NoticeBanner'

export function AuthLoginWidget() {
  const { showInactive, showExpired, dismissInactive, dismissExpired } = useInactiveAccountNotice()

  return (
    <div className="min-h-screen flex flex-col">
      {showInactive && (
        <NoticeBanner
          message="비활성화된 계정입니다. 관리자에게 문의하세요."
          variant="warning"
          autoCloseMs={6000}
          onClose={dismissInactive}
        />
      )}
      {showExpired && (
        <NoticeBanner
          message="로그인 세션이 만료되었습니다. 다시 로그인해 주세요."
          variant="warning"
          autoCloseMs={6000}
          onClose={dismissExpired}
        />
      )}

      <AuthLoginHeader />

      <main
        className="flex-1 flex flex-col items-center justify-center"
        style={{
          backgroundImage: 'url(/icons/login_background.png)',
          backgroundSize: 'cover',
          backgroundPosition: 'center',
          backgroundRepeat: 'no-repeat',
        }}
      >
        <div className="w-full max-w-[420px] flex flex-col px-4">
          <AuthLoginCard />
          <AuthFooterLinks />
        </div>
      </main>
    </div>
  )
}
