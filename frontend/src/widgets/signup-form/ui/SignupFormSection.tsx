import type { NewUserInfo } from '../../../entities/auth'
import { usePublicOrganizations } from '../../../entities/organization/model/usePublicOrganizations'
import { useSignupForm } from '../../../features/signup/model/useSignupForm'
import { ConsentField } from '../../../features/signup/ui/ConsentField'
import { GoogleAccountCard } from '../../../features/signup/ui/GoogleAccountCard'
import { OrganizationField } from '../../../features/signup/ui/OrganizationField'
import { PhoneField } from '../../../features/signup/ui/PhoneField'
import { SignupGuideAside } from '../../../features/signup/ui/SignupGuideAside'
import { StepIndicator } from '../../../features/signup/ui/StepIndicator'
import type { SignupRequestResponse } from '../../../features/signup/types'

type SignupFormSectionProps = {
  userInfo: NewUserInfo
  onBack: () => void
  onSubmitted: (result: SignupRequestResponse) => void
}

export function SignupFormSection({ userInfo, onBack, onSubmitted }: SignupFormSectionProps) {
  const { form, loading, error, canSubmit, updateField, submit } = useSignupForm(userInfo)
  const {
    organizations,
    loading: orgLoading,
    error: orgError,
  } = usePublicOrganizations()

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    try {
      const result = await submit()
      onSubmitted(result)
    } catch {
      // 에러는 useSignupForm 내부 error 상태로 표시됨
    }
  }

  const fieldBase =
    'w-full px-3 py-2 bg-[#fdfdfd] border border-[#edeef2] rounded text-[#1f2937] text-[13px] outline-none focus:border-[#4a90e2] transition-colors'

  return (
    <div className="flex flex-1 gap-0 min-h-0">
      <main className="flex-1 flex flex-col px-8 pt-7 pb-0 min-w-0">
        <h1
          id="member-info-form-title"
          className="text-[#494d57] text-[23px] font-medium leading-none mb-1.5"
        >
          회원가입/초기정보 입력
        </h1>
        <p className="text-[#6b7280] text-sm mb-7">
          서비스 이용을 위해 추가 정보를 입력해주세요.
        </p>

        <StepIndicator />

        <GoogleAccountCard userInfo={userInfo} />

        <form
          aria-labelledby="member-info-form-title"
          onSubmit={handleSubmit}
          className="flex flex-col gap-4 flex-1"
        >
          <div>
            <label className="block text-[#374151] text-sm mb-1.5">
              이름<span className="text-red-400 ml-0.5">*</span>
            </label>
            <input
              name="name"
              type="text"
              value={form.name}
              readOnly
              className={`${fieldBase} bg-[#f6f8fa] cursor-default`}
            />
          </div>

          <PhoneField
            value={form.phone}
            onChange={(v) => updateField('phone', v)}
            disabled={loading}
          />

          <OrganizationField
            value={form.organizationId}
            organizations={organizations}
            loading={orgLoading}
            error={orgError}
            onChange={(v) => updateField('organizationId', v)}
            disabled={loading}
          />

          <ConsentField
            value={form.consent}
            onChange={(v) => updateField('consent', v)}
            disabled={loading}
          />

          {error && <p className="text-red-500 text-sm">{error}</p>}

          <div className="h-px bg-[#f7f8fa] my-1" />

          <div className="flex justify-end gap-3 pb-8">
            <button
              type="button"
              onClick={onBack}
              disabled={loading}
              className="px-[46px] py-3 bg-[#fdfdfd] border border-[#dde0e7] rounded-md text-[#4b5563] text-[15px] hover:bg-[#f5f6fa] transition-colors disabled:opacity-50"
            >
              이전
            </button>
            <button
              type="submit"
              disabled={!canSubmit}
              className="px-[46px] py-3 bg-[#1164df] border border-[#5591e7] rounded-md text-[#91b9ee] text-sm hover:bg-[#0d55c4] transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? '처리 중...' : '가입 완료'}
            </button>
          </div>
        </form>
      </main>

      <SignupGuideAside />
    </div>
  )
}