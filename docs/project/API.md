# API 명세

## 0. 공통 규칙

| 항목 | 기준 |
| --- | --- |
| Spring API Base URL | `/api/v1` |
| FastAPI Internal Base URL | `/ai/v1/internal` |
| 인증 방식 | `Authorization: Bearer {accessToken}` |
| URL | 복수형 명사 사용 |
| 성공 응답 | `{ success, data, message }` |
| 에러 응답 | RFC 9457 Problem Details |
| 날짜 형식 | ISO-8601 |
| 페이지네이션 | `page`, `size`, `sort` |
| 기본 정렬 | 최신순 |
| Request ID | `X-Request-Id` |

### 성공 응답 예시

```json
{
  "success": true,
  "data": {},
  "message": "요청이 성공적으로 처리되었습니다."
}
```

### 에러 응답 예시

```json
{
  "type": "https://api.example.com/problems/validation-error",
  "title": "Validation failed",
  "status": 422,
  "detail": "요청 값이 올바르지 않습니다.",
  "instance": "/api/v1/inspections/upload",
  "errorCode": "VALIDATION_ERROR",
  "errors": [
    {
      "field": "roiX",
      "reason": "roiX는 0 이상 1 이하이어야 합니다."
    }
  ]
}
```

---

## 1. 공통 ENUM

| 구분 | 값 |
| --- | --- |
| Role | `ROLE_SITE_ADMIN / ROLE_COMPANY_ADMIN / ROLE_COMPANY_WORKER` |
| UserStatus | `PENDING / ACTIVE / REJECTED / INACTIVE` |
| DecisionCode | `NORMAL / DEFECT / RECHECK` |
| RunStatus | `PENDING / PROCESSING / COMPLETED / FAILED / STOPPED` |
| RunType | `UPLOAD / REALTIME` |
| InputType | `IMAGE / VIDEO` |
| SourceType | `IMAGE / VIDEO / BROWSER_CAMERA / RTSP_STREAM` |
| RoiMode | `FULL_FRAME / FIXED` |
| RoiCoordinateType | `NORMALIZED` |
| InputQualityStatus | `PASSED / WARNING / FAILED` |
| InputQualityReason | `TOO_DARK / TOO_BRIGHT / LOW_CONTRAST / BLURRY / ROI_INVALID / COLOR_SHIFT / SOME_FRAMES_RECHECK / INSUFFICIENT_VALID_FRAMES` |
| ModelCategory | `OBJECT / TEXTURE` |
| ModelProfile | `SPEED / PERFORMANCE` |
| ModelDeployStatus | `REGISTERED / VALIDATED / DEPLOYED / DEPRECATED` |
| ModelArtifactType | `CKPT / CONFIG / MEMORY_BANK / LABELS / EXTRA` |
| DeploymentScope | `ORGANIZATION / TARGET` |
| DeploymentStatus | `DEPLOYED / ROLLED_BACK / DEACTIVATED` |
| SystemComponentType | `SPRING_API / AI_SERVER / MARIADB / REDIS / MINIO / CHROMA / STORAGE` |
| SystemComponentStatus | `NORMAL / WARNING / ERROR / UNKNOWN` |
| LogLevel | `INFO / WARN / ERROR` |
| IndexingStatus | `PENDING / PROCESSING / COMPLETED / FAILED` |
| AsyncJobStatus | `PENDING / PROCESSING / COMPLETED / FAILED / CANCELED` |
| AnswerStatus | `ANSWERED / NO_RELEVANT_SOURCE / OUT_OF_SCOPE / LLM_FAILED / VECTOR_STORE_FAILED / DOCUMENT_SCOPE_FORBIDDEN / VALIDATION_FAILED` |

---

# 2. Auth / Users

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| POST | `/auth/login` | 이메일/비밀번호 로그인 | Public |
| POST | `/auth/google` | Google OAuth 로그인 | Public |
| POST | `/auth/logout` | 로그아웃 | Authenticated |
| POST | `/auth/refresh` | 토큰 재발급 | Authenticated |
| GET | `/auth/me` | 내 로그인 정보 조회 | Authenticated |
| GET | `/signup-requests/organizations/public` | 회원가입용 공개 조직 목록 | Public |
| POST | `/signup-requests` | 회원가입 신청 | Public |
| GET | `/signup-requests` | 가입 신청 목록 조회 | ROLE_SITE_ADMIN |
| PATCH | `/signup-requests/{requestId}/approve` | 가입 승인 | ROLE_SITE_ADMIN |
| PATCH | `/signup-requests/{requestId}/reject` | 가입 거절 | ROLE_SITE_ADMIN |
| GET | `/users/me` | 내 정보 조회 | Authenticated |
| PATCH | `/users/me` | 내 정보 수정 | Authenticated |
| GET | `/users` | 사용자 목록 조회 | ROLE_SITE_ADMIN |
| GET | `/users/{userId}` | 사용자 상세 조회 | ROLE_SITE_ADMIN |
| PATCH | `/users/{userId}/status` | 사용자 상태 변경 | ROLE_SITE_ADMIN |

---

# 3. Organizations

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/organizations` | 조직 목록 조회 | ROLE_SITE_ADMIN |
| POST | `/organizations` | 조직 생성 | ROLE_SITE_ADMIN |
| GET | `/organizations/{organizationId}` | 조직 상세 조회 | ROLE_SITE_ADMIN |
| PATCH | `/organizations/{organizationId}` | 조직 수정 | ROLE_SITE_ADMIN |
| PATCH | `/organizations/{organizationId}/status` | 조직 상태 변경 | ROLE_SITE_ADMIN |

---

# 4. Settings / Thresholds

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/users/me/settings` | 내 설정 조회 | Authenticated |
| PATCH | `/users/me/settings` | 내 설정 수정 | Authenticated |
| GET | `/users/me/thresholds` | 내 임계값 조회 | Authenticated |
| POST | `/users/me/thresholds` | 내 임계값 생성 | Authenticated |
| PATCH | `/users/me/thresholds/{thresholdId}` | 내 임계값 수정 | Authenticated |
| GET | `/users/me/thresholds/{thresholdId}/histories` | 임계값 변경 이력 조회 | Authenticated |

### PATCH `/users/me/thresholds/{thresholdId}`

```json
{
  "anomalyThreshold": 0.75,
  "lowConfidenceThreshold": 0.55,
  "applyScope": "DEFAULT",
  "changeReason": "공정 특성 반영"
}
```

### 검증 기준

| 조건 | 실패 처리 |
| --- | --- |
| 임계값 범위 초과 | 422 |
| 존재하지 않는 thresholdId | 404 |
| 권한 없는 사용자 접근 | 403 |

---

# 5. Analysis Targets / Cameras

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/analysis-targets` | 검사 대상 목록 조회 | Authenticated |
| POST | `/analysis-targets` | 검사 대상 등록 | ROLE_COMPANY_ADMIN 이상 |
| GET | `/analysis-targets/{targetId}` | 검사 대상 상세 조회 | Authenticated |
| PATCH | `/analysis-targets/{targetId}` | 검사 대상 수정 | ROLE_COMPANY_ADMIN 이상 |
| DELETE | `/analysis-targets/{targetId}` | 검사 대상 삭제 | ROLE_COMPANY_ADMIN 이상 |
| GET | `/camera-sources` | 카메라 목록 조회 | Authenticated |
| POST | `/camera-sources` | 카메라 등록 | ROLE_COMPANY_ADMIN 이상 |
| PATCH | `/camera-sources/{cameraId}` | 카메라 수정 | ROLE_COMPANY_ADMIN 이상 |
| DELETE | `/camera-sources/{cameraId}` | 카메라 삭제 | ROLE_COMPANY_ADMIN 이상 |

---

# 6. Inspections

## 6.1 엔드포인트

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/inspection-models/available` | 검사 화면 모델 선택 목록 조회 | Authenticated |
| GET | `/realtime/cameras/available` | 실시간 검사 카메라 선택 목록 조회 | Authenticated |
| POST | `/inspections/upload` | 이미지 업로드 검사 요청 | Authenticated |
| POST | `/inspections/realtime` | 실시간 검사 세션 시작. 확장 계약 | Authenticated |
| POST | `/inspections/{inspectionId}/frames` | 실시간 프레임 검사. 확장 계약 | Authenticated |
| PATCH | `/inspections/{inspectionId}/stop` | 실시간 검사 중지. 확장 계약 | Authenticated |
| GET | `/inspections` | 검사 실행 목록 조회 | Authenticated |
| GET | `/inspections/{inspectionId}` | 검사 실행 상세 조회 | Authenticated |
| GET | `/inspections/{inspectionId}/events` | 검사 이벤트 로그 조회 | Authenticated |

MVP 프론트는 이미지 업로드와 브라우저 카메라 단건 캡처만 사용한다. 영상/지속 스트리밍 API는 백엔드 확장 계약으로 유지한다.

---

## 6.2 ROI / 품질검사 기준

| 항목 | 기준 |
| --- | --- |
| MVP 입력 범위 | 이미지 업로드, 브라우저 카메라 단건 캡처 |
| ROI 기본 처리 | `FULL_FRAME` |
| ROI 지정 처리 | `FIXED`, 좌표는 `NORMALIZED` |
| 좌표 범위 | `0.0 ~ 1.0` |
| 품질검사 실패 | `DEFECT`가 아니라 `RECHECK` |
| 영상/지속 스트리밍 | MVP 프론트 미사용, 확장 계약 유지 |
| 객체탐지/세그멘테이션 | MVP 제외 |
| 자동 ROI 탐지 | MVP 제외 |

---

## 6.3 POST `/inspections/upload`

이미지 파일을 업로드하여 검사를 요청한다. 브라우저 카메라 캡처도 이미지 파일로 변환한 뒤 이 API를 사용한다.

`multipart/form-data`

| Field | Type | Required | 설명 |
| --- | --- | --- | --- |
| `file` | File | Y | 검사 이미지. jpg/jpeg/png/webp |
| `targetId` | Long | N | 검사 대상 ID |
| `thresholdId` | Long | N | 적용 임계값 ID |
| `inputMode` | String | N | 기본 `IMAGE` |
| `sourceType` | String | N | `IMAGE / BROWSER_CAMERA` |
| `roiMode` | String | N | `FULL_FRAME / FIXED` |
| `roiCoordinateType` | String | N | 기본 `NORMALIZED` |
| `roiX` | Decimal | N | 정규화 ROI x |
| `roiY` | Decimal | N | 정규화 ROI y |
| `roiWidth` | Decimal | N | 정규화 ROI width |
| `roiHeight` | Decimal | N | 정규화 ROI height |
| `samplingFps` | Decimal | N | 영상 확장용 |
| `maxFrames` | Int | N | 영상 확장용 |
| `qualityGateEnabled` | Boolean | N | 기본 `true` |
| `idempotencyKey` | String | N | 중복 요청 방지 키 |

### 요청 예시

```text
file=sample.jpg
inputMode=IMAGE
sourceType=IMAGE
roiMode=FULL_FRAME
qualityGateEnabled=true
idempotencyKey=upload-20260503-0001
```

### Response

```json
{
  "success": true,
  "data": {
    "inspectionId": 1001,
    "runStatus": "PROCESSING",
    "inputType": "IMAGE",
    "sourceType": "IMAGE",
    "roiMode": "FULL_FRAME",
    "qualityGateEnabled": true
  },
  "message": "업로드 검사가 요청되었습니다."
}
```

### 검증 기준

| 조건 | 실패 처리 |
| --- | --- |
| 파일 누락 | 400 |
| 지원하지 않는 MIME | 422 |
| 손상 파일 | 422 |
| `roiMode=FIXED`인데 ROI 좌표 누락 | 422 |
| ROI 좌표가 0~1 범위 밖 | 422 |
| `samplingFps <= 0` | 422 |
| `maxFrames <= 0` | 422 |
| 동일 `idempotencyKey` + 다른 payload | 409 |
| MVP 프론트에서 video/* MIME 선택 | 프론트 차단 |

---

## 6.4 POST `/inspections/realtime`

실시간 검사 세션을 시작한다. MVP 프론트에서는 호출하지 않는다.

### Request

```json
{
  "targetId": 1,
  "cameraId": 3,
  "thresholdId": 2,
  "sourceType": "BROWSER_CAMERA",
  "roi": {
    "roiMode": "FIXED",
    "roiCoordinateType": "NORMALIZED",
    "roiX": 0.25,
    "roiY": 0.2,
    "roiWidth": 0.5,
    "roiHeight": 0.5
  },
  "samplingFps": 1.0,
  "qualityGateEnabled": true,
  "idempotencyKey": "realtime-20260503-0001"
}
```

### Response

```json
{
  "success": true,
  "data": {
    "inspectionId": 2001,
    "runStatus": "PROCESSING",
    "sourceType": "BROWSER_CAMERA",
    "samplingFps": 1.0,
    "qualityGateEnabled": true
  },
  "message": "실시간 검사가 시작되었습니다."
}
```

---

## 6.5 POST `/inspections/{inspectionId}/frames`

실시간 세션에서 프레임 1장을 전송한다. MVP 프론트에서는 호출하지 않는다.

`multipart/form-data`

| Field | Type | Required | 설명 |
| --- | --- | --- | --- |
| `frame` | File | Y | JPEG/WebP 프레임 |
| `frameSeq` | Int | Y | 프레임 순번 |
| `capturedAt` | String | N | ISO-8601 캡처 시각 |

### Response

```json
{
  "success": true,
  "data": {
    "inspectionId": 2001,
    "frameSeq": 17,
    "decisionCode": "RECHECK",
    "score": null,
    "confidence": 0.42,
    "quality": {
      "status": "FAILED",
      "reason": "TOO_DARK"
    }
  },
  "message": "프레임 품질이 기준을 만족하지 않아 재검사로 분류되었습니다."
}
```

---

## 6.6 PATCH `/inspections/{inspectionId}/stop`

실시간 검사 세션을 중지하고 세션 단위 결과를 집계한다. MVP 프론트에서는 호출하지 않는다.

### Request

```json
{
  "reason": "USER_STOPPED"
}
```

### Response

```json
{
  "success": true,
  "data": {
    "inspectionId": 2001,
    "runStatus": "STOPPED",
    "resultId": 3001,
    "finalDecisionCode": "RECHECK",
    "analyzedFrameCount": 48,
    "skippedFrameCount": 12,
    "defectFrameCount": 0,
    "recheckFrameCount": 12,
    "maxFrameScore": 0.5512,
    "avgFrameScore": 0.2304,
    "representativeFrameSeq": 17
  },
  "message": "실시간 검사가 중지되었습니다."
}
```

---

## 6.7 GET `/inspections`

| Query | Type | Required | 설명 |
| --- | --- | --- | --- |
| `runType` | String | N | `UPLOAD / REALTIME` |
| `inputType` | String | N | `IMAGE / VIDEO` |
| `sourceType` | String | N | `IMAGE / BROWSER_CAMERA / RTSP_STREAM` |
| `runStatus` | String | N | 검사 상태 |
| `targetId` | Long | N | 검사 대상 ID |
| `startDate` | yyyy-MM-dd | N | 시작일 |
| `endDate` | yyyy-MM-dd | N | 종료일 |
| `page` | Int | N | 페이지 |
| `size` | Int | N | 크기 |
| `sort` | String | N | 정렬 |

---

## 6.8 GET `/inspections/{inspectionId}`

### Response

```json
{
  "success": true,
  "data": {
    "inspectionId": 2001,
    "runType": "UPLOAD",
    "inputType": "IMAGE",
    "sourceType": "BROWSER_CAMERA",
    "runStatus": "COMPLETED",
    "appliedThreshold": 0.75,
    "startedAt": "2026-05-03T10:00:00",
    "completedAt": "2026-05-03T10:00:03",
    "input": {
      "roiMode": "FULL_FRAME",
      "roiCoordinateType": "NORMALIZED",
      "qualityGateEnabled": true
    },
    "resultSummary": {
      "resultId": 3001,
      "finalDecisionCode": "NORMAL",
      "analyzedFrameCount": 1,
      "inputQualityStatus": "PASSED",
      "inputQualityReason": null
    }
  },
  "message": "검사 실행 상세를 조회했습니다."
}
```

---

## 6.9 GET `/inspections/{inspectionId}/events`

| Query | Type | Required | 설명 |
| --- | --- | --- | --- |
| `eventType` | String | N | 이벤트 타입 |
| `page` | Int | N | 페이지 |
| `size` | Int | N | 크기 |

---

## 6.10 GET `/inspection-models/available`

- 구현 상태: 구현 완료
- 설명: 업로드/실시간 검사 화면에서 선택 가능한 배포 모델 목록을 조회한다.
- 권한: Authenticated
- Request: `targetId`, `inspectionType`, `modelCategory`를 Query로 선택 전달한다.
- Response: `{ success, data: { items: AvailableInspectionModel[] }, message }`
- 실패 케이스: 인증 실패 `401`, 권한 부족 `403`, 요청 파라미터 형식 오류 `400`
- 관련 테이블: `model`, `model_version`, `model_artifact`, `model_deployment`, `file`
- 최근 변경 사유: 최근 배포 기반 검사 모델 선택 흐름 추가 시 `deployment_id`/`file_name`/`object_key` 기준으로 정합성을 맞추고, 조회 실패 시 500 대신 빈 목록 fallback을 적용했다.

### Response

```json
{
  "success": true,
  "data": {
    "items": [
      {
        "deploymentId": 3001,
        "modelVersionId": 10,
        "modelId": 1,
        "modelName": "PatchCore Texture Detector",
        "versionName": "v1.0.0-texture-performance",
        "displayName": "PatchCore Texture Detector / TEXTURE / PERFORMANCE / 검사대상 전용",
        "modelCategory": "TEXTURE",
        "modelProfile": "PERFORMANCE",
        "deploymentScope": "TARGET",
        "organizationId": 1001,
        "targetId": 10,
        "thresholdDefault": 0.75
      }
    ]
  },
  "message": "사용 가능한 검사 모델 목록을 조회했습니다."
}
```

### 빈 데이터 Response

```json
{
  "success": true,
  "data": {
    "items": []
  },
  "message": "사용 가능한 검사 모델 목록을 조회했습니다."
}
```

## 6.11 GET `/realtime/cameras/available`

- 구현 상태: 구현 완료
- 설명: 실시간 검사 화면에서 선택 가능한 카메라 목록을 조회한다.
- 권한: Authenticated
- Request: `targetId`를 Query로 선택 전달한다.
- Response: `{ success, data: { items: AvailableRealtimeCamera[] }, message }`
- 실패 케이스: 인증 실패 `401`, 권한 부족 `403`, 요청 파라미터 형식 오류 `400`
- 관련 테이블: `camera_source`
- 최근 변경 사유: 검사 선택 화면에서 모델 선택 API와 동일 패턴으로 카메라 선택 목록 API를 분리했다.

### Response

```json
{
  "success": true,
  "data": {
    "items": [
      {
        "cameraId": 7,
        "cameraName": "Press-Line-1",
        "organizationId": 1001,
        "targetId": 10,
        "targetName": null,
        "status": "ACTIVE",
        "displayName": "Press-Line-1"
      }
    ]
  },
  "message": "사용 가능한 카메라 목록을 조회했습니다."
}
```

---

# 7. Results

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/results` | 결과 목록 조회 | Authenticated |
| GET | `/results/{resultId}` | 결과 상세 조회 | Authenticated |
| GET | `/results/{resultId}/artifacts` | 결과 산출물 조회 | Authenticated |
| GET | `/results/{resultId}/images` | 결과 이미지 조회 | Authenticated |
| GET | `/results/{resultId}/regions` | 이상 영역 조회 | Authenticated |
| GET | `/results/{resultId}/explanation` | 결과 자연어 설명 조회 | Authenticated |
| GET | `/results/{resultId}/report` | 결과 보고서 다운로드 | Authenticated |

## GET `/results`

| Query | Type | Required | 설명 |
| --- | --- | --- | --- |
| `startDate` | yyyy-MM-dd | N | 시작일 |
| `endDate` | yyyy-MM-dd | N | 종료일 |
| `decisionCode` | String | N | `NORMAL / DEFECT / RECHECK` |
| `targetId` | Long | N | 검사 대상 |
| `inputType` | String | N | 입력 타입 |
| `runType` | String | N | `UPLOAD / REALTIME` |
| `page` | Int | N | 페이지 |
| `size` | Int | N | 크기 |
| `sort` | String | N | 정렬 |

---

## GET `/results/{resultId}`

### Response

```json
{
  "success": true,
  "data": {
    "resultId": 3001,
    "inspectionId": 2001,
    "targetId": 1,
    "equipmentName": "프레스 #1",
    "locationName": "라인 A-1",
    "inputType": "IMAGE",
    "sourceType": "BROWSER_CAMERA",
    "score": 0.1204,
    "confidence": 0.89,
    "decisionCode": "NORMAL",
    "finalDecisionCode": "NORMAL",
    "resultStatus": "COMPLETED",
    "thresholdId": 2,
    "thresholdVersion": 3,
    "modelVersionId": 10,
    "model": {
      "modelId": 1,
      "modelName": "PatchCore Texture Detector",
      "versionName": "v1.0.0-texture-performance",
      "modelCategory": "TEXTURE",
      "modelProfile": "PERFORMANCE"
    },
    "roi": {
      "roiMode": "FULL_FRAME",
      "roiCoordinateType": "NORMALIZED"
    },
    "videoSummary": null,
    "inputQuality": {
      "status": "PASSED",
      "reason": null
    },
    "failureReason": null,
    "createdAt": "2026-05-03T10:01:00"
  },
  "message": "결과 상세를 조회했습니다."
}
```

---

# 8. Review

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/reviews` | 재검토 큐 목록 조회 | ROLE_SITE_ADMIN |
| GET | `/reviews/{reviewQueueId}` | 재검토 상세 조회 | ROLE_SITE_ADMIN |
| PATCH | `/reviews/{reviewQueueId}` | 재검토 처리 | ROLE_SITE_ADMIN |
| GET | `/results/{resultId}/review-histories` | 결과별 재검토 이력 조회 | ROLE_SITE_ADMIN |
| POST | `/results/{resultId}/learning-candidates` | 학습 후보 등록 | ROLE_SITE_ADMIN |

### PATCH `/reviews/{reviewQueueId}`

```json
{
  "afterDecision": "DEFECT",
  "reviewComment": "시각화 결과상 표면 결함 확인",
  "registerLearningCandidate": true
}
```

### 검증 기준

| 조건 | 실패 처리 |
| --- | --- |
| 권한 없는 사용자 접근 | 403 |
| 존재하지 않는 reviewQueueId | 404 |
| 수정 사유 누락 | 422 |
| 허용되지 않는 판정값 | 422 |

---

# 9. Documents / RAG

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/documents` | 문서 목록 조회 | Authenticated |
| POST | `/documents` | 문서 업로드 | ROLE_COMPANY_ADMIN 이상 |
| GET | `/documents/{documentId}` | 문서 상세 조회 | Authenticated |
| PATCH | `/documents/{documentId}` | 문서 정보 수정 | ROLE_COMPANY_ADMIN 이상 |
| DELETE | `/documents/{documentId}` | 문서 삭제 | ROLE_COMPANY_ADMIN 이상 |
| GET | `/documents/{documentId}/versions` | 문서 버전 목록 조회 | Authenticated |
| POST | `/documents/{documentId}/versions` | 새 문서 버전 업로드 | ROLE_COMPANY_ADMIN 이상 |
| GET | `/document-versions/{versionId}/chunks` | 문서 청크 조회 | Authenticated |
| GET | `/document-versions/{versionId}/index-jobs` | 인덱싱 작업 조회 | ROLE_COMPANY_ADMIN 이상 |
| POST | `/document-versions/{versionId}/index-jobs` | 인덱싱 재요청 | ROLE_COMPANY_ADMIN 이상 |

---

## 9.1 문서 인덱싱 상태와 권한

| DB 값 | 화면 표시 |
| --- | --- |
| `PENDING` | 대기 |
| `PROCESSING` | 처리중 |
| `COMPLETED` | 반영완료 |
| `FAILED` | 반영실패 |

권한 기준:

- 일반 사용자: 자기 회사 문서 조회, 챗봇/RAG 사용
- `ROLE_COMPANY_ADMIN` 이상: 자기 회사 문서 업로드, 수정, 삭제, 재인덱싱 요청
- `ROLE_SITE_ADMIN`: 전체 문서 운영 현황과 인덱싱 상태 조회

FastAPI는 내부 서버이므로 최종 인증/권한 검증은 Spring이 담당한다. FastAPI는 `organizationId` 기준 Chroma collection scope를 반드시 지킨다.

---

## 9.2 POST `/documents`

문서 원본을 MinIO에 저장하고, `FILE`, `DOCUMENT`, `DOCUMENT_VERSION`, `DOCUMENT_INDEX_JOB` 메타데이터를 MariaDB에 저장한다. 인덱싱은 비동기 job으로 처리한다.

### Response

```json
{
  "success": true,
  "data": {
    "documentId": 1001,
    "documentVersionId": 2001,
    "indexJobId": 3001,
    "indexingStatus": "PROCESSING"
  },
  "message": "문서가 업로드되었고 인덱싱이 요청되었습니다."
}
```

---

## 9.3 POST `/document-versions/{versionId}/index-jobs`

재인덱싱 작업을 요청한다.

### Response

```json
{
  "success": true,
  "data": {
    "indexJobId": 3002,
    "documentVersionId": 2001,
    "aiJobId": "doc-index-3002",
    "indexingStatus": "PROCESSING"
  },
  "message": "문서 재인덱싱이 요청되었습니다."
}
```

---

## 9.4 GET `/document-versions/{versionId}/index-jobs`

문서 버전의 인덱싱 작업 상태를 조회한다.

---

## 9.5 DELETE `/documents/{documentId}`

문서를 soft delete하고, FastAPI deindex API를 호출해 ChromaDB vector 삭제를 요청한다. Chroma 삭제 실패 시 문서 삭제 자체는 성공 처리하되 운영 로그에 남긴다.

---

## 9.6 문서 정책

| 항목 | 기준 |
| --- | --- |
| 수정 | 새 파일 업로드 시 새 `DOCUMENT_VERSION` 생성 |
| 재인덱싱 | 기존 vector 삭제 후 다시 적재 |
| 삭제 | Spring soft delete + FastAPI deindex 요청 |
| 원본 저장 | MinIO |
| 메타 저장 | MariaDB |
| 벡터 저장 | ChromaDB |
| MVP 지원 형식 | `PDF / TXT / MD / DOCX` |
| OCR | MVP 제외 |
| 텍스트 추출 불가 문서 | 인덱싱 실패 처리 |

---

# 10. Chatbot

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/chat-conversations` | 챗봇 대화 목록 조회 | Authenticated |
| POST | `/chat-conversations` | 대화 생성 | Authenticated |
| GET | `/chat-conversations/{conversationId}` | 대화 상세 조회 | Authenticated |
| DELETE | `/chat-conversations/{conversationId}` | 대화 삭제 | Authenticated |
| GET | `/chat-conversations/{conversationId}/messages` | 메시지 목록 조회 | Authenticated |
| POST | `/chat-conversations/{conversationId}/messages` | 질문 전송 | Authenticated |
| GET | `/chat-messages/{messageId}/sources` | 답변 출처 조회 | Authenticated |

### 기본 정책

| 항목 | 기준 |
| --- | --- |
| 답변 범위 | 조직 범위 문서 기반 |
| 관련 문서 없음 | `NO_RELEVANT_SOURCE` |
| 범위 밖 질문 | `OUT_OF_SCOPE` |
| 출처 | `sources`에 문서/청크/페이지/스코어 포함 |
| 임의 생성 | 금지 |

---

# 11. Notifications

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/notifications` | 알림 목록 조회 | Authenticated |
| GET | `/notifications/{notificationId}` | 알림 상세 조회 | Authenticated |
| PATCH | `/notifications/{notificationId}/read` | 알림 읽음 처리 | Authenticated |
| PATCH | `/notifications/read-all` | 전체 읽음 처리 | Authenticated |
| DELETE | `/notifications/{notificationId}` | 알림 삭제 | Authenticated |

---

# 12. Reports

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/reports` | 보고서 목록 조회 | ROLE_SITE_ADMIN |
| POST | `/reports` | 보고서 생성 요청 | ROLE_SITE_ADMIN |
| GET | `/reports/{reportId}` | 보고서 상세 조회 | ROLE_SITE_ADMIN |
| GET | `/reports/{reportId}/items` | 보고서 항목 조회 | ROLE_SITE_ADMIN |
| GET | `/reports/{reportId}/files` | 보고서 파일 조회 | ROLE_SITE_ADMIN |
| GET | `/reports/{reportId}/download` | 보고서 다운로드 | ROLE_SITE_ADMIN |

---

# 13. Operation / Admin

운영 관리 API는 `ROLE_SITE_ADMIN` 전용이다.

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/admin/audit-logs` | 감사 로그 조회 | ROLE_SITE_ADMIN |
| GET | `/admin/action-logs` | 관리자 작업 로그 조회 | ROLE_SITE_ADMIN |
| GET | `/admin/operation-logs` | 운영 로그 조회 | ROLE_SITE_ADMIN |
| GET | `/admin/system-status` | 시스템 상태 조회 | ROLE_SITE_ADMIN |
| GET | `/admin/system-components` | 시스템 컴포넌트별 상태 조회 | ROLE_SITE_ADMIN |
| GET | `/admin/operation-policies` | 운영 정책 조회 | ROLE_SITE_ADMIN |
| PATCH | `/admin/operation-policies/{policyId}` | 운영 정책 수정 | ROLE_SITE_ADMIN |
| GET | `/admin/async-jobs` | 비동기 작업 목록 조회 | ROLE_SITE_ADMIN |
| GET | `/admin/async-jobs/{jobId}` | 비동기 작업 상세 조회 | ROLE_SITE_ADMIN |

---

## 13.1 로그 조회 공통 Query

`/admin/audit-logs`, `/admin/action-logs`, `/admin/operation-logs`는 아래 조회 조건을 공통으로 사용한다.

| Query | Type | Required | 설명 |
| --- | --- | --- | --- |
| `actorUserId` | Long | N | 작업 수행자 ID |
| `actionType` | String | N | 작업 유형 |
| `targetType` | String | N | 대상 유형 |
| `targetId` | Long | N | 대상 ID |
| `eventType` | String | N | 운영 로그 이벤트 유형 |
| `eventStatus` | String | N | 운영 로그 상태 |
| `logLevel` | String | N | `INFO / WARN / ERROR` |
| `sourceComponent` | String | N | `SPRING_API / AI_SERVER / MARIADB / REDIS / MINIO / CHROMA` |
| `requestId` | String | N | 요청 추적 ID |
| `startDate` | yyyy-MM-dd | N | 조회 시작일 |
| `endDate` | yyyy-MM-dd | N | 조회 종료일 |
| `page` | Int | N | 페이지 |
| `size` | Int | N | 크기 |
| `sort` | String | N | 정렬 |

### 운영 로그 Response 예시

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "operationLogId": 100,
        "eventType": "SYSTEM_COMPONENT_HEALTH_CHECK",
        "eventStatus": "SUCCESS",
        "logLevel": "INFO",
        "sourceComponent": "MINIO",
        "requestId": "req-20260506-abc123",
        "actorUserId": 1,
        "detailMessage": "MinIO 연결 정상",
        "relatedPath": "/api/v1/admin/system-components",
        "createdAt": "2026-05-06T10:20:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1
  },
  "message": "운영 로그를 조회했습니다."
}
```

---

## 13.2 GET `/admin/system-status`

시스템 전체 상태를 조회한다.

### Response

```json
{
  "success": true,
  "data": {
    "status": "NORMAL",
    "cpuUsage": 23.45,
    "memoryUsage": 61.12,
    "diskUsage": 48.9,
    "responseTimeMs": 32,
    "checkedAt": "2026-05-06T10:20:00"
  },
  "message": "시스템 상태를 조회했습니다."
}
```

---

## 13.3 GET `/admin/system-components`

시스템 컴포넌트별 상태를 조회한다.

응답에는 기본적으로 아래 컴포넌트가 포함되어야 한다.

- `SPRING_API`
- `AI_SERVER`
- `MARIADB`
- `REDIS`
- `MINIO`
- `CHROMA`

특정 컴포넌트 상태 수집에 실패해도 API 전체는 실패하지 않는다. 실패한 컴포넌트만 `ERROR` 또는 `UNKNOWN`으로 반환한다.

### Response

```json
{
  "success": true,
  "data": [
    {
      "componentType": "SPRING_API",
      "componentName": "Spring API",
      "status": "NORMAL",
      "message": "Spring API 정상",
      "cpuUsage": null,
      "memoryUsage": null,
      "diskUsage": null,
      "hostName": "localhost",
      "instanceId": "spring-local-1",
      "responseTimeMs": 5,
      "checkedAt": "2026-05-06T10:20:00"
    },
    {
      "componentType": "MINIO",
      "componentName": "MinIO",
      "status": "NORMAL",
      "message": "MinIO 연결 정상",
      "cpuUsage": null,
      "memoryUsage": null,
      "diskUsage": null,
      "hostName": "localhost",
      "instanceId": "minio-local-1",
      "responseTimeMs": 12,
      "checkedAt": "2026-05-06T10:20:00"
    },
    {
      "componentType": "CHROMA",
      "componentName": "ChromaDB",
      "status": "NORMAL",
      "message": "ChromaDB 연결 정상",
      "cpuUsage": null,
      "memoryUsage": null,
      "diskUsage": null,
      "hostName": "localhost",
      "instanceId": "chroma-local-1",
      "responseTimeMs": 18,
      "checkedAt": "2026-05-06T10:20:00"
    }
  ],
  "message": "시스템 컴포넌트 상태를 조회했습니다."
}
```

### 필드 설명

| Field | Type | 설명 |
| --- | --- | --- |
| `componentType` | String | 컴포넌트 타입 |
| `componentName` | String | 화면 표시용 이름 |
| `status` | String | `NORMAL / WARNING / ERROR / UNKNOWN` |
| `message` | String | 상태 메시지 |
| `cpuUsage` | Decimal | 수집 불가 시 null |
| `memoryUsage` | Decimal | 수집 불가 시 null |
| `diskUsage` | Decimal | 수집 불가 시 null |
| `hostName` | String | 호스트명 |
| `instanceId` | String | 인스턴스 ID |
| `responseTimeMs` | Int | 상태 체크 응답 시간 |
| `checkedAt` | String | 점검 시각 |

### 상태 체크 기준

| componentType | 정상 기준 | 실패 기준 |
| --- | --- | --- |
| `SPRING_API` | Spring API 정상 응답 | 상태 수집 실패 |
| `AI_SERVER` | AI 서버 health 정상 응답 | 연결 실패, timeout, non-2xx |
| `MARIADB` | DB 연결 또는 간단 query 성공 | 연결 실패, timeout |
| `REDIS` | Redis ping 성공 | 연결 실패, timeout |
| `MINIO` | `MinioClient.listBuckets()` 또는 bucket check 성공 | 연결 실패, 인증 실패, timeout |
| `CHROMA` | ChromaDB `/api/v1/heartbeat` 정상 응답 | 연결 실패, timeout, non-2xx |

### 검증 기준

| 조건 | 처리 |
| --- | --- |
| MinIO 연결 정상 | `MINIO / NORMAL` |
| MinIO 연결 실패 | `MINIO / ERROR` 또는 `UNKNOWN` |
| ChromaDB 연결 정상 | `CHROMA / NORMAL` |
| ChromaDB 연결 실패 | `CHROMA / ERROR` 또는 `UNKNOWN` |
| 캐시/DB에 상태값 없음 | 기본 컴포넌트 `UNKNOWN` fallback |
| 일부 컴포넌트 실패 | API 전체 200, 실패 컴포넌트만 오류 상태 |
| 권한 없는 사용자 접근 | 403 |

---

## 13.4 GET `/admin/operation-policies`

운영 정책 목록을 조회한다.

| Query | Type | Required | 설명 |
| --- | --- | --- | --- |
| `policyCategory` | String | N | 정책 카테고리 |
| `isActive` | Boolean | N | 활성 여부 |
| `page` | Int | N | 페이지 |
| `size` | Int | N | 크기 |

### Response

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "policyId": 1,
        "policyCategory": "MONITORING",
        "policyKey": "CHROMA_HEALTH_TIMEOUT_MS",
        "policyName": "ChromaDB health check timeout",
        "policyValue": "3000",
        "valueType": "NUMBER",
        "description": "ChromaDB 상태 체크 timeout. 단위 ms",
        "isActive": true,
        "updatedAt": "2026-05-06T10:20:00",
        "updatedBy": 1
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1
  },
  "message": "운영 정책 목록을 조회했습니다."
}
```

---

## 13.5 PATCH `/admin/operation-policies/{policyId}`

운영 정책을 수정한다. 정책 수정은 관리자 작업 로그 또는 감사 로그에 기록한다.

### Request

```json
{
  "policyValue": "3000",
  "description": "ChromaDB 상태 체크 timeout. 단위 ms",
  "isActive": true,
  "reason": "운영 모니터링 ChromaDB 상태 체크 timeout 기준 설정"
}
```

### Response

```json
{
  "success": true,
  "data": {
    "policyId": 1,
    "policyCategory": "MONITORING",
    "policyKey": "CHROMA_HEALTH_TIMEOUT_MS",
    "policyValue": "3000",
    "valueType": "NUMBER",
    "isActive": true,
    "updatedAt": "2026-05-06T10:25:00",
    "updatedBy": 1
  },
  "message": "운영 정책이 수정되었습니다."
}
```

---

## 13.6 GET `/admin/async-jobs`

비동기 작업 목록을 조회한다.

| Query | Type | Required | 설명 |
| --- | --- | --- | --- |
| `jobType` | String | N | 작업 유형 |
| `jobStatus` | String | N | 작업 상태 |
| `targetType` | String | N | 대상 유형 |
| `targetId` | Long | N | 대상 ID |
| `startDate` | yyyy-MM-dd | N | 시작일 |
| `endDate` | yyyy-MM-dd | N | 종료일 |
| `page` | Int | N | 페이지 |
| `size` | Int | N | 크기 |

### Response

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "jobId": 3001,
        "jobType": "DOCUMENT_INDEXING",
        "jobStatus": "PROCESSING",
        "targetType": "DOCUMENT_VERSION",
        "targetId": 2001,
        "errorMessage": null,
        "createdAt": "2026-05-06T10:00:00",
        "completedAt": null
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1
  },
  "message": "비동기 작업 목록을 조회했습니다."
}
```

---

## 13.7 GET `/admin/async-jobs/{jobId}`

비동기 작업 상세를 조회한다.

### Response

```json
{
  "success": true,
  "data": {
    "jobId": 3002,
    "jobType": "MODEL_MEMORY_BANK_GENERATION",
    "jobStatus": "COMPLETED",
    "targetType": "MODEL_VERSION",
    "targetId": 12,
    "errorMessage": null,
    "createdAt": "2026-05-06T10:10:00",
    "completedAt": "2026-05-06T10:15:00"
  },
  "message": "비동기 작업 상세를 조회했습니다."
}
```

---

# 14. Files

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/files/{fileId}` | 파일 메타데이터 조회 | Authenticated |
| GET | `/files/{fileId}/download` | 파일 다운로드 | Authenticated |
| DELETE | `/files/{fileId}` | 파일 삭제 | ROLE_COMPANY_ADMIN 이상 |

---

# 15. Models

모델 관리 API는 `ROLE_SITE_ADMIN` 전용이다.

이번 API 명세에서 말하는 모델 생성은 일반적인 딥러닝 재학습이 아니라 PatchCore `memory_bank` 생성 작업이다.

- `ckpt/config`는 고정 모델 프로필에서 재사용한다.
- 고객사/검사대상별로 달라지는 산출물은 `memory_bank`다.
- DINOv2는 `PERFORMANCE` 프로필로 사용한다.
- WideResNet50은 `SPEED` 프로필로 사용한다.

| modelProfile | modelCategory | 모델 계열 | 입력 크기 |
| --- | --- | --- | --- |
| `SPEED` | `OBJECT` | WideResNet50 + PatchCore | `224x224` |
| `SPEED` | `TEXTURE` | WideResNet50 + PatchCore | `256x256` |
| `PERFORMANCE` | `OBJECT` | DINOv2-base + PatchCore | `336x336` |
| `PERFORMANCE` | `TEXTURE` | DINOv2-base + PatchCore | `448x448` |

PatchCore 계열 모델은 `CKPT`, `CONFIG`, `MEMORY_BANK` 산출물이 모두 있어야 배포 가능하다.

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/models` | 모델 목록 조회 | ROLE_SITE_ADMIN |
| POST | `/models` | 모델 기본 정보 등록 | ROLE_SITE_ADMIN |
| GET | `/models/{modelId}` | 모델 상세 조회 | ROLE_SITE_ADMIN |
| GET | `/models/{modelId}/versions` | 모델 버전 목록 조회 | ROLE_SITE_ADMIN |
| POST | `/models/{modelId}/versions` | 모델 버전 및 산출물 수동 업로드 | ROLE_SITE_ADMIN |
| POST | `/models/{modelId}/versions/from-normal-images` | 정상 이미지셋 기반 memory bank 생성 및 모델 버전/배포 자동 생성 | ROLE_SITE_ADMIN |
| GET | `/model-versions/{versionId}` | 모델 버전 상세 조회 | ROLE_SITE_ADMIN |
| GET | `/model-versions/{versionId}/artifacts` | 모델 버전 산출물 목록 조회 | ROLE_SITE_ADMIN |
| PATCH | `/model-versions/{versionId}/activate` | 모델 버전 활성화 | ROLE_SITE_ADMIN |
| PATCH | `/model-versions/{versionId}/deprecate` | 모델 버전 사용 중단 | ROLE_SITE_ADMIN |
| GET | `/model-deployments` | 모델 배포 목록 조회 | ROLE_SITE_ADMIN |
| POST | `/model-versions/{versionId}/deployments` | 조직/검사대상에 모델 배포 | ROLE_SITE_ADMIN |
| PATCH | `/model-deployments/{deploymentId}/deactivate` | 모델 배포 비활성화 | ROLE_SITE_ADMIN |
| PATCH | `/model-deployments/{deploymentId}/rollback` | 이전 모델 배포로 롤백 | ROLE_SITE_ADMIN |

---

## 15.1 GET `/models`

| Query | Type | Required | 설명 |
| --- | --- | --- | --- |
| `modelType` | String | N | 모델 계열 |
| `page` | Int | N | 페이지 |
| `size` | Int | N | 크기 |

### Response

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "modelId": 1,
        "modelName": "PatchCore Texture Detector",
        "modelType": "PATCHCORE",
        "description": "Texture 카테고리 이상탐지 모델",
        "createdAt": "2026-05-03T10:00:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1
  },
  "message": "모델 목록을 조회했습니다."
}
```

---

## 15.2 POST `/models`

### Request

```json
{
  "modelName": "PatchCore Texture Detector",
  "modelType": "PATCHCORE",
  "description": "Texture 카테고리 이상탐지 모델"
}
```

### Response

```json
{
  "success": true,
  "data": {
    "modelId": 1,
    "modelName": "PatchCore Texture Detector",
    "modelType": "PATCHCORE"
  },
  "message": "모델이 등록되었습니다."
}
```

---

## 15.3 GET `/models/{modelId}`

### Response

```json
{
  "success": true,
  "data": {
    "modelId": 1,
    "modelName": "PatchCore Texture Detector",
    "modelType": "PATCHCORE",
    "description": "Texture 카테고리 이상탐지 모델",
    "createdAt": "2026-05-03T10:00:00"
  },
  "message": "모델 상세를 조회했습니다."
}
```

---

## 15.4 GET `/models/{modelId}/versions`

| Query | Type | Required | 설명 |
| --- | --- | --- | --- |
| `modelCategory` | String | N | `OBJECT / TEXTURE` |
| `modelProfile` | String | N | `SPEED / PERFORMANCE` |
| `deployStatus` | String | N | `REGISTERED / VALIDATED / DEPLOYED / DEPRECATED` |
| `isActive` | Boolean | N | 활성 여부 |
| `page` | Int | N | 페이지 |
| `size` | Int | N | 크기 |

### Response

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "modelVersionId": 10,
        "modelId": 1,
        "versionName": "v1.0.0-texture-performance",
        "modelCategory": "TEXTURE",
        "modelProfile": "PERFORMANCE",
        "framework": "PYTORCH",
        "inputSize": "448x448",
        "thresholdDefault": 0.75,
        "deployStatus": "REGISTERED",
        "isActive": false,
        "createdAt": "2026-05-03T10:00:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1
  },
  "message": "모델 버전 목록을 조회했습니다."
}
```

---

## 15.5 POST `/models/{modelId}/versions`

이미 생성된 `ckpt/config/memory_bank` 산출물을 직접 등록하는 수동 등록 API다.

`multipart/form-data`

| Field | Type | Required | 설명 |
| --- | --- | --- | --- |
| `ckptFile` | File | Y | 모델 가중치 또는 feature extractor 산출물 |
| `configFile` | File | Y | 모델 설정 파일 |
| `memoryBankFile` | File | Y | PatchCore memory bank 파일 |
| `labelsFile` | File | N | 라벨/클래스 매핑 파일 |
| `versionName` | String | Y | 모델 버전명 |
| `modelCategory` | String | Y | `OBJECT / TEXTURE` |
| `modelProfile` | String | Y | `SPEED / PERFORMANCE` |
| `framework` | String | N | 예: `PYTORCH` |
| `inputSize` | String | N | 예: `448x448` |
| `thresholdDefault` | Decimal | N | 기본 이상 점수 임계값 |
| `accuracy` | Decimal | N | 정확도 |
| `precisionScore` | Decimal | N | 정밀도 |
| `recallScore` | Decimal | N | 재현율 |
| `f1Score` | Decimal | N | F1 점수 |
| `aurocScore` | Decimal | N | AUROC 점수 |

### Response

```json
{
  "success": true,
  "data": {
    "modelVersionId": 10,
    "modelId": 1,
    "versionName": "v1.0.0-texture-performance",
    "modelCategory": "TEXTURE",
    "modelProfile": "PERFORMANCE",
    "framework": "PYTORCH",
    "inputSize": "448x448",
    "thresholdDefault": 0.75,
    "deployStatus": "REGISTERED",
    "isActive": false,
    "artifacts": [
      {
        "artifactType": "CKPT",
        "fileId": 501
      },
      {
        "artifactType": "CONFIG",
        "fileId": 502
      },
      {
        "artifactType": "MEMORY_BANK",
        "fileId": 503
      }
    ]
  },
  "message": "모델 버전이 등록되었습니다."
}
```

---

## 15.6 GET `/model-versions/{versionId}`

### Response

```json
{
  "success": true,
  "data": {
    "modelVersionId": 10,
    "modelId": 1,
    "modelName": "PatchCore Texture Detector",
    "versionName": "v1.0.0-texture-performance",
    "modelCategory": "TEXTURE",
    "modelProfile": "PERFORMANCE",
    "framework": "PYTORCH",
    "inputSize": "448x448",
    "thresholdDefault": 0.75,
    "deployStatus": "REGISTERED",
    "isActive": false,
    "validatedAt": null,
    "validatedBy": null,
    "createdAt": "2026-05-03T10:00:00"
  },
  "message": "모델 버전을 조회했습니다."
}
```

---

## 15.7 GET `/model-versions/{versionId}/artifacts`

### Response

```json
{
  "success": true,
  "data": [
    {
      "artifactType": "CKPT",
      "fileId": 501,
      "fileName": "model.ckpt",
      "objectKey": "models/1/versions/10/model.ckpt"
    },
    {
      "artifactType": "CONFIG",
      "fileId": 502,
      "fileName": "config.json",
      "objectKey": "models/1/versions/10/config.json"
    },
    {
      "artifactType": "MEMORY_BANK",
      "fileId": 503,
      "fileName": "memory_bank.pt",
      "objectKey": "models/1/versions/10/memory_bank.pt"
    }
  ],
  "message": "모델 산출물 목록을 조회했습니다."
}
```

---

## 15.8 PATCH `/model-versions/{versionId}/activate`

모델 버전을 배포 가능한 상태로 활성화한다. PatchCore 계열은 `CKPT`, `CONFIG`, `MEMORY_BANK` 산출물이 모두 필요하다.

### Request

```json
{
  "reason": "기본 검증 완료 후 배포 가능 상태로 전환"
}
```

### Response

```json
{
  "success": true,
  "data": {
    "modelVersionId": 10,
    "deployStatus": "VALIDATED",
    "isActive": true
  },
  "message": "모델 버전이 활성화되었습니다."
}
```

---

## 15.9 PATCH `/model-versions/{versionId}/deprecate`

### Request

```json
{
  "reason": "신규 버전 배포로 인한 사용 중단"
}
```

### Response

```json
{
  "success": true,
  "data": {
    "modelVersionId": 10,
    "deployStatus": "DEPRECATED",
    "isActive": false
  },
  "message": "모델 버전이 사용 중단되었습니다."
}
```

---

## 15.10 GET `/model-deployments`

| Query | Type | Required | 설명 |
| --- | --- | --- | --- |
| `organizationId` | Long | N | 조직 ID |
| `targetId` | Long | N | 검사대상 ID |
| `modelVersionId` | Long | N | 모델 버전 ID |
| `deploymentScope` | String | N | `ORGANIZATION / TARGET` |
| `isActive` | Boolean | N | 활성 배포 여부 |
| `page` | Int | N | 페이지 |
| `size` | Int | N | 크기 |

### Response

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "deploymentId": 3001,
        "organizationId": 1001,
        "targetId": 10,
        "modelVersionId": 10,
        "modelName": "PatchCore Texture Detector",
        "versionName": "v1.0.0-texture-performance",
        "deploymentScope": "TARGET",
        "deployStatus": "DEPLOYED",
        "isActive": true,
        "deployedAt": "2026-05-03T10:10:00",
        "deployedBy": 1,
        "reason": "고객사 A 프레스 검사대상에 Texture 성능형 모델 적용"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1
  },
  "message": "모델 배포 목록을 조회했습니다."
}
```

---

## 15.11 POST `/model-versions/{versionId}/deployments`

모델 버전을 조직 또는 특정 검사대상에 배포한다. 동일 범위에 기존 활성 배포가 있으면 기존 배포를 비활성화하고 신규 배포를 활성화한다.

### Request

```json
{
  "organizationId": 1001,
  "targetId": 10,
  "deploymentScope": "TARGET",
  "reason": "고객사 A 프레스 검사대상에 Texture 성능형 모델 적용"
}
```

### Response

```json
{
  "success": true,
  "data": {
    "deploymentId": 3001,
    "modelVersionId": 10,
    "organizationId": 1001,
    "targetId": 10,
    "deploymentScope": "TARGET",
    "deployStatus": "DEPLOYED",
    "isActive": true,
    "deployedAt": "2026-05-03T10:10:00"
  },
  "message": "모델이 배포되었습니다."
}
```

---

## 15.12 PATCH `/model-deployments/{deploymentId}/deactivate`

### Request

```json
{
  "reason": "운영 테스트 종료"
}
```

### Response

```json
{
  "success": true,
  "data": {
    "deploymentId": 3001,
    "deployStatus": "DEACTIVATED",
    "isActive": false
  },
  "message": "모델 배포가 비활성화되었습니다."
}
```

---

## 15.13 PATCH `/model-deployments/{deploymentId}/rollback`

### Request

```json
{
  "rollbackToDeploymentId": 2999,
  "reason": "신규 모델 적용 후 오탐 증가"
}
```

### Response

```json
{
  "success": true,
  "data": {
    "deploymentId": 3002,
    "modelVersionId": 9,
    "organizationId": 1001,
    "targetId": 10,
    "deploymentScope": "TARGET",
    "deployStatus": "DEPLOYED",
    "isActive": true,
    "rollbackFromDeploymentId": 3001
  },
  "message": "이전 모델로 롤백되었습니다."
}
```

---

## 15.14 POST `/models/{modelId}/versions/from-normal-images`

정상 이미지셋을 업로드하여 `memory_bank`를 생성하고, 고정 `ckpt/config`와 조합해 모델 버전 및 배포를 자동 생성한다.

- 검사 업로드 API가 아니다.
- 검사용 이미지 업로드는 `/inspections/upload`를 사용한다.
- `modelProfile` 미전송 시 `SPEED`, `PERFORMANCE`를 모두 생성한다.
- `modelProfile` 전송 시 해당 프로필만 생성한다.

`multipart/form-data`

| Field | Type | Required | 설명 |
| --- | --- | --- | --- |
| `normalImages` | File[] | Y | 정상 이미지 파일 목록. 최소 10장 |
| `modelCategory` | String | Y | `OBJECT / TEXTURE` |
| `modelProfile` | String | N | `SPEED / PERFORMANCE` |
| `organizationId` | Long | Y | 조직 ID |
| `targetId` | Long | N | 검사대상 ID |
| `deploymentScope` | String | Y | `ORGANIZATION / TARGET` |
| `versionName` | String | N | 모델 버전명 |
| `thresholdDefault` | Decimal | N | 기본 이상 점수 임계값 |
| `reason` | String | N | 생성 및 배포 사유 |

### 요청 예시: 전체 생성

```text
normalImages=normal_001.jpg
normalImages=normal_002.jpg
...
normalImages=normal_010.jpg
modelCategory=TEXTURE
organizationId=1001
targetId=10
deploymentScope=TARGET
thresholdDefault=0.7500
reason=고객사 A 프레스 검사대상 정상 이미지셋 기준 memory bank 생성
```

### Response 예시: 전체 생성

```json
{
  "success": true,
  "data": {
    "modelId": 1,
    "modelCategory": "TEXTURE",
    "normalImageCount": 10,
    "createdVersions": [
      {
        "modelProfile": "SPEED",
        "modelVersionId": 11,
        "versionName": "v1.0.0-texture-speed-org1001-target10",
        "inputSize": "256x256",
        "deployStatus": "DEPLOYED",
        "isActive": true,
        "memoryBankFileId": 601,
        "deploymentId": 3002
      },
      {
        "modelProfile": "PERFORMANCE",
        "modelVersionId": 12,
        "versionName": "v1.0.0-texture-performance-org1001-target10",
        "inputSize": "448x448",
        "deployStatus": "DEPLOYED",
        "isActive": true,
        "memoryBankFileId": 602,
        "deploymentId": 3003
      }
    ]
  },
  "message": "정상 이미지셋 기반 속도형/성능형 모델 버전이 생성되고 배포되었습니다."
}
```

### 요청 예시: 단일 profile 생성

```text
normalImages=normal_001.jpg
normalImages=normal_002.jpg
...
normalImages=normal_010.jpg
modelCategory=TEXTURE
modelProfile=PERFORMANCE
organizationId=1001
targetId=10
deploymentScope=TARGET
versionName=v1.0.0-texture-performance-org1001-target10
thresholdDefault=0.7500
reason=성능형 memory bank 재생성
```

### 검증 기준

| 조건 | 실패 처리 |
| --- | --- |
| `normalImages` 누락 | 400 |
| 정상 이미지 파일 10개 미만 | 422 |
| 지원하지 않는 이미지 MIME | 422 |
| 손상 이미지 포함 | 422 |
| `modelCategory` 누락 | 400 |
| `modelCategory` 허용값 아님 | 422 |
| `modelProfile` 허용값 아님 | 422 |
| 고정 `ckpt/config` 매핑 없음 | 404 |
| 존재하지 않는 `modelId` | 404 |
| 존재하지 않는 `organizationId` | 404 |
| `deploymentScope` 누락 | 400 |
| `deploymentScope` 허용값 아님 | 422 |
| `deploymentScope=TARGET`인데 `targetId` 누락 | 422 |
| `targetId`가 해당 조직 소속 아님 | 422 |
| `thresholdDefault`가 0~1 범위 밖 | 422 |
| 중복 `versionName` | 409 |
| MinIO 저장 실패 | 500 |
| FastAPI memory bank 생성 실패 | 500 |
| 모델 버전/산출물/배포 생성 실패 | 500 |

---

# 16. Dashboard

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/dashboard/overview` | 대시보드 전체 요약 조회 | Authenticated |

---

# 17. Internal FastAPI

FastAPI 내부 API는 Spring 전용이다. 외부 사용자와 프론트엔드는 직접 호출하지 않는다.

- 최종 인증/권한 검증은 Spring이 담당한다.
- Spring은 FastAPI 호출 시 `X-Request-Id`를 전달한다.
- FastAPI는 동일 request-id를 로그에 남긴다.

| Method | Endpoint | 설명 | 호출 주체 |
| --- | --- | --- | --- |
| GET | `/system-status` | AI 서버 상태 조회 | Spring |
| POST | `/vision/infer-image` | 이미지 추론 | Spring |
| POST | `/vision/infer-video` | 영상 추론 | Spring |
| POST | `/vision/infer-frame` | 실시간 프레임 추론 | Spring |
| POST | `/models/memory-bank` | 정상 이미지 파일 키 목록과 고정 ckpt/config를 받아 memory_bank 생성 | Spring |
| POST | `/documents/index` | 문서 인덱싱 비동기 job 등록 | Spring |
| GET | `/document-index-jobs/{aiJobId}` | 문서 인덱싱 job 상태 조회 | Spring |
| DELETE | `/document-versions/{documentVersionId}/index` | 문서 검색 인덱스 삭제 | Spring |
| POST | `/rag/query` | 조직 범위 문서 기반 RAG 질의 | Spring |

---

## 17.1 POST `/documents/index`

문서 인덱싱 비동기 job을 등록한다.

### Request

```json
{
  "indexJobId": 3001,
  "documentId": 1001,
  "documentVersionId": 2001,
  "organizationId": 1001,
  "file": {
    "fileId": 501,
    "fileKey": "documents/org-1001/doc-1001/v1/manual.pdf",
    "fileName": "manual.pdf",
    "mimeType": "application/pdf",
    "checksum": "sha256:..."
  },
  "metadata": {
    "title": "프레스 설비 점검 매뉴얼",
    "documentType": "MANUAL",
    "category": "MAINTENANCE",
    "equipmentType": "PRESS",
    "tags": ["프레스", "점검", "장애대응"]
  },
  "chunking": {
    "chunkSize": 800,
    "chunkOverlap": 120
  },
  "embedding": {
    "embeddingModel": "default"
  }
}
```

### Response

```json
{
  "success": true,
  "data": {
    "aiJobId": "doc-index-3001",
    "indexJobId": 3001,
    "documentId": 1001,
    "documentVersionId": 2001,
    "organizationId": 1001,
    "indexingStatus": "PROCESSING",
    "collectionName": "documents_org_1001",
    "queuedAt": "2026-05-05T10:00:00"
  },
  "message": "문서 인덱싱 작업이 등록되었습니다."
}
```

### 검증 기준

| 조건 | 실패 처리 |
| --- | --- |
| 필수 ID 누락 | 400 |
| `file.fileKey` 누락 | 400 |
| 지원하지 않는 MIME | 422 |
| `chunkSize <= 0` | 422 |
| `chunkOverlap < 0` | 422 |
| `chunkOverlap >= chunkSize` | 422 |
| Redis queue 등록 실패 | 500 |

---

## 17.2 GET `/document-index-jobs/{aiJobId}`

문서 인덱싱 job 상태를 조회한다. 실패 상태도 조회 성공이면 `success: true`로 반환하고, `indexingStatus=FAILED`, `errorMessage`에 사유를 담는다.

### Response

```json
{
  "success": true,
  "data": {
    "aiJobId": "doc-index-3001",
    "indexJobId": 3001,
    "documentId": 1001,
    "documentVersionId": 2001,
    "organizationId": 1001,
    "indexingStatus": "COMPLETED",
    "collectionName": "documents_org_1001",
    "indexedChunkCount": 12,
    "chunks": [
      {
        "sequenceNo": 1,
        "content": "점검 전 전원을 차단하고...",
        "pageNo": 1,
        "section": "안전 수칙",
        "vectorRef": "document_version:2001:chunk:1"
      }
    ],
    "errorMessage": null,
    "startedAt": "2026-05-05T10:00:02",
    "completedAt": "2026-05-05T10:00:20"
  },
  "message": "문서 인덱싱 작업 상태를 조회했습니다."
}
```

---

## 17.3 DELETE `/document-versions/{documentVersionId}/index`

문서 버전 기준으로 ChromaDB vector를 삭제한다.

### Request

```json
{
  "organizationId": 1001,
  "documentId": 1001,
  "reason": "문서 삭제로 인한 검색 인덱스 제거"
}
```

### Response

```json
{
  "success": true,
  "data": {
    "documentId": 1001,
    "documentVersionId": 2001,
    "organizationId": 1001,
    "collectionName": "documents_org_1001",
    "deletedVectorCount": 12,
    "deindexedAt": "2026-05-05T10:30:00"
  },
  "message": "문서 검색 인덱스가 삭제되었습니다."
}
```

---

## 17.4 POST `/rag/query`

조직 범위 문서 기반 RAG 질의를 수행한다. 검색 결과가 없으면 임의 답변을 만들지 않고 `NO_RELEVANT_SOURCE`를 반환한다.

### Request

```json
{
  "question": "컨베이어 정렬이 틀어졌을 때 점검 순서는?",
  "userId": 1,
  "organizationId": 1001,
  "conversationId": 7001,
  "resultContext": {
    "resultId": 3001,
    "inspectionId": 2001,
    "decisionCode": "RECHECK",
    "score": 0.62,
    "confidence": 0.71,
    "equipmentName": "컨베이어 #1",
    "targetId": 10,
    "anomalySummary": "벨트 좌측 정렬 이상 의심"
  },
  "topK": 5
}
```

### Response

```json
{
  "success": true,
  "data": {
    "answer": "먼저 컨베이어 가이드 위치와 체인 장력을 확인하세요...",
    "answerStatus": "ANSWERED",
    "questionMode": "RESULT_LINKED",
    "sources": [
      {
        "documentId": 1001,
        "documentVersionId": 2001,
        "documentTitle": "컨베이어 점검 매뉴얼",
        "chunkId": 301,
        "pageNo": 3,
        "section": "정렬 점검",
        "score": 0.8721,
        "sourceSnippet": "컨베이어 정렬 이상 시..."
      }
    ],
    "llmModel": "ollama-default",
    "createdAt": "2026-05-05T10:40:00"
  },
  "message": "문서 기반 답변이 생성되었습니다."
}
```

---

## 17.5 POST `/vision/infer-image`

이미지 1장을 추론한다.

### Request

```json
{
  "inspectionId": 1001,
  "fileKey": "inspections/1001/original/sample.jpg",
  "targetId": 1,
  "model": {
    "modelVersionId": 10,
    "modelCategory": "TEXTURE",
    "modelProfile": "PERFORMANCE",
    "framework": "PYTORCH",
    "inputSize": "448x448",
    "ckptFileKey": "models/1/versions/10/model.ckpt",
    "configFileKey": "models/1/versions/10/config.json",
    "memoryBankFileKey": "models/1/versions/10/memory_bank.pt",
    "labelsFileKey": null
  },
  "roi": {
    "roiMode": "FULL_FRAME",
    "roiCoordinateType": "NORMALIZED"
  },
  "qualityGateEnabled": true,
  "threshold": {
    "anomalyThreshold": 0.75,
    "lowConfidenceThreshold": 0.55
  }
}
```

### Response

```json
{
  "success": true,
  "data": {
    "inspectionId": 1001,
    "modelVersionId": 10,
    "score": 0.8123,
    "confidence": 0.91,
    "decisionCode": "DEFECT",
    "quality": {
      "status": "PASSED",
      "reason": null
    },
    "artifacts": [
      {
        "artifactType": "HEATMAP",
        "fileKey": "inspections/1001/artifacts/heatmap.png"
      }
    ],
    "regions": [],
    "processedAt": "2026-05-03T10:00:03"
  },
  "message": "이미지 추론이 완료되었습니다."
}
```

### 검증 기준

| 조건 | 실패 처리 |
| --- | --- |
| 이미지 파일 키 누락 | 400 |
| 모델 버전 ID 누락 | 400 |
| `ckptFileKey` 누락 | 400 |
| `configFileKey` 누락 | 400 |
| `memoryBankFileKey` 누락 | 400 |
| 모델 카테고리/프로필 허용값 오류 | 422 |
| threshold 범위 오류 | 422 |
| ROI 좌표 오류 | 422 |
| 이미지 또는 모델 산출물 파일 없음 | 404 |
| 모델 로드 실패 | 500 |
| memory bank 로드 실패 | 500 |
| 추론 실패 | 500 |

---

## 17.6 POST `/vision/infer-video`

영상 파일을 추론한다. MVP 프론트에서는 사용하지 않는다.

### Request

```json
{
  "inspectionId": 1001,
  "fileKey": "inspections/1001/original/sample.mp4",
  "targetId": 1,
  "model": {
    "modelVersionId": 10,
    "modelCategory": "TEXTURE",
    "modelProfile": "PERFORMANCE",
    "framework": "PYTORCH",
    "inputSize": "448x448",
    "ckptFileKey": "models/1/versions/10/model.ckpt",
    "configFileKey": "models/1/versions/10/config.json",
    "memoryBankFileKey": "models/1/versions/10/memory_bank.pt"
  },
  "samplingFps": 1.0,
  "maxFrames": 60,
  "roi": {
    "roiMode": "FIXED",
    "roiCoordinateType": "NORMALIZED",
    "roiX": 0.25,
    "roiY": 0.2,
    "roiWidth": 0.5,
    "roiHeight": 0.5
  },
  "qualityGateEnabled": true,
  "threshold": {
    "anomalyThreshold": 0.75,
    "lowConfidenceThreshold": 0.55
  }
}
```

### Response

```json
{
  "success": true,
  "data": {
    "inspectionId": 1001,
    "modelVersionId": 10,
    "finalDecisionCode": "RECHECK",
    "analyzedFrameCount": 48,
    "skippedFrameCount": 12,
    "defectFrameCount": 0,
    "recheckFrameCount": 12,
    "maxFrameScore": 0.5512,
    "avgFrameScore": 0.2304,
    "representativeFrameSeq": 17,
    "inputQualityStatus": "WARNING",
    "inputQualityReason": "SOME_FRAMES_RECHECK",
    "artifacts": [
      {
        "artifactType": "HEATMAP",
        "fileKey": "inspections/1001/artifacts/representative_heatmap.png"
      }
    ],
    "processedAt": "2026-05-03T10:00:30"
  },
  "message": "영상 추론이 완료되었습니다."
}
```

---

## 17.7 POST `/vision/infer-frame`

실시간 세션에서 프레임 1장을 추론한다. MVP 프론트에서는 사용하지 않는다.

### Request

```json
{
  "inspectionId": 2001,
  "frameSeq": 17,
  "frameFileKey": "inspections/2001/tmp/frame_17.jpg",
  "targetId": 1,
  "model": {
    "modelVersionId": 10,
    "modelCategory": "TEXTURE",
    "modelProfile": "PERFORMANCE",
    "framework": "PYTORCH",
    "inputSize": "448x448",
    "ckptFileKey": "models/1/versions/10/model.ckpt",
    "configFileKey": "models/1/versions/10/config.json",
    "memoryBankFileKey": "models/1/versions/10/memory_bank.pt"
  },
  "roi": {
    "roiMode": "FIXED",
    "roiCoordinateType": "NORMALIZED",
    "roiX": 0.25,
    "roiY": 0.2,
    "roiWidth": 0.5,
    "roiHeight": 0.5
  },
  "qualityGateEnabled": true,
  "threshold": {
    "anomalyThreshold": 0.75,
    "lowConfidenceThreshold": 0.55
  }
}
```

### Response

```json
{
  "success": true,
  "data": {
    "inspectionId": 2001,
    "frameSeq": 17,
    "modelVersionId": 10,
    "score": 0.5512,
    "confidence": 0.42,
    "decisionCode": "RECHECK",
    "quality": {
      "status": "FAILED",
      "reason": "TOO_DARK"
    },
    "artifacts": [
      {
        "artifactType": "HEATMAP",
        "fileKey": "inspections/2001/frames/17/heatmap.png"
      }
    ],
    "processedAt": "2026-05-03T10:00:03"
  },
  "message": "프레임 추론이 완료되었습니다."
}
```

---

## 17.8 POST `/models/memory-bank`

정상 이미지 파일 키 목록과 고정 `ckpt/config` 파일 키를 받아 `memory_bank`를 생성한다.

FastAPI는 `memory_bank` 생성과 MinIO 업로드까지만 담당한다. `MODEL_VERSION`, `MODEL_ARTIFACT`, `MODEL_DEPLOYMENT` 생성은 Spring이 담당한다.

### Request

```json
{
  "modelCategory": "TEXTURE",
  "modelProfile": "PERFORMANCE",
  "normalImageFileKeys": [
    "models/tmp/normal/org-1001/target-10/job-abc/normal_001.jpg",
    "models/tmp/normal/org-1001/target-10/job-abc/normal_002.jpg",
    "...",
    "models/tmp/normal/org-1001/target-10/job-abc/normal_010.jpg"
  ],
  "configFileKey": "models/base/performance-texture/config.json",
  "ckptFileKey": "models/base/performance-texture/model.ckpt",
  "outputPrefix": "models/generated/org-1001/target-10/performance-texture/job-abc"
}
```

### Response

```json
{
  "success": true,
  "data": {
    "memoryBankFileKey": "models/generated/org-1001/target-10/performance-texture/job-abc/memory_bank.pt",
    "normalImageCount": 10,
    "modelCategory": "TEXTURE",
    "modelProfile": "PERFORMANCE",
    "createdAt": "2026-05-04T10:10:00"
  },
  "message": "메모리뱅크가 생성되었습니다."
}
```

### 처리 요약

```text
1. MinIO에서 config, ckpt, 정상 이미지 다운로드
2. profile별 generator 선택
   - PERFORMANCE: DINOv2 PatchCore
   - SPEED: WideResNet50 PatchCore
3. 정상 이미지 feature 추출
4. memory_bank.pt 생성 및 outputPrefix에 업로드
5. memoryBankFileKey 반환
```

### 검증 기준

| 조건 | 실패 처리 |
| --- | --- |
| `normalImageFileKeys` 누락 | 400 |
| 정상 이미지 파일 키 10개 미만 | 422 |
| `modelCategory` 누락 | 400 |
| `modelCategory` 허용값 아님 | 422 |
| `modelProfile` 누락 | 400 |
| `modelProfile` 허용값 아님 | 422 |
| `ckptFileKey` 누락 | 400 |
| `configFileKey` 누락 | 400 |
| `outputPrefix` 누락 | 400 |
| 정상 이미지 파일 없음 | 404 |
| ckpt/config 파일 없음 | 404 |
| config와 요청 profile/category 불일치 | 422 |
| 이미지 로드 또는 전처리 실패 | 500 |
| 모델 로딩 실패 | 500 |
| feature 추출 실패 | 500 |
| memory bank 생성 실패 | 500 |
| memory bank MinIO 저장 실패 | 500 |
