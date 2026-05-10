# Workspace Rules

항상 한국어로 답변한다.

이 워크스페이스를 “AI 기반 설비 점검 보조 시스템(비전 이상탐지 + RAG + LLM + 이력/모니터링)” 프로젝트로 간주한다.

## 1. 프로젝트 맥락 (Project Context)
이 프로젝트는 산업 현장의 설비/품목 점검을 보조하는 운영형 시스템이다.
비전 기반 이상/결함 탐지 결과(판정/점수/시각화)와 설비 문서(RAG) 기반 근거를 결합하여,
작업자에게 설명/대응 절차/체크리스트를 제공하고, 검사 이력과 통계를 관리한다.

추가 맥락(웹 서비스):
- 본 시스템은 웹을 통해 다중 사용자가 접근하는 서비스를 기본 전제로 한다.
- 보안(인증/인가, HTTPS, CORS), 응답 속도, 동시성 고려를 포함한다.

핵심 목표:
- 현장 사용 가능한 MVP(안정성/재현성/이력관리 포함)
- AI 결과의 근거 제시(RAG 출처 포함) 및 이해 가능한 설명 제공
- 검사/점검 프로세스의 누적 관리(재검토·피드백 루프 포함)

## 2. 기본 아키텍처 (Default Architecture)
기본 구조를 다음과 같이 유지한다.

- Nginx: API Gateway / Reverse Proxy, 정적 리소스 서빙, 라우팅
- Frontend: 사용자 화면(입력/조회/대시보드/챗봇)
- Spring Boot Backend: 메인 도메인 API, 인증/권한, 이력/통계, 워크플로우 오케스트레이션
- FastAPI Server: AI 처리 전담(비전 추론, RAG 검색, 임베딩/추론, LLM 호출/응답 정리)
- MariaDB: 서비스 메타/이력 데이터(검사 요청, 결과, 사용자, 설정, 재검토 큐 등)
- MinIO: 파일 저장소(원본 이미지/영상, 시각화 산출물, 보고서 파일, 문서 원본 등)
- ChromaDB: 벡터 DB(문서 임베딩/유사도 검색)
- Redis: 캐시/세션/작업 상태(필요 시 큐/레이트리밋 포함)

원칙:
- Frontend는 DB에 직접 접근하지 않는다.
- FastAPI는 “AI 기능” 범위에 집중하고, 비즈니스 규칙/권한/이력 관리는 Spring이 책임진다.
- 저장소 책임을 섞지 않는다(MariaDB=메타, MinIO=파일, ChromaDB=벡터, Redis=캐시/세션/상태).

## 2.0 현재 기술스택 기준
본 프로젝트의 현재 기술스택 기준은 다음과 같다.

- Frontend: React 18, Vite 5, Tailwind CSS, TypeScript
- Backend: Spring Boot 3.x, JDK 17, MariaDB 10.6, Spring Data JPA, Redis
- AI / ML: Python 3.10.6, FastAPI 0.115, Pydantic 2.9, PyTorch, LangChain, LangGraph, Ollama(gemma2)
- Infra: Docker 24.x, Nginx, Ubuntu 22.04(배포 환경), WSL2 기반 로컬 개발 환경

## 2.1 코드베이스 아키텍처 규칙 (Codebase Architecture Rules)
본 프로젝트는 “시스템 아키텍처”와 별개로, 각 레포/서버 내부 코드는 아래 구조 규칙을 따른다.
기능 추가 시 반드시 해당 구조의 책임 경계를 준수한다.

### 2.1.1 Spring Boot Backend (Hexagonal Architecture, 패키지 고정)
- 기본 구조:
  - `common/`: 공통 예외/응답/유틸(중복 구현 금지, 표준 모듈 재사용)
  - `config/`: 보안/웹/CORS/JPA/client(FastAPI 호출 설정 포함) 등 설정
  - `domain/`: 순수 비즈니스 모델/VO/도메인 예외(기술 의존성 금지)
  - `application/`:
    - `port/in`: 외부에서 호출되는 유스케이스 인터페이스
    - `port/out`: DB/AI 등 외부 의존 인터페이스
    - `service`: 유스케이스 구현(업무 흐름 조합)
    - `dto`: 유스케이스 command/result DTO
  - `adapter/`:
    - `in/web`: Controller(요청/응답, 검증) — 비즈니스 로직 금지
    - `out/persistence`: MariaDB + JPA Entity / Spring Data Repository / Adapter
    - `out/fastapi`: FastAPI 호출 client/request/response + port 구현체
- 금지:
  - Controller에서 비즈니스 로직 처리 금지
  - `application/service`에서 DB 클라이언트/외부클라이언트 직접 new/하드코딩 금지(Port/Adapter/DI로 주입)
  - MyBatis Mapper / XML SQL 중심으로 persistence를 설계하지 않는다
  - JPA Entity를 Controller 응답 DTO로 직접 노출하지 않는다
  - FastAPI 호출을 임의로 흩뿌리지 말고 `config/client` + `adapter/out/fastapi` 경로로만 추가한다

### 2.1.2 Frontend (FSD: Feature Sliced Design, 폴더 책임 고정)
- 기본 구조: `app/` > `pages/` > `widgets/` > `features/` > `entities/` > `shared/`
- 원칙:
  - 하위 계층 참조만 허용(상위 계층을 아래에서 참조 금지)
  - API 호출은 `shared/api` 또는 `features/{feature}/api`로 통일(페이지에서 직접 fetch/axios 난립 금지)
  - UI와 로직 사이에는 ViewModel(Hook/Store/State)을 둔다.
    - `features/{feature}/model`: 상태/동작 관리(ViewModel)
    - `features/{feature}/ui`: 표현 컴포넌트 중심

### 2.1.3 FastAPI AI Server (Layered + DI, 폴더 책임 고정)
- 기본 구조: `api/` → `application/` → `domain/` ← `infrastructure/`
- 추가 계층:
  - `container/`: DI 조립(유스케이스 ↔ 구현체 연결). 라우터에서 구현체 생성(new) 금지
  - `config/`: 환경설정(모델 경로, 스토리지/DB/캐시 연결 정보)
- 원칙:
  - `api/`(router)는 입력 검증/응답만 담당, 비즈니스 로직 금지
  - `application/`(usecase)은 처리 흐름 조합 담당, 인프라 구현에 직접 의존 금지
  - `domain/`은 프레임워크/인프라 의존 금지(포트/인터페이스/모델만)
  - `infrastructure/`는 외부기술(MinIO/ChromaDB/Redis/모델 로더/LangChain/LangGraph/Ollama 연동 등) 구현 담당
  - 요청/응답 스키마와 설정 관리는 Pydantic 2.x 기준을 따른다

### 2.1.4 표준 구현 순서 (Standard Implementation Order)
신규 기능 개발은 다음 순서를 기본으로 한다(특별한 사유가 없으면 역순 금지).
1) Domain 정의(모델/VO/규칙)
2) Application Port(in/out) 정의(계약/인터페이스)
3) Application Service/Usecase 구현
4) Adapter In(Controller/Router/UI) 연결
5) Adapter Out(DB/AI Client/Storage) 연결

## 3. 서비스 간 인터페이스 규칙 (Spring Boot ↔ FastAPI)
- Spring Boot는 기본적으로 외부 클라이언트의 단일 진입점이며, FastAPI는 내부 서비스로 호출한다(원칙).
- API 계약은 OpenAPI / 공용 DTO / JSON Schema 등 “사전 정의된 스펙”을 우선한다.
- 요청/응답은 버전/호환성을 고려하며, breaking change는 금지 또는 버전 분리로 처리한다.
- 모든 서비스 호출은 timeout, 예외 처리, 로깅을 포함한다.
- request-id / correlation-id를 헤더로 전파하고, Spring / FastAPI 로그에 모두 포함한다.

### 3.1 응답 포맷(프로젝트 기본)
- 일반 성공 JSON API는 아래 형식을 기본으로 한다.
  `{ "success": true, "data": {}, "message": "..." }`
- 단, 아래 엔드포인트는 예외로 둘 수 있다.
  - 파일 다운로드 응답
  - 스트리밍 응답(SSE, websocket, multipart stream 등)
  - 헬스체크 응답
  - 204 No Content 응답
- 예외 엔드포인트라고 해도 상태 코드, 헤더, 문서화 규칙은 일관되게 유지한다.

### 3.2 에러 응답 포맷 (RFC 9457 Problem Details)
- 실패 응답(에러)은 RFC 9457 Problem Details(JSON) 형식을 기본으로 한다.
- 기본 필드:
  - `type`: 에러 유형 식별 값(URI 권장)
  - `title`: 에러 제목/요약
  - `status`: HTTP 상태 코드
  - `detail`: 구체 설명
  - `instance`: 요청 경로 또는 에러가 발생한 리소스 식별자
- 필요 시 확장 필드:
  - `errorCode`: 서비스 내부 코드(예: `VALIDATION_ERROR`, `AUTH_REQUIRED`)
  - `errors`: 유효성 검증 오류 상세 배열(필드/사유 등)
  - `requestId`: 요청 추적용 ID
- 내부 스택트레이스 / 민감정보 / 구현 상세는 응답에 노출하지 않는다.

### 3.3 공통 예외 처리 (전역 핸들러)
- 예외 처리는 가능한 한 전역 예외 처리 방식으로 일괄 처리한다.
  - Spring: `@ControllerAdvice` 기반 전역 예외 처리
  - FastAPI: global `exception_handler` 기반 처리
- Controller / Router / Service에서 제각각 응답 바디를 만들지 않는다.
- 예상하지 못한 서버 내부 예외는 일반화된 메시지와 `500`으로 응답하고 상세는 서버 로그로만 남긴다.
- 유효성 검증 실패는 전역 예외 처리에서 팀 표준 상태 코드로 일관되게 변환한다.
- 본 프로젝트에서는 아래 기준을 기본으로 한다.
  - JSON 형식 오류, 필수 파라미터 자체 누락, 타입 파싱 실패: `400 Bad Request`
  - 필드 값 검증 실패, 비즈니스 규칙상 처리 불가, 의미상 유효성 실패: `422 Unprocessable Content`
- Spring Bean Validation 기본 동작이 `400`으로 처리되더라도, 팀 표준상 `422`에 해당하는 경우 전역 예외 처리에서 일관되게 변환한다.

### 3.4 HTTP 상태 코드 기준
- HTTP 상태 코드는 의미에 맞게 사용하며 동일 상황은 동일 코드로 일관성을 유지한다.
  - `200 OK`: 조회/수정/처리 성공
  - `201 Created`: 생성 성공
  - `204 No Content`: 성공했으나 본문 불필요
  - `400 Bad Request`: 형식 오류, 필수 파라미터 누락, JSON 파싱 오류, 잘못된 요청 형식
  - `401 Unauthorized`: 인증 필요 또는 인증 실패
  - `403 Forbidden`: 권한 없음
  - `404 Not Found`: 리소스 없음
  - `409 Conflict`: 상태 충돌, 중복 생성 충돌, 멱등성 충돌
  - `422 Unprocessable Content`: 유효성 검증 실패, 허용 범위 초과, 의미상 처리 불가
  - `500 Internal Server Error`: 서버 내부 예기치 못한 오류
  - `502 Bad Gateway` / `504 Gateway Timeout`: 필요 시 Spring ↔ FastAPI 연동 실패 / 지연 상황에 사용 가능하나, 팀 표준에 따라 내부적으로 `500` 계열로 단순화할 수 있다

## 4. 비전 이상/결함 탐지 규칙 (Vision Anomaly Detection)
- 입력: 이미지 / 동영상 / 실시간 스트림(프레임 단위 처리 가능)을 “검사 요청 단위”로 관리한다.
- 출력(최소): 판정(정상 / 불량 / 재검사), anomaly score, 시각화 결과(heatmap / anomaly map / bbox 유사), 메타데이터.
- 신뢰도 낮은 결과 또는 경계 구간 결과는 재검사 대상으로 분류한다.
- 일반 사용자는 허용 범위 내에서 개인 임계값을 설정할 수 있으며, 해당 사용자 설정값은 본인의 신규 판정에 적용할 수 있다.
- 사용자 설정값이 없으면 회사 또는 설비 기준의 기본 임계값을 적용한다.
- 시스템 관리자는 회사 / 설비 단위 기본 임계값 정책과 허용 범위를 관리할 수 있어야 한다.
- 원본 및 산출물은 MinIO에 저장하고, MariaDB에는 참조(URI/키)와 메타데이터를 저장한다.
- 모델 실패 / 입력 오류 시에도 시스템이 중단되지 않도록 실패 응답 포맷과 대체 동작을 정의한다.
- 재검사 결과는 재검토 큐와 연계될 수 있어야 하며, 관리자 재판정 이력은 추적 가능해야 한다.

## 4.1 판정 / 재검토 용어 통일 규칙
- 본 프로젝트에서 아래 용어를 구분하여 사용한다.
- `재검사`:
  - 시스템의 자동 판정 결과 중 경계 구간 또는 저신뢰 상태를 의미한다.
  - 사용자 화면에서 보여주는 판정 용어로 사용한다.
- `재검토`:
  - 시스템 관리자(사이트 운영자)의 수동 검토 행위를 의미한다.
  - 운영 프로세스 또는 관리자 기능 용어로 사용한다.
- `REVIEW_REQUIRED`:
  - 시스템 내부 상태값 또는 저장용 ENUM으로 사용한다.
  - 외부 사용자 노출 시에는 필요에 따라 “재검사” 또는 “재검토 대상”으로 변환할 수 있다.
- `재검토 큐`:
  - 관리자 화면에서 재검토 대상 결과를 모아 보여주는 운영 기능 명칭으로 사용한다.
- `리뷰 큐`:
  - 기존 문서나 레거시 표현에서만 허용하는 보조 용어이며, 신규 문서 / API / 화면 문구에서는 사용을 지양한다.
- UI 문구, API 필드명, DB 상태값, 문서 표현은 위 구분을 기준으로 일관성을 유지한다.

## 5. RAG 문서 검색 규칙 (RAG Document Retrieval)
- 문서(매뉴얼 / 점검 기준 / 장애 대응)는 인덱싱(청킹 / 임베딩 / 메타데이터) 후 ChromaDB에 저장한다.
- 검색 결과는 반드시 “출처(문서명 / 섹션 / 페이지 / 링크 등 가능한 범위)”를 함께 제공한다.
- 문서가 없는 내용은 일반론 / 추정으로 명확히 구분하며, 근거 없이 단정하지 않는다.
- 문서 원본은 MinIO에 저장하고, 문서 메타데이터 / 버전은 MariaDB에서 관리한다.
- 업로드 또는 수정된 문서는 정책 기준 상태값(대기 / 처리중 / 반영완료 / 반영실패)으로 관리한다.

## 6. LLM 챗봇 / 설명 규칙 (LLM Explanation & QA)
- LLM 답변은 (모델 예측 결과 + RAG 근거)를 우선 사용하여 “현장 작업자 관점”으로 간결하게 작성한다.
- 안전 / 리스크 관련 질문에는 보수적으로 대응하고, 불확실 시 상위 보고 / 매뉴얼 우선을 권고한다.
- 프롬프트 인젝션 / 비밀정보 노출을 방지한다(시스템 프롬프트 / 키 / 내부 경로 / 자격증명 출력 금지).
- 답변에는 “근거 / 출처”와 “다음 조치”를 가능하면 포함한다.
- 결과 상세 화면에서 챗봇 질의 시 해당 설비, 이상 유형, 판정 결과 문맥을 자동 반영할 수 있어야 한다.

## 7. 이력 / 모니터링 / 통계 규칙 (History, Monitoring, Analytics)
- 모든 검사 요청 / 결과는 MariaDB에 이력으로 남긴다(요청자, 대상 설비 / 품목, 시간, 판정, 스코어, 파일 참조 등).
- 대시보드 지표는 “DB에서 재현 가능”해야 하며, 계산 로직을 문서화한다.
- 대시보드 / 목록 API는 웹 응답 속도를 고려하여 페이지네이션 / 필터링 / 집계 최적화(필요 시 캐싱)를 적용한다.
- 저신뢰 / 재검사 결과는 별도 큐(재검토 큐)로 관리하고, 관리자 재판정과 사유를 기록한다.
- 검사 결과 상태는 성공 / 실패 / 재검토대상 / 수정완료 등으로 구분하여 표시할 수 있어야 한다.
- 알림 기능은 MVP에서는 단순 경고 / 배지 / 메일 수준으로 두되, 확장은 가능하도록 이벤트 포인트를 분리한다.

## 8. 사용자 / 권한 / 관리 규칙 (Auth & Admin)
- 본 프로젝트의 권한 주체는 `일반 사용자`, `시스템 관리자(사이트 운영자)`, `시스템`으로 구분한다.
- 기본 인증 방식은 이메일 / 비밀번호 로그인 기준으로 정의한다.
- 회원가입은 “신청 → 승인 대기 → 관리자 승인 또는 거절 → 활성화” 흐름으로 처리한다.
- 일반 사용자는 다음 기능에 접근할 수 있다.
  - 검사 수행
  - 결과 조회
  - 챗봇 사용
  - 대시보드 조회
  - 자기 회사 범위 문서 업로드 / 수정 / 삭제 / 조회
  - 허용 범위 내 개인 임계값 설정
  - 자기 알림 및 자기 챗봇 이력 조회
- 시스템 관리자(사이트 운영자)는 다음 기능에 접근할 수 있다.
  - 운영 모니터링
  - 감사 로그 조회
  - 전체 데이터 조회
  - 가입 승인 / 거절
  - 재검토 처리
  - 보고서 조회
  - 회사 관리
  - 임계값 정책 관리(기본값 / 허용 범위 / 운영 기준)
- 시스템은 다음 책임을 가진다.
  - 검사 처리
  - 문서 인덱싱
  - 보고서 생성
  - 저장 / 이력 / 추적 자동 처리
- 승인 대기 상태 계정은 로그인 또는 주요 기능 접근이 제한되어야 한다.
- 권한이 필요한 작업은 Spring Boot에서 최종 검증한다(클라이언트 / AI 서버 단독 판단 금지).
- 관리자 작업은 감사 / 추적을 위해 로그 및 이력으로 남긴다.
- 재검토 처리 관련 용어와 상태값은 `4.1 판정 / 재검토 용어 통일 규칙`을 따른다.

## 9. 배포 / 설정 규칙 (Deployment & Configuration)
- 기본 배포는 Docker 기반을 우선한다(서비스별 컨테이너 분리 권장).
- 실행 환경 기준:
  - 로컬 개발: WSL2 기반 개발 환경
  - 배포 환경: Ubuntu 22.04
  - Reverse Proxy / Gateway: Nginx
- 환경별 값은 `.env` 또는 설정 파일로 분리하고, 코드에 하드코딩하지 않는다.
- 웹 서비스 배포를 기본값으로 하며 환경에 맞게 HTTPS, CORS allowlist, 인증 / 인가(세션 또는 토큰)를 적용한다.
- CORS는 운영에서 `*`를 금지하고, 허용 도메인 allowlist 기반으로 설정한다.
- Nginx 라우팅 규칙과 각 서비스 헬스체크 엔드포인트를 정의한다.
- Redis는 캐시 / 세션 / 작업 상태 관리 용도로 사용한다.
- 로컬 / 개발 / 운영 실행 순서와 필수 환경 변수는 README와 `.env.example`에 반영한다.

## 10. GitHub / 형상관리 규칙 (GitHub / VCS Safety)
- 에이전트는 사용자의 명시적 요청(“푸시해줘”, “PR 만들어줘” 등) 없이 GitHub 원격 저장소에 push / force-push 하지 않는다.
- 기본 동작은 로컬 변경 제안(패치 / 코드 블록 / 커밋 메시지 초안 / PR 설명)까지만 수행한다.
- 원격 반영이 필요하면, 먼저 다음을 사용자에게 확인받는다.
  - 대상 레포 / 브랜치
  - 반영 방식(직접 push vs PR)
  - 포함 범위(변경 파일 목록)
- 비밀값(.env, API 키, DB 비밀번호, 토큰) 또는 민감 정보가 포함된 파일은 커밋 / 푸시 대상에서 제외한다.
- `.env`, credentials, 운영 설정 파일은 예시 템플릿(.env.example 등)로만 공유하는 것을 원칙으로 한다.
- `.gitignore`에 포함(또는 커밋 금지)해야 하는 대표 항목:
  - Env / Secrets: `.env`, `.env.*`, `*.pem`, `*.key`
  - Logs / Cache: `*.log`, `__pycache__/`, `.pytest_cache/`, `.ruff_cache/`, `.mypy_cache/`
  - Python venv: `.venv/`
  - Frontend: `node_modules/`, `dist/`, `.next/`, `build/`
  - Java / Build: `target/`, `.gradle/`, `out/`, `build/`
  - IDE / OS: `.idea/`, `.vscode/`, `.DS_Store`
  - Large files(원칙적으로 Git 커밋 금지): `*.pt`, `*.onnx`, `*.ckpt`, `*.mp4`, `*.avi` (필요 시 LFS/MinIO 사용)
- PR 전 `git status` / `git diff`로 민감정보 포함 여부를 확인한다.

## 11. MVP 우선순위 (MVP First)
MVP에서 우선 구현할 범위:
- 검사 요청 / 입력 업로드(이미지 중심) → AI 추론 호출 → 결과 저장 / 조회
- anomaly score + 판정 + 시각화(최소 1종)
- RAG 문서 인덱싱 / 검색 + 출처 제공
- LLM 기반 결과 요약 / 질의응답(기본)
- 이메일 로그인 + 가입 승인 + 사용자 / 관리자 구분(최소 권한)
- 대시보드 핵심 지표(최소 3종)
- Docker Compose 기반 로컬 / 개발 환경 실행 가이드

명시 요청 없이는 후순위:
- 실시간 스트리밍 고도화(저지연 파이프라인)
- 대규모 멀티 테넌시 / 클라우드 네이티브 운영
- 복잡한 승인 워크플로우 / 결재 시스템
- 고급 APM / 분산 추적 플랫폼 연동

## 12. 팀 협업 및 Git 컨벤션 (Team Collaboration & Git Convention)

### 12.1 End-to-End 기능 책임
- 작업은 기능 단위로 진행한다.
- 기능별 Backlog(Jira 기준)를 기반으로 담당자가 해당 기능을 처음부터 끝까지 책임지고 개발한다.
- 가능한 범위에서 한 담당자가 end-to-end(Frontend UI → API 연동 → Port 생성 → 비즈니스 로직)로 연결해 완료하는 것을 원칙으로 한다.

### 12.2 Jira 작업 관리 기준
- 작업 진행 상황과 담당 업무는 Jira를 기준으로 관리한다.
- Epic: 요구사항 명세 기준의 큰 기능 단위
- Task: Epic을 구현 가능한 작업 단위로 분리한 이슈
- Bug: 기능 오동작 / 예외 / 요구사항과 다른 동작 수정 이슈
- 담당자는 자신이 맡은 Jira 이슈 상태를 직접 업데이트한다.

### 12.3 브랜치 전략
- 브랜치 전략: `main` < `develop` < `feat/`, `fix/`, `docs/`, `refactor/`, `chore/`, `test/`, `hotfix/`, `release/` 구조를 따른다.
- `main` 브랜치에는 직접 push하지 않는다.
- 모든 작업은 `develop`에서 분기한 작업 브랜치에서 진행하고, PR 기반으로 병합한다.

### 12.4 커밋 / PR 규칙
- 커밋 메시지: `type: 작업 내용` 형식을 준수한다. (예: `feat: 이상 탐지 API 추가`)
- 허용 커밋 type은 `feat`, `fix`, `docs`, `refactor`, `test`, `chore`로 제한한다.
- PR 제목: `[type] 작업 내용` 형식을 권장한다.
- PR 본문에는 작업 내용 / 변경 사항 / 테스트(검증) 내용을 포함한다.
- PR은 가능한 한 기능 단위로, 리뷰 가능한 크기로 분리한다.

### 12.5 제출 전 점검
- 불필요한 콘솔 로그는 제출 전 제거한다.
- 디버깅용 코드, 임시 파일, 사용하지 않는 주석은 정리한다.
- 기능 동작 여부와 주요 예외 상황을 최소 1회 이상 점검한다.

## 13. 프로젝트 구조 원칙 (Architecture Guideline: Hexagonal / FSD / Layered+DI)
- 본 프로젝트는 아래 아키텍처를 병행한다.
  - Spring: Hexagonal(Port & Adapter)
  - Frontend: FSD(Feature Sliced Design)
  - FastAPI: Layered + DI(Container)
- 용어 / 경로 매핑(중요):
  - Spring Controller(진입점) = `adapter/in/web`
  - Spring 외부연동(DB/AI) = `adapter/out/persistence`, `adapter/out/fastapi`
  - Spring Port = `application/port/in`, `application/port/out`
  - FastAPI Router = `api/`, Usecase = `application/`, Port / Model = `domain/`, Impl = `infrastructure/`, DI = `container/`
  - FE(FSD) = `app/pages/widgets/features/entities/shared` 계층을 따른다
- 의존성 방향:
  - Spring: Adapter → Application → Domain(내부로만)
  - FastAPI: api / application → domain(내부), infrastructure는 domain 인터페이스 구현
  - FE(FSD): 상위(app / pages / widgets) → 하위(features / entities / shared) 의존만 허용

## 14. 개발 환경 문서화 (README Maintenance)
- 개발 환경 설정 방법은 각 시스템의 README에 작성한다.
- README에는 실행 방법, 필수 설치 항목, 환경 변수, 의존성 설치 방법, 실행 순서를 포함한다.
- 기능 개발로 인해 실행 방식 / 환경 변수 / 의존성 / 실행 순서가 변경되면 README를 함께 수정하여 최신 상태를 유지한다.
- WSL2 로컬 개발 환경과 Ubuntu 22.04 배포 환경의 차이가 실행에 영향을 주는 경우, README에 분리하여 명시한다.
