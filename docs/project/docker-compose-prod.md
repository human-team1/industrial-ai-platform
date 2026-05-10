# 운영 fullstack Docker Compose 기준 정리

## 실행 모드 (문서 기준 2가지)

- **로컬 개발 실행**: `infra/docker-compose.yml`로 인프라만 띄운 뒤 Spring/FastAPI/Frontend는 호스트에서 실행. [`테스트실행가이드.md`](테스트실행가이드.md) 참고.
- **배포용 로컬 Docker fullstack(본 문서)**: `infra/docker-compose.prod.yml` 기준 전 서비스 컨테이너 (nginx · frontend · spring · ai-server · mariadb · redis · minio · chroma).
- **Compose 네트워크 안**에서는 다른 컨테이너 접속 시 **`localhost` 대신 Compose 서비스명**만 사용함.

## 환경 변수

| 파일 | Git | 설명 |
|------|-----|------|
| `infra/.env.prod.example` | 커밋 | 키 이름·구조만 포함. 비밀 없음 |
| `infra/.env.prod` | **커밋 금지** | 배포 서버에서만 존재, 실 비밀/키 채움 |

`.gitignore`가 `.env`, `.env.*` 를 제외하지만 `**/.env.prod.example` 예외로 예시 파일만 버전관리함.

### 볼륨 초기화 주의

`docker compose down -v` 또는 네임드 볼륨 삭제 시 아래 데이터가 함께 사라짐.

| 볼륨 | 내용 |
|------|------|
| `mariadb_data_prod` | MariaDB 모든 데이터 |
| `minio_data_prod` | MinIO 객체 저장소 |
| `chroma_data_prod` | Chroma 영속 디렉터리 |
| `redis_data_prod` | Redis AOF 등 |

운영에서는 백업/복구 절차 후에만 초기화할 것.

## 서비스 주소 규격 (외부 라우팅)

| 사용자(브라우저) 요청 | Nginx 업스트림 | 목적 |
|----------------------|----------------|------|
| `GET /` 및 SPA 라우팅 | `frontend:80` | 정적 프론트 |
| `/api/` | `spring:8080` | Spring API |
| `/ai/` 등 | 차단 (`403`) | FastAPI 브라우저 노출 방지 |

Nginx 게이트웨이 설정: `infra/nginx/nginx.prod.conf` + `infra/nginx/conf.d/default.prod.conf`  
로컬 `docker-compose.prod.yml` 기본값은 **HTTPS 443** 만 publish 하는 경우가 많음(`NGINX_PUBLISH_HTTPS_PORT`). HTTP/ACME 등은 compose 주석·`NGINX_PUBLISH_HTTP_PORT` 로 선택. TLS 종료를 이 nginx에서 하거나, 상위에서 종료 후 `X-Forwarded-Proto` 전달 패턴 모두 허용.

## 호출 매트릭스 (Compose 내부 DNS)

원칙: **대상 호스트는 서비스명**, **포트는 수신 프로세스의 리슨 포트**. `localhost` 금지.

| 발신 | 수신 | 호스트명 | 포트 | 비고 |
|------|------|----------|------|------|
| 브라우저 (경유 Nginx) | Spring REST | `spring` | `8080` | 브라우저 경로 접두사 `/api/` |
| 브라우저 (경유 Nginx) | Frontend 정적 SPA | `frontend` | `80` | 같은 오리진에서 API 는 상대경로 `VITE_API_BASE_URL`(예 `/api/v1`) |
| Spring | MariaDB | `mariadb` | `3307` | `DB_HOST`, `DB_PORT` — 이미지 `command` 포트와 일치 |
| Spring | Redis | `redis` | `6379` | `REDIS_HOST`, `REDIS_PORT` |
| Spring | MinIO | `minio` | `9000` | `MINIO_ENDPOINT=http://minio:9000` |
| Spring | Chroma HTTP | `chroma` | `8000` | `CHROMA_HOST`, `CHROMA_PORT` / 참조 `CHROMA_URL=http://chroma:8000` |
| Spring | FastAPI | `ai-server` | `8001` | **`AI_SERVER_BASE_URL=http://ai-server:8001`** (경로 미포함). 코드가 `/ai/v1/internal/...` 덧붙임 |
| FastAPI | Chroma | `chroma` | `8000` | 컬렉션명 등 Spring 과 동일 권장 |
| FastAPI | MinIO | `minio` | `9000` | `MINIO_ENABLED` 등 설정에 따름 |
| FastAPI | Redis | `redis` | `6379` | `REDIS_ENABLED` 등 설정에 따름 |

**포트 참고**: Chroma 의 기본 HTTP 포트가 `8000` 이므로 ai-server 의 앱 포트와 겹치지 않게 레포에서는 FastAPI **`8001`** 를 사용함.

## 실행 예시

```powershell
cd infra
Copy-Item .env.prod.example .env.prod
# .env.prod 편집 후

docker compose -f docker-compose.prod.yml --env-file .env.prod up -d --build
docker compose -f docker-compose.prod.yml ps
```

최초 DB 시드 등은 운영 절차(`db-reset`/마이그레이션 등)에 맞게 별도 실행함.

### Spring 빌드 이미지 참고

`backend-spring/Dockerfile` 은 레포에 `gradle-wrapper.jar` 가 없어도 되도록 `gradle:7.6.4-jdk17-jammy` 이미지로 `bootJar` 를 수행함.

### FastAPI 및 LLM 호스트

예시 환경에서 `OLLAMA_BASE_URL=http://host.docker.internal:11434` 는 **Docker Desktop(윈/Mac)** 에서 호스트의 Ollama 를 가리키는 패턴임. 리눅스 서버 전용 Compose에서는 `extra_hosts`(또는 별도 `ollama` 서비스)로 대체해야 함.

## Spring CORS

- Spring 은 **`APP_CORS_ALLOWED_ORIGIN_PATTERNS`** → `app.cors.allowed-origin-patterns` 만 사용함. (`APP_CORS_ALLOWED_ORIGINS` 는 무시)
- 로컬 HTTPS fullstack: `APP_CORS_ALLOWED_ORIGIN_PATTERNS=https://localhost` 권장.

## MinIO 버킷

Fresh MinIO 볼륨이면 `models`, `documents`, `inspection-artifacts`, `reports` 가 없을 수 있음.  
`infra/scripts/init-minio-buckets.ps1` 또는 `init-minio-buckets.sh` 로 생성.  
`seed_base_model_artifacts.py` 는 주로 **models** 쪽; **검사 업로드**에는 **inspection-artifacts** 필수.

## 팀 실행 가이드 (통합)

env·스모크·트러블슈팅: [`테스트실행가이드.md`](테스트실행가이드.md)
