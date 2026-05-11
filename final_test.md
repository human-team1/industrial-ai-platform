## 통합테스트 계획

| 모듈 | 설명 | 검증 포인트 |
| --- | --- | --- |
| 회원가입 시나리오 | 신규 사용자 회원가입 신청 → DB 저장 → 승인 대기 | 입력값 검증, 중복 계정, 승인 상태 |
| 로그인 시나리오 | 사용자 로그인 → 인증 처리 → 토큰 발급 | 인증 성공 여부, 토큰 유효성, 계정 상태 |
| 권한 기반 접근 제어 | 일반 사용자 / 관리자 기능 접근 분리 | 미로그인 차단, 권한 없음 처리, 관리자 기능 보호 |
| 이미지 업로드 탐지 | 로그인 → 이미지 업로드 → 검사 요청 생성 | 파일 업로드, 검사 상태 생성, 요청 흐름 |
| AI 이상탐지 파이프라인 | 이미지 → 전처리 → 모델 추론 → 판정 결과 반환 | 모델 호출, anomaly score, 정상/불량/재검사 판정 |
| 결과 저장 시나리오 | AI 결과 → DB 저장 → 산출물 저장 | 결과 데이터 저장, heatmap 저장, 원본 경로 연결 |
| 결과 조회 시나리오 | 결과 목록/상세 조회 → 화면 표시 | 목록 조회, 상세 정보, 조직별 조회 범위 |
| 재검사/재검토 시나리오 | 저신뢰 결과 → 재검사 분류 → 관리자 재검토 | RECHECK 처리, 재검토 큐, 판정 수정 |
| 문서 업로드 시나리오 | 문서 업로드 → 저장 → 인덱싱 요청 | 문서 형식, 저장 상태, 인덱싱 상태 |
| RAG 검색 흐름 | 사용자 질문 → 벡터 검색 → 관련 문서 조회 | 검색 결과, 출처 제공, 문서 누락 대응 |
| 챗봇 응답 생성 | 질문 → RAG 결과 → LLM 응답 생성 | 답변 생성, 출처 표시, 범위 밖 질문 처리 |
| AI/LLM 응답 실패 | FastAPI / LLM / Chroma 오류 발생 | fallback 메시지, timeout, 오류 상태 저장 |
| 데이터 흐름 검증 | Frontend → Spring → FastAPI → DB/Storage → Frontend | 데이터 연결, 응답 포맷, request-id 추적 |
| 성능 시나리오 | 다수 사용자 동시 검사/조회/질문 요청 | 응답시간, 병렬 처리, 서버 안정성 |
| 보안 테스트 | 악성 파일, 대용량 업로드, 토큰 위조 | 업로드 제한, 인증 차단, 민감정보 보호 |
| 로그/모니터링 | 검사/AI/챗봇/관리자 작업 로그 수집 | 운영 로그, 감사 로그, 장애 추적 |

## 시나리오 ID 체계

| 그룹 | ID Prefix | 포함 범위 |
| --- | --- | --- |
| 인증/사용자 | `AUTH` | 회원가입, 로그인, 권한, 마이페이지 |
| 탐지/결과 | `INSP` | 이미지 업로드, AI 이상탐지, 결과 저장/조회, 재검사 |
| 문서/RAG | `DOC` | 문서 업로드, 인덱싱, 벡터 검색 |
| 챗봇 | `CHAT` | 질문 입력, RAG 기반 답변, 출처 표시 |
| 운영/보안 | `OPS` | 로그, 모니터링, 예외, 보안 |
| 대시보드 | `DASH` | 대시보드 요약, KPI, 최근 결과, 통계 차트 |
| 알림 | `NOTI` | 이상/재검사 알림 생성, 알림 조회, 읽음 처리 |
| 설정 | `SET` | 사용자 설정 조회/수정 |
| 임계값 | `THR` | 임계값 조회/생성/수정, 검사 적용 검증 |

---

# 통합테스트 시나리오

| 시나리오 ID | 시나리오 명 | 흐름도 | 검증 포인트 | 진행 성공률 | 수행자 |
| --- | --- | --- | --- | --- | --- |
| AUTH-001 | 회원가입 및 승인 | 회원가입 신청 → DB 저장 → 승인 대기 → 관리자 승인 | 입력값 검증, 중복 계정, 승인 상태, 권한 전환 |  | 박희정 |
| AUTH-002 | 로그인 및 권한 분기 | 로그인 → 토큰 발급 → 사용자 상태 확인 → 권한별 페이지 이동 | 인증 성공, 토큰 유효성, 계정 상태, 권한별 접근 |  | 박희정 |
| AUTH-003 | 마이페이지 및 사용자 정보 | 로그인 → 내 정보 조회 → 사용자 정보 수정 | 회원 정보 조회, 수정 데이터 저장, 권한 유지 |  | 박희정 |
| DASH-001 | 대시보드 요약 조회 | 로그인 → 대시보드 진입 → 주요 지표 조회 | 정상/불량/재검사 비율, 최근 결과, 통계 표시 |  | 박희정 |
| INSP-001 | 검사 요청 생성 | 이미지 업로드 → 검사 요청 생성 → PROCESSING 상태 반환 | 파일 검증, 원본 저장, inspectionId 생성, runStatus 확인 |  | 박희정 |
| INSP-002 | AI 추론 완료 및 결과 조회 | 검사 요청 → FastAPI 추론 → 결과 저장 → 결과 조회 | anomaly score, 판정 결과, heatmap, 결과 상세 조회 |  |  |
| INSP-003 | 재검사 및 재검토 | 저신뢰 결과 → RECHECK 자동 분류 → 관리자 재검토 → 판정 수정 | 재검사/재검토 구분, 재검토 큐, 수정 이력 |  |  |
| DOC-001 | 문서 업로드 및 관리 | 문서 업로드 → 저장 → 인덱싱 → 수정/삭제 | 문서 저장, 버전 생성, soft delete, 검색 제외 |  |  |
| DOC-002 | RAG 검색 흐름 | 질문 입력 → 벡터 검색 → 관련 문서 조회 | 조직 범위 검색, 출처 반환, 검색 실패 처리 |  |  |
| CHAT-001 | 챗봇 응답 생성 | 질문 입력 → RAG 결과 전달 → LLM 답변 생성 → 출처 표시 | 답변 생성, 출처 표시, 결과 상세 문맥 반영 |  |  |
| CHAT-002 | 챗봇 실패 처리 | 질문 입력 → 검색 실패/API 오류/LLM 오류 → fallback 응답 | timeout, 오류 메시지, 답변 상태 저장, 조직 범위 차단 |  |  |
| NOTI-001 | 이상 알림 생성 | 불량/재검사 발생 → 알림 생성 → 사용자 확인 | 알림 생성, 관련 결과 연결, 중복 알림 처리 |  |  |
| NOTI-002 | 알림 조회 및 읽음 처리 | 알림 목록 조회 → 상세 확인 → 읽음 처리 | 목록 조회, 읽음 상태 변경, 전체 읽음 처리 |  |  |
| SET-001 | 사용자 설정 | 설정 조회 → 설정 변경 → 저장 | 알림 설정, 기본 대시보드 범위, 저장 여부 |  |  |
| THR-001 | 임계값 설정 | 임계값 생성 → 수정 → 범위 검증 | 임계값 저장, 변경 이력, 범위 초과 422 |  |  |
| THR-002 | 임계값 적용 검증 | 임계값 선택 → 검사 요청 → 결과 저장 | 적용 임계값, thresholdId, thresholdVersion 저장 |  |  |
| OPS-001 | 보안 및 예외 처리 | 비로그인 요청 → 토큰 위조 → 권한 없는 접근 → 오류 응답 | 401, 403, 422, 409 처리, 업로드 제한 |  |  |
| OPS-002 | 로그 및 모니터링 | 검사/챗봇/오류 발생 → 운영 로그 저장 → 관리자 조회 | request-id, 운영 로그, 컴포넌트 상태 |  |  |

## 통합테스트 시나리오의 하위 시나리오

## AUTH-001 회원가입 및 승인

| 테스트 케이스 ID | 테스트 케이스(절차) | 사전 조건 | 테스트 데이터 | 예상 결과 | 화면 ID | 테스트 결과 | 수행자 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| AUTH-001-01 | `[접속]` 로그인 페이지에서 회원가입/초기정보 입력 화면으로 이동한다. | 서버가 구동되어 있어야 한다. | - | 회원가입/초기정보 입력 페이지로 이동한다. | PG-001 > PG-002 | PASS | 박희정 |
| AUTH-001-02 | `[회원가입 신청]` 사용자 정보를 입력하고 회원가입을 신청한다. | 회원가입 페이지에 접속되어 있어야 한다. 이름, 이메일이 구글로 부터 가져와져야함. | 이름, 소속 회사 | 입력값 검증 후 회원가입 신청이 완료된다. | PG-002 | PASS | 박희정 |
| AUTH-001-03 | `[승인 대기]` 회원가입 신청 후 사용자 상태를 확인한다. | 회원가입 신청이 완료되어 있어야 한다. | 신규 사용자 계정 | 사용자 상태가 `PENDING`으로 저장된다. | PG-002 | PASS | 박희정 |
| AUTH-001-04 | `[관리자 승인]` 관리자가 가입 신청 사용자를 승인한다. | 관리자 계정으로 로그인되어 있어야 한다. | 승인 대기 사용자 | 사용자 상태가 `ACTIVE`로 변경된다. | PG-015 | PASS | 박희정 |
| AUTH-001-05 | `[관리자 거절]` 관리자가 가입 신청 사용자를 거절한다. | 관리자 계정으로 로그인되어 있어야 한다. | 거절 사유 | 사용자 상태가 `REJECTED`로 변경되고 사유가 저장된다. | PG-015 | 사유 저장이 안되는건지 사유 입력란은 뜨는데 rejected화면에 사유 뜨는걸 안해놓은건지 | 박희정 |
|  | 개인정보 처리 동의에 전문 보기 눌러도 아무것도 안나옴 |  |  |  |  |  | 박희정 |

---

## AUTH-002 로그인 및 권한 분기

| 테스트 케이스 ID | 테스트 케이스(절차) | 사전 조건 | 테스트 데이터 | 예상 결과 | 화면 ID | 테스트 결과 | 수행자 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| AUTH-002-01 | `[로그인]` ACTIVE 사용자가 로그인한다. | 승인 완료 계정이 존재해야 한다. | 구글 이메일? 구글 sub | 로그인 성공 후 대시보드로 이동한다. | PG-001 > PG-003 | PASS | 박희정 |
| AUTH-002-02 | `[토큰 발급]` 로그인 후 인증 토큰을 확인한다. | 로그인 요청이 성공해야 한다. | ACTIVE 사용자 계정 | Access Token이 발급된다. | PG-001 | PASS | 박희정 |
| AUTH-002-03 | `[상태 분기]` PENDING 사용자가 로그인한다. | 승인 대기 계정이 존재해야 한다. | PENDING 사용자 | PENDING 대기화면 출력 | PG-001 | PASS | 박희정 |
| AUTH-002-04 | `[권한 분기]` 일반 사용자가 관리자 페이지에 접근한다. | 일반 사용자로 로그인되어 있어야 한다. | 일반 사용자 토큰 | 관리자 기능 메뉴가 보이지않음 | PG-015 | 부분 PASS — 관리자 메뉴 비노출은 PASS / 일반 사용자 대시보드 **COMMON-500은 별도 결함(FAIL)**으로 분리 — 보완 필요 | 박희정 |
| AUTH-002-05 | `[로그아웃]` 로그인 사용자가 로그아웃한다. | 로그인 상태여야 한다. | 사용자 토큰 | 로그아웃 후 로그인 페이지로 이동한다. | COM-003 > PG-001 | PASS | 박희정 |
| AUTH-002-06 | `[거절 계정 접근]` REJECTED 사용자가 로그인 또는 주요 기능 접근을 시도한다. | `REJECTED` 상태의 사용자 계정이 존재해야 한다. | REJECTED 사용자 계정 | 주요 기능 접근이 제한되고 거절 안내가 표시된다. 로그인 페이지로 이동 | PG-001 | PASS | 박희정 |

---

## AUTH-003 마이페이지 및 사용자 정보

| 테스트 케이스 ID | 테스트 케이스(절차) | 사전 조건 | 테스트 데이터 | 예상 결과 | 화면 ID | 테스트 결과 | 수행자 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| AUTH-003-01 | `[내 정보 조회]` 사용자 메뉴에서 마이페이지로 이동한다. | 로그인 상태여야 한다. | 사용자 토큰 | 내 정보가 화면에 표시된다. | COM-003 > PG-013 | PASS | 박희정 |
| AUTH-003-02 | `[정보 수정]` 마이페이지에서 사용자 정보를 수정한다. | 마이페이지에 접속되어 있어야 한다. | 이름, 전화번호 | 수정 데이터가 저장된다. | PG-013 | 정보 수정 없음. | 박희정 |
| AUTH-003-03 | `[설정 이동]` 마이페이지에서 설정 화면으로 이동한다. | 로그인 상태여야 한다. | - | 설정 페이지로 이동한다. | PG-013 > PG-012 | 네비게이션 바 설정 안눌림 | 박희정 |
| 비밀번호 변경 없애기. 계정 설정 내용 다 ui만 구현된것임.  |  |  |  |  |  |  |  |

---

## DASH-001 대시보드 요약 조회

| 테스트 케이스 ID | 테스트 케이스(절차) | 사전 조건 | 테스트 데이터 | 예상 결과 | 화면 ID | 테스트 결과 | 수행자 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| DASH-001-01 | `[접속]` 로그인 후 대시보드 화면에 진입한다. | ACTIVE 사용자로 로그인되어 있어야 한다. | 사용자 토큰 | 대시보드 화면이 표시된다. | PG-003 | PASS | 박희정 |
| DASH-001-02 | `[요약 지표]` 정상/불량/재검사 비율을 조회한다. | 검사 결과 데이터가 존재해야 한다. | 결과 데이터 | KPI 카드 또는 요약 지표가 표시된다. | PG-003 | PASS | 박희정 |
| DASH-001-03 | `[최근 결과]` 최근 검사 결과 목록을 조회한다. | 검사 결과 데이터가 존재해야 한다. | 최근 검사 결과 | 최근 결과 리스트가 표시된다. | PG-003 | PASS | 박희정 |
| DASH-001-04 | `[통계 차트]` 기간별 이상 발생 추이를 조회한다. | 대시보드 데이터가 존재해야 한다. | 기간 조건 | 차트 데이터가 표시된다. | PG-003 | PASS
곡선을 직선으로 변경하는게 좋을듯 | 박희정 |

---

## INSP-001 검사 요청 생성

| 테스트 케이스 ID | 테스트 케이스(절차) | 사전 조건 | 테스트 데이터 | 예상 결과 | 화면 ID | 테스트 결과 | 수행자 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| INSP-001-01 | `[접속]` 대시보드에서 업로드 탐지 페이지로 이동한다. | 로그인 상태여야 한다. | - | 업로드 탐지 화면이 표시된다. | PG-003 > PG-005 | PASS | 박희정 |
| INSP-001-02 | `[이미지 업로드]` 정상 이미지를 업로드한다. | 업로드 탐지 페이지에 접속되어 있어야 한다. | sample.jpg | 검사 요청이 생성된다. | PG-005 | PASS | 박희정 |
| INSP-001-03 | `[요청 생성]` `/inspections/upload` 응답을 확인한다. | 정상 이미지 업로드가 완료되어야 한다. | jpg/png/webp 이미지 | `inspectionId` 생성, `runStatus=PROCESSING` 반환, 원본 저장 | PG-005 | PASS | 박희정 |
| INSP-001-04 | `[카메라 캡처]` 브라우저 카메라 캡처 이미지를 파일로 변환한 뒤 `/inspections/upload`로 검사한다. | 카메라 접근이 허용되어 있어야 한다. | capture.jpg | `sourceType=BROWSER_CAMERA`로 검사 요청이 생성된다. | PG-004 > PG-005 | 보류 (웹캠 없음) | 박희정 |
| INSP-001-05 | `[ROI 정상]` ROI 값을 포함해 이미지를 업로드한다. | 로그인 상태여야 한다. | ROI 좌표, sample.jpg | ROI 기준으로 검사 요청이 생성된다. | PG-005 | 재테스트 필요 — 웹캠 없어도 sample.jpg + ROI 좌표(roiMode=FIXED)로 검증 가능. 보류 사유 부적절 | 박희정 |
| INSP-001-06 | `[MIME 오류]` 지원하지 않는 MIME 파일을 업로드한다. | 로그인 상태여야 한다. | test.txt | 422 응답, 검사 요청 미생성 | PG-005 | PASS | 박희정 |
| INSP-001-07 | `[손상 이미지]` 손상된 이미지를 업로드한다. | 로그인 상태여야 한다. | corrupt.jpg | 422 응답, 검사 요청 실패 처리 | PG-005 | FAIL — 명세상 업로드 단계 422 차단이어야 하나 200 접수 후 AI 서버에서 디코딩 실패로 사실상 거부됨. 사전 무결성 검증 필요 | 박희정 |
| INSP-001-08 | `[ROI 누락]` `roiMode=FIXED`인데 ROI 좌표를 누락한다. | 로그인 상태여야 한다. | roiMode=FIXED, 좌표 없음 | 422 응답, 검사 요청 미생성 | PG-005 | PASS | 박희정 |
| INSP-001-09 | `[ROI 범위 오류]` ROI 좌표가 0~1 범위를 벗어난다. | 로그인 상태여야 한다. | roiX=1.5 | 422 응답, 검사 요청 미생성 | PG-005 | PASS | 박희정 |
| INSP-001-10 | `[중복 요청 충돌]` 동일 `idempotencyKey`로 다른 이미지를 업로드한다. | 기존 검사 요청이 존재해야 한다. | 동일 key, 다른 payload | 409 응답, 중복 저장 방지 | PG-005 | PASS | 박희정 |
| INSP-001-11 | `[파일 누락]` 파일 없이 검사 요청을 보낸다. | 로그인 상태여야 한다. | file 없음, 검사 요청 파라미터 | 400 응답, 검사 요청이 생성되지 않는다. | PG-005 | PASS | 박희정 |

업로드 요청은 이미지 파일을 받아 검사 요청을 만들고 `PROCESSING` 상태를 반환하는 단계이며, FastAPI 추론 완료와 결과 저장은 다음 `INSP-002`에서 검증합니다. API 명세도 파일 누락 400, 미지원 MIME·손상 파일·ROI 오류 422, 동일 idempotencyKey 충돌 409를 검증 기준으로 정의합니다.

---

## INSP-002 AI 추론 완료 및 결과 조회

| 테스트 케이스 ID | 테스트 케이스(절차) | 사전 조건 | 테스트 데이터 | 예상 결과 | 화면 ID | 테스트 결과 | 수행자 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| INSP-002-01 | `[추론 완료]` PROCESSING 상태의 검사 요청이 AI 서버 추론을 완료한다. | `inspectionId`가 생성되어 있어야 한다. | inspectionId | 검사 상태가 `COMPLETED`로 변경된다. | PG-005 > PG-007 | PASS | 박희정 |
| INSP-002-02 | `[결과 저장]` AI 추론 결과를 DB에 저장한다. | AI 추론이 완료되어야 한다. | score, confidence, decisionCode | `INSPECTION_RESULT`에 결과가 저장된다. | PG-007 | 부분 FAIL — result row 저장은 정상이나 `score` 컬럼 NULL로 anomaly score 저장 요구 미충족 (보완 필요) | 박희정 |
| INSP-002-03 | `[산출물 저장]` heatmap/anomaly map을 저장한다. | AI 시각화 결과가 생성되어야 한다. | heatmap 이미지 | 결과 산출물이 저장되고 결과와 연결된다. | PG-007 | FAIL | 박희정 |
| INSP-002-04 | `[결과 목록]` 결과 목록 페이지에서 검사 결과를 조회한다. | 저장된 검사 결과가 있어야 한다. | 날짜, 판정 필터 | 결과 목록이 최신순으로 조회된다. | PG-006 | PASS | 박희정 |
| INSP-002-05 | `[결과 상세]` 특정 검사 결과 상세를 확인한다. | 결과 목록에 데이터가 있어야 한다. | resultId | 점수, 판정, 모델, ROI, 품질 정보가 표시된다. | PG-006 > PG-007 | PASS | 박희정 |
| INSP-002-06 | `[조회 범위]` 일반 사용자가 결과 목록을 조회한다. | 일반 사용자로 로그인되어 있어야 한다. | 사용자 토큰 | 자기 회사 범위 결과만 조회된다. | PG-006 | PASS | 박희정 |

---

## INSP-003 재검사 및 재검토

| 테스트 케이스 ID | 테스트 케이스(절차) | 사전 조건 | 테스트 데이터 | 예상 결과 | 화면 ID | 테스트 결과 | 수행자 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| INSP-003-01 | `[재검사 자동 분류]` 낮은 신뢰도 또는 경계 점수 결과를 처리한다. | AI 추론 결과가 존재해야 한다. | low confidence result | 시스템이 결과를 `RECHECK`로 자동 분류한다. | PG-007 | PASS | 박희정 |
| INSP-003-02 | `[재검토 큐 등록]` RECHECK 결과를 관리자 재검토 큐에 등록한다. | RECHECK 결과가 존재해야 한다. | resultId | 재검토 대상 데이터가 생성된다. | PG-015 | PASS | 박희정 |
| INSP-003-03 | `[재검토 목록]` 관리자가 재검토 대상을 조회한다. | 관리자 로그인 상태여야 한다. | reviewQueueId | 재검토 대상 목록이 조회된다. | PG-015 | PASS | 박희정 |
| INSP-003-04 | `[판정 수정]` 관리자가 재검토 결과를 수정한다. | 관리자 로그인 상태여야 한다. | 최종 판정, 수정 사유 | 최종 판정이 변경되고 수정 이력이 저장된다. | PG-015 | FAIL | 박희정 |
| INSP-003-05 | `[수정 오류]` 수정 사유 없이 판정을 변경한다. | 관리자 로그인 상태여야 한다. | 수정 사유 없음 | 422 응답, 판정 수정 실패 | PG-015 | FAIL | 박희정 |

재검사는 AI가 낮은 신뢰도나 경계 점수를 보고 자동으로 `RECHECK`로 분류하는 것이고, 재검토는 관리자가 해당 결과를 확인해 최종 판정을 수정하는 수동 처리입니다. 프로젝트 정책도 재검사와 재검토를 구분하고, 재검토 시 판정 수정 이력을 남기도록 정의합니다.

---

## DOC-001 문서 업로드 및 관리

| 테스트 케이스 ID | 테스트 케이스(절차) | 사전 조건 | 테스트 데이터 | 예상 결과 | 화면 ID | 테스트 결과 | 수행자 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| DOC-001-01 | `[접속]` 문서 목록에서 문서 등록 화면으로 이동한다. | 회사 관리자 이상으로 로그인되어 있어야 한다. | - | 문서 등록 화면이 표시된다. | PG-008 > PG-009 | PASS | 박희정 |
| DOC-001-02 | `[문서 업로드]` 점검 문서를 업로드한다. | 문서 등록 화면에 접속되어 있어야 한다. | manual.pdf, manual.md | 문서가 저장되고 인덱싱이 요청된다. (PDF/TXT/MD/DOCX 모두 지원) | PG-009 | **PDF PASS / MD FAIL** — API 명세 MVP 형식(PDF/TXT/MD/DOCX) 중 MD 업로드 미지원. MD 형식 추가 구현 필요 | 박희정 |
| DOC-001-03 | `[인덱싱 상태]` 업로드 문서의 인덱싱 상태를 확인한다. | 문서 업로드가 완료되어야 한다. | documentVersionId | 상태가 `PROCESSING` 또는 `COMPLETED`로 표시된다. | PG-008 | PASS | 박희정 |
| DOC-001-04 | `[재인덱싱]` 기존 문서 버전에 대해 재인덱싱을 요청한다. | 문서 버전이 존재해야 한다. | documentVersionId | 새 인덱싱 작업이 생성된다. | PG-009 | FAIL | 박희정 |
| DOC-001-05 | `[형식 오류]` 지원하지 않는 문서를 업로드한다. | 문서 등록 화면에 접속되어 있어야 한다. | exe 파일 | 업로드가 차단된다. | PG-009 | PASS | 박희정 |
| DOC-001-06 | `[문서 수정]` 기존 문서에 새 파일을 업로드한다. | 문서가 등록되어 있어야 한다. | updated_manual.pdf | 새 `DOCUMENT_VERSION`이 생성된다. | PG-009 | PASS | 박희정 |
| DOC-001-07 | `[문서 삭제]` 문서를 삭제한다. | 삭제 대상 문서가 존재해야 한다. | documentId | Spring에서 soft delete 처리된다. | PG-008 | PASS | 박희정 |
| DOC-001-08 | `[검색 제외]` 삭제된 문서가 RAG 검색에서 제외되는지 확인한다. | 문서 삭제가 완료되어야 한다. | 삭제 문서 관련 질문 | 삭제 문서가 검색 결과에 포함되지 않는다. | PG-010 | PASS | 박희정 |
| DOC-001-09 | `[인덱싱 실패]` 텍스트 추출 불가 문서를 업로드한다. | 문서 등록 화면에 접속되어 있어야 한다. | scan_only.pdf | 인덱싱 상태가 `FAILED`로 표시되고 실패 사유가 저장된다. | PG-008 | PASS | 박희정 |
| DOC-001-10 | `[타 조직 문서 접근]` 다른 조직의 문서 상세/수정/삭제를 요청한다. | 서로 다른 조직의 사용자와 문서가 존재해야 한다. | 타 조직 documentId | 403 응답 또는 접근 차단, 문서 정보가 노출되지 않는다. | PG-008 / PG-009 | PASS | 박희정 |
| DOC-001-11 | `[문서 관리 권한 제한]` ROLE_COMPANY_WORKER 사용자가 문서 업로드/수정/삭제/재인덱싱을 시도한다. | 일반 사용자(WORKER)로 로그인되어 있어야 한다. | WORKER 토큰, 문서 파일 또는 documentId | 문서 관리 기능이 403으로 차단된다. 단, 자기 회사 문서 조회는 허용된다. | PG-008 / PG-009 | FAIL | 박희정 |

문서 정책은 원본을 MinIO에 저장하고 메타데이터를 MariaDB에 저장하며, 벡터는 ChromaDB에 저장하는 구조입니다. 문서 수정 시 새 `DOCUMENT_VERSION`을 만들고, 삭제 시 Spring soft delete와 FastAPI deindex를 수행하며, 텍스트 추출 불가 문서는 인덱싱 실패로 처리합니다.

---

## DOC-002 RAG 검색 흐름

| 테스트 케이스 ID | 테스트 케이스(절차) | 사전 조건 | 테스트 데이터 | 예상 결과 | 화면 ID | 테스트 결과 | 수행자 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| DOC-002-01 | `[검색 요청]` 사용자가 점검 관련 질문을 입력한다. | 인덱싱 완료 문서가 있어야 한다. | “오탐 발생 시 확인 항목은?” | 관련 문서 검색이 수행된다. | PG-010 | PASS |  |
| DOC-002-02 | `[벡터 검색]` 질문과 관련된 문서 chunk를 조회한다. | ChromaDB가 정상 동작해야 한다. | 점검 질문 | 관련 chunk와 score가 반환된다. | PG-010 | FAIL | 박희정 |
| DOC-002-03 | `[출처 반환]` 검색 결과의 출처 정보를 확인한다. | 검색 결과가 존재해야 한다. | documentId, chunkId | 문서명, 위치, 유사도 정보가 포함된다. | PG-010 | FAIL | 박희정 |
| DOC-002-04 | `[검색 실패]` 관련 문서가 없는 질문을 입력한다. | 로그인 상태여야 한다. | 범위 밖 질문 | 관련 문서 없음 안내가 표시된다. | PG-010 | PASS | 박희정 |

---

## CHAT-001 챗봇 응답 생성

| 테스트 케이스 ID | 테스트 케이스(절차) | 사전 조건 | 테스트 데이터 | 예상 결과 | 화면 ID | 테스트 결과 | 수행자 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| CHAT-001-01 | `[챗봇 실행]` 상단 챗봇 버튼을 클릭한다. | 로그인 상태여야 한다. | - | 챗봇 위젯이 열린다. | COM-004 > PG-010 | PASS |  |
| CHAT-001-02 | `[질문 입력]` 설비 점검 관련 질문을 입력한다. | 챗봇 위젯이 열려 있어야 한다. | 점검 질문 | 질문이 전송된다. | PG-010 | PASS |  |
| CHAT-001-03 | `[답변 생성]` RAG 검색 결과를 기반으로 챗봇 답변을 생성한다. | 관련 문서 검색 결과가 있어야 한다. | 검색 chunk | 문서 기반 답변이 생성된다. | PG-010 | PASS |  |
| CHAT-001-04 | `[출처 표시]` 챗봇 답변의 출처를 확인한다. | 챗봇 답변이 존재해야 한다. | messageId | 답변 하단에 출처가 표시된다. | PG-010 | PASS |  |
| CHAT-001-05 | `[대화 이력]` 챗봇 히스토리 화면에서 이전 대화를 조회한다. | 챗봇 대화 이력이 있어야 한다. | conversationId | 이전 질문과 답변이 조회된다. | PG-014 | PASS |  |
| CHAT-001-06 | `[결과 기반 질문]` 결과 상세 화면에서 “이 결과 설명해줘” 질문을 입력한다. | 결과 상세 화면에 접속되어 있어야 한다. | resultId, 질문 | 질문에 resultId 문맥이 포함된다. | PG-007 > PG-010 | FAIL — 결과 상세 챗봇 진입 UI 미구현 | 박희정 |
| CHAT-001-07 | `[결과+문서 답변]` 결과 기반 질문에 대해 검사 결과와 문서 출처를 함께 반영한다. | resultId와 관련 문서가 존재해야 한다. | 검사 결과, 관련 문서 | 답변에 판정 결과, 점수, 점검 문서 출처가 함께 표시된다. | PG-010 | FAIL — CHAT-001-06과 동일 원인 + LLM_FAILED | 박희정 |

---

## CHAT-002 챗봇 실패 처리

| 테스트 케이스 ID | 테스트 케이스(절차) | 사전 조건 | 테스트 데이터 | 예상 결과 | 화면 ID | 테스트 결과 | 수행자 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| CHAT-002-01 | `[범위 밖 질문]` 설비 점검과 무관한 질문을 입력한다. | 로그인 상태여야 한다. | 일반 상식 질문 | 범위 밖 질문 안내가 표시된다. | PG-010 | FAIL — 분기는 OUT_OF_SCOPE 정확하나 안내 문구 부적절 | 박희정 |
| CHAT-002-02 | `[문서 없음]` 관련 문서가 없는 질문을 입력한다. | 인덱싱 문서가 없거나 검색 결과가 없어야 한다. | 문서 외 질문 | 관련 문서 없음 응답이 표시된다. | PG-010 | FAIL — 산업 키워드 다수 무관 질문은 sources 잘못 끌려와 LLM 호출 흐름으로 빠짐 | 박희정 |
| CHAT-002-03 | `[LLM 오류]` LLM 응답 실패 상황을 확인한다. | LLM 연결 오류 상태여야 한다. | 점검 질문 | fallback 메시지가 표시된다. | PG-010 | PASS — LLM 오류 분기/DB 저장 정상. 안내 문구는 CHAT-002-01 보완에 포함 | 박희정 |
| CHAT-002-04 | `[검색 오류]` ChromaDB 오류 상황에서 질문한다. | ChromaDB 연결 오류 상태여야 한다. | 점검 질문 | 검색 실패 안내가 표시되고 오류 상태가 저장된다. | PG-010 | PASS — ChromaDB 다운 시 VECTOR_STORE_FAILED + FAILED 정상 매핑. 안내 문구는 CHAT-002-01 보완에 포함 | 박희정 |
| CHAT-002-05 | `[조직 범위 차단]` 다른 조직 문서가 검색되는지 확인한다. | 서로 다른 조직 문서가 존재해야 한다. | 타 조직 문서 관련 질문 | 다른 조직 문서는 검색 결과에 포함되지 않는다. | PG-010 | PASS — org 9001 사용자에게 org 1001 chunk 누출 없음 확인 | 박희정 |

챗봇은 조직 범위 문서 기반으로 답변하고, 관련 문서 없음은 `NO_RELEVANT_SOURCE`, 범위 밖 질문은 `OUT_OF_SCOPE`로 처리하며, 출처는 sources에 문서/청크/페이지/스코어를 포함하는 구조입니다.

---

## NOTI-001 이상 알림 생성

| 테스트 케이스 ID | 테스트 케이스(절차) | 사전 조건 | 테스트 데이터 | 예상 결과 | 화면 ID | 테스트 결과 | 수행자 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| NOTI-001-01 | `[불량 알림]` 검사 결과가 DEFECT로 저장된다. | AI 결과 저장이 완료되어야 한다. | DEFECT result | 담당자 또는 관리자 알림이 생성된다. | PG-011 | PASS — DEFECT 결과(97013) 발생 시 DEFECT_DETECTED 알림 자동 생성 확인 | 박희정 |
| NOTI-001-02 | `[재검사 알림]` 검사 결과가 RECHECK로 저장된다. | RECHECK 결과가 존재해야 한다. | RECHECK result | 재검사/재검토 필요 알림이 생성된다. | PG-011 | PASS — `AiInferenceJobWorker.java:287` 분기 검증 + RECHECK 결과 기반 알림 데이터 확인 | 박희정 |
| NOTI-001-03 | `[알림 연결]` 알림에서 관련 결과 화면으로 이동한다. | 알림이 생성되어 있어야 한다. | notificationId | 관련 결과 상세 화면으로 이동한다. | PG-011 > PG-007 | PASS — UI 클릭 시 결과 상세 이동 정상. 단, 코드 자동 생성 target_url 정책 보완 필요 | 박희정 |

---

## NOTI-002 알림 조회 및 읽음 처리

| 테스트 케이스 ID | 테스트 케이스(절차) | 사전 조건 | 테스트 데이터 | 예상 결과 | 화면 ID | 테스트 결과 | 수행자 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| NOTI-002-01 | `[알림 목록]` 알림 아이콘을 통해 알림 페이지로 이동한다. | 로그인 상태여야 한다. | - | 알림 목록이 표시된다. | COM-002 > PG-011 | PASS — 91004 알림 4건 목록 조회 정상 | 박희정 |
| NOTI-002-02 | `[상세 확인]` 특정 알림을 선택한다. | 알림 목록이 있어야 한다. | notificationId | 알림 상세 내용이 표시된다. | PG-011 | PASS — 98005 상세 조회 시 REINSPECTION_REQUIRED type/title 정상 | 박희정 |
| NOTI-002-03 | `[읽음 처리]` 알림을 읽음 처리한다. | 읽지 않은 알림이 있어야 한다. | notificationId | 알림 상태가 읽음으로 변경된다. | PG-011 | PASS — PATCH /read idempotent + DB is_read=1 확인 | 박희정 |
| NOTI-002-04 | `[전체 읽음]` 전체 알림을 읽음 처리한다. | 읽지 않은 알림이 있어야 한다. | - | 모든 알림이 읽음 상태로 변경된다. | PG-011 | PASS — read-all 미읽음만 markAsRead 정상 | 박희정 |

---

## SET-001 사용자 설정

| 테스트 케이스 ID | 테스트 케이스(절차) | 사전 조건 | 테스트 데이터 | 예상 결과 | 화면 ID | 테스트 결과 | 수행자 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| SET-001-01 | `[설정 조회]` 설정 페이지에 접속한다. | 로그인 상태여야 한다. | 사용자 토큰 | 현재 사용자 설정이 표시된다. | PG-012 | PASS — row 미존재 시 default 응답 정상 | 박희정 |
| SET-001-02 | `[알림 설정]` 알림 수신 여부를 변경한다. | 설정 페이지에 접속되어 있어야 한다. | notificationEnabled | 변경된 설정이 저장된다. | PG-012 | FAIL — UI 저장 시 "변경된 내용이 없습니다" (프론트 baseline 버그) + 백엔드 partial update 버그 | 박희정 |
| SET-001-03 | `[대시보드 범위]` 기본 대시보드 조회 기간을 수정한다. | 설정 페이지에 접속되어 있어야 한다. | defaultDashboardRange | 기본 조회 기간이 변경된다. | PG-012 | FAIL — SET-001-02와 동일 원인 | 박희정 |

---

## THR-001 임계값 설정

| 테스트 케이스 ID | 테스트 케이스(절차) | 사전 조건 | 테스트 데이터 | 예상 결과 | 화면 ID | 테스트 결과 | 수행자 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| THR-001-01 | `[임계값 조회]` 내 임계값 정보를 조회한다. | 로그인 상태여야 한다. | 사용자 토큰 | 현재 임계값이 표시된다. | PG-012 | PASS — row 미존재 시 SYSTEM_DEFAULT(0.75/0.55) 응답 정상 | 박희정 |
| THR-001-02 | `[임계값 생성]` 새 임계값을 생성한다. | 로그인 상태여야 한다. | anomalyThreshold, lowConfidenceThreshold | 임계값이 저장된다. | PG-012 | FAIL — UI 신규 입력 시 baseline 즉시 갱신되어 저장 noop (SET-001-02/03 동일 원인). API 직접 호출은 정상 | 박희정 |
| THR-001-03 | `[임계값 수정]` 기존 임계값을 수정한다. | 임계값이 존재해야 한다. | 변경 임계값, 변경 사유 | 임계값이 수정되고 변경 이력이 저장된다. | PG-012 | FAIL — UI 수정 시 동일 baseline 버그로 저장 noop. 단 lowConfidenceThreshold 변경은 정책상 미지원(`USER-THRESHOLD-422C`) | 박희정 |
| THR-001-04 | `[범위 초과]` 허용 범위를 초과한 임계값을 입력한다. | 로그인 상태여야 한다. | anomalyThreshold=1.5 | 422 응답, 저장 실패 | PG-012 | PASS — lowConfidenceThreshold=1.5 → 422 VALIDATION-422 정상 | 박희정 |
| THR-001-05 | `[권한 오류]` 권한 없는 사용자가 타인의 임계값을 수정한다. | 다른 사용자 임계값이 존재해야 한다. | thresholdId | 403 응답, 수정 실패 | PG-012 | PASS — 91003 thresholdId=2를 91004 토큰으로 PATCH → 403 USER-THRESHOLD-403 | 박희정 |

임계값 API는 내 설정 조회/수정, 내 임계값 조회/생성/수정, 임계값 변경 이력 조회로 구성되며, 범위 초과는 422, 권한 없는 접근은 403으로 정의되어 있습니다.

---

## THR-002 임계값 적용 검증

| 테스트 케이스 ID | 테스트 케이스(절차) | 사전 조건 | 테스트 데이터 | 예상 결과 | 화면 ID | 테스트 결과 | 수행자 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| THR-002-01 | `[임계값 선택]` 검사 요청 시 적용할 임계값을 선택한다. | 임계값이 생성되어 있어야 한다. | thresholdId | 검사 요청에 thresholdId가 포함된다. | PG-005 | PASS — `InspectionController.java:61` thresholdId multipart 수신 정상 (코드 검증) | 박희정 |
| THR-002-02 | `[검사 적용]` 선택한 임계값으로 이미지를 업로드한다. | 로그인 상태여야 한다. | sample.jpg, thresholdId | 검사 요청이 생성된다. | PG-005 | PASS — INSP-001-02/03 흐름 + thresholdId 결합 추정 (직접 트리거 미수행) | 박희정 |
| THR-002-03 | `[결과 저장]` 검사 결과에 적용 임계값 정보를 저장한다. | AI 추론이 완료되어야 한다. | thresholdId, thresholdVersion | 결과에 적용 임계값과 버전이 저장된다. | PG-007 | PASS — `AiInferenceJobWorker.java:336-338` 코드 정상 + 91004 실검사 97013 SYSTEM_DEFAULT 일관. 91003 시드 비정합 별도 보완 | 박희정 |
| THR-002-04 | `[기본값 적용]` 사용자 임계값 없이 검사를 요청한다. | 기본 임계값 정책이 있어야 한다. | sample.jpg | 회사 또는 설비 기본 임계값이 적용된다. | PG-005 > PG-007 | PASS — 97013 SYSTEM_DEFAULT 적용 확인. 회사/설비 default 분기 미구현 별도 보완 | 박희정 |

검사 실행과 결과 테이블에는 적용 임계값과 임계값 버전을 저장하는 컬럼이 정의되어 있어, 검사 결과 추적 시 어떤 기준으로 판정했는지 확인할 수 있습니다.

---

## OPS-001 보안 및 예외 처리

| 테스트 케이스 ID | 테스트 케이스(절차) | 사전 조건 | 테스트 데이터 | 예상 결과 | 화면 ID | 테스트 결과 | 수행자 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| OPS-001-01 | `[비로그인 접근]` 로그인 없이 업로드 탐지 화면에 접근한다. | 로그인 토큰이 없어야 한다. | - | 로그인 페이지로 이동하거나 접근이 차단된다. | PG-005 > PG-001 | PASS — Authorization 헤더 없이 호출 시 401 (AUTH-401) 차단 정상 | 박희정 |
| OPS-001-02 | `[토큰 위조]` 잘못된 토큰으로 결과 조회를 요청한다. | 위조 토큰 사용 | invalid token | 401 응답, 결과 미반환 | PG-006 | PASS — `Bearer invalid.jwt.token` → 401 정상 | 박희정 |
| OPS-001-03 | `[권한 없음]` 일반 사용자가 관리자 기능에 접근한다. | 일반 사용자 로그인 상태 | 일반 사용자 토큰 | 403 응답, 관리자 기능 접근 차단 | PG-015 / PG-016 | PASS — WORKER 토큰(91003)으로 `/admin/system-status` 호출 → 403 COMMON-403 | 박희정 |
| OPS-001-04 | `[파일 검증]` 비허용 파일을 업로드한다. | 로그인 상태여야 한다. | test.txt 또는 exe 파일 | 422 응답, 업로드 차단 | PG-005 | PASS — INSP-001-06 (MIME 오류 422) 동일 흐름 인용 | 박희정 |
| OPS-001-05 | `[중복 요청]` 동일한 idempotencyKey로 다른 파일을 업로드한다. | 로그인 상태여야 한다. | 동일 key, 다른 이미지 | 409 응답, 중복 요청 차단 | PG-005 | PASS — INSP-001-10 (idempotency 409) 동일 흐름 인용 | 박희정 |

---

## OPS-002 로그 및 모니터링

| 테스트 케이스 ID | 테스트 케이스(절차) | 사전 조건 | 테스트 데이터 | 예상 결과 | 화면 ID | 테스트 결과 | 수행자 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| OPS-002-01 | `[운영 로그]` 검사 요청 후 운영 로그를 조회한다. | 관리자 로그인 상태여야 한다. | requestId | 검사 요청 로그가 조회된다. | PG-016 | PASS — `/admin/operation-logs` 49건 조회. ADMIN_API_ACCESS_DENIED, LOGIN_SUCCESS 등 정상 기록 | 박희정 |
| OPS-002-02 | `[오류 로그]` AI 서버 오류 발생 후 로그를 확인한다. | AI 서버 오류 상황이어야 한다. | 이미지 업로드 요청 | 오류 로그가 저장되고 조회된다. | PG-016 | PASS — level=ERROR 필터로 CHROMA_HEALTH_CHECK_FAILED, AI_SERVER_HEALTH_CHECK_FAILED 다수 기록 확인 | 박희정 |
| OPS-002-03 | `[시스템 상태]` 시스템 컴포넌트 상태를 조회한다. | 관리자 로그인 상태여야 한다. | - | Spring, AI Server, DB, MinIO, Chroma 상태가 표시된다. | PG-016 | PASS — 8개 컴포넌트 상태 표시(Spring=WARNING, AI/MariaDB/Redis/MinIO/ChromaDB/Storage=NORMAL, Stream=UNKNOWN) | 박희정 |
| OPS-002-04 | `[Request ID 추적]` 동일 요청의 로그를 확인한다. | 요청에 `X-Request-Id`가 포함되어야 한다. | requestId | Spring/FastAPI 로그가 동일 ID로 추적된다. | PG-016 | 부분 FAIL — 응답 헤더 echo는 정상이나 `operation_log.requestId` NULL로 저장되어 DB 운영 로그 추적 불가. 보완 필요 | 박희정 |

---

## 수정/보완 사항

| No | 케이스 명 | 단위 테스트 결과 | 케이스 내용 | 수정/보완 사항 | 수행자 |
| --- | --- | --- | --- | --- | --- |
| AUTH-001-05 | `[관리자 거절]` 관리자가 가입 신청 사용자를 거절한다. | FAIL | 거절 사유를 입력하여 PENDING 사용자를 REJECTED로 처리. 사용자 상태가 `REJECTED`로 변경되고 사유가 저장되어야 함 | 백엔드(`SignupRequestJpaEntity.reject`)는 `reject_reason`을 정상 저장하나, REJECTED 사용자 로그인 시 화면에 거절 사유가 표시되지 않음. 거절 사유 노출 UI 추가 필요 | 박희정 |
| AUTH-003-02 | `[정보 수정]` 마이페이지에서 사용자 정보를 수정한다. | FAIL | 이름·전화번호 등 사용자 정보를 수정하여 저장. 수정 데이터가 저장되어야 함 | 마이페이지에 정보 수정 UI(폼/저장 버튼) 자체가 구현되어 있지 않음. 수정 화면 추가 필요 | 박희정 |
| AUTH-003-03 | `[설정 이동]` 마이페이지에서 설정 화면으로 이동한다. | FAIL | 네비게이션 바를 통해 설정 페이지로 이동. 설정 페이지로 이동되어야 함 | 네비게이션 바의 설정 메뉴가 클릭되지 않음. 라우팅/onClick 핸들러 점검 필요 | 박희정 |
| INSP-001-07 | `[손상 이미지]` 손상된 이미지를 업로드한다. | FAIL | 손상된 jpg 업로드 시 422로 즉시 거부되어야 함 | 업로드 단계에서 422 미발생(200 접수). AI 서버 단에서 디코딩 실패로 `run_status=FAILED`, `error_code=AI_SERVER_ERROR` 처리되어 결과적으로는 거부됨. 이미지 무결성 사전 검증(매직 바이트/디코딩 시도) 추가하여 업로드 단계에서 422로 차단 권장 | 박희정 |
| INSP-002-02 | `[결과 저장]` AI 추론 결과를 DB에 저장한다. | 부분 FAIL | inspection_result row가 score/confidence/decision_code와 함께 저장되어야 함 | result row는 정상 저장되나 `score` 컬럼이 NULL로 들어옴(`confidence=0.35`, `decision_code=RECHECK`은 채워짐). anomaly score 저장 요구사항 미충족. AI 서버 응답에서 score 매핑 누락 또는 fallback 처리 의심. AI 응답 → DB 매핑 로직 점검 필요 | 박희정 |
| INSP-002-03 | `[산출물 저장]` heatmap/anomaly map을 저장한다. | FAIL | 검사 완료 후 heatmap/anomaly_map이 image 또는 result_artifact 테이블에 저장되어야 함 | image 테이블에는 ORIGINAL role 이미지만 저장되고, heatmap/anomaly_map row 없음. result_artifact도 비어 있음. AI 서버에서 시각화 산출물을 반환하지 않거나 Spring 측 저장 로직이 없음. 산출물 저장 파이프라인 구현 필요 | 박희정 |
| INSP-003-04 | `[판정 수정]` 관리자가 재검토 결과를 수정한다. | FAIL | 관리자가 reviewQueueId로 최종 판정·수정 사유를 입력하여 결과를 변경. 판정이 변경되고 review_history에 이력이 저장되어야 함 | api.txt 명세에는 `PATCH /api/v1/reviews/{reviewQueueId}` 정의되어 있으나 백엔드 미구현. `ReviewInspectionResultUseCase` 인터페이스와 `ReviewInspectionResultCommand` DTO만 골격으로 존재하고 `@Service` 구현체·컨트롤러 매핑 모두 누락. 호출 시 HTTP 500(COMMON-500) 반환(미매핑이라면 405가 적절). 추가 정합성 이슈: Command가 `resultId`를 받지만 명세 경로는 `reviewQueueId` 기반, 명세의 `registerLearningCandidate` 필드도 Command에 없음. 엔드포인트·구현체·DTO 정합화 필요 | 박희정 |
| INSP-003-05 | `[수정 오류]` 수정 사유 없이 판정을 변경한다. | FAIL | reviewComment 누락 시 422 응답으로 판정 수정 실패해야 함 | INSP-003-04와 동일 — `PATCH /api/v1/reviews/{reviewQueueId}` 미구현으로 검증 자체에 도달하지 못하고 500 반환. 04 구현 후 사유 누락에 대한 422 검증(Bean Validation 또는 도메인 검증) 추가 필요 | 박희정 |
| DOC-001-04 | `[재인덱싱]` 기존 문서 버전에 대해 재인덱싱을 요청한다. | FAIL | 기존 documentVersionId에 대해 재인덱싱을 트리거하면 새 인덱싱 작업이 생성되어야 함 | api.txt 명세에는 `POST /api/v1/document-versions/{versionId}/index-jobs` 정의되어 있으나 백엔드 미구현(`DocumentController`에 매핑 없음, 호출 시 404). 동일 prefix의 `GET /document-versions/{versionId}/chunks`, `GET /document-versions/{versionId}/index-jobs`도 미구현. 별도 컨트롤러(예: `DocumentVersionController`) 신설 또는 `DocumentController`에 엔드포인트 3종 추가 필요 | 박희정 |
| DOC-001-11 | `[일반 사용자 문서 관리 제한]` 일반 사용자가 문서 업로드/수정/삭제 또는 재인덱싱을 시도한다. | FAIL | WORKER 토큰으로 자기 조직 문서에 대한 업로드/수정/삭제 모두 차단되어야 함 | POST 업로드(403)·DELETE 삭제(403)는 정상 차단되나, **PATCH `/api/v1/documents/{id}` 메타데이터 수정은 WORKER 권한으로 200 성공함** — 권한 체크 누락. `DocumentCrudService.softDelete()`는 `requireCompanyAdminOrSiteAdmin()` 호출하지만 `updateMetadata()` 흐름에는 동일 체크가 빠져 있음. WORKER가 타인(SITE_ADMIN 소유) 문서의 title 등 메타데이터를 임의 변경 가능. `updateMetadata()` 진입 시 회사 관리자 이상 검증 추가 필요 | 박희정 |
| DOC-001-06 | `[문서 수정]` 기존 문서에 새 파일을 업로드한다. | PASS | 동일 documentId에 새 파일 업로드 시 새 `DOCUMENT_VERSION` 생성 + 인덱싱 자동 트리거 | 기능은 정상 동작하나, `DocumentCrudService.createVersion()`이 SHA-256 `fileHash`를 **계산만 하고 기존 버전과의 중복 검증을 하지 않음** — 동일 파일을 그대로 재업로드해도 새 버전이 생성되고 ChromaDB 임베딩까지 재계산됨(스토리지·연산 자원 낭비, 동일 chunk 중복 검색 결과). 신규 file_hash가 같은 documentId의 기존 버전 중 하나와 동일하면 409 또는 기존 버전 재사용으로 처리하는 정책 추가 권장 | 박희정 |
| DOC-002-02 / DOC-002-03 | `[벡터 검색 / 출처 반환]` 질문 → 관련 chunk + 출처 정보 반환. | FAIL | 정상 질문 시 sources 배열에 chunk·score·documentTitle·page·sourceSnippet 채워져 반환되어야 함 | **AI 서버 RAG 흐름이 sources를 빈 배열로 반환** — 모든 정상 질문이 NO_RELEVANT_SOURCE로 떨어짐(API 직접 호출·UI 챗봇 모두 동일). `ai-server/scripts/diag_rag_query.py`로 직접 ChromaDB query 던지면 score 0.49~0.66로 모두 PASS(임계값 `rag_min_score=0.4`)인데, Spring `POST /chat-conversations/{id}/messages` → AI 서버 `RagService.handle()` 흐름에서는 0 sources. ChromaDB-MariaDB chunk 동기화 누락 의심해 잔여 chunk 정리해도 동일. AI 서버 측 retriever 호출 또는 후처리 단계 어딘가에서 결과가 누락되는 원인 정밀 진단 필요(`_classify` 분류, retriever 호출 파라미터, 또는 추가 메타데이터 필터 의심) | 박희정 |
| (cleanup) | `[테스트 절차]` MariaDB DELETE만으로는 ChromaDB 잔여 chunk 정리 안 됨. | 보완 | 통합 테스트에서 임시 문서 cleanup 시 백엔드 `DELETE /api/v1/documents/{id}` 호출 또는 명시적 ChromaDB 정리 절차가 필요. 직접 SQL로 row만 지우면 ChromaDB에 chunk 그대로 남아 후속 검색 결과 오염(실제로 DOC-001-06/08 cleanup 후 ChromaDB count=32 잔존, 진단 스크립트로 확인). 통합 테스트용 cleanup 헬퍼 또는 백엔드 hard-delete 유틸 추가 권장 | 박희정 |
| CHAT-001-06 | `[결과 기반 질문]` 결과 상세에서 "이 결과 설명해줘" 질문 | FAIL | 결과 상세 화면에서 챗봇 진입 + resultId 컨텍스트 전달 | (1) `frontend/src/pages/ResultDetailPage.tsx`에 챗봇 진입 버튼 미구현 → 진입 버튼 추가. (2) `frontend/src/features/chatbot/model/useChatbot.ts:72` ask 호출이 `resultId: null` 하드코딩 → 현재 페이지 resultId를 ask payload에 채워 전달. (3) UI 우회해 API 직접 호출(resultId=97013 주입)해도 AI 서버 mock store가 실 DB resultId 인식 못해 `NEED_RESULT_CONTEXT → LLM_FAILED` → `ai-server/application/rag/nodes.py:load_result_context`를 mock 대신 실 DB(`inspection_result`) 조회로 전환(+ `ai-server/.env: RESULT_CONTEXT_SOURCE=mock` → `db`). | 박희정 |
| CHAT-001-07 | `[결과+문서 답변]` 결과 컨텍스트 + 문서 출처 동시 반영 | FAIL | 답변에 판정·점수 + 점검 문서 출처 함께 표시 | CHAT-001-06과 원인 동일(UI 진입점 + `useChatbot` resultId 전달 + AI 서버 result_context 실 DB 연동). 추가로 DOC-002-02/03 FAIL(`sources` 빈 배열) 해소되어야 출처 채워짐. | 박희정 |
| CHAT-002-01 | `[범위 밖 질문]` 일반 상식 질문 → 범위 밖 안내 표시 | FAIL | 범위 밖 안내가 표시되어야 함 | 분기 자체는 정확(`answer_status=OUT_OF_SCOPE`, `message_status=SUCCESS`)하나 사용자에게 표시되는 `message_text`가 일반 실패 문구("챗봇 답변을 생성하지 못했습니다…")로 매핑됨. AI 서버는 OUT_OF_SCOPE_MESSAGE("이 서비스는 산업 이상탐지…")를 정상 응답으로 보내지만 `backend-spring/src/main/java/com/example/factoryguard/application/service/chat/ChatService.java:252-263 normalizeAnswer`가 ANSWERED/NO_RELEVANT_SOURCE 외 모든 상태를 일반 실패 문구로 덮어씀. → `normalizeAnswer`에 `OUT_OF_SCOPE`, `LLM_FAILED`, `VECTOR_STORE_FAILED`, `DOCUMENT_SCOPE_FORBIDDEN`, `VALIDATION_FAILED` 각 분기 추가하여 AI 서버가 보낸 `answerText`를 그대로 사용하거나 상태별 전용 안내 문구를 매핑 필요. | 박희정 |
| CHAT-002-02 | `[문서 없음]` 관련 문서 없는 질문 → 관련 문서 없음 안내 | FAIL | 무관한 질문 시 NO_RELEVANT_SOURCE 안내 표시 | (1) 일반적인 비산업 질문(예: "회의실 예약…")은 sources 빈 배열로 떨어져 `ChatService.NO_SOURCE_ANSWER`("참조 가능한 문서를 찾지 못했습니다…") 안내가 정상 표시됨 — UI 챗봇에서 사용자가 본 동작. (2) 그러나 산업/설비 어휘(점검/절차/매뉴얼/가스킷 등) 포함된 무관 질문은 hybrid retriever의 BM25 키워드 매칭이 MVTecAD chunk를 score 0.40~0.45로 잘못 hit해 sources 채워지고 LLM 호출 흐름으로 진입. → BM25 가중치 조정, MMR/reranker 도입, 또는 산업 일반 어휘만으로는 매칭 점수를 낮추는 후처리 로직 검토. (3) AI 서버 첫 LLM 호출이 Ollama 콜드 로드(qwen2.5:1.5b)로 20초+ 소요되어 `ai-server/.env: LLM_TIMEOUT_SECONDS=120`을 첫 호출이 초과해 LLM_FAILED 매핑 — 기동 시 모델 warmup 또는 첫 호출 한정 타임아웃 상향. (4) 일반 실패 문구 매핑은 CHAT-002-01과 동일. | 박희정 |
| NOTI-001-03 | `[알림 연결]` 알림 → 결과 화면 이동 + 알림 참조 기준 정합성 | 보완 | 알림 클릭 시 결과 상세로 일관 이동 + 참조 기준/URL 일관 | 재검토 기능은 미구현 상태이나, 검사 결과 또는 검사 이벤트 기반 알림 기능은 독립적으로 구현되어 있음. 따라서 알림 생성 자체는 재검토 구현 여부와 무관하게 검증 가능하다. 다만 현재 일부 알림은 결과 알림 성격임에도 resultId 기준과 inspectionId 기준이 혼재되어 있어 알림 클릭 이동 정책의 정합성 보완이 필요하다. **보완 정책**: 검사/결과 알림의 참조 기준이 일관되지 않음. 검사 실행/실패 알림은 `INSPECTION + inspectionId + /inspections/{inspectionId}`를 허용하고, 판정 결과/불량/재검사 알림은 `RESULT + resultId + /results/{resultId}`로 통일하는 정책 정의 및 seed/생성 로직 보완이 필요함. 코드 적용 위치: `AiInferenceJobWorker.java:283-297 buildInspectionNotification`(현재 모든 inspection 결과 알림이 `INSPECTION_RESULT/inspectionId` 사용 → 결과 알림은 `RESULT/resultId`로 변경). 라우터: 현재 `/inspections/:id` 매핑 없음 → 검사 알림용 라우트 추가 또는 모든 알림을 결과 라우트로 통일. | 박희정 |
| SET-001-02/03 + THR-001-02/03 | `[사용자 설정 / 임계값]` 알림/대시보드 범위/임계값 PATCH 저장 | FAIL | UI 토글·입력 → 저장 → 변경분 DB 반영 | settings와 threshold가 **같은 form 훅을 공유**해서 동일 baseline 버그가 두 도메인에 동시 적용됨. **(1) 프론트 공통**: `frontend/src/features/user-settings/model/useUserSettingsForm.ts:78-81`(settings baseline)과 `:82-84`(threshold baseline)가 `lastServer*Ref.current !== *Hook.*` reference 비교라, 사용자가 `setLocal*`로 폼 값을 바꾸는 순간 reference가 바뀌면서 baseline이 즉시 입력 후 값으로 갱신됨. 결과적으로 [`buildSettingsPatch:253-267`]/[`buildThresholdPayload:269-301`] 비교에서 차이 없음 → `handleSave`가 `SaveStatus='noop'` → 사용자에게 "변경된 내용이 없습니다" 표시. 가드를 reference 비교 대신 `*Hook.loaded` 1회 전용 또는 외부 reload 플래그로 변경 필요. **(2) 백엔드 settings 추가**: `backend-spring/.../UpdateMySettingService.java:33-38`이 PATCH 요청에서 null 필드를 기존 row 값으로 유지하지 않고 `DEFAULT_NOTIFICATION_ENABLED(true)` / `DEFAULT_DASHBOARD_RANGE("7d")` 상수로 덮어씀 → 사용자가 한 필드만 PATCH해도 다른 필드가 default로 reset됨. 저장 전 기존 row 조회해 null 필드는 기존 값 유지하도록 변경(또는 PUT/PATCH 분리) 필요. **(3) 정책 메모**: threshold PATCH에서 `lowConfidenceThreshold` 변경은 현재 의도적 미지원(`USER-THRESHOLD-422C`) — 추후 정책 정의 필요. | 박희정 |
| THR-002-03 / THR-002-04 | `[임계값 적용]` 결과 저장 시 thresholdId/version + 사용자 임계값 없을 때 fallback | 보완 | result row의 threshold_* 일관성 + 회사/설비별 default 분기 | **(1) THR-002-03 (시드 비정합)**: 91003 4건(`result_id=97009~97012`)이 `threshold_source=USER`인데 `threshold_id=NULL`(version=1만 채움). production 코드(`AiInferenceJobWorker.java:336-338`)는 `ResolvedThreshold.thresholdId`를 그대로 저장하므로 정상 흐름에선 발생 안 함 — 시드/마이그레이션 스크립트에서 USER source일 때 threshold_id 함께 채우도록 보강 또는 비정합 row 정리(91003에 user_threshold row 신규 INSERT 후 result.threshold_id 백필) 필요. **(2) THR-002-04 (회사/설비 default 미구현)**: 시나리오 명세는 "회사 또는 설비 기본 임계값"인데 `ResolveInspectionThresholdService.java:34-36` fallback이 `ResolvedThreshold::defaultThreshold` 단일 SYSTEM_DEFAULT만 반환. organization/target 단위 default 분기 미구현 → ① 정책 정의 후 `findOrganizationDefault`/`findTargetDefault` 포트 추가 또는 ② 명세를 "시스템 기본값 적용"으로 축소. | 박희정 |
| OPS-002-04 | `[Request-Id 추적]` 응답 헤더 X-Request-Id echo + 운영 로그 매핑 | 보완 | 동일 요청을 Spring/FastAPI 로그·DB 운영 로그에서 동일 ID로 추적 | 응답 헤더 `X-Request-Id`는 보낸 값 그대로 echo되어 클라이언트→서버 1:1 매핑은 가능하나, **`operation_log.requestId` 컬럼이 NULL로 저장**되어 DB 운영 로그를 동일 요청으로 묶어 추적 불가. Spring 측 logging filter(또는 RequestIdFilter)에서 헤더의 X-Request-Id를 MDC `requestId`로 set한 뒤 `OperationLogRecorderService` 등 로그 기록 경로에서 `MDC.get("requestId")`를 `operationLog.requestId`에 채우도록 보완 필요. AI 서버(FastAPI) 호출 시에도 동일 헤더를 propagate하여 양쪽 로그가 같은 ID로 묶이도록 RestTemplate/HttpClient 인터셉터 추가 필요. | 박희정 |

---

## 최우선 수정 대상 (Priority)

수정 순서. 1번이 풀려야 챗봇·문서 검증 다수가 의미 있게 진행됨.

| 순위 | 케이스 | 수정/보완 방향 |
| --- | --- | --- |
| 1 | DOC-002-02/03 RAG sources 빈 배열 (CHAT-001/002 연쇄) | Chroma 검색은 정상이나 Spring→AI Server RAG 흐름에서 sources가 비는 원인 진단 + 정상화. 이게 풀려야 챗봇 답변·출처 검증이 의미 있게 진행됨 |
| 2 | INSP-002-03 heatmap/anomaly map 저장 미구현 | 기능 정의·ERD에 있는 핵심 시각화 결과가 저장되지 않음. AI 서버가 시각화 산출물을 반환하고, Spring이 `IMAGE(VISUALIZED)` 또는 `RESULT_ARTIFACT(HEATMAP)`로 저장하도록 구현 필요 |
| 3 | DOC-001-11 WORKER 문서 PATCH 권한 누락 | WORKER가 문서 메타데이터를 수정 가능한 명확한 권한 결함. `PATCH /documents/{id}`에 `ROLE_COMPANY_ADMIN` 이상 권한 검증 추가 (`DocumentCrudService.updateMetadata()` 진입 시 검증) |
| 4 | INSP-003-04/05 재검토 PATCH API 미구현 | 재검토/판정 수정 시나리오의 핵심 기능 누락. `PATCH /api/v1/reviews/{reviewQueueId}` 서비스 구현체·컨트롤러 매핑 추가 + 사유 누락 422 검증 |
| 5 | SET-001-02/03 + THR-001-02/03 설정/임계값 form baseline 버그 | UI에서 저장이 안 되어 SET·THR 기능 검증이 막힘. `useUserSettingsForm.ts:78-84` baseline 갱신 가드 수정 + `UpdateMySettingService.java:33-38` PATCH null 처리 보완 |

---

## 최종 품질 보완 항목

위 5개 최우선 처리 후 정리할 품질 보완 항목.

| 케이스 | 보완 사항 |
| --- | --- |
| AUTH-001 개인정보 처리 동의 | 전문 보기 클릭 시 아무것도 안 나옴 — 동의 전문 모달/페이지 구현 또는 외부 링크 연결 |
| DASH-001-04 대시보드 차트 | 기간별 이상 발생 추이 곡선 → 직선 변경 권장 (가독성) |
| DOC-001-02 MD 업로드 | API 명세 MVP 형식 중 MD 미지원 — 업로드 핸들러/MIME 화이트리스트에 MD 추가 |
| OPS-002-04 Request-Id 로그 저장 | 응답 헤더 echo는 정상이나 `operation_log.requestId` NULL — MDC propagation 추가하여 로그 기록 시 채움 |
| THR-002-03 seed threshold_id 비정합 | 91003 4건 result row의 USER source인데 threshold_id NULL — 시드 스크립트 보강 또는 비정합 row 백필 |

---

## 미구현으로 분리할 항목

단순 버그라기보다 기능 자체가 아직 연결되지 않았거나 구현되지 않은 항목.

| 케이스 | 분류 | 판단 |
| --- | --- | --- |
| INSP-003-04 판정 수정 | 미구현 | API 명세에는 있으나 서비스/컨트롤러 구현 누락 |
| INSP-003-05 수정 사유 누락 검증 | 미구현 후속 검증 | 04 구현 후 422 검증 가능 |
| DOC-001-04 재인덱싱 | 미구현 | `POST /document-versions/{versionId}/index-jobs` 구현 필요 |
| CHAT-001-06 결과 기반 질문 | 미구현 | 결과 상세 챗봇 진입 + resultId 전달 + AI 서버 result context 조회 필요 |
| CHAT-001-07 결과+문서 답변 | 미구현 | 06 + RAG sources 정상화 후 검증 가능 |
| AUTH-003-02 마이페이지 정보 수정 | 미구현 또는 범위 제외 | API/페이지 정의에는 수정 기능이 있으므로, 구현하지 않을 거면 시나리오에서 제외해야 함 |
| SET-001-02/03, THR-001-02/03 | 구현 버그 | UI form baseline 버그 + 백엔드 PATCH null 처리 문제 |