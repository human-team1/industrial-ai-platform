import type { NewUserInfo } from '../../../entities/auth'
import { IconGoogle } from '../../../shared/ui/icons/IconGoogle'

type GoogleAccountCardProps = {
  userInfo: NewUserInfo
}

export function GoogleAccountCard({ userInfo }: GoogleAccountCardProps) {
  return (
    <div className="flex items-center gap-4 px-4 py-3 bg-[#fdfdfd] border border-[#374151] rounded-md mb-6">
      {userInfo.picture ? (
        <img
          src={userInfo.picture}
          alt="프로필"
          className="w-[52px] h-[52px] rounded-full object-cover shrink-0"
        />
      ) : (
        <div className="w-[52px] h-[52px] rounded-full bg-[#e8ecf3] flex items-center justify-center shrink-0">
          <svg
            className="w-6 h-6 text-[#9aa3b2]"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            strokeWidth={1.5}
          >
            <path
              strokeLinecap="round"
              d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2M12 11a4 4 0 100-8 4 4 0 000 8z"
            />
          </svg>
        </div>
      )}
      <div className="min-w-0">
        <div className="text-[#707684] text-[17px] font-medium truncate">{userInfo.name}</div>
        <div className="text-[#c3c7d2] text-[13px] truncate">{userInfo.email}</div>
      </div>
      <div className="ml-auto flex items-center gap-3 shrink-0">
        <div className="h-[34px] w-px bg-[#eef0f4]" />
        <IconGoogle />
        <span className="text-[#828690] text-[13px] whitespace-nowrap">Google 계정 연동 완료</span>
      </div>
    </div>
  )
}
