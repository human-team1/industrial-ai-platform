# Industrial AI Platform

산업 설비/품목 점검을 지원하는 이상 탐지 플랫폼입니다.
이미지 기반 비전 이상 탐지 결과와 설비 문서 RAG, LLM 요약을 결합해 작업자에게 판정 근거, 대응 절차, 점검 이력을 제공합니다.

## 목차

1. [프로젝트 목적](#프로젝트-목적)
2. [현재 MVP 구현 범위](#현재-mvp-구현-범위)
3. [주요 기능](#주요-기능)
4. [기술 스택](#기술-스택)
5. [시스템 아키텍처](#시스템-아키텍처)
6. [프로젝트 구조](#프로젝트-구조)
7. [주요 화면](#주요-화면)
8. [실행 방법](#실행-방법)
9. [로컬 접속 정보 / 포트](#로컬-접속-정보--포트)
10. [실행 확인 체크리스트](#실행-확인-체크리스트)
11. [API 개요](#api-개요)
12. [모델 및 AI 파이프라인 개요](#모델-및-ai-파이프라인-개요)
13. [문서 지도](#문서-지도)
14. [협업 및 개발 컨벤션](#협업-및-개발-컨벤션)
15. [참고 / 부록](#참고--부록)

## 프로젝트 목적

산업 현장에서 발생하는 설비 이상, 품목 결함, 점검 이슈를 웹 기반으로 관리하기 위한 MVP입니다.

- 비전 모델로 이미지 이상 여부를 판정하고 anomaly score와 시각화 결과를 제공합니다.
- 검사 요청, 결과, 재검토 상태, 사용자 피드백을 이력으로 관리합니다.
- 설비 매뉴얼/점검 기준 문서를 RAG로 검색해 근거 있는 대응 가이드를 제공합니다.
- 일반 사용자와 시스템 관리자 권한을 분리해 운영 기능을 제공합니다.

## 현재 MVP 구현 범위

현재 README는 “완성 제품 설명서”가 아니라 저장소 진입점입니다. 구현 상태와 확장 예정 범위를 구분해 봅니다.

| 구분 | 범위 |
| --- | --- |
| 구현 범위 | 이미지 업로드 기반 이상 탐지, 브라우저 카메라 단건 캡처 후 검사 요청, 검사 결과 조회/상세, 문서 업로드/인덱싱/RAG 검색, 챗봇 응답, 운영 모니터링, 모델 버전/배포 관리 기반 구조 |
| 부분 구현/검증 필요 | 운영 환경 배포 자동화, 모델 아티팩트 seed, 실제 설비별 모델 품질 검증, RAG 문서 품질 평가, 관리자 운영 플로우 E2E 검증 |
| 확장 예정 | 영상 전체 분석, 지속 스트리밍, PLC/센서 트리거, 외부 알림, 자동 보고서, 고급 APM/분산 추적, 대규모 멀티 테넌시 |

세부 기능 정의와 우선순위는 [기능 정의서](docs/project/기능정의.md)와 [정책 정의서](docs/project/정책정의.md)를 기준으로 확인합니다.

## 주요 기능

- 이상 탐지: 이미지 기반 검사 요청, AI 추론, 판정/점수/시각화 결과 관리
- 점검 지원: 설비 문서 인덱싱, RAG 검색, 출처 기반 LLM 답변
- 운영 관리: 검사 이력, 대시보드, 재검토 큐, 모델 버전/배포 상태 관리
- 권한 관리: 사용자 로그인, 가입 승인, 일반 사용자/관리자 역할 분리

## 기술 스택

| 영역 | 기술 |
| --- | --- |
| Frontend | React 18.3, Vite 5.4, TypeScript 5.6, Tailwind CSS 3.4, React Router 6, Axios |
| Spring Backend | Java 17, Spring Boot 2.7.18, Spring Security, Spring Data JPA, Spring Data Redis, MariaDB Driver, MinIO Java Client, JJWT, PDFBox |
| FastAPI AI Server | Python 3.10, FastAPI 0.115, Pydantic 2.9, Uvicorn, PyTorch 2.6, Torchvision, Anomalib 2.4, LangChain, LangGraph, ChromaDB Client, Redis Client, MinIO Client |
| Data / Infra | MariaDB 10.6, Redis 7, MinIO, ChromaDB 0.5, Nginx, Docker Compose |
| DevOps | GitHub Actions, Windows self-hosted runner, Dockerfile, 운영용 Docker Compose |

후속 연결 또는 환경 의존 항목:

- Ollama/LLM 런타임은 `ai-server/.env.example`에 설정값이 있으며, 실제 모델 실행 환경 준비가 필요합니다.
- 외부 알림, 자동 보고서, 고급 모니터링은 확장 예정 범위입니다.

## 시스템 아키텍처

<!-- TODO: 아키텍처 이미지 추가 -->

```text
사용자 브라우저
  -> Frontend(React/Vite)
  -> Nginx
  -> Spring API
     -> MariaDB: 사용자, 검사, 문서 메타데이터, 이력
     -> Redis: 세션, 캐시, 작업 상태
     -> MinIO: 원본 이미지, 시각화 결과, 문서 파일
     -> FastAPI AI Server
        -> 비전 추론
        -> RAG 검색/문서 인덱싱
        -> ChromaDB 벡터 검색
        -> LLM 응답 정리
```

원칙적으로 외부 클라이언트의 API 진입점은 Spring Backend입니다. FastAPI는 AI 전용 내부 서비스로 두고, 권한/비즈니스 규칙/이력 관리는 Spring이 담당합니다.

## 프로젝트 구조

```text
industrial-ai-platform/
  frontend/          React + Vite 클라이언트
  backend-spring/    Spring Boot 메인 API 서버
  ai-server/         FastAPI AI/RAG 서버
  infra/             Docker Compose, Nginx, MariaDB 초기화, 운영 스크립트
  docs/              기능/정책/API/ERD/환경/컨벤션 문서
  .github/           GitHub Actions 워크플로우
```

세부 디렉터리 원칙은 [디렉터리 구조](docs/project/디렉터리구조.md)를 참고합니다.

## 주요 화면

<!-- TODO: 주요 화면 스크린샷 추가 -->

| 화면 | 목적 |
| --- | --- |
| 로그인 / 가입 신청 | 사용자 인증과 신규 계정 승인 요청을 처리합니다. |
| 검사 업로드 | 이미지 파일을 업로드해 이상 탐지 검사를 요청합니다. |
| 실시간 검사 진입 | 브라우저 카메라 프리뷰에서 현재 프레임 1장을 캡처해 검사합니다. |
| 검사 결과 목록 / 상세 | 판정, 점수, 시각화 결과, 모델 정보, 재검토 상태를 확인합니다. |
| 대시보드 | 검사 건수, 불량/재검사 비율, 운영 지표를 요약합니다. |
| 문서 목록 / 등록 / 수정 | 설비 문서를 관리하고 인덱싱 상태를 확인합니다. |
| 챗봇 / RAG 질의응답 | 검사 결과와 문서 출처를 바탕으로 대응 가이드를 제공합니다. |
| 관리자 운영 모니터링 | 서비스 상태, 작업 상태, 운영 지표를 확인합니다. |
| 재검토 큐 / 설정 | 재검토 대상 결과와 정책/임계값 관련 운영 기능을 다룹니다. |

<!-- TODO: 대시보드 스크린샷 추가 -->
<!-- TODO: 검사 결과 상세 스크린샷 추가 -->
<!-- TODO: 문서/RAG 화면 스크린샷 추가 -->
<!-- TODO: 관리자 운영 모니터링 스크린샷 추가 -->

화면 목록과 와이어프레임은 [페이지 목록](docs/project/페이지목록.md)과 `docs/project/wireframes/`를 참고합니다.

## 실행 방법

아래 절차는 Windows PowerShell 기준 로컬 개발 실행 순서입니다.

### 1. 필수 도구 준비

- JDK 17
- Node.js 20 권장
- Python 3.10.6
- Docker Desktop 또는 Docker Engine
- Git

### 2. 환경 변수 파일 생성

```powershell
Copy-Item .\infra\.env.example .\infra\.env
Copy-Item .\backend-spring\.env.example .\backend-spring\.env
Copy-Item .\frontend\.env.example .\frontend\.env
Copy-Item .\ai-server\.env.example .\ai-server\.env
```

실제 비밀번호와 토큰은 `.env`에만 작성하고 커밋하지 않습니다.

### 3. 인프라 실행 및 DB 초기화

```powershell
cd .\infra
docker compose --env-file .env up -d
.\scripts\db-migrate.ps1
.\scripts\db-seed.ps1
docker compose ps
cd ..
```

로컬 DB를 완전히 재생성해야 할 때만 아래 명령을 사용합니다.

```powershell
cd .\infra
.\scripts\db-reset.ps1 -Force
cd ..
```

### 4. FastAPI AI Server 실행

```powershell
cd .\ai-server
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
uvicorn main:app --reload --host 0.0.0.0 --port 8001
```

### 5. Spring Backend 실행

```powershell
cd .\backend-spring
.\gradlew.bat bootRun --args="--spring.profiles.active=local"
```

### 6. Frontend 실행

```powershell
cd .\frontend
npm install
npm run dev
```

운영용 Docker fullstack 실행은 [운영 compose 가이드](docs/project/docker-compose-prod.md)와 [Infra README](infra/README.md)를 참고합니다.

## 로컬 접속 정보 / 포트

로컬 개발 기준 포트입니다. 값은 `.env.example`, `docker-compose.yml`, 모듈 설정 파일을 기준으로 확인했습니다.

| 대상 | 로컬 주소 | 기준 파일 | 비고 |
| --- | --- | --- | --- |
| Nginx Gateway | `http://localhost` 또는 `http://localhost:80` | `infra/docker-compose.yml`, `infra/nginx/conf.d/default.conf` | `/`는 Frontend, `/api/`는 Spring으로 프록시 |
| Frontend | `http://localhost:5173` | `frontend/package.json`, `infra/nginx/conf.d/default.conf` | Vite dev server 기본 포트 |
| Spring API | `http://localhost:8080/api/v1` | `backend-spring/.env.example`, `application.yml` | health: `/api/v1/health` |
| FastAPI AI Server | `http://localhost:8001` | `ai-server/.env.example` | health: `/ai/v1/health` |
| MinIO API | `http://localhost:9000` | `infra/docker-compose.yml`, `backend-spring/.env.example` | 내부 파일 저장소 |
| MinIO Console | `http://localhost:9001` | `infra/docker-compose.yml` | 로컬 관리 콘솔 |
| ChromaDB | `http://localhost:8000` | `infra/docker-compose.yml`, `.env.example` | heartbeat: `/api/v1/heartbeat` |
| MariaDB | `localhost:3307` | `infra/docker-compose.yml`, `backend-spring/.env.example` | DB name 기본값: `industrial_ai` |
| Redis | `localhost:6379` | `infra/docker-compose.yml`, `.env.example` | 세션/캐시/작업 상태 |

## 실행 확인 체크리스트

- [ ] `infra`에서 `docker compose --env-file .env up -d` 후 `docker compose ps`로 MariaDB, Redis, MinIO, ChromaDB 상태 확인
- [ ] `http://localhost:8080/api/v1/health`로 Spring health check 확인
- [ ] `http://localhost:8001/ai/v1/health`로 FastAPI health check 확인
- [ ] `http://localhost:5173` 또는 `http://localhost`로 Frontend 접속 확인
- [ ] 팀 테스트 가이드의 계정/시드 기준으로 로그인 가능 여부 확인
- [ ] 이미지 업로드 검사 요청 후 결과가 `PROCESSING -> COMPLETED/FAILED`로 전이되는지 확인
- [ ] 문서 업로드 후 인덱싱 상태와 RAG 질의응답 가능 여부 확인

상세 검증 절차는 [테스트 실행 가이드](docs/project/테스트실행가이드.md)와 [팀 테스트 가이드](docs/project/team-test-guide.md)를 참고합니다.

## API 개요

외부 API 기본 prefix는 `/api/v1`입니다.

- 인증/사용자: 로그인, 토큰 갱신, 내 정보, 가입 신청
- 검사: 이미지 업로드, 검사 상태 조회, 결과 목록/상세 조회
- 문서: 문서 업로드, 버전 관리, 인덱싱 요청, 미리보기
- RAG/챗봇: 검사 결과 문맥 기반 질의응답, 출처 포함 검색 결과
- 관리자: 가입 승인, 운영 모니터링, 재검토 처리, 정책 관리
- 모델 관리: 모델 등록, 버전/아티팩트/배포 상태 관리

응답 형식은 성공 시 `{ "success": true, "data": ..., "message": "..." }`를 기본으로 하며, 오류 응답은 Problem Details 형식을 따릅니다. 상세 API는 [API 명세서](docs/project/API.md)를 확인합니다.

## 모델 및 AI 파이프라인 개요

```text
이미지 업로드
  -> Spring 검사 요청/파일 저장
  -> FastAPI 비전 추론
  -> anomaly score, 판정, 시각화 산출물 생성
  -> Spring 결과 저장
  -> 결과 상세/RAG/LLM 설명 제공
```

RAG 파이프라인은 문서 업로드 후 청킹, 임베딩, ChromaDB 저장을 수행하고, 질의 시 관련 문서 조각과 출처를 함께 반환합니다. AI 서버의 세부 구조와 실행 방법은 [AI Server README](ai-server/README.md)를 참고합니다.

## 문서 지도

| 문서 | 위치 |
| --- | --- |
| 프로젝트 개요 | [docs/project/프로젝트개요.md](docs/project/프로젝트개요.md) |
| 기능 정의서 | [docs/project/기능정의.md](docs/project/기능정의.md) |
| 정책 정의서 | [docs/project/정책정의.md](docs/project/정책정의.md) |
| API 명세서 | [docs/project/API.md](docs/project/API.md) |
| ERD | [docs/project/ERD.md](docs/project/ERD.md) |
| 디렉터리 구조 | [docs/project/디렉터리구조.md](docs/project/디렉터리구조.md) |
| 개발환경 설정 | [docs/project/개발환경.md](docs/project/개발환경.md) |
| 컨벤션 | [docs/project/컨벤션.md](docs/project/컨벤션.md) |
| 테스트 실행 가이드 | [docs/project/테스트실행가이드.md](docs/project/테스트실행가이드.md) |
| 팀 테스트 가이드 | [docs/project/team-test-guide.md](docs/project/team-test-guide.md) |
| 운영 Docker Compose | [docs/project/docker-compose-prod.md](docs/project/docker-compose-prod.md) |
| 포트 정리 | [docs/project/포트정리.md](docs/project/포트정리.md) |
| 권한 매트릭스 | [docs/auth/api-authority-matrix.md](docs/auth/api-authority-matrix.md) |
| DB 초기화 | [docs/db/mariadb-schema-init.md](docs/db/mariadb-schema-init.md) |

## 협업 및 개발 컨벤션

- 브랜치 전략: `main` 배포, `develop` 통합, `feat/*`, `fix/*`, `docs/*` 등 작업 브랜치 사용
- 커밋 메시지: `type: 작업 내용` 형식 사용
- Frontend: FSD 구조를 따르고 API 호출은 `shared/api` 또는 feature API 계층에 둡니다.
- Spring: Hexagonal Architecture 기준으로 Controller, UseCase, Port, Adapter 책임을 분리합니다.
- FastAPI: Router, Application, Domain, Infrastructure, Container 계층을 분리합니다.
- API 계약 변경은 문서/DTO/요청·응답 영향도를 먼저 확인한 뒤 반영합니다.
- `.env`, `.env.*`, 인증서, API Key, DB 비밀번호, Google OAuth Client Secret, JWT Secret, 모델 대용량 파일(`*.pt`, `*.onnx`, `*.ckpt`)은 Git에 커밋하지 않습니다.
- 공유가 필요한 설정은 실제 값이 없는 `.env.example`에만 반영합니다.

상세 규칙은 [컨벤션 문서](docs/project/컨벤션.md)를 참고합니다.

## 참고 / 부록

- [Frontend README](frontend/README.md)
- [Backend Spring README](backend-spring/README.md)
- [AI Server README](ai-server/README.md)
- [Infra README](infra/README.md)
- [Docs Index](docs/README.md)

운영 배포 전에는 `git status`, 변경 파일, 환경 변수 템플릿, DB 마이그레이션, Docker Compose 설정, 민감정보 포함 여부를 함께 확인합니다.
