# Setup Overview

이 문서는 로컬 실행 구조만 빠르게 확인하기 위한 요약입니다. 처음 개발환경을 설치하는 절차는 루트 [README.md](../README.md)를 기준으로 합니다.

## 실행 구조

```text
Browser
  -> frontend (http://localhost:5173)
  -> backend-spring (http://localhost:8080/api/v1)
       -> MariaDB (localhost:3307)
       -> Redis (localhost:6379)
       -> MinIO (http://localhost:9000)
       -> ai-server (http://localhost:8001/ai/v1)
            -> Chroma (http://localhost:8000)
            -> MinIO
            -> Redis
```

## 포트

| 구성요소 | 포트 | 실행 방식 |
| --- | ---: | --- |
| Frontend | 5173 | 로컬 `npm run dev` |
| Backend Spring | 8080 | 로컬 Gradle bootRun |
| AI Server | 8001 | 로컬 Uvicorn |
| MariaDB | 3307 | Docker Compose |
| Redis | 6379 | Docker Compose |
| MinIO API | 9000 | Docker Compose |
| MinIO Console | 9001 | Docker Compose |
| Chroma | 8000 | Docker Compose |

## 저장소 역할

| 저장소 | 역할 |
| --- | --- |
| MariaDB | 사용자, 권한, 검사 세션, 결과/문서/챗봇 메타데이터, 감사 로그 |
| Redis | 세션, 캐시, 작업 상태, 임시 데이터 |
| MinIO | 원본 이미지, 영상, PDF, 보고서, 시각화 파일 |
| Chroma | 문서 청크 임베딩, 벡터 인덱스, 유사도 검색 |

## 실행 순서

1. `.env.example` 파일들을 `.env`로 복사합니다.
2. `infra`를 Docker Compose로 실행합니다.
3. MariaDB init SQL과 seed SQL을 적용합니다.
4. 회원가입/로그인 후 로컬 관리자 계정이 필요하면 DB에서 해당 계정을 `ACTIVE` + `ADMIN`으로 승격합니다.
5. `ai-server`를 실행합니다.
6. `backend-spring`을 실행합니다.
7. `frontend`를 실행합니다.

현재 Docker는 `infra`에만 사용합니다. 애플리케이션 서비스는 IDE 디버깅과 빠른 재시작을 위해 로컬 실행을 기준으로 합니다.

## MariaDB 빠른 초기화

`infra/docker-compose.yml`은 init SQL을 자동 마운트하지 않습니다. 테이블이 없거나 JPA validation 오류가 발생하면 `infra` 폴더에서 아래 순서로 실행합니다.

```powershell
cd infra
docker compose down -v
docker compose --env-file .env up -d

Get-Content -Raw -Encoding UTF8 .\mariadb\init\industrial-ai-platform.sql |
  docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -p<root_password> <database_name>

Get-Content -Raw -Encoding UTF8 .\mariadb\seed\seed-sample-organizations.sql |
  docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -p<root_password> <database_name>
```

관리자 승격 예시:

```powershell
docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -p<root_password> <database_name> -e "UPDATE users SET status='ACTIVE', role='ADMIN', deleted_at=NULL, updated_at=CURRENT_TIMESTAMP WHERE email='본인@gmail.com';"
```

자세한 DB 초기화 절차는 `docs/db/mariadb-schema-init.md`를 기준으로 합니다.
