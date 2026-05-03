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

- 기본 로컬 값은 `APP_CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost`입니다.
- 중간발표 Cloudflare Tunnel 예시: `APP_CORS_ALLOWED_ORIGINS=https://xxxx.trycloudflare.com`
- 최종발표 도메인 예시: `APP_CORS_ALLOWED_ORIGINS=https://your-domain.com`
- 운영/외부 공개 기준에서 `*`는 사용하지 않습니다.
- 프론트가 `/api/v1` 상대경로를 사용하고 Nginx가 같은 origin에서 프록시하면 CORS 의존도를 줄일 수 있습니다.
