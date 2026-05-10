# Industrial AI Platform

## MVP 탐지 범위

- 업로드 탐지는 현재 MVP에서 이미지 파일만 지원합니다. 허용 형식은 `jpg`, `jpeg`, `png`, `webp`입니다.
- 영상 업로드, 지속 스트리밍, 자동 주기 프레임 업로드는 이번 MVP 프론트 범위에서 제외합니다.
- 실시간 탐지 화면은 브라우저 카메라 프리뷰를 유지하지만, 검사 방식은 `버튼 클릭 시 현재 프레임 1장 캡처 -> 업로드 검사 API 재사용`입니다.
- 실제 설비 환경에서는 이 버튼 클릭이 향후 센서 또는 PLC 트리거로 대체될 수 있습니다.

AI 기반 설비 점검 보조 시스템 프로젝트입니다.
이 문서는 **프로젝트 입구(문서 지도 + 실행 진입점)** 역할만 담당합니다.

## 빠른 시작

처음 clone 받은 뒤에는 아래 순서로 한 번에 준비하면 됩니다.

### 1. 환경 변수 파일 만들기

```powershell
Copy-Item .\infra\.env.example .\infra\.env
Copy-Item .\backend-spring\.env.example .\backend-spring\.env
Copy-Item .\frontend\.env.example .\frontend\.env
Copy-Item .\ai-server\.env.example .\ai-server\.env
```

### 2. 인프라 실행 + DB 준비

```powershell
cd .\infra
docker compose --env-file .env up -d
.\scripts\db-migrate.ps1
.\scripts\db-seed.ps1
docker compose ps
cd ..
```

`docker-compose`는 DB를 자동 생성하지 않으므로 처음 실행 시 `db-migrate.ps1`과 `db-seed.ps1`까지 같이 실행하는 것을 권장합니다.

한글 시드 데이터가 프론트에서 깨져 보이면, 예전에 잘못된 문자셋으로 들어간 데이터가 남아 있는 경우가 많습니다. 이때는 아래 순서로 DB를 다시 만든 뒤 시드를 재적용합니다.

```powershell
cd .\infra
.\scripts\db-reset.ps1 -Force
.\scripts\db-migrate.ps1
.\scripts\db-seed.ps1
docker compose ps
cd ..
```

주의: `db-reset.ps1 -Force`는 현재 로컬 DB 데이터를 삭제합니다.

### 3. 각 시스템 실행 예시

Backend(Spring):

```powershell
cd .\backend-spring
.\gradlew.bat bootRun --args="--spring.profiles.active=local"
```

Frontend:

```powershell
cd .\frontend
npm install
npm run dev
```

AI Server(FastAPI):

```powershell
cd .\ai-server
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
uvicorn main:app --reload --host 0.0.0.0 --port 8001
```

## 시스템별 실행 문서

- **팀원용: 로컬 개발 실행 / 배포용 로컬 Docker fullstack** — [`docs/project/테스트실행가이드.md`](docs/project/테스트실행가이드.md) · 구 경로 [`team-test-guide.md`](docs/project/team-test-guide.md) 는 동 문서로 리다이렉트
- 인프라: [`infra/README.md`](infra/README.md)
- Backend(Spring): [`backend-spring/README.md`](backend-spring/README.md)
- Frontend: [`frontend/README.md`](frontend/README.md)
- AI Server(FastAPI): [`ai-server/README.md`](ai-server/README.md)

## 문서 지도

문서의 기준 경로와 역할은 [`docs/README.md`](docs/README.md)에서 먼저 확인합니다.

- 프로젝트 정책/요구/설계: `docs/project/`
- 권한/역할 기준(SSOT): `docs/auth/api-authority-matrix.md`
- DB 초기화/마이그레이션/시드: `docs/db/`

## 권한 기준 (요약)

권한 관련 단일 기준 문서는 다음입니다.

- [`docs/auth/api-authority-matrix.md`](docs/auth/api-authority-matrix.md)

## Nginx 단일 진입점

중간발표 Cloudflare Tunnel과 최종발표 집컴 도메인/DDNS 배포는 같은 Nginx 게이트웨이를 공통으로 사용합니다.

```text
외부 사용자
  ↓
Cloudflare Tunnel 또는 도메인/DDNS
  ↓
Nginx
  ├─ /      → Frontend
  └─ /api   → Spring Boot
```

- 로컬 개발용 Nginx는 보통 `80`을 쓰고, **배포용 `docker-compose.prod.yml` 로컬 실행**은 **`https://localhost`(443)** 기준인 경우가 많습니다. [`docs/project/테스트실행가이드.md`](docs/project/테스트실행가이드.md) 를 참고합니다.
- FastAPI, MariaDB, Redis, MinIO, ChromaDB는 외부에 직접 공개하지 않습니다.
- 프론트 API base URL 기본값은 `/api/v1` 상대경로입니다.
- 자세한 실행 방법은 [`infra/README.md`](infra/README.md), 포트 정책은 [`docs/project/포트정리.md`](docs/project/포트정리.md)를 따릅니다.

## 운영 fullstack Compose (Docker)

- **실행 모드 (배포 초안 단일 기준 SSOT)**: `nginx`, `frontend`, `spring`, `ai-server`, `mariadb`, `redis`, `minio`, `chroma` 를 한 스택으로 기동함.
- Compose 네트워크에서는 peer 접속에 **`localhost` 를 쓰지 않고 Compose 서비스명**만 사용함.
- **환경 변수**: 커밋 대상 예시만 [`infra/.env.prod.example`](infra/.env.prod.example). 실 배포 파일 `infra/.env.prod` 및 비밀은 **커밋하지 않음**.
- **설명 표·경로 검증 요약**: [`docs/project/docker-compose-prod.md`](docs/project/docker-compose-prod.md)
- Compose 파일: [`infra/docker-compose.prod.yml`](infra/docker-compose.prod.yml)

다른 문서에서는 권한 상세를 중복 설명하지 않고 위 문서를 참조합니다.
