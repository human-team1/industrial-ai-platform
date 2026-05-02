# Storage / Cache Notes

## Scope

이번 Spring 작업 범위는 MariaDB 정합성, MinIO 파일 저장, Redis 보조 캐시/멱등성 처리까지다.
FastAPI 추론, ChromaDB 인덱싱, embedding 생성, RAG/LLM 응답 생성은 제외한다.

## MariaDB

- 최종 상태와 이력은 MariaDB에 저장한다.
- 검사 업로드는 TX1에서 `inspection_run`을 `PENDING`으로 먼저 생성하고 커밋한다.
- MinIO 업로드 후 TX2에서 `file`, `inspection_input`, `inspection_event_log`를 저장하고 `inspection_run.run_status`를 `PROCESSING`으로 변경한다.
- MinIO 업로드 실패 또는 TX2 실패 시 별도 `REQUIRES_NEW` 트랜잭션으로 `inspection_run.run_status=FAILED`, `inspection_run.error_code`, 실패 이벤트를 남긴다.
- 최종 중복 방지는 `inspection_run(organization_id, user_id, idempotency_key)` unique key와 `payload_fingerprint` 비교로 처리한다.
- 문서 업로드는 실제 인덱싱 없이 `document_version.indexing_status=PENDING`, `document_index_job.job_status=PENDING` 상태를 저장하는 구조를 유지한다.

## MinIO

- 바이너리 본문은 MinIO에 저장하고 MariaDB `file` 테이블에는 bucket, object key, 파일명, 확장자, MIME type, size, checksum을 저장한다.
- 검사 업로드 원본 object key: `inspections/{inspectionId}/inputs/{uuid}.{ext}`
- 검사 산출물 object key: `inspections/{inspectionId}/artifacts/{artifactType}/{uuid}.{ext}`
- 문서 원본 object key: `documents/{documentId}/versions/{versionNo}/{uuid}.{ext}`
- 보고서 파일 object key: `reports/{reportId}/{uuid}.{ext}`
- API 응답에는 MinIO endpoint, access key, secret key, raw object key를 노출하지 않는다.
- MinIO 업로드 성공 후 DB 저장이 실패하면 방금 업로드한 object 삭제를 시도하고, 삭제 실패는 경고 로그와 운영 로그로 추적한다.
- 파일 다운로드는 `file_id` 기준으로 메타데이터를 조회하고 연결 리소스의 조직 범위를 확인한 뒤 스트림을 반환한다.

## Redis

- Redis는 메인 DB가 아니라 유실 가능한 보조 저장소다.
- 검사 요청 idempotency 보조 key: `inspection:idempotency:{organizationId}:{userId}:{idempotencyKey}`
- idempotency TTL: 10분
- `SET NX` 성격의 `setIfAbsent`로 중복 요청을 보조 방지하되, Redis 장애 또는 cache miss는 정상적으로 MariaDB 흐름으로 fallback한다.
- Redis에서 같은 key의 다른 fingerprint가 확인되면 빠르게 409로 차단하지만, 최종 판정은 MariaDB unique key와 fingerprint 비교가 기준이다.
- 운영 상태 cache key는 `operation:system-status:spring:latest`, `operation:component-status:{componentType}`를 사용한다.
- 운영 조회는 Redis cache miss/장애 시 MariaDB 최신 이력으로 fallback한다.

## Validation Checklist

- 검사 업로드 성공 시 `inspection_run`, `file`, `inspection_input`, `inspection_event_log` 확인
- 같은 `Idempotency-Key` 재요청 시 기존 `inspection_run` 재사용 또는 409 충돌 확인
- Redis 중지 상태에서도 DB unique key 기준으로 검사 업로드 진행 확인
- MinIO 업로드 실패 시 `inspection_run.run_status=FAILED`와 실패 이벤트 확인
- MinIO 업로드 성공 후 DB 저장 실패 시 MinIO 보상 삭제 호출 확인
- `/api/v1/files/{fileId}/download` 호출 시 조직 범위 권한과 MinIO object 없음 오류 확인
- 문서 업로드 시 `document`, `document_version`, `document_index_job`, `file` 확인
- 운영 모니터링은 Redis cache miss/장애 시 MariaDB fallback 확인
