import { LegalModalShell } from './LegalModalShell'

type PrivacyPolicyModalProps = {
  open: boolean
  onClose: () => void
}

export function PrivacyPolicyModal({ open, onClose }: PrivacyPolicyModalProps) {
  return (
    <LegalModalShell open={open} onClose={onClose} title="개인정보 처리방침">
      <p className="mb-4 text-slate-600">
        본 산업 AI 플랫폼(이하 “서비스”)은 정보주체의 개인정보를 중요시하며, 「개인정보 보호법」을
        준수합니다. 본 처리방침은 회원가입 및 서비스 이용 과정에서 수집·이용되는 개인정보의 항목과
        목적, 보유 기간, 정보주체의 권리에 대해 안내합니다.
      </p>

      <h3 className="mt-5 mb-2 text-sm font-semibold text-slate-900">제1조 (수집하는 개인정보 항목)</h3>
      <ul className="mb-3 list-disc pl-5 text-slate-700">
        <li>필수: 이름, 이메일, 소속 회사, 휴대전화번호, Google 계정 식별자(sub)</li>
        <li>자동 수집: 로그인 일시, 접속 IP, 브라우저 정보, 서비스 이용 기록</li>
        <li>검사·문서 업로드 시: 업로드한 이미지/문서 파일, 검사 결과 데이터, 챗봇 대화 내역</li>
      </ul>

      <h3 className="mt-5 mb-2 text-sm font-semibold text-slate-900">제2조 (개인정보의 수집 및 이용 목적)</h3>
      <ul className="mb-3 list-disc pl-5 text-slate-700">
        <li>회원가입 및 본인 확인, 사용자 인증·권한 관리</li>
        <li>이상탐지 검사 결과 및 재검사 알림 제공</li>
        <li>업로드 문서 인덱싱·검색, 챗봇 답변 생성</li>
        <li>서비스 운영 로그·감사 로그 기록 및 부정 이용 방지</li>
        <li>관리자 승인·반려 및 사용자 상태 관리</li>
      </ul>

      <h3 className="mt-5 mb-2 text-sm font-semibold text-slate-900">제3조 (개인정보의 보유 및 이용 기간)</h3>
      <p className="mb-3 text-slate-700">
        서비스 이용 종료 또는 회원 탈퇴 시 지체 없이 파기합니다. 단, 관계 법령에 따라 보존이 필요한
        경우 해당 기간 동안 보관합니다.
      </p>
      <ul className="mb-3 list-disc pl-5 text-slate-700">
        <li>로그인·접속 기록: 「통신비밀보호법」에 따라 3개월</li>
        <li>운영 로그·감사 로그: 「전자상거래법」 등 관련 법령에 따라 최대 5년</li>
        <li>검사 결과 및 산출물: 회원 탈퇴 시까지 (조직 운영 목적상 필요)</li>
      </ul>

      <h3 className="mt-5 mb-2 text-sm font-semibold text-slate-900">제4조 (개인정보의 제3자 제공)</h3>
      <p className="mb-3 text-slate-700">
        회사는 정보주체의 동의 없이 개인정보를 외부에 제공하지 않습니다. 다만, 법령에 근거가 있거나
        수사기관의 적법한 요청이 있는 경우 예외로 합니다.
      </p>

      <h3 className="mt-5 mb-2 text-sm font-semibold text-slate-900">제5조 (개인정보 처리 위탁)</h3>
      <p className="mb-3 text-slate-700">
        서비스 운영을 위해 다음과 같이 일부 업무를 외부에 위탁할 수 있으며, 위탁 계약 시 안전성 확보
        조치를 적용합니다.
      </p>
      <ul className="mb-3 list-disc pl-5 text-slate-700">
        <li>인증 제공자: Google (OAuth 로그인 처리)</li>
        <li>인프라: 자체 운영 또는 클라우드 사업자 (스토리지·DB·벡터DB 호스팅)</li>
      </ul>

      <h3 className="mt-5 mb-2 text-sm font-semibold text-slate-900">제6조 (정보주체의 권리·의무 및 행사 방법)</h3>
      <p className="mb-3 text-slate-700">
        정보주체는 언제든지 개인정보의 열람·정정·삭제·처리정지 및 동의 철회를 요청할 수 있습니다.
        요청은 마이페이지 또는 관리자 문의를 통해 행사할 수 있습니다.
      </p>

      <h3 className="mt-5 mb-2 text-sm font-semibold text-slate-900">제7조 (안전성 확보 조치)</h3>
      <ul className="mb-3 list-disc pl-5 text-slate-700">
        <li>접근 권한 관리(역할 기반 접근 제어) 및 인증 토큰 보호</li>
        <li>전송 구간 암호화(TLS), 비밀번호·민감 정보의 단방향 저장</li>
        <li>운영 로그·감사 로그를 통한 접근 이력 기록</li>
      </ul>

      <h3 className="mt-5 mb-2 text-sm font-semibold text-slate-900">제8조 (동의 거부의 권리)</h3>
      <p className="mb-5 text-slate-700">
        정보주체는 본 처리방침에 대한 동의를 거부할 권리가 있습니다. 다만, 필수 항목에 대한 동의를
        거부하실 경우 회원가입 및 서비스 이용이 제한될 수 있습니다.
      </p>

      <p className="text-xs text-slate-500">본 처리방침은 서비스 정책 변경에 따라 사전 공지 후 개정될 수 있습니다.</p>
    </LegalModalShell>
  )
}
