# Backend Spring

Spring Boot 기반 메인 도메인 API 서버입니다. 인증/권한, 검사 요청, 결과 이력, 문서 메타데이터, 파일 참조, 모델 관리, 운영 모니터링, FastAPI 연동 오케스트레이션을 담당합니다.

## 현재 스택

- Java 17
- Spring Boot 2.7.18
- Gradle Wrapper 7.6.4
- Spring Security / OAuth2 Client
- Spring Data JPA / Spring Data Redis
- MariaDB Java Client 2.7.5
- MinIO Java Client 8.5.12
- JJWT 0.11.5
- PDFBox 2.0.30

## 구조

```text
src/main/java/com/example/factoryguard/
  common/        공통 응답, 예외, 유틸
  config/        security, web, persistence, client 설정
  domain/        순수 도메인 모델/VO
  application/   port, service, dto
  adapter/
    in/web/      Controller, request/response, mapper
    out/         persistence, fastapi, minio, redis adapter
```

Controller는 요청/응답과 검증만 담당하고, 업무 흐름은 `application/service`와 port를 통해 처리합니다.

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

주의: Spring Boot는 기본적으로 `.env` 파일을 자동 로드하지 않습니다. 로컬에서 예시값을 그대로 쓰지 않는다면 IDE 환경 변수 또는 PowerShell 환경 변수로 주입해야 합니다.

## 실행

먼저 루트 README 또는 [DB 문서](../docs/db/README.md)에 따라 MariaDB 스키마를 준비합니다.

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=local"
```

헬스체크:

```text
http://localhost:8080/api/v1/health
```

## 검증

```powershell
.\gradlew.bat compileJava
.\gradlew.bat test
```

## 주요 책임

- `/api/v1/auth/**`: Google 로그인, 토큰 갱신, 로그아웃, 내 정보
- `/api/v1/inspections/**`: 이미지 검사 요청, 검사 상태/이벤트 조회
- `/api/v1/results/**`: 검사 결과 목록/상세, 산출물/설명 조회
- `/api/v1/documents/**`: 문서 등록, 버전 관리, 인덱싱 요청
- `/api/v1/chat-*`: 챗봇 대화/메시지/출처 관리
- `/api/v1/models/**`, `/api/v1/model-*`: 모델 버전/아티팩트/배포 관리
- `/api/v1/admin/**`: 운영 관리자 기능

상세 API는 [API 명세서](../docs/project/API.md), 권한 기준은 [권한 매트릭스](../docs/auth/api-authority-matrix.md)를 따릅니다.

## 응답 규칙

- 일반 성공 응답: `{ "success": true, "data": ..., "message": "..." }`
- 에러 응답: RFC 9457 Problem Details 형식
- 헬스체크, 파일 preview/download, 204 응답은 예외 가능

## 비동기 검사 흐름

`POST /api/v1/inspections/upload`는 검사 요청을 생성하고 `PROCESSING` 상태로 응답합니다. 내부 worker가 pending job을 선점해 FastAPI `/ai/v1/internal/vision/infer-image`를 호출하고, 성공 시 결과/산출물/이벤트를 저장합니다.

관련 환경 변수:

- `AI_JOB_WORKER_ENABLED`
- `AI_JOB_WORKER_CONCURRENCY`
- `AI_JOB_POLL_INTERVAL_MS`
- `AI_JOB_BATCH_SIZE`
- `AI_JOB_MAX_RETRY`
- `AI_REQUEST_TIMEOUT_SECONDS`

## 커밋 제외

`.env`, `build/`, `.gradle/`, `bootrun-*.txt`, 로그 파일, 인증서/키 파일은 커밋하지 않습니다.
