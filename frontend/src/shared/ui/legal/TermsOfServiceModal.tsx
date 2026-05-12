import { LegalModalShell } from './LegalModalShell'

type TermsOfServiceModalProps = {
  open: boolean
  onClose: () => void
}

export function TermsOfServiceModal({ open, onClose }: TermsOfServiceModalProps) {
  return (
    <LegalModalShell open={open} onClose={onClose} title="이용약관">
      <p className="mb-4 text-slate-600">
        본 이용약관은 산업 AI 플랫폼(이하 “서비스”)을 이용함에 있어 회사와 회원 간의 권리·의무 및
        책임 사항을 규정합니다. 회원가입 절차를 완료한 사용자는 본 약관에 동의한 것으로 간주됩니다.
      </p>

      <h3 className="mt-5 mb-2 text-sm font-semibold text-slate-900">제1조 (목적)</h3>
      <p className="mb-3 text-slate-700">
        본 약관은 회사가 제공하는 산업 이상탐지·문서 RAG·챗봇 등 일체의 서비스를 회원이 안전하고
        원활하게 이용할 수 있도록 필요한 사항을 정함을 목적으로 합니다.
      </p>

      <h3 className="mt-5 mb-2 text-sm font-semibold text-slate-900">제2조 (용어의 정의)</h3>
      <ul className="mb-3 list-disc pl-5 text-slate-700">
        <li>“회원”이란 본 약관에 동의하고 회사의 승인을 받아 서비스를 이용하는 자를 말합니다.</li>
        <li>“조직(회사)”이란 회원이 소속된 사업자 단위이며, 데이터 접근 범위의 기준이 됩니다.</li>
        <li>“관리자”란 조직 내 회원 승인 및 서비스 운영 권한을 가진 사용자를 말합니다.</li>
      </ul>

      <h3 className="mt-5 mb-2 text-sm font-semibold text-slate-900">제3조 (회원가입 및 승인)</h3>
      <ul className="mb-3 list-disc pl-5 text-slate-700">
        <li>회원가입은 Google 계정 인증 후 필수 정보를 입력하여 신청할 수 있습니다.</li>
        <li>가입 신청은 관리자의 승인 후 서비스 이용이 가능하며, 사유에 따라 반려될 수 있습니다.</li>
        <li>허위 정보 기재, 타인 정보 도용 시 회원가입이 제한되거나 사후 해지될 수 있습니다.</li>
      </ul>

      <h3 className="mt-5 mb-2 text-sm font-semibold text-slate-900">제4조 (서비스의 제공 및 변경)</h3>
      <p className="mb-3 text-slate-700">
        회사는 검사 이미지 업로드, AI 이상탐지, 문서 인덱싱·검색, 챗봇 응답, 알림, 사용자 설정 등
        다양한 기능을 제공합니다. 서비스 내용은 운영 정책 또는 기술적 사유로 변경될 수 있으며,
        주요 변경 사항은 사전 공지합니다.
      </p>

      <h3 className="mt-5 mb-2 text-sm font-semibold text-slate-900">제5조 (회원의 의무)</h3>
      <ul className="mb-3 list-disc pl-5 text-slate-700">
        <li>회원은 본 약관 및 관계 법령, 회사 운영 정책을 준수해야 합니다.</li>
        <li>계정 정보를 타인에게 제공하거나 무단 공유해서는 안 됩니다.</li>
        <li>업로드 자료는 본인 또는 소속 조직이 적법한 권한을 보유한 자료여야 합니다.</li>
        <li>서비스 운영을 방해하거나 시스템 취약점을 악의적으로 이용해서는 안 됩니다.</li>
      </ul>

      <h3 className="mt-5 mb-2 text-sm font-semibold text-slate-900">제6조 (이용 제한 및 해지)</h3>
      <p className="mb-3 text-slate-700">
        다음 각 호에 해당하는 경우 회사는 사전 통지 후 또는 사안에 따라 즉시 서비스 이용을 제한·
        해지할 수 있습니다.
      </p>
      <ul className="mb-3 list-disc pl-5 text-slate-700">
        <li>본 약관 또는 관계 법령을 위반한 경우</li>
        <li>업로드 자료 또는 챗봇 사용이 부정 목적으로 판단되는 경우</li>
        <li>관리자가 회원 자격을 박탈한 경우</li>
      </ul>

      <h3 className="mt-5 mb-2 text-sm font-semibold text-slate-900">제7조 (지적재산권)</h3>
      <p className="mb-3 text-slate-700">
        서비스에 사용되는 모든 디자인·코드·모델·문서의 저작권 및 지적재산권은 회사 또는 정당한
        권리자에게 귀속되며, 회원은 무단으로 복제·배포·변형해서는 안 됩니다.
      </p>

      <h3 className="mt-5 mb-2 text-sm font-semibold text-slate-900">제8조 (책임의 한계)</h3>
      <ul className="mb-3 list-disc pl-5 text-slate-700">
        <li>AI 추론 결과 및 챗봇 답변은 참고 정보로 제공되며, 최종 판단의 책임은 회원에게 있습니다.</li>
        <li>회사는 천재지변, 통신 장애, 제3자 서비스 장애 등 불가항력으로 인한 손해에 대해
          책임지지 않습니다.</li>
      </ul>

      <h3 className="mt-5 mb-2 text-sm font-semibold text-slate-900">제9조 (약관의 개정)</h3>
      <p className="mb-5 text-slate-700">
        회사는 필요 시 본 약관을 개정할 수 있으며, 변경된 약관은 서비스 내 사전 공지를 통해
        효력이 발생합니다.
      </p>

      <p className="text-xs text-slate-500">본 약관은 서비스 정책 변경에 따라 사전 공지 후 개정될 수 있습니다.</p>
    </LegalModalShell>
  )
}
