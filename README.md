# Industrial AI Platform

AI 기반 설비 점검 보조 시스템입니다. 비전 이상 탐지 결과, RAG 문서 검색, LLM 설명, 검사 이력/통계를 결합해 현장 작업자가 설비 상태를 빠르게 판단하도록 돕는 것을 목표로 합니다.

## 현재 구성

| 영역 | 스택 / 역할 |
| --- | --- |
| Frontend | React 18, Vite 5, TypeScript, Tailwind CSS |
| Backend | Spring Boot 2.7.18, JDK 17, Spring Security, Spring Data JPA |
| AI Server | Python 3.10.6, FastAPI 0.115, Pydantic 2.x |
| DB / Infra | MariaDB 10.6, Redis 7, MinIO, ChromaDB, Docker Compose |

> 프로젝트 표준 목표는 Spring Boot 3.x이지만, 현재 코드베이스는 Spring Boot 2.7.18 기준으로 동작합니다. 업그레이드는 별도 작업으로 분리합니다.

## 디렉터리

```text
ai-server/        FastAPI AI/RAG 서버
backend-spring/   Spring Boot 메인 API 서버
frontend/         React 웹 클라이언트
infra/            MariaDB, Redis, MinIO, ChromaDB Docker Compose
docs/             권한, DB, 설계/운영 문서
```

## 사전 준비

- JDK 17
- Node.js 20.x 권장
- Python 3.10.6
- Docker Desktop 또는 Docker Engine 24.x 이상
- DBeaver 또는 MariaDB 접속 도구

## 1. 환경 변수 준비

민감정보는 실제 `.env`에만 작성하고 Git에 커밋하지 않습니다.

```powershell
Copy-Item infra\.env.example infra\.env
Copy-Item backend-spring\.env.example backend-spring\.env
Copy-Item frontend\.env.example frontend\.env
Copy-Item ai-server\.env.example ai-server\.env
```

로컬 기본 포트:

| 서비스 | URL / Port |
| --- | --- |
| Frontend | `http://localhost:5173` |
| Spring API | `http://localhost:8080/api/v1` |
| FastAPI | `http://localhost:8001/ai/v1` |
| MariaDB | `localhost:3307` |
| Redis | `localhost:6379` |
| MinIO | `http://localhost:9001` |
| ChromaDB | `http://localhost:8000` |

## 2. 인프라 실행

```powershell
cd infra
docker compose --env-file .env up -d
docker ps
```

중지:

```powershell
cd infra
docker compose down
```

볼륨까지 삭제하면 로컬 DB/스토리지 데이터가 삭제됩니다.

```powershell
cd infra
docker compose down -v
```

## 3. MariaDB 스키마 초기화

현재 `infra/docker-compose.yml`은 init SQL을 자동 마운트하지 않습니다. 최초 1회 또는 재초기화 시 SQL을 수동 실행합니다.

```powershell
cd infra
Get-Content .\mariadb\init\industrial-ai-platform.sql -Raw |
  docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -pchange_me_root_password industrial_ai
```

가입 화면의 공개 조직 목록을 보려면 샘플 조직도 넣습니다.

```powershell
cd infra
Get-Content .\mariadb\seed\seed-sample-organizations.sql -Raw |
  docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -pchange_me_root_password industrial_ai
```

자세한 절차는 [docs/db/mariadb-schema-init.md](docs/db/mariadb-schema-init.md)를 참고하세요.

## 4. Backend 실행

```powershell
cd backend-spring
.\gradlew.bat bootRun --args="--spring.profiles.active=local"
```

검증:

```powershell
.\gradlew.bat compileJava
.\gradlew.bat test
```

주요 엔드포인트:

- `GET /api/v1/health`
- `GET /api/v1/health/infra`
- `POST /api/v1/auth/google`
- `GET /api/v1/documents`
- `POST /api/v1/documents`

## 5. Frontend 실행

```powershell
cd frontend
npm install
npm run dev
```

검증:

```powershell
npm run build
npm run lint
```

현재 문서 관리 화면은 다음 라우트를 사용합니다.

- `/documents`
- `/documents/new`
- `/documents/:documentId/edit`

등록/수정은 같은 `DocumentFormPage`와 `DocumentForm`을 재사용합니다.

## 6. AI Server 실행

```powershell
cd ai-server
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
uvicorn main:app --reload --host 0.0.0.0 --port 8001
```

확인:

- `http://localhost:8001/docs`
- `http://localhost:8001/ai/v1/health`

## 7. 인증/권한 기준

권한 기준은 [docs/auth/api-authority-matrix.md](docs/auth/api-authority-matrix.md)를 단일 기준으로 봅니다.

현재 주요 Role:

- `ROLE_SITE_ADMIN`
- `ROLE_COMPANY_ADMIN`
- `ROLE_COMPANY_WORKER`

가입 요청은 기본적으로 `PENDING` 상태와 `ROLE_COMPANY_WORKER` 역할로 생성되고, 사이트 관리자가 승인/거절합니다.

## 8. 문서/RAG 운영 기준

문서 업로드 MVP 기준:

- 허용 확장자: `pdf`, `docx`, `md`
- 업로드 위치: MinIO `documents` 버킷
- 메타데이터: MariaDB `document`, `document_version`, `document_tag`
- 인덱싱 상태: `PENDING`, `PROCESSING`, `COMPLETED`, `FAILED`
- 벡터/청크: `chunk`, `vector_index`, ChromaDB

문서 등록/수정 UI는 기존 파일 정보, 인덱싱 상태, 미리보기 영역을 표시합니다. 현재 백엔드가 텍스트 스니펫 API를 제공하지 않으므로 미리보기는 `GET /api/v1/files/{fileId}/preview` 기반으로 새 탭에서 확인합니다.

## 9. 자주 나는 문제

### Spring 실행 시 테이블이 없다고 나옴

`application-local.yml`은 `spring.jpa.hibernate.ddl-auto=validate`입니다. Hibernate가 테이블을 만들지 않으므로 MariaDB init SQL을 먼저 실행해야 합니다.

### 가입 페이지 조직 목록이 비어 있음

샘플 조직 seed를 실행했는지 확인합니다.

```powershell
cd infra
Get-Content .\mariadb\seed\seed-sample-organizations.sql -Raw |
  docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -pchange_me_root_password industrial_ai
```

### 파일 업로드가 실패함

프론트와 백엔드 모두 `pdf`, `docx`, `md` 기준입니다. 브라우저가 `.md` 파일을 `text/plain`으로 보낼 수 있어 백엔드는 `text/markdown`, `text/plain`을 모두 허용합니다.

### 로컬 로그가 Git에 잡힘

`bootrun-*.txt`, `*.log`, `.env`, `dist`, `node_modules`, `.venv`, `build`는 커밋하지 않습니다. 커밋 전 확인:

```powershell
git status --short --ignored
git diff --check
```
