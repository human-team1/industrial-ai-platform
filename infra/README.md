# Infra

Docker Compose, Nginx, MariaDB 초기화/마이그레이션, MinIO 버킷 생성 스크립트를 관리합니다.

## 실행 모드

| 모드 | 파일 | 용도 |
| --- | --- | --- |
| 로컬 개발 | `docker-compose.yml` + `.env` | MariaDB, Redis, MinIO, ChromaDB, Nginx를 띄우고 Spring/FastAPI/Frontend는 호스트에서 실행 |
| 로컬 fullstack/prod 검증 | `docker-compose.prod.yml` + `.env.prod` | Nginx, Frontend, Spring, AI Server, DB/Redis/MinIO/Chroma를 모두 Docker로 실행 |

## 로컬 개발 인프라

```powershell
cd infra
Copy-Item .env.example .env
docker compose --env-file .env up -d
docker compose --env-file .env ps
```

주요 포트:

| 서비스 | 주소 |
| --- | --- |
| Nginx | `http://localhost` |
| MariaDB | `localhost:3307` |
| Redis | `localhost:6379` |
| MinIO API | `http://localhost:9000` |
| MinIO Console | `http://localhost:9001` |
| ChromaDB | `http://localhost:8000` |

## DB 초기화

신규 로컬 환경 또는 DB를 재생성해도 되는 경우:

```powershell
cd infra
.\scripts\db-reset.ps1 -Force
```

개별 실행:

```powershell
.\scripts\db-migrate.ps1
.\scripts\db-seed.ps1
.\scripts\db-status.ps1
```

`db-reset.ps1 -Force`는 로컬 DB 데이터를 재생성합니다. 보존해야 할 데이터가 있으면 실행하지 않습니다.

## MinIO 버킷

필수 버킷:

- `documents`
- `inspection-artifacts`
- `reports`
- `models`

생성:

```powershell
cd infra
.\scripts\init-minio-buckets.ps1
```

## 운영용 Docker Compose

```powershell
cd infra
Copy-Item .env.prod.example .env.prod
# .env.prod 값 채우기
docker compose -f docker-compose.prod.yml --env-file .env.prod up -d --build
```

운영용 compose는 기본적으로 Nginx HTTPS 443을 외부 진입점으로 사용합니다. TLS 파일은 `.env.prod`의 `NGINX_SSL_DIR`에 `fullchain.pem`, `privkey.pem` 이름으로 배치합니다.

상세는 [운영 compose 문서](../docs/project/docker-compose-prod.md)와 [TLS 문서](nginx/ssl/README.md)를 참고합니다.

## Nginx 라우팅

| 외부 경로 | 처리 |
| --- | --- |
| `/` | Frontend |
| `/api/` | Spring Boot |
| `/ai/` | 차단 |
| `/minio/` | 차단 |
| `/chroma/` | 차단 |

FastAPI, MariaDB, Redis, MinIO, ChromaDB는 외부에 직접 공개하지 않습니다.
