import { LegalModalShell } from './LegalModalShell'

type HelpModalProps = {
  open: boolean
  onClose: () => void
}

export function HelpModal({ open, onClose }: HelpModalProps) {
  return (
    <LegalModalShell open={open} onClose={onClose} title="도움말">
      <p className="mb-4 text-slate-600">
        산업 AI 플랫폼 이용 시 자주 묻는 항목과 기본 사용 가이드를 정리했습니다. 추가 문의는 조직
        관리자에게 요청해 주세요.
      </p>

      <h3 className="mt-5 mb-2 text-sm font-semibold text-slate-900">1. 회원가입 및 로그인</h3>
      <ul className="mb-3 list-disc pl-5 text-slate-700">
        <li>로그인 화면에서 “Google로 로그인”을 통해 인증합니다.</li>
        <li>최초 가입자는 이름·소속 회사·연락처 등을 입력해 가입 신청을 완료합니다.</li>
        <li>관리자의 승인 후 서비스 이용이 가능합니다. 승인 대기 상태는 가입 안내 화면에서 확인할 수 있습니다.</li>
      </ul>

      <h3 className="mt-5 mb-2 text-sm font-semibold text-slate-900">2. 검사 업로드 및 결과 확인</h3>
      <ul className="mb-3 list-disc pl-5 text-slate-700">
        <li>대시보드 또는 검사 메뉴에서 “업로드 검사”로 이동합니다.</li>
        <li>지원 형식: JPG, PNG, WEBP (단일 이미지). 손상되었거나 형식이 일치하지 않는 파일은 업로드가 차단됩니다.</li>
        <li>업로드 후 “PROCESSING” → “COMPLETED” 상태가 되면 결과 상세에서 점수·판정·시각화를 확인할 수 있습니다.</li>
        <li>결과 판정이 DEFECT/RECHECK인 경우 알림을 통해 결과 상세로 바로 이동할 수 있습니다.</li>
      </ul>

      <h3 className="mt-5 mb-2 text-sm font-semibold text-slate-900">3. 문서 업로드 및 RAG 검색</h3>
      <ul className="mb-3 list-disc pl-5 text-slate-700">
        <li>지원 형식: PDF, DOCX, MD, TXT.</li>
        <li>문서 업로드 후 자동으로 인덱싱이 수행되며, 챗봇이 해당 문서를 참고해 답변합니다.</li>
        <li>WORKER 권한 사용자는 자기 조직 문서를 조회만 할 수 있고, 업로드·수정·삭제는 회사 관리자 이상이 가능합니다.</li>
      </ul>

      <h3 className="mt-5 mb-2 text-sm font-semibold text-slate-900">4. 챗봇 사용</h3>
      <ul className="mb-3 list-disc pl-5 text-slate-700">
        <li>상단의 챗봇 버튼 또는 챗봇 메뉴를 통해 질문을 입력합니다.</li>
        <li>답변은 자기 조직 문서를 기반으로 생성되며, 출처가 함께 표시됩니다.</li>
        <li>서비스 범위를 벗어난 질문은 “산업 이상탐지·문서 관련 질문에만 답변” 안내가 표시됩니다.</li>
      </ul>

      <h3 className="mt-5 mb-2 text-sm font-semibold text-slate-900">5. 사용자 설정 및 임계값</h3>
      <ul className="mb-3 list-disc pl-5 text-slate-700">
        <li>사용자 설정 페이지에서 알림 수신 여부, 기본 대시보드 기간을 조정할 수 있습니다.</li>
        <li>임계값은 AI 판정 기준을 사용자별로 조정할 수 있는 항목이며, 변경 시 이력이 저장됩니다.</li>
      </ul>

      <h3 className="mt-5 mb-2 text-sm font-semibold text-slate-900">6. 권한 및 관리자 기능</h3>
      <ul className="mb-3 list-disc pl-5 text-slate-700">
        <li>일반 사용자(WORKER): 검사 업로드, 자기 조직 결과·문서 조회, 챗봇 사용</li>
        <li>회사 관리자(COMPANY_ADMIN): 위 권한 + 문서 등록·수정·삭제, 사용자 승인</li>
        <li>사이트 관리자(SITE_ADMIN): 전체 운영, 가입 승인, 시스템 모니터링</li>
      </ul>

      <h3 className="mt-5 mb-2 text-sm font-semibold text-slate-900">7. 문제 해결</h3>
      <ul className="mb-3 list-disc pl-5 text-slate-700">
        <li>로그인 후 빈 화면이 표시되면 새로고침 또는 재로그인해 보세요.</li>
        <li>업로드가 422 오류로 차단되면 파일 형식·크기·내용 무결성을 확인하세요.</li>
        <li>지속적으로 문제가 발생하면 조직 관리자에게 운영 로그 ID(헤더 또는 모니터링 화면)를 함께 전달해 주세요.</li>
      </ul>

      <p className="mt-2 text-xs text-slate-500">
        본 도움말은 서비스 업데이트에 따라 변경될 수 있습니다.
      </p>
    </LegalModalShell>
  )
}
