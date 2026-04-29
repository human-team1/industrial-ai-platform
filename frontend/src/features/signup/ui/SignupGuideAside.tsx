const infoCards = [
  {
    icon: '/icons/auth_icon.png',
    title: '권한 및 접근 제어',
    text: '소속 조직과 역할을 기준으로 서비스 접근 권한이 설정됩니다.',
  },
  {
    icon: '/icons/alarm_icon.png',
    title: '맞춤 알림 제공',
    text: '이상 탐지, 시스템 점검 등 중요한 알림을 정확하게 받아보실 수 있습니다.',
  },
  {
    icon: '/icons/secure_icon.png',
    title: '안전한 정보 관리',
    text: '입력하신 정보는 안전하게 보호되며, 관련 법령에 따라 관리됩니다.',
  },
]

export function SignupGuideAside() {
  return (
    <aside
      aria-label="입력 정보 안내"
      className="w-[329px] shrink-0 bg-[#fbfcfd] border-l border-[#f5f6fa] flex flex-col px-9 pt-[27px] pb-5"
    >
      <div className="flex items-center gap-3 mb-5">
        <img
          src="/icons/info_icon.png"
          alt=""
          aria-hidden="true"
          className="w-[40px] h-[40px] shrink-0"
        />
        <p className="text-[#818695] text-sm leading-[1.6]">
          입력한 정보는 권한 설정과
          <br />
          알림 수신에 사용됩니다.
        </p>
      </div>

      <div className="bg-[#fdfdfd] border-2 border-[#f5f6fa] rounded-[7px] overflow-hidden mb-4 flex-1">
        {infoCards.map((card, idx) => (
          <div
            key={card.title}
            className={`flex items-start gap-3 p-4 ${idx < infoCards.length - 1 ? 'border-b border-[#f5f6fa]' : ''}`}
          >
            <img
              src={card.icon}
              alt=""
              aria-hidden="true"
              className="w-[50px] h-[50px] shrink-0 object-contain"
            />
            <div>
              <div className="text-[#878e9c] text-[13px] mb-1.5">{card.title}</div>
              <p className="text-[#b5bac4] text-xs leading-[1.65]">{card.text}</p>
            </div>
          </div>
        ))}
      </div>

      <div className="flex items-center gap-3 bg-[#eff6fd] border border-[#d0e8fb] rounded p-3">
        <img
          src="/icons/secure_icon2.png"
          alt=""
          aria-hidden="true"
          className="w-[20px] h-[20px] shrink-0"
        />
        <p className="text-[#4b6a8a] text-xs leading-[1.65]">
          모든 정보는 암호화되어 안전하게
          <br />
          저장되며, 외부에 공개되지 않습니다.
        </p>
      </div>
    </aside>
  )
}
