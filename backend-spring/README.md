# Backend Spring

Spring Boot 기반 메인 도메인 API 서버입니다. 인증/권한, 검사/결과 이력, 문서 메타데이터, 파일 미리보기, FastAPI 연동 오케스트레이션을 담당합니다.

## 현재 스택

- Java 17
- Spring Boot 2.7.18
- Gradle Wrapper 7.6.4
- Spring Security
- Spring Data JPA
- MariaDB Java Client 2.7.5
- Redis
- MinIO Java Client 8.5.12
- JJWT 0.11.5

## 구조

```text
src/main/java/com/example/factoryguard
  common/        공통 응답, 예외, 유틸
  config/        security, web, persistence, client, document 설정
  domain/        도메인 모델/VO
  application/   port, service, dto
  adapter/       web, persistence, fastapi, minio, redis adapter
```

Controller는 `adapter/in/web`, JPA/외부 연동은 `adapter/out/*`, 유스케이스는 `application/service`에 둡니다.

## 환경 변수

```powershell
Copy-Item .env.example .env
```

주요 값:

| 변수 | 설명 |
| --- | --- |
| `APP_PORT` | Spring 서버 포트, 기본 `8080` |
| `DB_HOST`, `DB_PORT`, `DB_NAME` | MariaDB 접속 정보 |
| `REDIS_HOST`, `REDIS_PORT` | Redis 접속 정보 |
| `MINIO_ENDPOINT`, `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY` | MinIO 접속 정보 |
| `CHROMA_HOST`, `CHROMA_PORT` | ChromaDB 접속 정보 |
| `AI_SERVER_BASE_URL` | FastAPI 서버 주소 |
| `JWT_SECRET`, `JWT_EXPIRE_MINUTES` | JWT 설정 |
| `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET` | Google OAuth 설정 |
| `DOCUMENT_UPLOAD_ALLOWED_EXTENSIONS` | 문서 업로드 확장자, 기본 `pdf,docx,md` |

실제 비밀값은 `.env`에만 작성하고 커밋하지 않습니다.

## 실행

먼저 루트 README 또는 `docs/db/mariadb-schema-init.md`에 따라 MariaDB 스키마를 초기화합니다.

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=local"
```

## 검증

```powershell
.\gradlew.bat compileJava
.\gradlew.bat test
```

## API 응답 규칙

- API prefix: `/api/v1`
- 일반 성공 응답: `{ "success": true, "data": ..., "message": "..." }`
- 에러 응답: RFC 9457 Problem Details 형식
- 헬스체크, 파일 preview/download, 204 응답은 예외 가능

## Mapper 분리 규칙

- `adapter/in/web/*/mapper/*WebMapper`:
  - RequestDto -> Application Command/Query
  - Application Result -> ResponseDto 변환 책임
- `adapter/out/persistence/*/mapper/*PersistenceMapper`:
  - JPA Entity <-> Domain Model 변환 책임
- Controller는 UseCase 호출 조립만 수행하고, 필드 단위 직접 매핑을 두지 않습니다.
- Service는 Web DTO/JPA Entity에 직접 의존하지 않습니다.
- 
## 문서 업로드와 RAG 인덱싱

`POST /api/v1/documents`는 문서 원본을 MinIO에 저장하고, `FILE`, `DOCUMENT`, `DOCUMENT_VERSION`, `DOCUMENT_INDEX_JOB` 메타를 MariaDB에 저장한 뒤 FastAPI 내부 API `POST /ai/v1/internal/documents/index`를 호출합니다. Spring 요청 DTO에는 청킹/임베딩/Chroma 옵션을 넣지 않습니다.

Request:

```text
multipart/form-data
file: File, required
title: String, required
documentType: PDF | DOCX | MD | TXT, required
category: String, optional
equipmentType: String, optional
description: String, optional
tags: comma-separated String, optional
```

Response:

```json
{
  "success": true,
  "data": {
    "documentId": 101,
    "documentVersionId": 1001,
    "fileId": 501,
    "indexJobId": 9001,
    "indexingStatus": "PROCESSING"
  },
  "message": "문서가 업로드되었고 인덱싱 작업이 시작되었습니다."
}
```

재인덱싱:

```text
POST /api/v1/document-versions/{versionId}/index-jobs
```

스모크 테스트:

```powershell
curl -X POST "http://localhost:8080/api/v1/documents" `
  -H "Authorization: Bearer ${ACCESS_TOKEN}" `
  -H "X-Request-Id: smoke-doc-upload-001" `
  -F "file=@sample.pdf" `
  -F "title=FlexLink X65 유지보수 매뉴얼" `
  -F "documentType=PDF" `
  -F "category=MAINTENANCE" `
  -F "equipmentType=CONVEYOR" `
  -F "description=컨베이어 유지보수 문서"
```

DB 확인:

```sql
SELECT indexing_status, indexed_chunk_count, index_error_message, indexed_at
FROM DOCUMENT_VERSION
WHERE document_version_id = 1001;

SELECT COUNT(*) FROM CHUNK WHERE document_version_id = 1001;
SELECT COUNT(*)
FROM VECTOR_INDEX vi
JOIN CHUNK c ON c.chunk_id = vi.chunk_id
WHERE c.document_version_id = 1001;
```

## 주요 엔드포인트

- `GET /api/v1/health`
- `GET /api/v1/inspections/status`
- `POST /api/v1/documents`
- `POST /api/v1/document-versions/{versionId}/index-jobs`
- `GET /actuator/health`

## 현재 구현된 주요 엔드포인트

| Method | Path | 설명 |
| --- | --- | --- |
| GET | `/api/v1/health` | 기본 헬스체크 |
| GET | `/api/v1/health/infra` | MariaDB/Redis/MinIO/Chroma 확인 |
| POST | `/api/v1/auth/google` | Google 로그인 |
| POST | `/api/v1/auth/refresh` | Access Token 갱신 |
| POST | `/api/v1/auth/logout` | 로그아웃 |
| GET | `/api/v1/auth/me` | 현재 인증 정보 |
| POST | `/api/v1/signup-requests` | 가입 신청 |
| GET | `/api/v1/signup-requests/organizations/public` | 가입용 공개 조직 목록 |
| GET | `/api/v1/organizations/public` | 공개 조직 목록 |
| GET | `/api/v1/documents` | 문서 목록 |
| POST | `/api/v1/documents` | 문서 등록 |
| GET | `/api/v1/documents/{documentId}` | 문서 상세 |
| PATCH | `/api/v1/documents/{documentId}` | 문서 메타데이터 수정 |
| POST | `/api/v1/documents/{documentId}/versions` | 문서 파일 새 버전 등록 |
| DELETE | `/api/v1/documents/{documentId}` | 문서 삭제 |
| GET | `/api/v1/files/{fileId}/preview` | 파일 미리보기 |

권한 기준은 `docs/auth/api-authority-matrix.md`를 따릅니다.

## 문서 업로드 계약

- 확장자: `pdf`, `docx`, `md`
- MIME: `application/pdf`, `application/vnd.openxmlformats-officedocument.wordprocessingml.document`, `text/markdown`, `text/plain`
- 최대 크기: 기본 50MB
- 새 문서 등록은 multipart form-data
- 메타데이터 수정은 JSON `PATCH`
- 파일 교체는 `/documents/{documentId}/versions`에 새 버전으로 등록

## 주의

- 현재 local profile은 `ddl-auto=validate`입니다. 테이블 자동 생성이 아니라 init SQL 실행이 필요합니다.
- `bootrun-*.txt`, `.env`, `build/`, `.gradle/`은 커밋하지 않습니다.
## CORS 메모

- Spring 은 **`APP_CORS_ALLOWED_ORIGIN_PATTERNS`** 만 사용합니다(`APP_CORS_ALLOWED_ORIGINS` 는 무시).
- 기본 로컬 값은 `APP_CORS_ALLOWED_ORIGIN_PATTERNS=http://localhost:5173,http://localhost`입니다.
- 운영 확정 도메인 예: `APP_CORS_ALLOWED_ORIGIN_PATTERNS=https://industrial-ai.ddns.net`
- Cloudflare Tunnel 예시: `APP_CORS_ALLOWED_ORIGIN_PATTERNS=https://xxxx.trycloudflare.com`
- 운영/외부 공개 기준에서 `*`는 사용하지 않습니다.
- 프론트가 `/api/v1` 상대경로를 사용하고 Nginx가 같은 origin에서 프록시하면 CORS 의존도를 줄일 수 있습니다.
## Async Inspection Worker

- `POST /api/v1/inspections/upload` 는 FastAPI 추론 완료를 기다리지 않고 `runStatus=PROCESSING` 으로 즉시 응답합니다.
- 업로드 요청이 성공하면 `async_job(job_type=AI_IMAGE_INFERENCE, job_status=PENDING)` 를 함께 생성합니다.
- Spring 내부 worker 가 pending job 을 1건씩 선점해 FastAPI `/ai/v1/internal/vision/infer-image` 를 호출합니다.
- 성공 시 `inspection_result`, `result_artifact`, `image`, `inspection_event_log` 를 저장하고 `inspection_run` 을 `COMPLETED` 로 전이합니다.
- 실패 시 `inspection_run`, `async_job` 을 `FAILED` 로 확정하고 실패 사유를 남깁니다.
- 프론트는 `inspectionId` 기준 polling 으로 `PROCESSING -> COMPLETED/FAILED` 상태를 확인합니다.

관련 환경 변수:

- `AI_JOB_WORKER_ENABLED=true`
- `AI_JOB_WORKER_CONCURRENCY=1`
- `AI_JOB_POLL_INTERVAL_MS=1000`
- `AI_JOB_BATCH_SIZE=1`
- `AI_JOB_MAX_RETRY=0`
- `AI_REQUEST_TIMEOUT_SECONDS=180`

발표/시연 환경의 FastAPI 는 worker 1개 기준으로 실행합니다.

```powershell
cd ..\ai-server
uvicorn main:app --host 0.0.0.0 --port 8001 --workers 1
```

## Active Session Limit

- 집컴 시연 서버 안정성을 위해 활성 세션 수를 최대 3개로 제한합니다.
- 제한 기준은 Nginx connection 이 아니라 Spring 인증/세션 기준입니다.
- 4번째 로그인은 `429 ACTIVE_USER_LIMIT_EXCEEDED` 로 차단됩니다.
- 로그아웃 시 해당 세션 슬롯이 즉시 반환됩니다.
- `ACTIVE_SESSION_TTL_SECONDS` 가 지나면 active session 이 자동 만료되고, stale session 은 count 계산 전에 정리됩니다.
- `/api/v1/auth/me` 호출 시 active session TTL 이 갱신됩니다.

관련 환경 변수:

- `MAX_ACTIVE_USERS=3`
- `ACTIVE_SESSION_TTL_SECONDS=1800`

## Pending E2E Note

- 업로드 검사 정상 추론 E2E 는 현재 `MEMORY_BANK` 및 활성 모델 배포 seed 가 없어 보류했습니다.
- 현재 확인된 worker 실패 원인은 `active model deployment not found` 입니다.
- `MEMORY_BANK` 및 모델 산출물 seed 확보 후 정상 추론 E2E 를 재검증할 예정입니다.
