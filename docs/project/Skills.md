## Global Rules

```markdown
# Global Rules

항상 한국어로 답변한다.

간결하고 실용적으로 작성한다.

코드를 작성할 때:
- 해당 모듈/서버의 기본 스택을 우선한다.
  - Frontend 영역: React 18 + Vite 5 + TypeScript + Tailwind CSS
  - Spring Boot 영역: Spring Boot 3.x + JDK 17 + MariaDB + Spring Data JPA
  - FastAPI · AI/RAG 영역: Python 3.10.6 + FastAPI 0.115 + Pydantic 2.x
- 해당 레포의 표준 폴더/패키지 구조(Spring Hex / FE FSD / FastAPI Layered+DI)를 우선 준수한다.
- 이해에 도움이 되는 경우에만 주석을 추가한다.
- 기존 코드 스타일과 구조를 맞춘다.

## 1. 코딩 전에 생각하기 (Think Before Coding)
코드를 작성하기 전에:
- 핵심 가정을 명시적으로 적는다.
- 요구사항이 모호할 경우 가능한 해석을 제시한다.
- 모호함이 정합성/정확성에 영향을 주면 먼저 확인 질문을 한다.
- "코딩 전 가설 명시" 단계에서 서비스 간 인터페이스(Spring Boot ↔ FastAPI) 및 계약(OpenAPI/DTO/요청·응답 JSON) 영향도를 먼저 확인한다.
- 해당 작업이 Jira Backlog의 어떤 Task/Bug인지, End-to-End 흐름(UI → API → 로직 → 저장)상 어디인지 먼저 인지한다.

## 2. 단순함 우선 (Simplicity First)
문제를 해결하는 데 필요한 최소한의 코드만 작성한다.
- 불필요한 추상화를 피한다.
- 추측 기반 기능을 추가하지 않는다.
- 사용되지 않는 유연성(확장성)을 미리 넣지 않는다.

## 3. 최소 범위 변경 (Surgical Changes)
필요한 부분만 변경한다.
- 관련 없는 코드는 리팩터링하지 않는다.
- 기존 스타일과 아키텍처를 유지한다.
- 변경으로 인해 명백히 불필요해진 코드만 제거한다.

## 4. 목표 중심 실행 (Goal-Driven Execution)
작고 검증 가능한 단계로 작업한다.
- 구현 전에 성공 기준을 정의한다.
- 의미 있는 단계마다 결과를 검증한다.
- 검증에 실패하면 확장하지 말고 멈춘 뒤 문제를 설명한다.

## 5. 인터페이스 표준화 (Standardized Interface)
서로 다른 스택/서비스 간 연동에서는 데이터 계약을 최우선으로 한다.
- API 요청/응답, DB 스키마/DTO 관련 코드는 사전에 정의된 데이터 모델(예: OpenAPI, 공용 DTO, JSON Schema)을 반드시 준수한다.
- 스키마/계약 변경이 필요하면 “스펙(문서/정의) 먼저 수정 → 영향도 확인 → 코드 반영” 순서로 진행한다.
- 기존 연동에 영향을 주는 breaking change는 금지하며, 불가피한 경우 버전 분리 또는 하위 호환 전략을 함께 제시한다.
- 언어별 네이밍 컨벤션을 준수한다(단, 기존 코드/프레임워크 관례가 우선).
  - Python(FastAPI/AI): `snake_case`(변수/함수), `PascalCase`(클래스)
  - Java/Kotlin(Spring): `camelCase`(변수/메서드), `PascalCase`(클래스)
  - TypeScript/React: 기존 코드베이스 컨벤션 우선
  - 공통 상수: `UPPER_SNAKE_CASE`
  - Boolean: `is/has/can` 등 상태가 드러나는 접두어 사용

## 6. 장애 안전성 & 로깅 (Fail-Safe & Logging)
운영형 시스템에서는 “멈추지 않고 원인 파악이 가능”해야 한다.
- 실패 가능 지점(외부 API, MariaDB, Redis, 파일 I/O, 모델 추론, RAG 검색 등)에는 적절한 예외 처리와 로깅을 포함한다.
- 예외를 무시(삼키기)하지 말고, 원인과 맥락(요청 파라미터 요약, 대상 ID, 처리 단계)을 로그로 남긴다.
- 로그는 가능하면 구조화(필드 기반)하고, 요청 추적을 위한 request-id/correlation-id를 포함한다.
- ML 추론 실패 또는 RAG 검색 실패 시 시스템이 중단되지 않도록 대체 동작(Fallback)을 고려하고, 사용자에게는 실패 사실과 다음 조치를 명확히 안내한다.

## 7. 커밋/PR 품질 기본 원칙 (Commit/PR Quality Baseline)
- 하나의 커밋에는 하나의 작업만 담는 것을 원칙으로 한다(가능하면 하나의 Jira 이슈 범위).
- PR 전 불필요한 콘솔 로그/디버깅 코드/임시 파일/사용하지 않는 주석은 제거한다.
- 민감정보(.env, API Key, 비밀번호, 토큰 등)는 코드/문서/커밋에 포함하지 않는다.

```

## Workspace Rules

```c
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

```

## Skills 1-15

- **핵심 구현 스킬**
    - Skill 1) Frontend 기능 구현 스킬 (FSD 구조 준수)
    - Skill 2) Spring 기능 구현 스킬 (Hexagonal 아키텍처 준수, MariaDB + Spring Data JPA 기준)
    - Skill 3) FastAPI 기능 구현 스킬 (Layered + DI 구조 준수, Python 3.10 / Pydantic 2.x 기준)
    - Skill 4) API 계약 / 연동 스킬 (Spring ↔ FastAPI / 응답 포맷 / RFC9457 / timeout)
    - Skill 9) 검증 / 테스트 스킬 (스모크 / 계약 / RFC9457 / 엣지케이스)
    - Skill 10) 인증 / 인가 스킬 (이메일 로그인 / 가입 승인 / 권한 분리)
- **도메인 스킬**
    - Skill 5) AI 파이프라인 스킬 (비전 추론 / 시각화 / 저장 / 재검사 / Fallback)
    - Skill 6) RAG 스킬 (문서 인덱싱 / 검색 / 출처 / 버전)
    - Skill 11) 대시보드 / 집계 스킬 (지표 정의 / 쿼리 / 성능 / 캐싱)
    - Skill 12) 알림 / 보고서 스킬 (이벤트 트리거 / 확장 포인트 / MinIO 산출물)
    - Skill 13) 문서 관리 / 버전 스킬 (RAG 운영: 업로드 / 권한 / 재인덱싱)
    - Skill 14) 재검토 큐 / 피드백 루프 스킬 (관리자 재판정 / 사유 / 학습데이터 Export)
- **운영 / 인프라 스킬**
    - Skill 7) 데이터 저장 적용 스킬 (MariaDB / MinIO / ChromaDB / Redis 역할 분리 + 이력 / 대시보드 고려)
    - Skill 8) 공통 로깅 적용 스킬 (request-id 전파, 구조화 로그, 연동 추적)
    - Skill 15) 배포 / 환경설정 스킬 (Docker Compose / .env / 헬스체크 / Nginx 라우팅)

```markdown
# Skills

## Skill 공통 전제

- 모든 Skill은 Global Rules와 Workspace Rules를 우선 적용한다.
- 공통으로 반복되는 아래 규칙은 각 Skill에서 별도 서술이 없더라도 동일하게 적용한다.
  - 최소 변경 원칙
  - 패턴 분석 선행
  - request-id 전파 및 구조화 로그
  - 실패 응답은 RFC9457 Problem Details 사용
  - 최소 1개 스모크 테스트 작성
  - 민감정보 로그 / 응답 노출 금지
- 성공 응답은 기본적으로 아래 형식을 따른다.
  `{ "success": true, "data": {}, "message": "..." }`
- 단, 아래 엔드포인트는 성공 래퍼 예외를 허용한다.
  - 파일 다운로드 응답
  - 스트리밍 응답
  - 헬스체크 응답
  - 204 No Content 응답
- 유효성 검증 및 요청 오류 상태 코드는 아래 기준을 따른다.
  - JSON 형식 오류, 필수 파라미터 자체 누락, 타입 파싱 실패: `400 Bad Request`
  - 필드 값 검증 실패, 허용 범위 초과, 의미상 처리 불가: `422 Unprocessable Content`
- Spring persistence 기본 전제는 MariaDB + Spring Data JPA이다.
- FastAPI AI 서버 기본 전제는 Python 3.10 + FastAPI 0.115 + Pydantic 2.x 이다.
- Frontend 기본 전제는 React 18 + Vite 5 + Tailwind CSS + TypeScript 이다.

## Skill 사용 Rule

- Skill 적용 전 요구사항 기준 문서를 아래 우선순위로 확인한다.
  - `프로젝트개요.md` → 프로젝트 목적과 범위
  - `기능정의.md` → 기능 단위 완료 기준
  - `정책정의.md` → 권한, 판정, 저장, 예외, 운영 정책
  - `페이지목록.md` → 화면 흐름과 사용자 액션
  - `API.md` / `ERD.md` → 계약과 데이터 기준
  - `디렉터리구조.md` / `컨벤션.md` → 구현 위치와 코드 규칙
- 하나의 작업은 가능한 한 “대표 Skill 1개 + 보조 Skill 0~2개” 조합으로 수행한다.
- 화면 작업이라도 API 계약 변경이 있으면 `Skill 4) API 계약 / 연동 스킬`을 함께 적용한다.
- Spring 또는 FastAPI 기능 구현 전에는 반드시 해당 서버의 아키텍처 규칙을 먼저 맞춘다.
  - Spring: `domain -> application -> adapter`
  - FastAPI: `api -> application -> domain <- infrastructure -> container`
- 권한이 걸린 기능은 구현 전 `정책정의.md`의 인증 / 권한 정책과 `기능정의.md`의 역할 범위를 먼저 확인한다.
- 저장이 들어가는 기능은 구현 전 저장 책임을 먼저 고정한다.
  - 정형 메타데이터: MariaDB
  - 파일 산출물: MinIO
  - 문서 임베딩 / 검색: ChromaDB
  - 캐시 / 세션 / 상태: Redis
- 챗봇은 문서 근거 기반 답변만 허용하며, 출처 표시는 필수로 본다.
- 이상 탐지 결과는 `정상 / 불량 / 재검사` 3단계 판정 기준과 재검토 정책을 함께 확인한다.
- 관리자 전용 기능은 UI 숨김만으로 끝내지 않고 서버 권한 검증까지 포함해야 완료로 본다.
- Skill 결과물에는 가능한 한 아래 항목이 포함되어야 한다.
  - 변경된 레이어 또는 폴더
  - 영향받는 API / DTO / 엔티티 / 상태값
  - 권한 영향 여부
  - 테스트 또는 스모크 확인 포인트

## 프로젝트 기능별 Skill 선택표

- 로그인 / 회원가입 / 권한 기능
  - 대표 Skill: `Skill 10) 인증 / 인가 스킬`
  - 보조 Skill: `Skill 2) Spring 기능 구현 스킬`, `Skill 1) Frontend 기능 구현 스킬`
- 대시보드 기능
  - 대표 Skill: `Skill 11) 대시보드 / 집계 스킬`
  - 보조 Skill: `Skill 2) Spring 기능 구현 스킬`, `Skill 1) Frontend 기능 구현 스킬`
- 이상 탐지 기능
  - 대표 Skill: `Skill 5) AI 파이프라인 스킬`
  - 보조 Skill: `Skill 3) FastAPI 기능 구현 스킬`, `Skill 4) API 계약 / 연동 스킬`, `Skill 1) Frontend 기능 구현 스킬`
- 검사 결과 기능
  - 대표 Skill: `Skill 2) Spring 기능 구현 스킬`
  - 보조 Skill: `Skill 7) 데이터 저장 적용 스킬`, `Skill 1) Frontend 기능 구현 스킬`
- 재검토 / 피드백 기능
  - 대표 Skill: `Skill 14) 재검토 큐 / 피드백 루프 스킬`
  - 보조 Skill: `Skill 2) Spring 기능 구현 스킬`, `Skill 1) Frontend 기능 구현 스킬`
- 문서 관리 기능
  - 대표 Skill: `Skill 13) 문서 관리 / 버전 스킬`
  - 보조 Skill: `Skill 6) RAG 스킬`, `Skill 1) Frontend 기능 구현 스킬`
- 챗봇 / 문서 질의응답 기능
  - 대표 Skill: `Skill 6) RAG 스킬`
  - 보조 Skill: `Skill 3) FastAPI 기능 구현 스킬`, `Skill 4) API 계약 / 연동 스킬`, `Skill 1) Frontend 기능 구현 스킬`
- 알림 기능
  - 대표 Skill: `Skill 12) 알림 / 보고서 스킬`
  - 보조 Skill: `Skill 2) Spring 기능 구현 스킬`, `Skill 1) Frontend 기능 구현 스킬`
- 사용자 설정 / 마이페이지 기능
  - 대표 Skill: `Skill 1) Frontend 기능 구현 스킬`
  - 보조 Skill: `Skill 2) Spring 기능 구현 스킬`, `Skill 10) 인증 / 인가 스킬`
- 운영 모니터링 기능
  - 대표 Skill: `Skill 8) 공통 로깅 적용 스킬`
  - 보조 Skill: `Skill 11) 대시보드 / 집계 스킬`, `Skill 2) Spring 기능 구현 스킬`
- 사이트 운영자 관리 기능
  - 대표 Skill: `Skill 10) 인증 / 인가 스킬`
  - 보조 Skill: `Skill 2) Spring 기능 구현 스킬`, `Skill 1) Frontend 기능 구현 스킬`
- 공통 네비게이션 / UI 기능
  - 대표 Skill: `Skill 1) Frontend 기능 구현 스킬`
  - 보조 Skill: `Skill 10) 인증 / 인가 스킬`

## 기본 Skill 조합 순서

- 화면만 바꾸는 작업: `Skill 1`
- 화면 + 백엔드 API 연동: `Skill 4 -> Skill 2 -> Skill 1`
- 화면 + AI 추론 / 챗봇 연동: `Skill 4 -> Skill 3 -> Skill 1`
- 저장 구조가 함께 바뀌는 작업: `Skill 7`을 먼저 검토한 뒤 구현 Skill을 적용한다.
- 운영 / 장애 추적성이 중요한 작업: 구현 Skill 뒤에 `Skill 8`과 `Skill 9`를 붙인다.
- 배포나 실행 방식이 바뀌는 작업: 구현 완료 후 `Skill 15`까지 적용하고 README를 함께 본다.

---

## 1. 핵심 구현 스킬

### Skill 1) Frontend 기능 구현 스킬 (FSD 구조 준수)

#### 목적
기존 프론트엔드 코드베이스의 FSD 구조를 깨지 않고, 신규 화면 / 기능을 **UI → 상태관리 → API 연동 → 사용자 피드백** 흐름 안에서 최소 변경으로 안전하게 구현한다.

#### 적용 대상
- 신규 페이지 추가
- 기존 페이지 기능 확장
- 폼 / 목록 / 상세 / 필터 / 모달 / 위젯 기능 추가
- 백엔드 API 연동
- 공통 로딩 / 에러 / 권한 처리 반영

#### 작업 범위
- 포함: 필요한 레이어에만 코드 추가 / 수정
  - `shared/`
  - `entities/`
  - `features/`
  - `widgets/`
  - `pages/`
  - `app/`
- 포함: React 18 + Vite 5 + Tailwind CSS + TypeScript 기준 API 연동, 요청 / 응답 타입 반영, 로딩 / 에러 / 빈 상태 처리, 최소 스모크 테스트
- 제외: 요구되지 않은 전면 리팩터링, 폴더 재구성, 공통 컴포넌트 대량 교체

#### 입력(필수)
- Jira 이슈 키
- 기능 설명(무엇을 / 왜 / 완료 기준)
- 대상 화면 또는 기능 위치
- 연관 페이지 / 위젯 / feature
- 호출 API 명세(메서드, 경로, 요청 / 응답)
- 입력 / 출력 타입 정의
- 권한 영향 여부
- 로딩 / 에러 / 빈 상태 처리 기준

#### 작업 절차
0. **패턴분석**  
   현재 레포 내 유사 페이지, feature, API 호출 방식, 상태관리 패턴을 먼저 확인하고 동일한 구조와 명명 규칙을 따른다.

1. **타입 정의**
   - `entities/*/model` 또는 `features/*/model`
   - DTO, ViewModel, 필터 조건, 폼 타입 정의

2. **API 연결**
   - `shared/api` 또는 `features/*/api`에 API 함수 추가
   - 화면 내부 직접 axios 호출 금지

3. **상태 / 로직 구성**
   - `features/*/model`에 hook, store, 상태 전이 로직 구현
   - 비즈니스 로직은 page / component에 넣지 않음

4. **UI 구현**
   - feature 단위 UI 작성
   - 필요 시 widget / page에 조합
   - 권한별 노출 / 비노출 처리 반영

5. **페이지 연결**
   - `pages/*`에서 feature / widget 조립
   - `app/router` 또는 라우트 설정 반영

6. **공통 UX 반영**
   - 로딩, 스켈레톤, 에러, 빈 데이터, 중복 요청 방지
   - 공통 정책 반영

7. **정리**
   - 변경 파일 목록
   - 연관 API
   - 테스트 방법
   - 영향 범위 정리

#### 반드시 지킬 사항
- FSD 레이어 책임 유지
  - `pages`는 조합
  - `widgets`는 화면 블록
  - `features`는 사용자 행동 단위
  - `entities`는 도메인 표현
  - `shared`는 공통 모듈
- 컴포넌트 내부 API 직접 호출 난립 금지
- 화면에서 비즈니스 규칙 직접 계산 최소화
- 공통 로딩 / 에러 / 빈 상태 정책 준수
- 권한별 메뉴 / 버튼 / 화면 접근 제어 반영
- 최소 변경 원칙 유지

#### 산출물
- 구현 코드
- 수정 파일 목록
- 화면 흐름 요약
- 요청 / 응답 연결 설명
- 스모크 테스트 방법

#### 검증 포인트
- FSD 레이어 침범 없음
- API 타입 계약 위반 없음
- 로딩 / 에러 / 빈 상태 처리 누락 없음
- 권한별 UI 제어 정상 동작
- 실제 화면에서 최소 1개 시나리오 재현 가능

---

### Skill 2) Spring 기능 구현 스킬 (Hexagonal 아키텍처 준수, MariaDB + Spring Data JPA 기준)

#### 목적
기존 Spring 코드베이스의 Hexagonal 구조를 유지하면서, 신규 기능을 **Controller → Usecase → Port → Persistence / 외부연동** 흐름 안에서 최소 변경으로 구현한다.

#### 적용 대상
- 신규 REST API 추가
- 기존 유스케이스 확장
- 결과 조회 / 저장, 재검토, 회원가입 승인, 문서 메타데이터 관리 등
- FastAPI 연동이 필요한 백엔드 기능 추가
- MariaDB / Redis / MinIO 연동 기능 추가

#### 작업 범위
- 포함: 필요한 레이어에만 코드 추가 / 수정
  - `domain/`
  - `application/port/in`
  - `application/port/out`
  - `application/service`
  - `adapter/in/web`
  - `adapter/out/persistence`
  - `adapter/out/fastapi`
- 포함: DTO / 응답 스펙 반영, 전역 예외 처리, request-id / 로그 반영, 최소 스모크 테스트
- 포함: MariaDB + JPA Entity / Spring Data Repository / Persistence Adapter 구현
- 제외: 구조 개편, MyBatis 도입 / 전환, 무관한 공통 리팩터링

#### 입력(필수)
- Jira 이슈 키
- 기능 설명
- 엔드포인트(메서드, 경로)
- 요청 / 응답 DTO
- 도메인 규칙 / 예외
- 저장소 사용 여부(MariaDB / Redis / MinIO)
- FastAPI 계약 변경 여부
- 권한 / 인증 필요 여부

#### 작업 절차
0. **패턴분석**  
   기존 Controller, Service, Port, Persistence Adapter, FastAPI Adapter, JPA Entity, Spring Data Repository의 유사 구현을 먼저 분석하고 동일 패턴을 유지한다.
   
1. **Domain**
   - 모델, VO, enum, 예외 정의
   - 도메인 규칙이 필요할 때만 추가

2. **Port 정의**
   - `application/port/in`: 유스케이스 인터페이스
   - `application/port/out`: 저장 / 외부연동 인터페이스

3. **Service 구현**
   - `application/service`에 유스케이스 구현
   - 비즈니스 로직은 여기서 처리

4. **Adapter In**
   - `adapter/in/web` Controller 작성
   - 요청 검증 + usecase 호출 + 응답 반환만 수행

5. **Adapter Out**
	 - `adapter/out/persistence`: MariaDB + JPA Entity / Spring Data Repository / Adapter 처리
	 - `adapter/out/fastapi`: FastAPI HTTP 호출 처리

6. **공통 처리**
   - 전역 예외 처리
   - RFC9457 기반 에러 응답
   - request-id 전달
   - 로깅 / 타임아웃 / 멱등성 처리

7. **정리**
   - 변경 파일 목록
   - API 영향 범위
   - 외부 연동 영향
   - 테스트 방법 정리

#### 반드시 지킬 사항
- Controller에 비즈니스 로직 금지
- Service가 유스케이스 중심이 되도록 유지
- 외부 시스템 호출은 반드시 adapter/out으로 분리
- DB 접근은 `application/port/out` 인터페이스와 `adapter/out/persistence` 구현체를 통해 수행
- JPA Entity를 Controller 응답 DTO로 직접 노출하지 않음
- MyBatis Mapper / XML SQL 중심 구조를 사용하지 않음
- 임의 JSON 에러 바디 생성 금지
- 기존 명명 규칙, 응답 포맷, 예외 처리 방식 유지
- 성공 응답은 기본 래퍼를 따르되, 파일 다운로드 / 스트리밍 / 헬스체크 / 204 응답은 예외를 허용한다
- 400과 422 기준은 Workspace Rules의 전역 정책을 따른다
- 최소 변경 원칙 준수

#### 산출물
- 구현 코드
- 수정 파일 목록
- 레이어 연결 흐름 요약
- 요청 / 응답 예시
- JPA Entity / Spring Data Repository / Persistence Adapter 변경 사항
- 스모크 테스트 방법

#### 검증 포인트
- Hexagonal 레이어 침범 없음
- Controller가 얇게 유지됨
- Port 계약과 구현이 일치함
- MariaDB + Spring Data JPA 전제와 충돌 없음
- FastAPI 연동 계약 위반 없음
- RFC9457 에러 응답 일관성 유지
- 최소 1개 엔드포인트 스모크 가능

---

### Skill 3) FastAPI 기능 구현 스킬 (Layered + DI 구조 준수, Python 3.10 / Pydantic 2.x 기준)

#### 목적
기존 FastAPI 코드베이스의 Layered + DI 구조를 유지하면서, 신규 기능을 **Router → Application Usecase → Domain Interface → Infrastructure** 흐름으로 최소 변경 구현한다.

#### 적용 대상
- AI 추론 API 추가 / 확장
- 문서 인덱싱 / RAG 검색 기능 추가
- 챗봇 응답, 설명 생성, 검색 파이프라인 추가
- Spring 연동용 내부 API 구현
- ChromaDB / MinIO / Redis / 외부 모델 연동 기능 추가

#### 작업 범위
- 포함: 필요한 레이어에만 코드 추가 / 수정
  - `domain/`
  - `application/`
  - `api/`
  - `infrastructure/`
  - `container/`
- 포함: Pydantic 2.x schema 반영, 전역 예외 처리, request-id / 로그 반영, 최소 스모크 테스트
- 제외: DI 컨테이너 전면 개편, 무관한 파이프라인 재작성, 대규모 폴더 이동

#### 입력(필수)
- Jira 이슈 키
- 기능 설명
- 엔드포인트(메서드, 경로)
- 요청 / 응답 Schema
- 도메인 규칙 / 예외
- 저장소 사용 여부(ChromaDB / Redis / MinIO / DB)
- Spring 계약 영향 여부
- 외부 모델 / LLM 호출 여부

#### 작업 절차
0. **패턴분석**  
   기존 router, usecase, domain interface, infrastructure 구현, DI 조립 방식을 먼저 분석하고 동일한 패턴을 따른다.

1. **Domain**
   - 엔티티, VO, 인터페이스, 예외 정의
   - 비즈니스 규칙을 domain / application 경계에 맞게 정의

2. **Application**
   - usecase 구현
   - 입력 처리, 도메인 규칙 적용, 인터페이스 호출 조합

3. **API**
   - `api/router`에 엔드포인트 추가
   - 요청 검증 + usecase 호출 + 응답 반환만 수행

4. **Infrastructure**
   - 벡터DB, 파일저장소, Redis, 외부 모델, LLM, 문서 파서, LangChain / LangGraph / Ollama 연동 구현
   - 구체 구현은 infrastructure에 위치

5. **Container**
   - 의존성 주입 조립
   - 설정값, 구현체 바인딩, 환경별 분기 반영

6. **공통 처리**
   - global exception handler
   - RFC9457 기반 에러 응답
   - request-id 전달 및 로그 반영
   - timeout / retry 정책 반영

7. **정리**
   - 변경 파일 목록
   - API 계약 영향
   - Spring 연동 영향
   - 테스트 방법 정리

#### 반드시 지킬 사항
- router에 비즈니스 로직 금지
- application이 흐름 제어를 담당해야 함
- domain은 인터페이스와 규칙 중심 유지
- 외부 연동은 infrastructure로 분리
- container에서 DI 조립 일관성 유지
- 임의 에러 바디 생성 금지
- 기존 schema / 응답 구조와 호환성 유지
- 성공 응답은 기본 래퍼를 따르되, 스트리밍 / 헬스체크 / 204 응답은 예외를 허용한다
- 400과 422 기준은 Workspace Rules의 전역 정책을 따른다
- 최소 변경 원칙 준수

#### 산출물
- 구현 코드
- 수정 파일 목록
- 레이어 연결 흐름 요약
- 요청 / 응답 예시
- 스모크 테스트 방법

#### 검증 포인트
- Layered + DI 레이어 침범 없음
- router가 얇게 유지됨
- Pydantic 2.x / OpenAPI 계약 위반 없음
- Spring 연동 계약 위반 없음
- RFC9457 에러 응답 일관성 유지
- 최소 1개 API 시나리오 스모크 가능

---

### Skill 4) API 계약 / 연동 스킬 (Spring ↔ FastAPI / 응답 포맷 / RFC9457 / timeout)

#### 목적
Spring ↔ FastAPI 연동에서 DTO 불일치 / timeout 누락 / 에러 포맷 혼재를 방지하고, 프론트가 일관되게 처리 가능한 규격을 유지한다.

#### 적용 대상
- Spring이 FastAPI(AI / RAG / LLM)를 호출하는 모든 기능
- 요청 / 응답 DTO 변경(필드 / 타입 / nullable 변경 포함)
- timeout / 예외 / 에러 매핑 정책 정의가 필요한 연동

#### 작업 범위
- 포함: 계약(OpenAPI / DTO / JSON) 확인 및 반영, timeout 필수 적용, request-id 전파, 에러(RFC9457) 표준화, 스모크 테스트
- 제외: 불필요한 재시도 / 서킷브레이커(요구 없으면 도입 금지), 계약 없는 임의 필드 추가

#### 입력(필수)
- Jira 이슈 키
- 호출 주체 / 대상(예: Spring → FastAPI)
- 엔드포인트(메서드 / 경로)
- 요청 / 응답 DTO 또는 OpenAPI 스펙
- timeout 기준(초) 및 예상 처리시간
- request-id 헤더명(팀 표준, 미정이면 제안 필요)
- 에러코드 / 매핑 규칙(없으면 “정의 필요” 표시)

#### 작업 절차
1. 계약(OpenAPI / DTO / JSON Schema) 존재 여부 확인(없으면 먼저 정의 / 합의)
2. DTO 정합성 맞추기
   - Spring: request / response DTO + validation
   - FastAPI: Pydantic 2.x schema + validation
3. 호출 정책 정의
   - timeout **필수**
   - request-id 헤더 전파 **필수**
4. 응답 규격 적용(컨벤션 고정)
   - 성공(2xx): `{ "success": true, "data": ..., "message": "..." }`
   - 단, 파일 다운로드 / 스트리밍 / 헬스체크 / 204 응답은 성공 래퍼 예외 허용
   - 실패(4xx / 5xx): RFC 9457 Problem Details
5. 예외 / 실패 매핑
   - FastAPI가 RFC9457로 응답 → Spring은 그대로 전달하거나 내부 `errorCode`만 보강(구조는 유지)
   - 통신 실패(timeout / connection) → Spring 전역 예외 처리에서 RFC9457로 변환
6. 스모크 테스트(정상 / 검증실패 / timeout / 내부오류) 시나리오 작성

#### 반드시 지킬 사항
- “스펙 먼저 → 영향도 확인 → 코드 반영”
- breaking change 금지(불가피하면 버전 분리 / 하위호환 제시)
- timeout 없이 외부 호출 추가 금지
- 실패 응답을 `{success:false,...}`로 임의 생성 금지
- 성공 응답 예외는 파일 다운로드 / 스트리밍 / 헬스체크 / 204에만 한정한다
- 민감정보(토큰 / 키 / 비밀번호) 요청 / 응답 / 로그 포함 금지

#### 산출물
- DTO / 스펙 변경 요약
- Spring 호출 구현(`config/client` + `adapter/out/fastapi`) 및 FastAPI 라우터 / 스키마 반영
- 에러 매핑 표(간단)
- 스모크 테스트 방법(curl / postman)

#### 검증 포인트
- DTO / 필드 / 타입 불일치 없음
- timeout 시 시스템 중단 없이 RFC9457 응답
- 상태코드(422 / 409 / 500 등) 의미에 맞게 일관됨
- request-id가 양쪽 로그에서 연결됨

---

### Skill 9) 검증 / 테스트 스킬 (스모크 / 계약 / RFC9457 / 엣지케이스)

#### 목적
변경 사항이 계약 / 예외 / 엣지케이스를 깨지 않도록 최소 검증 루틴을 제공한다.

#### 적용 대상
- 신규 API / 기능 개발
- DTO / 스키마 변경
- AI / RAG처럼 실패가 빈번한 기능

#### 작업 범위
- 포함: 성공 기준 정의(최소 3케이스), 스모크 테스트, 계약 위반 점검, 상태코드 / 에러포맷 검증, PR 체크리스트
- 제외: 대규모 E2E 자동화(필요 시 별도), 테스트 없이 완료 처리

#### 입력(필수)
- Jira 이슈 키
- 변경된 기능 / 엔드포인트
- 성공 기준(정상 / 입력오류 / 외부실패 · timeout)
- 계약 스키마(요청 / 응답 예시)
- 엣지 케이스 목록(최소 3개)

#### 작업 절차
1. 성공 기준(최소)
   - 정상 성공(2xx)
   - 요청 형식 오류 또는 파라미터 누락(400)
   - 유효성 검증 실패 또는 허용 범위 초과(422)
   - 외부 실패 / timeout(5xx 또는 팀 표준 정책 코드)
2. 스모크 테스트(curl / postman) 작성
3. 계약 위반(필드 누락 / 타입 변경) 체크
4. 응답 규격 확인
   - 성공: `{ "success": true, "data": ..., "message": "..." }`
   - 단, 파일 다운로드 / 스트리밍 / 헬스체크 / 204 응답은 예외 가능
   - 실패: RFC9457(`type`, `title`, `status`, `detail`, `instance` + 필요 시 확장 필드)
5. PR 체크리스트로 정리
   - 민감정보 포함 여부
   - request-id 로깅 여부
   - README / `.env.example` 변경 여부
   - 임시 로그 / 디버깅 코드 제거 여부

#### 반드시 지킬 사항
- 실패 케이스도 반드시 검증
- 다른 팀원이 그대로 재현 가능해야 함
- 제출 전 콘솔 로그 / 임시 코드 제거
- 400과 422를 혼용하지 않고 팀 표준에 맞게 검증한다
- Spring 검증 오류가 422 정책과 맞게 처리되는지 전역 예외 처리 기준까지 확인한다

#### 산출물
- 스모크 테스트 시나리오 및 실행 방법
- (가능 시) 테스트 코드
- 검증 결과 요약 + 남은 리스크
- PR 체크리스트(간단)

#### 검증 포인트
- 정의 케이스 재현 가능
- 계약 위반 없음
- 실패 시에도 시스템 중단 없이 표준 에러 / 로그 남음

---

### Skill 10) 인증 / 인가 스킬 (이메일 로그인 / 가입 승인 / 권한 분리)

#### 목적
웹 기반 다중 사용자 환경에서 인증 / 인가를 표준화하고, 관리자 기능을 안전하게 보호하며 감사 / 추적이 가능하도록 한다.

#### 적용 대상
- 이메일 / 비밀번호 로그인
- 로그아웃
- 회원가입 신청 / 승인 대기 / 승인 / 거절
- 세션 또는 토큰 기반 로그인 유지
- Role 기반 접근 제어(일반 사용자 vs 시스템 관리자)
- 관리자 전용 기능 보호(가입 승인, 재검토 처리, 운영 모니터링, 감사 로그, 보고서, 임계값 정책 관리 등)
- 사용자 기능 보호(검사, 결과 조회, 자기 회사 문서 관리, 개인 임계값 설정, 챗봇 이력 등)
- 인증 실패 / 권한 없음 / 만료 처리 UX 표준화

#### 작업 범위
- 포함: Spring Security 설정, 비밀번호 암호화, 계정 상태 관리(`PENDING`, `ACTIVE`, `REJECTED`, `INACTIVE` 등), 권한 어노테이션 / 매처, 전역 예외 처리(RFC9457), 감사 로그(누가 / 언제 / 무엇을)
- 포함: 로그인 유지 정책에 따라 Redis 기반 세션 저장 또는 토큰 상태 관리 구현
- 제외: 과도한 IAM / SSO 연동, 복잡한 멀티테넌시

#### 입력(필수)
- Jira 이슈 키
- 인증 유지 방식(세션 / 토큰 / 혼합)
- 사용자 / 권한 모델(예: `ROLE_USER`, `ROLE_ADMIN`)
- 계정 상태 모델(승인 대기 / 활성 / 거절 / 비활성)
- 보호해야 할 엔드포인트 목록
- 실패 응답 / 상태코드 정책(401 / 403 등) 및 RFC9457 필드 규칙
- 세션 / 토큰 저장소: Redis / MariaDB / 무저장(Stateless)

#### 작업 절차 (Spring Hexagonal 기준)
1. Domain
   - `domain/user` 권한 / 역할 / 계정상태 모델 및 예외 정의
2. Application
   - `application/port/in/auth` 유스케이스 정의(회원가입 신청 / 로그인 / 로그아웃 / 승인 / 거절 등)
   - `application/service/auth` 구현
   - `application/port/out/user` 및 (필요 시) 세션 / 토큰 저장 Port 정의
3. Adapter out
   - `adapter/out/persistence/user` 사용자 조회 / 저장 / 승인대기 / 상태변경 구현
   - (필요 시) `adapter/out/persistence/auth` 또는 Redis 세션 / 토큰 저장 구현
4. Config
   - `config/security`에 인증 필터, 인증 엔트리포인트(401), 접근거부 핸들러(403) 적용
   - CORS / HTTPS 환경 고려(`config/web`)
5. Adapter in
   - `adapter/in/web/auth` Controller 구현(비즈니스 로직 금지)
6. 예외 표준화
   - 인증 실패 / 만료 / 권한 없음은 RFC9457 Problem Details로 통일
7. Frontend(FSD) 연동(필요 시)
   - `features/auth/api` 로그인 / 회원가입 신청 / 로그아웃 / 승인대기 처리
   - `shared/api`에 인증 헤더 또는 세션 처리 표준화
8. 스모크 테스트
   - 회원가입 신청
   - 승인 대기 상태 접근 제한
   - 관리자 승인 / 거절
   - 정상 로그인
   - 권한 없음
   - 관리자 API 접근
   - 일반 사용자 기능 접근

#### 반드시 지킬 사항
- 기본 사용자 인증 흐름은 이메일 / 비밀번호 + 관리자 승인 기준을 따른다
- 승인 대기 상태 계정은 주요 기능 접근이 제한되어야 한다
- 권한 최종 검증은 Spring에서 수행한다(클라이언트 / AI 서버 단독 판단 금지)
- 민감정보(비밀번호 / 토큰 / 키)를 로그 / 응답에 노출하지 않는다
- 비밀번호는 안전한 해시 방식으로 저장하며 평문 저장을 금지한다
- 인증 / 인가 실패 응답은 RFC9457로 통일하고 상태코드는 401 / 403을 의미에 맞게 사용한다
- 일반 사용자는 자기 회사 범위 문서 관리와 허용 범위 내 개인 임계값 설정 기능에 접근할 수 있어야 한다
- 시스템 관리자는 가입 승인 / 거절, 재검토 처리, 운영 모니터링, 감사 로그, 전체 데이터 조회, 보고서, 임계값 정책 관리 기능에 접근할 수 있어야 한다
- 관리자 작업(가입 승인, 재판정, 운영 기준 변경 등)은 감사 로그 / 이력으로 남긴다

#### 산출물
- Spring Security 설정 코드(`config/security`)
- Auth 유스케이스 / Controller / DTO / Port / Adapter 변경 목록
- 보호 엔드포인트 매트릭스
- 스모크 테스트 방법(curl / postman) + 기대 응답(401 / 403 / 승인대기 차단)

#### 검증 포인트
- 승인 대기 계정은 로그인 후에도 주요 기능 접근이 제한됨
- 관리자가 아닌 사용자가 관리자 API 접근 시 403(RFC9457)
- 인증 만료 / 무효 인증정보 시 401(RFC9457)
- 일반 사용자가 허용된 기능 접근 시 정상 처리
- request-id가 인증 실패 로그에도 포함됨
- 민감정보 노출 없음

---

## 2. 도메인 스킬

### Skill 5) AI 파이프라인 스킬 (비전 추론 / 시각화 / 저장 / 재검사 / Fallback)

#### 목적
검사 요청 단위로 AI 추론을 수행하고(정상 / 불량 / 재검사 + anomaly score), 시각화 산출물을 생성 · 저장하며 실패해도 “요청 흐름 / 이력”이 끊기지 않게 한다.

#### 적용 대상
- 이미지 / 동영상 / 스트림(프레임 단위) 검사 처리
- 재검사 분류 및 관리자 재검토 큐 연동
- 추론 결과 저장 및 조회 API

#### 작업 범위
- 포함: 입력 검증, 원본 / 산출물 MinIO 저장, 메타 MariaDB 저장, 상태 전이 기록, 실패 Fallback, timeout / 로깅
- 제외: 모델 성능 개선 / 재학습, 고급 실시간 최적화(요구 없으면 제외)

#### 입력(필수)
- Jira 이슈 키
- 입력 형태 범위(이미지 / 영상 / 스트림) + (영상 / 스트림인 경우) 프레임 추출 정책(간격 / FPS)
- 판정 정책(정상 / 불량 / 재검사 기준, 임계값 / 신뢰도 기준)
- 산출물 종류(heatmap / anomaly map / bbox 유사 중 최소 1개)
- 저장 정책(MinIO 키 규칙, 원본 저장 여부)
- 실패 Fallback 규칙(추론 실패 / 시각화 실패 / 저장 실패 시 동작)
- 상태값 표준(예: `PENDING`, `PROCESSING`, `COMPLETED`, `FAILED`, `REVIEW_REQUIRED`)
- 사용자 임계값 적용 여부 및 기본 임계값 fallback 기준

#### 작업 절차
1. Spring에서 검사 요청 생성(MariaDB) + 상태 `PENDING` 또는 `PROCESSING`
2. 원본 업로드(MinIO) + MariaDB에 참조키 저장
3. 판정 기준 결정
   - 사용자 개인 임계값이 있으면 해당 값을 우선 적용
   - 사용자 설정값이 없으면 회사 / 설비 기본 임계값 적용
4. FastAPI 추론 호출(timeout / request-id 전파) → anomaly score / 판정 / 시각화 생성
5. 시각화 산출물 MinIO 저장
   - 실패 시 대체 동작으로 “시각화 없음” 상태와 실패 사유를 기록
6. 결과 메타 MariaDB 저장
   - 판정
   - anomaly score
   - 산출물 참조
   - 처리시간
   - 적용 임계값
   - 상태 전이
   - 에러 사유
7. 재검사 정책 적용
   - 낮은 신뢰도 또는 경계 score는 `REVIEW_REQUIRED`로 분류
   - 관리자 재검토 큐와 연결 가능하도록 저장
8. 실패 시에도 요청 상태를 `FAILED` 등으로 확정 기록하고 사용자에게 다음 조치를 안내

#### 반드시 지킬 사항
- 저장소 책임 분리: 원본 / 산출물 = MinIO, 메타 / 이력 = MariaDB
- 실패를 삼키지 않고 상태 / 사유 / request-id로 추적 가능해야 함
- 추론 실패 시 전체 시스템 중단 금지(명확한 실패 응답 + 다음 조치)
- 판정 정책은 하드코딩보다 사용자 설정값 + 기본 정책 조합을 우선한다
- 재검사와 재검토를 혼용하지 않는다
  - 재검사: 시스템 자동 판정 결과
  - 재검토: 관리자 수동 검토 행위
- 사용자 노출 판정 용어는 `정상 / 불량 / 재검사`를 따른다

#### 산출물
- FastAPI 추론 / 시각화 구현(`application` + `infrastructure`)
- Spring 오케스트레이션 흐름(요청 생성 → 호출 → 저장 → 상태 전이)
- MinIO 키 규칙 / 메타 스키마 정의
- 실패 / 재검사 정책 문서(상태 전이 포함)
- 스모크 테스트(성공 / 추론실패 / 시각화실패 / 저장실패)

#### 검증 포인트
- 성공 시 원본 / 산출물 / 메타 저장 및 조회 가능
- 실패 시에도 이력과 상태가 남고 RFC9457 또는 표준 응답으로 안내됨
- 재검사 분류가 정책대로 수행됨
- 적용된 임계값이 추적 가능함

---

### Skill 6) RAG 스킬 (문서 인덱싱 / 검색 / 출처 / 버전)

#### 목적
설비 문서를 인덱싱하여 검색하고, 답변에 출처를 포함해 근거 기반 안내를 제공한다.

#### 적용 대상
- 문서 업로드 / 갱신 / 삭제(버전 포함)
- 청킹 / 임베딩 / 인덱싱 파이프라인
- 질의 시 검색 + 출처 제공 응답

#### 작업 범위
- 포함: 문서 저장(MinIO), 메타(MariaDB), 임베딩 / 벡터(ChromaDB), 검색 API, citation 포맷
- 제외: 근거 없는 단정형 답변 생성, 문서 원본 편집 / 작성

#### 입력(필수)
- Jira 이슈 키
- 문서 소스 / 형식(PDF / HTML / MD / DOCX 등) + OCR 필요 여부
- 청킹 규칙(기준, overlap)
- 메타데이터 규격(문서명 / 버전 / 섹션 / 페이지 / 링크 / 작성일)
- 임베딩 모델 / ChromaDB 컬렉션 규칙
- citation(출처) 표기 포맷(필수)
- 재인덱싱 트리거(수동 / 자동 / 주기)

#### 작업 절차
1. 문서 원본 저장(MinIO) + 메타(MariaDB: 문서ID, 버전, 상태) 등록
2. 청킹 + 메타데이터 부여
3. 임베딩 생성 → ChromaDB 적재(문서ID / 버전 / 섹션 / 페이지 메타 포함)
4. 질의 시 top-k 검색 → score와 함께 chunk 반환
5. 응답에 sources 포함(문서명 / 섹션 / 페이지 / 링크 / score)
6. 문서 미존재 / 검색 실패 시 “근거 없음” 명시 + fallback(추정 / 일반론 구분)

#### 반드시 지킬 사항
- 출처(citation) 누락 금지
- 문서에 없는 내용은 “추정 / 일반론”으로 구분
- 문서 보안 / 권한 정책 준수(내부 문서 외부 유출 금지)
- 사용자 노출 문서 반영 상태는 `대기 / 처리중 / 반영완료 / 반영실패` 기준을 따른다

#### 산출물
- 인덱싱 파이프라인 코드
- 메타데이터 / 컬렉션 설계 문서
- 검색 API 응답 예시(sources 포함)

#### 검증 포인트
- 동일 질의에서 재현 가능한 검색 결과
- sources 누락 없음(문서명 / 섹션 / 페이지 등)
- 근거 없는 단정 답변 방지

---

### Skill 11) 대시보드 / 집계 스킬 (지표 정의 / 쿼리 / 성능 / 캐싱)

#### 목적
대시보드 지표를 DB에서 재현 가능하게 정의하고, 조회 성능(페이지네이션 / 필터 / 집계)을 운영 수준으로 맞춘다.

#### 적용 대상
- 정상 / 불량 / 재검사 비율, 기간별 추이, 설비 / 카테고리별 빈도, 점수 추이 등 집계 API
- 목록 / 필터 / 정렬 / 페이지네이션이 필요한 조회 API
- 대시보드 지표 추가 / 수정 / 검증

#### 작업 범위
- 포함: 지표 정의서(분모 / 분자 / 필터), MariaDB 스키마 / 인덱스 설계, JPA 기반 조회 구현, JPQL / 커스텀 Repository / QueryDSL 검토, 캐싱(필요 시 Redis), API 응답 DTO
- 제외: 과도한 OLAP / 데이터웨어하우스 도입(요구 없으면)

#### 입력(필수)
- Jira 이슈 키
- 지표 목록 및 정의(기간 기준, 그룹 기준, 분모 / 분자, 제외 조건)
- 조회 조건(필터: 날짜 / 설비 / 품목 / 카테고리 / 판정 / 사용자 등)
- 응답 형태(차트 시계열 / 랭킹 / 분포 / 단건 KPI)
- 성능 요구(대략적인 데이터 규모, 목표 응답시간)
- 캐싱 필요 여부(있다면 TTL / 무효화 조건)

#### 작업 절차 (Spring Hexagonal 기준)
1. Domain / Model
   - `domain/dashboard/model` 집계 모델(또는 Projection) 정의(필요 시)
2. Application
   - `application/port/in/dashboard` 유스케이스 정의
   - `application/service/dashboard` 구현(집계 흐름)
   - `application/port/out/dashboard` 조회 Port 정의
3. Adapter out
   - `adapter/out/persistence/dashboard` JPA Repository / 커스텀 Repository / Projection 구현
	 - 복잡한 동적 조건 또는 통계 조회가 필요한 경우 QueryDSL 도입 여부 검토
	 - 인덱스 / 쿼리 플랜 점검(가능 범위에서 EXPLAIN)
4. Adapter in
   - `adapter/in/web/dashboard` Controller + DTO 응답 구성
5. (선택) Redis 캐싱
   - 캐시 키 규칙(기간 / 필터 포함), TTL 설정, 갱신 전략 문서화
6. 검증
   - 샘플 데이터로 계산 로직 검증(수동 계산 대비)
   - 페이지네이션 / 필터 조합 테스트
7. 문서화
   - 지표 정의서(산식 / 필터 / 그룹 기준) 작성

#### 반드시 지킬 사항
- 지표는 “DB에서 재현 가능”해야 하며 산식을 문서화한다
- 응답은 과도한 payload를 피하고(필요 필드만), 페이지네이션 기본 적용
- 성능 문제를 방치하지 말고 인덱스 / 쿼리 개선 또는 캐싱 여부를 제시
- MariaDB + Spring Data JPA 전제를 따른다
- 단순 조회는 Spring Data JPA 메서드 / JPQL / Projection을 우선 검토한다
- 복잡한 동적 검색, 통계, 대시보드 집계는 커스텀 Repository 또는 QueryDSL 도입을 검토한다
- JPA Entity를 API 응답 DTO로 직접 노출하지 않는다
- 실패 응답은 RFC9457로 통일

#### 산출물
- 지표 정의 문서(간단 표)
- JPA Repository / 커스텀 Repository / Projection / Adapter 코드
- 인덱스 변경(필요 시 DDL) 및 근거(EXPLAIN 결과 요약)
- 스모크 테스트(필터 / 기간별) 방법

#### 검증 포인트
- 동일 조건에서 결과 재현 가능
- 대용량 조건에서 타임아웃 / 풀스캔 방지
- 필터 / 정렬 / 페이지네이션이 일관되게 동작

---

### Skill 12) 알림 / 보고서 스킬 (이벤트 트리거 / 확장 포인트 / MinIO 산출물)

#### 목적
불량 판정 / 재검사 / 재검토 완료 등 이벤트 기반 알림과 일 · 주 · 월 보고서 산출을 표준화한다(확장 가능 구조 유지, MVP는 단순 구현).

#### 적용 대상
- 불량 판정 시 관리자 / 담당자 알림
- 재검사 / 재검토 큐 상태 변화 알림
- 일 / 주 / 월 리포트(요약 지표 + 파일 산출물)
- 추후 메일 / 슬랙 / SMS 확장 포인트

#### 작업 범위
- 포함: 이벤트 포인트 정의, 알림 데이터 모델(MariaDB), (선택) 메일 / 슬랙 어댑터, 보고서 파일 생성 및 MinIO 저장, 다운로드 API
- 제외: 복잡한 워크플로우 / 결재 시스템, 고급 메시지 브로커 도입(요구 없으면)

#### 입력(필수)
- Jira 이슈 키
- 알림 트리거(불량 / 재검사 / 재검토완료 / 임계값 초과 등)
- 알림 대상(관리자 / 특정 사용자 / 설비 담당자)
- MVP 알림 채널(웹 알림 / 배지 / 이메일 중 선택)
- 보고서 범위(일 / 주 / 월), 포함 지표, 파일 포맷(PDF / CSV / XLSX)
- 저장 규칙: 보고서 파일 MinIO 경로 / 키 규칙, 메타(MariaDB) 필드

#### 작업 절차
1. Domain
   - 알림 / 보고서 메타 모델 정의(필요 시)
2. Application
   - 알림 생성 / 조회 유스케이스(읽음 처리 포함)
   - 보고서 생성 / 조회 유스케이스
3. Adapter out
   - 알림 저장 / 조회 MariaDB 구현
   - 보고서 파일 생성(애플리케이션 레벨) + MinIO 업로드(인프라)
4. Adapter in
   - 알림 조회 API(목록 / 읽음 처리)
   - 보고서 생성 / 다운로드 API
5. 확장 포인트
   - 메일 / 슬랙 등은 Port / Adapter로 분리(요구 시에만 구현)
6. 스모크 테스트
   - 이벤트 발생 → 알림 생성 확인
   - 보고서 생성 → MinIO 업로드 → 다운로드 확인

#### 반드시 지킬 사항
- 파일 산출물은 MinIO, 메타는 MariaDB(역할 혼합 금지)
- 알림 전송 실패가 핵심 기능을 중단시키지 않게(비동기 / 후처리 또는 실패 상태 기록)
- 민감정보 포함 금지(알림 본문 / 보고서에 토큰 / 키 / 내부 경로 노출 금지)
- 보고서 다운로드 API는 성공 JSON 래퍼 대신 파일 응답 예외를 허용할 수 있다
- 알림 / 보고서 조회 API의 실패 응답은 RFC9457로 통일한다

#### 산출물
- 알림 / 보고서 스키마(또는 엔티티) + API
- MinIO 키 규칙 및 다운로드 방식
- (선택) 메일 / 슬랙 어댑터 인터페이스(Port) 정의
- 스모크 테스트 시나리오

#### 검증 포인트
- 알림이 트리거 조건대로 생성됨
- 보고서 파일이 MinIO에 저장되고 재다운로드 가능
- 실패 시 상태 / 사유가 남고 시스템은 중단되지 않음

---

### Skill 13) 문서 관리 / 버전 스킬 (RAG 운영: 업로드 / 권한 / 재인덱싱)

#### 목적
RAG 대상 문서를 운영 관점에서 안전하게 관리한다(업로드 / 수정 / 삭제 / 버전 / 재인덱싱 / 권한).

#### 적용 대상
- 일반 사용자의 자기 회사 범위 문서 업로드 / 수정 / 삭제 / 조회
- 문서 메타데이터 / 버전 관리(MariaDB)
- 원본 저장(MinIO) 및 인덱싱(ChromaDB) 트리거
- 시스템의 인덱싱 상태 관리 및 검색 반영 처리
- 관리자의 전체 운영 현황 / 감사 / 정책 조회

#### 작업 범위
- 포함: 문서 CRUD API, 권한 검증, 문서 메타 스키마, 재인덱싱(동기 / 비동기) 정책, 인덱싱 상태 관리
- 제외: 문서 콘텐츠 편집기 / 협업 작성 도구

#### 입력(필수)
- Jira 이슈 키
- 문서 타입 / 포맷(PDF / MD / HTML / DOCX 등), OCR 필요 여부
- 문서 메타 필드(문서명 / 버전 / 작성일 / 설비 / 카테고리 / 태그 등)
- 권한 정책
  - 일반 사용자: 자기 회사 범위 문서 업로드 / 수정 / 삭제 / 조회
  - 시스템 관리자: 전체 문서 운영 현황 / 인덱싱 상태 / 감사 로그 조회
- 재인덱싱 정책(업로드 시 즉시 / 배치 / 수동)
- 삭제 정책(원본 삭제 여부, 인덱스 삭제 여부, 검색 제외 반영 방식)

#### 작업 절차
1. Spring(운영 API)
   - 문서 메타 CRUD API 구현
   - 사용자 권한 및 회사 범위 검증
2. 저장
   - 원본 파일 MinIO 업로드
   - 메타 MariaDB 저장(문서ID, 버전, 작성자, 회사, 반영 상태 등)
3. FastAPI(RAG 인덱싱)
   - 인덱싱 요청 엔드포인트 제공(문서ID / 버전 / MinIO 키 기반)
   - ChromaDB 적재 / 삭제(문서ID + 버전 기준)
4. 상태 관리
   - 사용자 노출 상태는 `대기`, `처리중`, `반영완료`, `반영실패` 기준으로 관리
   - 문서 목록 / 상세에서 상태 조회 가능하도록 반영
5. 예외 / 로깅
   - 실패는 RFC9457 + request-id 기반 추적
6. 스모크 테스트
   - 업로드 → 인덱싱 → 검색 반영 확인
   - 수정 → 버전 반영 확인
   - 삭제 → 검색 제외 확인

#### 반드시 지킬 사항
- 일반 사용자는 자기 회사 범위 문서만 관리할 수 있어야 한다
- 시스템 관리자는 전체 문서 자체를 직접 수정하는 역할보다 운영 현황 / 감사 / 정책 관리 역할을 우선한다
- MinIO(원본) / MariaDB(메타 / 버전) / ChromaDB(벡터) 역할을 분리한다
- 출처 / 버전 식별이 가능하도록 메타데이터를 반드시 유지한다
- 삭제 / 교체 시 검색 결과 출처와 버전 정합성이 깨지지 않도록 정책을 명확히 한다
- 문서 관리 API에도 request-id, 권한 검증, 감사 로그 기준을 적용한다
- 정책 정의 기준의 문서 반영 상태 용어(대기 / 처리중 / 반영완료 / 반영실패)를 화면 / API 문구에서 일관되게 사용한다

#### 산출물
- 문서 메타 스키마 / 엔티티
- 문서 업로드 / 수정 / 삭제 / 목록 API
- 인덱싱 트리거(연동 API) + 상태 전이 정의
- 스모크 테스트 방법

#### 검증 포인트
- 일반 사용자가 자기 회사 범위 문서를 정상 관리할 수 있음
- 다른 회사 문서 접근 / 수정 / 삭제 시 403 또는 정책에 맞는 차단 응답
- 버전 교체 시 최신 버전이 정책대로 검색에 반영됨
- 문서 삭제 시 검색에 노출되지 않음(정책대로)
- 인덱싱 실패 시 상태와 사유가 추적 가능함

---

### Skill 14) 재검토 큐 / 피드백 루프 스킬 (관리자 재판정 / 사유 / 학습데이터 Export)

#### 목적
저신뢰 결과를 재검토 큐로 모으고, 관리자가 재판정 / 사유를 남기며, 향후 모델 개선을 위한 피드백 데이터를 구조화한다.

#### 적용 대상
- `REVIEW_REQUIRED`(재검사) 결과의 재검토 큐 관리(목록 / 필터 / 상태)
- 관리자 재판정(정상 / 불량) + 사유 기록
- 학습데이터 export(메타 / 라벨 / 파일 참조)

#### 작업 범위
- 포함: 재검토 큐 조회 API, 재판정 API, 재판정 이력 필드 설계, 감사 로그, export 포맷 정의
- 제외: 실제 재학습 파이프라인 자동화(요구 없으면)

#### 입력(필수)
- Jira 이슈 키
- 재검토 큐 진입 조건(점수 범위 / 신뢰도 / 모델 실패 등)
- 재판정 필드 규격(예: `review_result`, `review_reason`, `reviewed_by`, `reviewed_at`)
- export 범위(기간 / 설비 / 카테고리) 및 포맷(JSON / CSV)
- 권한 정책(관리자만 재판정 가능)

#### 작업 절차
1. Domain / Model
   - 결과 / 재검토 상태 모델 정리(필요 시)
2. Application
   - 재검토 큐 조회 유스케이스(필터 / 페이지네이션)
   - 재판정 유스케이스(상태 전이 + 사유 기록)
3. Persistence(MariaDB)
   - 결과 테이블 또는 재검토 테이블에 재판정 필드 추가
   - 감사 로그 / 이력 저장
4. Adapter in
   - 관리자 전용 API 제공(권한 체크)
5. Export
   - export API 또는 배치(요구에 맞게) 구현
   - 파일 산출물이면 MinIO 저장 + MariaDB 메타
6. 스모크 테스트
   - REVIEW_REQUIRED 생성 → 재검토 큐 조회 → 재판정 → 상태 / 이력 반영 확인

#### 반드시 지킬 사항
- 재판정은 감사 / 추적 가능해야 함(누가 / 언제 / 무엇을 / 왜)
- 권한 없는 사용자는 403(RFC9457)
- export에는 민감정보 포함 금지(필요 최소 메타 + 파일 참조 키)
- 재판정으로 기존 추론 원본 / 산출물을 임의로 삭제하지 않는다(재현성 유지)
- 운영 용어는 `재검토 큐`를 기본으로 사용한다

#### 산출물
- 재검토 큐 API(목록 / 상세 / 재판정)
- 스키마 변경(필요 시) + 필드 정의서
- export 포맷 문서 + 샘플 결과
- 스모크 테스트 시나리오

#### 검증 포인트
- 재판정 후 상태 전이가 정책대로 반영됨
- 재판정 이력 / 사유가 누락되지 않음
- export가 재현 가능한 키(inspectionId, fileKey 등)를 포함

---

## 3. 운영 / 인프라 스킬

### Skill 7) 데이터 저장 적용 스킬 (MariaDB / MinIO / ChromaDB / Redis 역할 분리 + 이력 / 대시보드 고려)

#### 목적
저장소별 책임을 혼동하지 않고, 이력 / 파일 / 벡터 / 캐시를 운영 가능하게 저장 / 조회한다.

#### 적용 대상
- 검사 / 결과 / 재검토 큐 / 질의이력 / 설정 / 사용자(MariaDB)
- 원본 / 시각화 / 문서 / 보고서(MinIO)
- 문서 임베딩(ChromaDB)
- 캐시 / 세션 / 작업상태 / 레이트리밋(Redis)

#### 작업 범위
- 포함: 스키마 / 키 설계, 인덱스 / 쿼리 고려(목록 / 필터 / 대시보드), 예외 처리, README / `.env.example` 갱신 트리거
- 제외: 과도한 성능 튜닝(필요 시 별도), 저장소 역할 혼합(금지)

#### 입력(필수)
- Jira 이슈 키
- 저장 대상 분류(메타 / 파일 / 벡터 / 캐시)
- 키 / 경로 규칙(예: `inspections/{inspection_id}/original.jpg`)
- 조회 요구(필터 / 정렬 / 페이지네이션 / 집계 항목)
- 정합성 요구(트랜잭션 범위, 실패 시 처리)
- 보관 / 삭제 정책(원본 저장 여부, 민감정보 여부)
- 환경변수 / 설정 변경 여부(있으면 README 갱신 필요)

#### 작업 절차
1. 데이터 분류(역할 분리) 및 매핑 확정
2. 스키마 / 키 규칙 설계 + 인덱스 / 집계 경로 고려
3. 저장 / 조회 구현(예외 처리 + request-id 로깅 포함)
4. 샘플 데이터로 조회 / 필터 / 집계 확인
5. 환경변수 / 실행 방법 변경 시 README + `.env.example` 갱신

#### 반드시 지킬 사항
- 역할 분리 준수(MariaDB / MinIO / ChromaDB / Redis)
- 민감정보 최소 저장(필요 시 마스킹 / 암호화)
- “어디에 무엇이 저장되는지”를 문서로 남긴다(키 / 스키마 규칙)
- 참조키 일관성을 유지한다
- 삭제 정책은 데이터 성격과 운영 정책에 맞게 적용한다
  - 업무 이력 / 감사 대상 데이터는 Soft Delete 또는 상태값 관리 우선
  - 캐시 / 세션 / 임시 데이터는 물리 삭제 가능
  - 파일 / 벡터 데이터는 메타 정책과 정합성을 맞춰 동기화한다
- 환경별 설정 분리
- 연결 재시도(Retry)는 제한적으로 적용한다

#### 산출물
- DDL / 스키마 변경 제안(필요 시)
- 저장 / 조회 코드 + 예시 쿼리
- 키 / 경로 / 컬렉션 규칙 문서
- README / `.env.example` 변경 사항(해당 시)

#### 검증 포인트
- 참조 무결성(메타 ↔ 파일 키) 유지
- 목록 / 대시보드 API 요구조건 충족
- 저장 실패 시 복구 / 추적 가능(로그 / 상태)
- 대량 조회 성능

---

### Skill 8) 공통 로깅 적용 스킬 (request-id 전파, 구조화 로그, 연동 추적)

#### 목적
장애 / 지연 원인을 request-id 기반으로 Spring ↔ FastAPI까지 관통 추적 가능하게 만든다.

#### 적용 대상
- 신규 API / 유스케이스 추가 시
- Spring ↔ FastAPI 연동 추가 / 수정 시
- AI 추론 / RAG / 파일 I/O처럼 실패 지점이 많은 흐름

#### 작업 범위
- 포함: request-id 생성 / 전파, Spring(Filter / Interceptor + Client 필터), FastAPI(middleware), 예외 로그 표준화
- 제외: 민감정보 로깅, 로그 포맷 임의 변경(기존 표준 우선)

#### 입력(필수)
- Jira 이슈 키
- request-id 헤더명(권장: `X-Request-Id`)
- 로그 필드 표준(예: requestId, endpoint, latencyMs, status, inspectionId, userId 등)
- 마스킹 / 금지 항목(토큰 / 키 / 비밀번호 / 문서 전문 / 원본 이미지 등)

#### 작업 절차
1. request-id 규칙 확정
   - 외부 요청에 없으면 Spring에서 생성
2. Spring 수신 로그 + FastAPI 호출 시 헤더 전파 + FastAPI 수신 로그
3. 예외 시 원인 / 맥락(단계, 주요 파라미터 요약) 로그 남기기
4. 필요 시 Problem Details 확장 필드로 `requestId` 포함 여부 결정(스펙 기준)

#### 반드시 지킬 사항
- 민감정보 로그 금지
- 예외 삼키기 금지(원인 / 맥락 기록)
- request-id 전파가 끊기지 않게 구현

#### 산출물
- Spring Filter / Client 필터, FastAPI middleware 코드
- 표준 로그 예시
- 추적 방법 문서(“requestId로 어디서 찾는지”)

#### 검증 포인트
- 동일 요청이 Spring / FastAPI에서 같은 requestId로 연결됨
- 장애 시 원인 파악 가능한 정보가 남음

---

### Skill 15) 배포 / 환경설정 스킬 (Docker Compose / .env / 헬스체크 / Nginx 라우팅)

#### 목적
개발 / 로컬 / 운영 환경에서 일관되게 실행되도록 배포 / 환경설정 규칙을 표준화하고, 실행 가이드(README)를 최신 상태로 유지한다.

#### 적용 대상
- Docker Compose 구성(서비스 분리: Spring / FastAPI / MariaDB / MinIO / ChromaDB / Redis / Nginx)
- 환경변수(.env) 및 설정 파일(application-*.yml 등)
- Nginx 라우팅 / 리버스 프록시 / 정적 리소스 서빙
- 헬스체크 엔드포인트 및 서비스 기동 순서 가이드
- Ubuntu 22.04 배포 환경 및 WSL2 로컬 개발 환경

#### 작업 범위
- 포함: compose 파일 / 네트워크 / 볼륨, `.env.example`, 헬스체크, Nginx 라우팅, CORS allowlist 기본값, README 업데이트
- 제외: 클라우드 전용 인프라(terraform / k8s) (요구 없으면)

#### 입력(필수)
- Jira 이슈 키
- 대상 환경(local / dev / prod) 및 필요한 서비스 목록
- 포트 / 도메인 / 라우팅 규칙(Nginx)
- 필수 환경변수 목록(MariaDB / MinIO / ChromaDB / Redis / 모델 경로 등)
- 헬스체크 엔드포인트 정의(각 서비스)

#### 작업 절차
1. Docker Compose
   - 서비스별 컨테이너 분리, 내부 네트워크 구성
   - 데이터 저장을 위한 볼륨 설정(MariaDB / MinIO 등)
2. 환경변수
   - `.env`는 커밋 금지
   - `.env.example`에 필수 키 정리
3. Nginx
   - `/api` → Spring, `/ai`(또는 내부) → FastAPI 등 라우팅 규칙 정의
   - CORS는 운영에서 `*` 금지(allowlist)
4. 헬스체크
   - Spring / FastAPI health endpoint 정의 및 compose healthcheck 설정(가능 범위)
5. 문서화
   - 실행 방법 / 순서 / 필수 의존성 / 초기화(버킷 생성 등)를 README에 반영
   - WSL2 로컬 개발 환경과 Ubuntu 22.04 배포 환경 차이점 반영
6. 스모크
   - compose up → 헬스체크 통과 → 기본 API 호출 확인

#### 반드시 지킬 사항
- 민감정보(.env, 키, 비밀번호) 커밋 금지
- 운영 CORS `*` 금지(allowlist)
- 헬스체크 / 로그로 장애 원인 파악 가능해야 함
- Redis는 캐시 / 세션 / 작업 상태 관리 용도로 구성한다
- 변경 시 README / `.env.example`를 반드시 갱신한다

#### 산출물
- docker-compose(.yml) 변경안
- Nginx 설정 파일(라우팅 / 프록시)
- `.env.example` 업데이트
- README 실행 가이드 업데이트
- 스모크 테스트 절차

#### 검증 포인트
- 팀원이 동일 절차로 로컬에서 재현 가능
- 서비스 간 통신(Spring ↔ FastAPI, MariaDB / MinIO / ChromaDB / Redis) 정상
- 설정 누락 / 포트 충돌 / 기동 순서 문제 최소화
```
