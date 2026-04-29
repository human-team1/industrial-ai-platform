# industrial-ai-platform

AI 기반 설비 점검 보조 시스템입니다. 로컬 개발은 아래 순서로 실행합니다.

- Infra: MariaDB, Redis, MinIO, ChromaDB
- Backend: Spring Boot API
- AI Server: FastAPI
- Frontend: React + Vite

## 1. 필수 설치 파일

아래 프로그램을 먼저 설치합니다.

| 항목 | 권장 버전 | 용도 |
| --- | --- | --- |
| Git | 최신 안정 버전 | 저장소 clone / branch 관리 |
| Docker Desktop | Docker 24.x 이상 | MariaDB, Redis, MinIO, ChromaDB 실행 |
| JDK | 17 | Spring Boot 실행 |
| Python | 3.10.6 | FastAPI AI 서버 실행 |
| Node.js | 20.x | Frontend 실행 |
| DBeaver | 최신 안정 버전 | MariaDB 접속 / ERD 확인 |

다운로드:

- Git: https://git-scm.com/download/win
- Docker Desktop: https://www.docker.com/products/docker-desktop/
- JDK 17: https://adoptium.net/temurin/releases/?version=17
- Python 3.10.6: https://www.python.org/downloads/release/python-3106/
- Node.js: https://nodejs.org/
- DBeaver: https://dbeaver.io/download/

설치 확인:

```powershell
git --version
docker --version
docker compose version
java -version
javac -version
python --version
pip --version
node -v
npm -v
```

## 2. 프로젝트 받기

```powershell
git clone <REPOSITORY_URL>
cd industrial-ai-platform
```

프로젝트 구조:

```text
industrial-ai-platform/
  infra/            # MariaDB, Redis, MinIO, ChromaDB Docker Compose
  backend-spring/   # Spring Boot API 서버
  ai-server/        # FastAPI AI/RAG 서버
  frontend/         # React + Vite 클라이언트
  docs/             # 프로젝트 문서
```

## 3. 환경 파일 생성

루트에서 아래 명령을 실행합니다.

```powershell
Copy-Item infra\.env.example infra\.env
Copy-Item backend-spring\.env.example backend-spring\.env
Copy-Item ai-server\.env.example ai-server\.env
Copy-Item frontend\.env.example frontend\.env
```

주의:

- `.env` 파일은 실제 비밀번호/토큰이 들어갈 수 있으므로 커밋하지 않습니다.
- 기본 로컬 실행은 `.env.example` 값 그대로 복사해도 동작하도록 맞춰져 있습니다.
- 비밀번호를 바꾸면 `infra/.env`, `backend-spring/.env`, `ai-server/.env`의 MinIO/DB 값도 같이 맞춥니다.

## 4. env 값 예시

### infra/.env

```env
MARIADB_ROOT_PASSWORD=change_me_root_password
MARIADB_DATABASE=industrial_ai
MARIADB_USER=industrial_user
MARIADB_PASSWORD=change_me_user_password

REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=

MINIO_ROOT_USER=minioadmin
MINIO_ROOT_PASSWORD=change_me_minio_password
MINIO_ENDPOINT=http://minio:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=change_me_minio_password
MINIO_BUCKET_DOCUMENTS=documents
MINIO_BUCKET_INSPECTION_ARTIFACTS=inspection-artifacts
MINIO_BUCKET_REPORTS=reports
MINIO_BUCKET_MODELS=models

CHROMA_HOST=chromadb
CHROMA_PORT=8000
CHROMA_COLLECTION_DOCUMENTS=industrial_document_chunks
```

### backend-spring/.env

```env
APP_NAME=factory-guard-api
APP_ENV=local
APP_PORT=8080

DB_HOST=localhost
DB_PORT=3307
DB_NAME=industrial_ai
DB_USER=industrial_user
DB_PASSWORD=change_me_user_password

REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

MINIO_ENDPOINT=http://localhost:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=change_me_minio_password
MINIO_BUCKET_DOCUMENTS=documents
MINIO_BUCKET_INSPECTION_ARTIFACTS=inspection-artifacts
MINIO_BUCKET_REPORTS=reports
MINIO_BUCKET_MODELS=models
MINIO_SECURE=false
MINIO_AUTO_CREATE_BUCKETS=true

CHROMA_HOST=localhost
CHROMA_PORT=8000
CHROMA_COLLECTION_DOCUMENTS=industrial_document_chunks
AI_SERVER_BASE_URL=http://localhost:8001

JWT_SECRET=change_me_jwt_secret
JWT_EXPIRE_MINUTES=60
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
```

### ai-server/.env

```env
APP_NAME=industrial-ai-server
APP_ENV=local
APP_PORT=8001

CHROMA_HOST=localhost
CHROMA_PORT=8000
CHROMA_COLLECTION_DOCUMENTS=industrial_document_chunks

MINIO_ENDPOINT=http://localhost:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=change_me_minio_password
MINIO_BUCKET_DOCUMENTS=documents
MINIO_BUCKET_INSPECTION_ARTIFACTS=inspection-artifacts
MINIO_BUCKET_REPORTS=reports
MINIO_BUCKET_MODELS=models
MINIO_SECURE=false

REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

MODEL_NAME=anomaly-baseline
EMBEDDING_MODEL_NAME=
LLM_MODEL_NAME=
LOG_LEVEL=INFO
TZ=Asia/Seoul
```

### frontend/.env

```env
VITE_APP_NAME=Industrial AI Platform
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_AI_API_BASE_URL=http://localhost:8001/ai/v1
VITE_GOOGLE_CLIENT_ID=
```

## 5. Infra 실행

Docker Desktop을 먼저 실행한 뒤 진행합니다.

```powershell
cd infra
docker compose --env-file .env up -d
docker ps
```

기본 포트:

| 서비스 | URL / Port |
| --- | --- |
| MariaDB | `localhost:3307` |
| Redis | `localhost:6379` |
| MinIO API | `http://localhost:9000` |
| MinIO Console | `http://localhost:9001` |
| ChromaDB | `http://localhost:8000` |

MinIO Console 로그인:

- ID: `infra/.env`의 `MINIO_ROOT_USER`
- PW: `infra/.env`의 `MINIO_ROOT_PASSWORD`

Infra 중지:

```powershell
cd infra
docker compose down
```

로컬 데이터를 모두 삭제하고 다시 시작해야 할 때만 사용합니다.

```powershell
cd infra
docker compose down -v
```

## 6. MariaDB 테이블 생성

테이블 생성 SQL 파일:

```text
infra/mariadb/init/industrial-ai-platform.sql
```

현재 `docker-compose.yml`은 MariaDB init SQL을 자동 마운트하지 않습니다. 따라서 최초 1회 수동 실행합니다.

```powershell
cd infra
Get-Content .\mariadb\init\industrial-ai-platform.sql -Raw | docker exec -i industrial-mariadb sh -c 'mariadb -uindustrial_user -p"$MARIADB_PASSWORD" industrial_ai'
```

테이블 생성 확인:

```powershell
docker exec -it industrial-mariadb mariadb -uindustrial_user -p industrial_ai
```

MariaDB 접속 후:

```sql
SHOW TABLES;

SELECT COUNT(*) AS table_count
FROM information_schema.tables
WHERE table_schema = 'industrial_ai';

SHOW TABLES LIKE 'USERS';
SHOW TABLES LIKE 'INSPECTION_RUN';
SHOW TABLES LIKE 'DOCUMENT';
```

이미 테이블이 있으면 `Table already exists`가 날 수 있습니다. 완전히 다시 만들려면 MariaDB 볼륨을 삭제한 뒤 SQL을 다시 실행합니다.

```powershell
cd infra
docker compose down
docker volume rm infra_mariadb_data
docker compose --env-file .env up -d mariadb
Get-Content .\mariadb\init\industrial-ai-platform.sql -Raw | docker exec -i industrial-mariadb sh -c 'mariadb -uindustrial_user -p"$MARIADB_PASSWORD" industrial_ai'
```

주의: `docker volume rm infra_mariadb_data`는 로컬 MariaDB 데이터를 삭제합니다.

## 7. Backend 실행

패키지 다운로드/컴파일:

```powershell
cd backend-spring
.\gradlew.bat compileJava
```

실행:

```powershell
cd backend-spring
.\gradlew.bat bootRun --args="--spring.profiles.active=local"
```

검증:

```powershell
cd backend-spring
.\gradlew.bat test
```

확인 URL:

- `http://localhost:8080/actuator/health`
- `http://localhost:8080/api/v1/health`

참고:

- Spring Boot는 기본값으로 `application.yml`과 `application-local.yml`을 사용합니다.
- 기본 로컬 값은 `.env.example`과 맞춰져 있습니다.
- IDE에서 `.env`를 직접 주입하려면 EnvFile 플러그인 또는 Run Configuration 환경변수 설정을 사용합니다.

## 8. AI Server 실행

가상환경 생성:

```powershell
cd ai-server
python -m venv .venv
```

가상환경 활성화:

```powershell
.\.venv\Scripts\Activate.ps1
```

PowerShell 실행 정책 때문에 활성화가 막히면 1회 실행합니다.

```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

패키지 설치:

```powershell
pip install -r requirements.txt
```

실행:

```powershell
uvicorn main:app --reload --host 0.0.0.0 --port 8001
```

검증:

```powershell
pytest
```

확인 URL:

- `http://localhost:8001/docs`
- `http://localhost:8001/ai/v1/health`

## 9. Frontend 실행

패키지 설치:

```powershell
cd frontend
npm install
```

실행:

```powershell
npm run dev
```

검증:

```powershell
npm run build
npm run lint
```

확인 URL:

- `http://localhost:5173`

## 10. 전체 실행 순서 요약

PowerShell 창을 서비스별로 나누어 실행하는 것을 권장합니다.

### 1번 터미널: Infra

```powershell
cd infra
docker compose --env-file .env up -d
```

### 2번 터미널: Backend

```powershell
cd backend-spring
.\gradlew.bat bootRun --args="--spring.profiles.active=local"
```

### 3번 터미널: AI Server

```powershell
cd ai-server
.\.venv\Scripts\Activate.ps1
uvicorn main:app --reload --host 0.0.0.0 --port 8001
```

### 4번 터미널: Frontend

```powershell
cd frontend
npm run dev
```

## 11. DBeaver 접속 정보

| 항목 | 값 |
| --- | --- |
| DBMS | MariaDB |
| Host | `localhost` |
| Port | `3307` |
| Database | `industrial_ai` |
| Username | `industrial_user` |
| Password | `infra/.env`의 `MARIADB_PASSWORD` |

DBeaver에서 ERD 확인:

1. MariaDB 연결 생성
2. `industrial_ai` 데이터베이스 선택
3. `Tables` 우클릭
4. `View Diagram` 선택

## 12. 최초 관리자 계정 만들기

현재 로컬 개발 환경에는 관리자 seed 계정이 자동 생성되지 않습니다.

Google 로그인 후 가입 승인 대기 화면이 뜨면, 해당 이메일을 관리자 권한으로 승격합니다.

프로젝트 루트에서 실행:

```powershell
.\infra\scripts\promote-admin.ps1 -Email "bonggyulim0728@gmail.com"
```

조직명을 직접 지정하려면:

```powershell
.\infra\scripts\promote-admin.ps1 -Email "본인구글이메일@example.com" -OrganizationName "Factory Guard Admin"
```

처리 내용:

- `ORGANIZATION`에 관리자 조직이 없으면 생성
- `USERS.status`를 `ACTIVE`로 변경
- `USERS.role`을 `ROLE_SITE_ADMIN`으로 변경
- `USERS.organization_id`를 관리자 조직으로 연결
- `SIGNUP_REQUEST.request_status`를 `APPROVED`로 변경

승격 후 브라우저에서 로그아웃한 뒤 다시 로그인합니다.

수동으로 확인하려면:

```powershell
docker exec -it industrial-mariadb mariadb -uindustrial_user -p industrial_ai
```

```sql
SELECT user_id, email, status, role, organization_id
FROM USERS
WHERE email = '본인구글이메일@example.com';
```

## 13. 자주 나는 문제

### Docker daemon 연결 실패

Docker Desktop이 실행 중인지 확인합니다.

```powershell
docker ps
```

### MariaDB 접속은 되는데 테이블이 없음

초기 SQL을 실행하지 않은 상태입니다.

```powershell
cd infra
Get-Content .\mariadb\init\industrial-ai-platform.sql -Raw | docker exec -i industrial-mariadb sh -c 'mariadb -uindustrial_user -p"$MARIADB_PASSWORD" industrial_ai'
```

### Table already exists

이미 테이블이 생성된 상태입니다. 로컬 데이터를 지워도 되는 경우에만 MariaDB 볼륨을 삭제합니다.

```powershell
cd infra
docker compose down
docker volume rm infra_mariadb_data
```

### Backend DB 로그인 실패

`infra/.env`와 `backend-spring/.env`의 DB 값이 같은지 확인합니다.

```text
MARIADB_DATABASE=industrial_ai
MARIADB_USER=industrial_user
MARIADB_PASSWORD=change_me_user_password

DB_NAME=industrial_ai
DB_USER=industrial_user
DB_PASSWORD=change_me_user_password
```

### 3307 포트 충돌

```powershell
netstat -ano | findstr :3307
```

충돌 프로세스를 종료하거나 `infra/docker-compose.yml`의 포트 매핑을 조정합니다.

### npm / java / python 명령 인식 실패

설치 후 새 PowerShell을 열고 다시 확인합니다.

```powershell
node -v
java -version
python --version
```

## 14. 제출 전 기본 검증

```powershell
cd backend-spring
.\gradlew.bat compileJava
```

```powershell
cd frontend
npm run build
```

```powershell
cd ai-server
.\.venv\Scripts\Activate.ps1
pytest
```

커밋 전 확인:

```powershell
git status
git diff
```

`.env`, 비밀번호, API Key, 토큰은 커밋하지 않습니다.
