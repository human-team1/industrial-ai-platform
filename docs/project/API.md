# API 명세

## 0. 공통 규칙

| 항목 | 기준 |
| --- | --- |
| Base URL | `/api/v1` |
| 인증 방식 | `Authorization: Bearer {accessToken}` |
| URL | 복수형 명사 사용 |
| 성공 응답 | `{ success, data, message }` |
| 에러 응답 | RFC 9457 Problem Details |
| 날짜 형식 | ISO-8601 |
| 페이지네이션 | `page`, `size`, `sort` |
| 기본 정렬 | 최신순 |

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
| InputType | `IMAGE / VIDEO / BROWSER_CAMERA / RTSP_STREAM` |
| SourceType | `IMAGE / VIDEO / BROWSER_CAMERA / RTSP_STREAM` |
| RoiMode | `FULL_FRAME / FIXED` |
| RoiCoordinateType | `NORMALIZED` |
| InputQualityStatus | `PASSED / WARNING / FAILED` |
| InputQualityReason | `TOO_DARK / TOO_BRIGHT / LOW_CONTRAST / BLURRY / ROI_INVALID / COLOR_SHIFT / SOME_FRAMES_RECHECK / INSUFFICIENT_VALID_FRAMES` |

---

# 2. Auth / Users

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| POST | `/auth/login` | 이메일/비밀번호 로그인 | Public |
| POST | `/auth/google` | Google OAuth 로그인 | Public |
| POST | `/auth/logout` | 로그아웃 | USER |
| POST | `/auth/refresh` | 토큰 재발급 | USER |
| GET | `/auth/me` | 내 로그인 정보 조회 | USER |
| GET | `/signup-requests/organizations/public` | 회원가입용 공개 조직 목록 | Public |
| POST | `/signup-requests` | 회원가입 신청 | Public |
| GET | `/signup-requests` | 가입 신청 목록 조회 | SITE_ADMIN |
| PATCH | `/signup-requests/{requestId}/approve` | 가입 승인 | SITE_ADMIN |
| PATCH | `/signup-requests/{requestId}/reject` | 가입 거절 | SITE_ADMIN |
| GET | `/users/me` | 내 정보 조회 | USER |
| PATCH | `/users/me` | 내 정보 수정 | USER |
| GET | `/users` | 사용자 목록 조회 | ADMIN |
| GET | `/users/{userId}` | 사용자 상세 조회 | ADMIN |
| PATCH | `/users/{userId}/status` | 사용자 상태 변경 | ADMIN |

---

# 3. Organizations

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/organizations` | 조직 목록 조회 | SITE_ADMIN |
| POST | `/organizations` | 조직 생성 | SITE_ADMIN |
| GET | `/organizations/{organizationId}` | 조직 상세 조회 | SITE_ADMIN |
| PATCH | `/organizations/{organizationId}` | 조직 수정 | SITE_ADMIN |
| PATCH | `/organizations/{organizationId}/status` | 조직 상태 변경 | SITE_ADMIN |

---

# 4. Settings / Thresholds

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/users/me/settings` | 내 설정 조회 | USER |
| PATCH | `/users/me/settings` | 내 설정 수정 | USER |
| GET | `/users/me/thresholds` | 내 임계값 조회 | USER |
| POST | `/users/me/thresholds` | 내 임계값 생성 | USER |
| PATCH | `/users/me/thresholds/{thresholdId}` | 내 임계값 수정 | USER |
| GET | `/users/me/thresholds/{thresholdId}/histories` | 임계값 변경 이력 조회 | USER |

### PATCH `/users/me/thresholds/{thresholdId}`

```json
{
  "anomalyThreshold": 0.75,
  "lowConfidenceThreshold": 0.55,
  "applyScope": "DEFAULT",
  "changeReason": "공정 특성 반영"
}
```

---

# 5. Analysis Targets / Cameras

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/analysis-targets` | 검사 대상 목록 조회 | USER |
| POST | `/analysis-targets` | 검사 대상 등록 | ADMIN |
| GET | `/analysis-targets/{targetId}` | 검사 대상 상세 조회 | USER |
| PATCH | `/analysis-targets/{targetId}` | 검사 대상 수정 | ADMIN |
| DELETE | `/analysis-targets/{targetId}` | 검사 대상 삭제 | ADMIN |
| GET | `/camera-sources` | 카메라 목록 조회 | USER |
| POST | `/camera-sources` | 카메라 등록 | ADMIN |
| PATCH | `/camera-sources/{cameraId}` | 카메라 수정 | ADMIN |
| DELETE | `/camera-sources/{cameraId}` | 카메라 삭제 | ADMIN |

---

# 6. Inspections

## 6.1 엔드포인트

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| POST | `/inspections/upload` | 이미지/영상 업로드 검사 요청 | USER |
| POST | `/inspections/realtime` | 실시간 검사 세션 시작 | USER |
| POST | `/inspections/{inspectionId}/frames` | 실시간 프레임 검사 | USER |
| PATCH | `/inspections/{inspectionId}/stop` | 실시간 검사 중지 | USER |
| GET | `/inspections` | 검사 실행 목록 조회 | USER |
| GET | `/inspections/{inspectionId}` | 검사 실행 상세 조회 | USER |
| GET | `/inspections/{inspectionId}/events` | 검사 이벤트 로그 조회 | USER |

## 6.2 ROI / 품질검사 기준

| 항목 | 기준 |
| --- | --- |
| 영상/실시간 기본 처리 | 고정 ROI 기반 |
| 객체탐지/세그멘테이션 | MVP 범위 제외 |
| 좌표 타입 | `NORMALIZED` |
| 좌표 범위 | `0.0 ~ 1.0` |
| 품질검사 실패 | `DEFECT`가 아니라 `RECHECK` |
| 프레임별 전체 결과 저장 | MVP 범위 제외 |

---

## 6.3 POST `/inspections/upload`

이미지 또는 영상 파일을 업로드하여 검사를 요청한다.

- 이미지: 단일 이미지 추론
- 영상: 프레임 샘플링 → 고정 ROI crop → 입력 품질 검사 → PatchCore 추론 → 영상 단위 집계

`multipart/form-data`

| Field | Type | Required | 설명 |
| --- | --- | --- | --- |
| `file` | File | Y | 이미지/영상 파일 |
| `targetId` | Long | N | 검사 대상 ID |
| `thresholdId` | Long | N | 적용 임계값 ID |
| `inputMode` | String | N | `IMAGE / VIDEO`, 없으면 MIME 기반 판별 |
| `roiMode` | String | N | `FULL_FRAME / FIXED`, 기본 `FULL_FRAME` |
| `roiCoordinateType` | String | N | 기본 `NORMALIZED` |
| `roiX` | Decimal | N | 정규화 ROI x. `0~1` |
| `roiY` | Decimal | N | 정규화 ROI y. `0~1` |
| `roiWidth` | Decimal | N | 정규화 ROI width. `0~1` |
| `roiHeight` | Decimal | N | 정규화 ROI height. `0~1` |
| `samplingFps` | Decimal | N | 영상 분석 FPS. 기본 `1.00` |
| `maxFrames` | Int | N | 영상 분석 최대 프레임 수. 기본 `60` |
| `qualityGateEnabled` | Boolean | N | 입력 품질 검사 사용 여부. 기본 `true` |
| `idempotencyKey` | String | N | 중복 요청 방지 키 |

### Response

```json
{
  "success": true,
  "data": {
    "inspectionId": 1001,
    "runStatus": "PROCESSING",
    "inputType": "VIDEO",
    "roiMode": "FIXED",
    "samplingFps": 1.0,
    "maxFrames": 60,
    "qualityGateEnabled": true
  },
  "message": "업로드 검사가 요청되었습니다."
}
```

### 검증 기준

| 조건 | 실패 처리 |
| --- | --- |
| 지원하지 않는 MIME | 422 |
| 손상 파일 | 422 |
| `roiMode=FIXED`인데 ROI 좌표 누락 | 422 |
| ROI 좌표가 0~1 범위를 벗어남 | 422 |
| `samplingFps <= 0` | 422 |
| `maxFrames <= 0` | 422 |
| 동일 `idempotencyKey` + 다른 payload | 409 |

---

## 6.4 POST `/inspections/realtime`

실시간 검사 세션을 시작한다.  
MVP에서는 고정 ROI 기반으로 처리한다.

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

실시간 세션에서 캡처한 프레임 1장을 전송한다.  
MVP에서는 WebSocket 대신 HTTP multipart 전송을 기본으로 한다.

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
      "reason": "TOO_DARK",
      "brightness": 42.1,
      "contrast": 18.4,
      "blurScore": 112.7,
      "saturation": 73.2
    }
  },
  "message": "프레임 품질이 기준을 만족하지 않아 재검사로 분류되었습니다."
}
```

---

## 6.6 PATCH `/inspections/{inspectionId}/stop`

실시간 검사 세션을 중지하고 세션 단위 결과를 집계한다.

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
    "representativeFrameSeq": 17,
    "inputQualityStatus": "WARNING",
    "inputQualityReason": "SOME_FRAMES_RECHECK"
  },
  "message": "실시간 검사가 중지되었습니다."
}
```

---

## 6.7 GET `/inspections`

| Query | Type | Required | 설명 |
| --- | --- | --- | --- |
| `runType` | String | N | `UPLOAD / REALTIME` |
| `inputType` | String | N | `IMAGE / VIDEO / BROWSER_CAMERA / RTSP_STREAM` |
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
    "runType": "REALTIME",
    "inputType": "BROWSER_CAMERA",
    "sourceType": "BROWSER_CAMERA",
    "runStatus": "COMPLETED",
    "appliedThreshold": 0.75,
    "startedAt": "2026-05-03T10:00:00",
    "completedAt": "2026-05-03T10:01:00",
    "input": {
      "roiMode": "FIXED",
      "roiCoordinateType": "NORMALIZED",
      "roiX": 0.25,
      "roiY": 0.2,
      "roiWidth": 0.5,
      "roiHeight": 0.5,
      "samplingFps": 1.0,
      "maxFrames": null,
      "qualityGateEnabled": true
    },
    "resultSummary": {
      "resultId": 3001,
      "finalDecisionCode": "RECHECK",
      "analyzedFrameCount": 48,
      "skippedFrameCount": 12,
      "defectFrameCount": 0,
      "recheckFrameCount": 12,
      "maxFrameScore": 0.5512,
      "avgFrameScore": 0.2304,
      "representativeFrameSeq": 17,
      "inputQualityStatus": "WARNING",
      "inputQualityReason": "SOME_FRAMES_RECHECK"
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

# 7. Results

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/results` | 결과 목록 조회 | USER |
| GET | `/results/{resultId}` | 결과 상세 조회 | USER |
| GET | `/results/{resultId}/artifacts` | 결과 산출물 조회 | USER |
| GET | `/results/{resultId}/images` | 결과 이미지 조회 | USER |
| GET | `/results/{resultId}/regions` | 이상 영역 조회 | USER |
| GET | `/results/{resultId}/explanation` | 결과 자연어 설명 조회 | USER |
| GET | `/results/{resultId}/report` | 결과 보고서 다운로드 | USER |

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
    "inputType": "VIDEO",
    "score": 0.8821,
    "confidence": 0.89,
    "decisionCode": "DEFECT",
    "finalDecisionCode": "DEFECT",
    "resultStatus": "COMPLETED",
    "thresholdId": 2,
    "thresholdVersion": 3,
    "modelVersionId": 10,
    "roi": {
      "roiMode": "FIXED",
      "roiCoordinateType": "NORMALIZED",
      "roiX": 0.25,
      "roiY": 0.2,
      "roiWidth": 0.5,
      "roiHeight": 0.5
    },
    "videoSummary": {
      "samplingFps": 1.0,
      "frameCount": 60,
      "analyzedFrameCount": 48,
      "skippedFrameCount": 12,
      "defectFrameCount": 2,
      "recheckFrameCount": 8,
      "maxFrameScore": 0.8821,
      "avgFrameScore": 0.3412,
      "representativeFrameSeq": 31
    },
    "inputQuality": {
      "status": "WARNING",
      "reason": "SOME_FRAMES_RECHECK"
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
| GET | `/reviews` | 재검토 큐 목록 조회 | ADMIN |
| GET | `/reviews/{reviewQueueId}` | 재검토 상세 조회 | ADMIN |
| PATCH | `/reviews/{reviewQueueId}` | 재검토 처리 | ADMIN |
| GET | `/results/{resultId}/review-histories` | 결과별 재검토 이력 조회 | ADMIN |
| POST | `/results/{resultId}/learning-candidates` | 학습 후보 등록 | ADMIN |

### PATCH `/reviews/{reviewQueueId}`

```json
{
  "afterDecision": "DEFECT",
  "reviewComment": "시각화 결과상 표면 결함 확인",
  "registerLearningCandidate": true
}
```

---

# 9. Documents / RAG

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/documents` | 문서 목록 조회 | USER |
| POST | `/documents` | 문서 업로드 | ADMIN |
| GET | `/documents/{documentId}` | 문서 상세 조회 | USER |
| PATCH | `/documents/{documentId}` | 문서 정보 수정 | ADMIN |
| DELETE | `/documents/{documentId}` | 문서 삭제 | ADMIN |
| GET | `/documents/{documentId}/versions` | 문서 버전 목록 조회 | USER |
| POST | `/documents/{documentId}/versions` | 새 문서 버전 업로드 | ADMIN |
| GET | `/document-versions/{versionId}/chunks` | 문서 청크 조회 | USER |
| GET | `/document-versions/{versionId}/index-jobs` | 인덱싱 작업 조회 | ADMIN |
| POST | `/document-versions/{versionId}/index-jobs` | 인덱싱 재요청 | ADMIN |

---

# 10. Chatbot

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/chat-conversations` | 챗봇 대화 목록 조회 | USER |
| POST | `/chat-conversations` | 대화 생성 | USER |
| GET | `/chat-conversations/{conversationId}` | 대화 상세 조회 | USER |
| DELETE | `/chat-conversations/{conversationId}` | 대화 삭제 | USER |
| GET | `/chat-conversations/{conversationId}/messages` | 메시지 목록 조회 | USER |
| POST | `/chat-conversations/{conversationId}/messages` | 질문 전송 | USER |
| GET | `/chat-messages/{messageId}/sources` | 답변 출처 조회 | USER |

---

# 11. Notifications

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/notifications` | 알림 목록 조회 | USER |
| GET | `/notifications/{notificationId}` | 알림 상세 조회 | USER |
| PATCH | `/notifications/{notificationId}/read` | 알림 읽음 처리 | USER |
| PATCH | `/notifications/read-all` | 전체 읽음 처리 | USER |
| DELETE | `/notifications/{notificationId}` | 알림 삭제 | USER |

---

# 12. Reports

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/reports` | 보고서 목록 조회 | ADMIN |
| POST | `/reports` | 보고서 생성 요청 | ADMIN |
| GET | `/reports/{reportId}` | 보고서 상세 조회 | ADMIN |
| GET | `/reports/{reportId}/items` | 보고서 항목 조회 | ADMIN |
| GET | `/reports/{reportId}/files` | 보고서 파일 조회 | ADMIN |
| GET | `/reports/{reportId}/download` | 보고서 다운로드 | ADMIN |

---

# 13. Operation / Admin

운영 관리 API는 `ROLE_SITE_ADMIN` 전용이다.

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/admin/audit-logs` | 감사 로그 조회 | SITE_ADMIN |
| GET | `/admin/action-logs` | 관리자 작업 로그 조회 | SITE_ADMIN |
| GET | `/admin/operation-logs` | 운영 로그 조회 | SITE_ADMIN |
| GET | `/admin/system-status` | 시스템 상태 조회 | SITE_ADMIN |
| GET | `/admin/system-components` | 시스템 컴포넌트별 상태 조회 | SITE_ADMIN |
| GET | `/admin/operation-policies` | 운영 정책 조회 | SITE_ADMIN |
| PATCH | `/admin/operation-policies/{policyId}` | 운영 정책 수정 | SITE_ADMIN |
| GET | `/admin/async-jobs` | 비동기 작업 목록 | SITE_ADMIN |
| GET | `/admin/async-jobs/{jobId}` | 비동기 작업 상세 | SITE_ADMIN |

---

# 14. Files

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/files/{fileId}` | 파일 메타데이터 조회 | USER |
| GET | `/files/{fileId}/download` | 파일 다운로드 | USER |
| DELETE | `/files/{fileId}` | 파일 삭제 | ADMIN |

---

# 15. Models

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/models` | 모델 목록 조회 | SITE_ADMIN |
| POST | `/models` | 모델 등록 | SITE_ADMIN |
| GET | `/models/{modelId}/versions` | 모델 버전 목록 조회 | SITE_ADMIN |
| POST | `/models/{modelId}/versions` | 모델 버전 등록 | SITE_ADMIN |
| PATCH | `/model-versions/{versionId}/activate` | 모델 버전 활성화 | SITE_ADMIN |
| POST | `/model-versions/{versionId}/deployments` | 모델 배포 | SITE_ADMIN |
| PATCH | `/model-deployments/{deploymentId}/rollback` | 모델 롤백 | SITE_ADMIN |

---

# 16. Dashboard

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/dashboard/overview` | 대시보드 전체 요약 조회 | USER |

---

# 17. Internal FastAPI

Spring 내부 연동용 API다. 외부 사용자에게 직접 노출하지 않는다.

| Method | Endpoint | 설명 | 호출 주체 |
| --- | --- | --- | --- |
| GET | `/ai/v1/internal/system-status` | AI 서버 상태 조회 | Spring |
| POST | `/ai/v1/internal/vision/infer-image` | 이미지 추론 | Spring |
| POST | `/ai/v1/internal/vision/infer-video` | 영상 추론 | Spring |
| POST | `/ai/v1/internal/vision/infer-frame` | 실시간 프레임 추론 | Spring |

## POST `/ai/v1/internal/vision/infer-video`

```json
{
  "inspectionId": 1001,
  "fileKey": "inspections/1001/original/sample.mp4",
  "targetId": 1,
  "modelVersionId": 10,
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

## POST `/ai/v1/internal/vision/infer-frame`

```json
{
  "inspectionId": 2001,
  "frameSeq": 17,
  "frameFileKey": "inspections/2001/tmp/frame_17.jpg",
  "targetId": 1,
  "modelVersionId": 10,
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