# 검사 도메인 백엔드 구현 보고서

작성일: 2026-04-30

## 1. 작업 개요

API 명세 `docs/project/API.md`와 현재 Spring Boot 코드 구조를 기준으로 검사 도메인 백엔드 구현을 정리했다.

이번 작업의 핵심 목표는 FastAPI AI 서버가 아직 준비되지 않은 상태에서도 Spring 백엔드의 검사 요청/조회 API가 안정적으로 동작하도록 만드는 것이다.

## 2. 핵심 구현 내용

### 2.1 업로드 검사 요청

대상 API:

- `POST /api/v1/inspections/upload`

구현 내용:

- multipart `file` 필수 검증
- 기존 `FileValidator` 기반 이미지/영상 파일 검증 재사용
- 기존 MinIO 저장 구조 재사용
- 업로드 파일 메타데이터를 `file` 테이블에 저장
- `inspection_run` 생성
- `inspection_input` 생성
- 이벤트 로그 생성
- 응답 상태를 `PROCESSING`으로 반환
- `targetId`, `thresholdId` optional 처리
- `targetId`가 있는 경우 검사 대상 존재 여부 및 조직 접근 범위 검증
- `thresholdId`가 있는 경우 기존 임계값 해석 서비스로 사용자 범위 검증
- 업로드 흐름에서 FastAPI 실제 추론 호출 제거

응답 형식:

```json
{
  "success": true,
  "data": {
    "inspectionId": 1001,
    "runStatus": "PROCESSING"
  },
  "message": "업로드 검사가 요청되었습니다."
}
```

### 2.2 실시간 검사 API

대상 API:

- `POST /api/v1/inspections/realtime`
- `PATCH /api/v1/inspections/{inspectionId}/stop`

처리 내용:

- 실시간 검사는 기존 정책인 `REALTIME_NOT_ENABLED` 흐름을 유지
- 이번 작업 범위에서 실시간 AI 추론은 구현하지 않음
- stop API는 기존 흐름을 유지하되 응답 메시지를 정상 한국어로 정리

### 2.3 검사 실행 조회

대상 API:

- `GET /api/v1/inspections`
- `GET /api/v1/inspections/{inspectionId}`
- `GET /api/v1/inspections/{inspectionId}/events`

구현 내용:

- 기존 `Controller -> UseCase/Service -> Port -> Persistence Adapter` 흐름 유지
- 일반 사용자는 자기 조직 범위의 검사 실행만 조회 가능
- 타 조직 검사 접근 시 `403 Problem Details` 반환
- 없는 검사 ID는 `404 Problem Details` 반환

### 2.4 결과 조회

대상 API:

- `GET /api/v1/results`
- `GET /api/v1/results/{resultId}`
- `GET /api/v1/results/{resultId}/artifacts`
- `GET /api/v1/results/{resultId}/images`
- `GET /api/v1/results/{resultId}/regions`
- `GET /api/v1/results/{resultId}/explanation`

구현 내용:

- 결과 목록 경로는 API.md 기준인 `/results` 유지
- `/inspection-results` 같은 별도 경로는 추가하지 않음
- query 지원:
  - `startDate`
  - `endDate`
  - `decisionCode`
  - `targetId`
  - `page`
  - `size`
- 기존 프론트 호환을 위해 `from`, `to`, `decision` query도 보조적으로 유지
- `decisionCode`는 API.md 기준 `NORMAL / DEFECT / RECHECK` 사용
- 기존 `RETEST` 노출 매핑은 `RECHECK` 기준으로 정리
- artifacts/images/regions는 데이터가 없으면 빈 배열 반환 가능
- explanation은 AI/RAG 연동 전에도 깨지지 않도록 기본 설명 응답 제공
- 없는 resultId는 `404 Problem Details`
- 권한 없는 resultId 접근은 `403 Problem Details`

## 3. FastAPI 연동 분리

기존 문제:

- Spring 업로드 검사 흐름에서 FastAPI `/inspect` 호출을 즉시 수행
- 현재 FastAPI 서버에는 `/inspect`가 없고 계약도 달라 404/422 실패가 발생

이번 처리:

- `SubmitInspectionService`에서 업로드 검사 시 FastAPI 호출을 수행하지 않도록 분리
- `InspectionAiAdapter`와 관련 Port 구조는 추후 연동을 위해 유지
- 업로드 API 성공 여부가 FastAPI 서버 존재 여부에 의존하지 않도록 변경

남긴 TODO:

- `POST /ai/v1/inference/anomaly` 계약 정의
- AI 응답 기반 `inspection_result` 저장
- artifacts/images/regions 저장
- `RECHECK` 결과에 대한 재검토 큐 연계

## 4. 주요 변경 파일

### 검사 도메인

- `backend-spring/src/main/java/com/example/factoryguard/adapter/in/web/inspection/InspectionController.java`
- `backend-spring/src/main/java/com/example/factoryguard/adapter/in/web/inspection/dto/UploadInspectionResponse.java`
- `backend-spring/src/main/java/com/example/factoryguard/application/dto/inspection/SubmitInspectionCommand.java`
- `backend-spring/src/main/java/com/example/factoryguard/application/dto/inspection/SubmitInspectionResult.java`
- `backend-spring/src/main/java/com/example/factoryguard/application/service/inspection/SubmitInspectionService.java`

### 결과 조회

- `backend-spring/src/main/java/com/example/factoryguard/adapter/in/web/result/ResultController.java`
- `backend-spring/src/main/java/com/example/factoryguard/adapter/in/web/result/mapper/ResultWebMapper.java`
- `backend-spring/src/main/java/com/example/factoryguard/adapter/out/persistence/result/ResultQueryRepository.java`
- `backend-spring/src/main/java/com/example/factoryguard/application/dto/result/ListInspectionResultsQuery.java`
- `backend-spring/src/main/java/com/example/factoryguard/application/port/in/result/GetAnomalyRegionsUseCase.java`
- `backend-spring/src/main/java/com/example/factoryguard/application/port/in/result/GetResultArtifactsUseCase.java`
- `backend-spring/src/main/java/com/example/factoryguard/application/port/in/result/GetResultImagesUseCase.java`
- `backend-spring/src/main/java/com/example/factoryguard/application/port/in/result/GetResultExplanationUseCase.java`
- `backend-spring/src/main/java/com/example/factoryguard/application/service/result/ResultQueryService.java`

### 테스트

- `backend-spring/src/test/java/com/example/factoryguard/application/service/inspection/SubmitInspectionServiceTest.java`
- `backend-spring/src/test/java/com/example/factoryguard/application/service/inspection/InspectionQueryServiceTest.java`
- `backend-spring/src/test/java/com/example/factoryguard/application/service/result/ResultQueryServiceTest.java`

## 5. 테스트 결과

실행 명령:

```bash
./gradlew.bat test
```

결과:

```text
BUILD SUCCESSFUL
```

확인한 내용:

- FastAPI 서버 없이 Spring 테스트 통과
- file만 업로드 시 `inspectionId`, `PROCESSING` 반환 흐름 검증
- file 누락 검증 오류 전파 검증
- 잘못된 targetId 접근 시 권한 오류 검증
- 검사 목록/상세 조회 서비스 검증
- 결과 목록이 비어 있어도 success 응답 가능한 서비스 흐름 검증

## 6. API.md 대비 차이점

API.md에 없는 신규 경로는 추가하지 않았다.

단, 기존 프론트 및 기존 코드 호환을 위해 `GET /api/v1/results`에서 아래 query를 보조적으로 유지했다.

- `from`
- `to`
- `decision`

API.md 기준 query인 `startDate`, `endDate`, `decisionCode`가 들어오면 해당 값을 우선 사용한다.

## 7. 후속 작업 제안

1. FastAPI 추론 계약 확정
2. Spring `CallAiInspectionPort`와 FastAPI 응답 DTO 재정의
3. 비동기 처리 방식 결정
4. PROCESSING 검사 실행을 AI 처리 후 COMPLETED/FAILED로 전환하는 워커 또는 스케줄러 구현
5. AI 결과 기반 `inspection_result`, `result_artifact`, `image`, `anomaly_region` 저장 구현
6. `RECHECK` 결과의 재검토 큐 자동 등록 구현
