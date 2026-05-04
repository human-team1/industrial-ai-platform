# Backend Spring

Spring Boot API 서버 전용 프로젝트입니다. SSR, JSP, WAR 구조를 사용하지 않고 persistence는 Spring Data JPA를 사용합니다.

## 기술 스택

- Java 17
- Spring Boot 2.7.18
- Gradle Wrapper 7.6.4
- Spring Data JPA
- MariaDB Java Client 2.7.5
- Spring Security
- OAuth2 Client
- Redis
- JJWT 0.11.5
- MinIO Java Client 8.5.12

## 환경변수

PowerShell:

```powershell
Copy-Item .env.example .env
```

주요 값:
- DB 접속 정보
- Redis 접속 정보
- MinIO endpoint/key/bucket
- Chroma host/port
- AI server base URL
- JWT secret/expire
- Google OAuth client 값

실제 비밀값은 커밋하지 않습니다.

## 실행

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=local"
```

## 테스트

```powershell
.\gradlew.bat test
```

## API 규칙

- API prefix는 `/api/v1`입니다.
- 성공 응답은 `ApiResponse` 구조를 사용합니다.
- 실패 응답은 Problem Details 스타일 JSON을 사용합니다.

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

## 패키지 구조

```text
com.example.factoryguard
  common/       # 공통 응답, 예외, 유틸
  config/       # security, persistence, web, client
  domain/       # 도메인 모델
  application/  # port, service, dto
  adapter/      # web, persistence, fastapi, minio, redis adapter
```

JPA Entity와 Spring Data Repository는 `adapter/out/persistence` 하위에 둡니다.
