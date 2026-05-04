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
| ModelCategory | `OBJECT / TEXTURE` |
| ModelProfile | `SPEED / PERFORMANCE` |
| ModelDeployStatus | `REGISTERED / VALIDATED / DEPLOYED / DEPRECATED` |
| ModelArtifactType | `CKPT / CONFIG / MEMORY_BANK / LABELS / EXTRA` |
| DeploymentScope | `ORGANIZATION / TARGET` |
| DeploymentStatus | `DEPLOYED / ROLLED_BACK / DEACTIVATED` |

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
| POST | `/inspections/upload` | 이미지 업로드 검사 요청 | USER |
| POST | `/inspections/realtime` | 실시간 검사 세션 시작. 백엔드 확장 계약으로 유지 | USER |
| POST | `/inspections/{inspectionId}/frames` | 실시간 프레임 검사. 백엔드 확장 계약으로 유지 | USER |
| PATCH | `/inspections/{inspectionId}/stop` | 실시간 검사 중지. 백엔드 확장 계약으로 유지 | USER |
| GET | `/inspections` | 검사 실행 목록 조회 | USER |
| GET | `/inspections/{inspectionId}` | 검사 실행 상세 조회 | USER |
| GET | `/inspections/{inspectionId}/events` | 검사 이벤트 로그 조회 | USER |

> MVP 프론트에서는 영상 파일 업로드와 지속 실시간 스트리밍을 제공하지 않는다.
> 
> 
> 업로드 탐지는 이미지 파일만 지원하고, 카메라 검사는 브라우저 카메라 프리뷰에서 버튼 클릭 시 현재 프레임 1장을 캡처하여 `/inspections/upload`로 전송한다.
> 

---

## 6.2 ROI / 품질검사 기준

| 항목 | 기준 |
| --- | --- |
| MVP 프론트 입력 범위 | 이미지 업로드, 브라우저 카메라 단건 캡처 |
| ROI 기본 처리 | `FULL_FRAME` 기본, ROI 지정 시 `FIXED` 고정 ROI |
| 영상/지속 스트리밍 처리 | 백엔드 확장 계약으로 유지, MVP 프론트 미사용 |
| 객체탐지/세그멘테이션 | MVP 범위 제외 |
| 자동 ROI 탐지 | MVP 범위 제외 |
| 좌표 타입 | `NORMALIZED` |
| 좌표 범위 | `0.0 ~ 1.0` |
| 품질검사 실패 | `DEFECT`가 아니라 `RECHECK` |
| 프레임별 전체 결과 저장 | MVP 범위 제외 |

---

## 6.3 POST `/inspections/upload`

이미지 파일을 업로드하여 검사를 요청한다.

MVP 프론트에서는 이미지 파일만 전송한다.

브라우저 카메라 단건 캡처도 현재 프레임을 이미지 파일로 변환한 뒤 이 API로 전송한다.

- 이미지 업로드: 단일 이미지 추론
- 카메라 캡처: 버튼 클릭 시 캡처한 프레임 1장을 이미지 추론
- 영상 파일 업로드: 백엔드 확장 계약으로 유지하되 MVP 프론트에서는 사용하지 않는다.

`multipart/form-data`

| Field | Type | Required | 설명 |
| --- | --- | --- | --- |
| `file` | File | Y | 검사할 이미지 파일. MVP 프론트 허용 형식은 jpg/jpeg/png/webp |
| `targetId` | Long | N | 검사 대상 ID |
| `thresholdId` | Long | N | 적용 임계값 ID |
| `inputMode` | String | N | MVP 프론트 기본값 `IMAGE`. `VIDEO`는 백엔드 확장 계약으로 유지 |
| `sourceType` | String | N | 입력 출처. 기본 `IMAGE`, 카메라 캡처 시 `BROWSER_CAMERA` |
| `roiMode` | String | N | `FULL_FRAME / FIXED`, 기본 `FULL_FRAME` |
| `roiCoordinateType` | String | N | 기본 `NORMALIZED` |
| `roiX` | Decimal | N | 정규화 ROI x. `0~1` |
| `roiY` | Decimal | N | 정규화 ROI y. `0~1` |
| `roiWidth` | Decimal | N | 정규화 ROI width. `0~1` |
| `roiHeight` | Decimal | N | 정규화 ROI height. `0~1` |
| `samplingFps` | Decimal | N | 영상 확장용 필드. MVP 프론트에서는 전송하지 않음 |
| `maxFrames` | Int | N | 영상 확장용 필드. MVP 프론트에서는 전송하지 않음 |
| `qualityGateEnabled` | Boolean | N | 입력 품질 검사 사용 여부. 기본 `true` |
| `idempotencyKey` | String | N | 중복 요청 방지 키 |

### 이미지 업로드 요청 예시

```
file=sample.jpg
inputMode=IMAGE
sourceType=IMAGE
roiMode=FULL_FRAME
qualityGateEnabled=true
idempotencyKey=upload-20260503-0001
```

### 카메라 캡처 요청 예시

```
file=captured-frame-20260503-100000.jpg
inputMode=IMAGE
sourceType=BROWSER_CAMERA
roiMode=FULL_FRAME
qualityGateEnabled=true
idempotencyKey=camera-capture-20260503-100000-a1b2c3
```

### 이미지 업로드 Response

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

### 카메라 캡처 Response

```json
{
  "success": true,
  "data": {
    "inspectionId": 1002,
    "runStatus": "PROCESSING",
    "inputType": "IMAGE",
    "sourceType": "BROWSER_CAMERA",
    "roiMode": "FULL_FRAME",
    "qualityGateEnabled": true
  },
  "message": "카메라 캡처 검사가 요청되었습니다."
}
```

### 검증 기준

| 조건 | 실패 처리 |
| --- | --- |
| 지원하지 않는 MIME | 422 |
| 손상 파일 | 422 |
| `roiMode=FIXED`인데 ROI 좌표 누락 | 422 |
| ROI 좌표가 0~1 범위를 벗어남 | 422 |
| 영상 확장 요청에서 `samplingFps <= 0` | 422 |
| 영상 확장 요청에서 `maxFrames <= 0` | 422 |
| 동일 `idempotencyKey` + 다른 payload | 409 |
| MVP 프론트에서 video/* MIME 선택 | 프론트에서 차단 |

---

## 6.4 POST `/inspections/realtime`

실시간 검사 세션을 시작한다.

이 API는 실제 센서/RTSP/PLC 연동 또는 자동 주기 검사 확장용 계약으로 유지한다.

현재 MVP 프론트에서는 이 API를 호출하지 않는다.

MVP 프론트의 카메라 검사는 `/inspections/upload`를 재사용하여 버튼 클릭 시 캡처한 이미지 1장을 전송한다.

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

이 API는 실시간 세션 기반 확장용 계약으로 유지한다.

현재 MVP 프론트에서는 호출하지 않는다.

MVP 프론트의 브라우저 카메라 검사는 `/inspections/upload`로 단건 이미지 캡처를 전송한다.

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

이 API는 실시간 세션 기반 확장용 계약으로 유지한다.

현재 MVP 프론트에서는 호출하지 않는다.

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
      "roiX": null,
      "roiY": null,
      "roiWidth": null,
      "roiHeight": null,
      "samplingFps": null,
      "maxFrames": null,
      "qualityGateEnabled": true
    },
    "resultSummary": {
      "resultId": 3001,
      "finalDecisionCode": "NORMAL",
      "analyzedFrameCount": 1,
      "skippedFrameCount": 0,
      "defectFrameCount": 0,
      "recheckFrameCount": 0,
      "maxFrameScore": 0.1204,
      "avgFrameScore": 0.1204,
      "representativeFrameSeq": 1,
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
      "roiCoordinateType": "NORMALIZED",
      "roiX": null,
      "roiY": null,
      "roiWidth": null,
      "roiHeight": null
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

모델 관리 API는 `ROLE_SITE_ADMIN` 전용이다.

정상 이미지셋 업로드를 통한 `memory_bank` 생성은 모델 관리 API 범위에 포함한다.

단, `ckpt/config`를 새로 학습하거나 생성하지는 않는다.
`ckpt/config`는 4개 고정 모델 프로필에서 재사용하고, 업로드된 정상 이미지셋으로 `memory_bank`만 생성한다.

고정 모델 프로필은 아래 4가지를 사용한다.

| modelProfile | modelCategory | 설명 |
| --- | --- | --- |
| `SPEED` | `OBJECT` | Object 계열 속도형 프로필 |
| `SPEED` | `TEXTURE` | Texture 계열 속도형 프로필 |
| `PERFORMANCE` | `OBJECT` | Object 계열 성능형 프로필 |
| `PERFORMANCE` | `TEXTURE` | Texture 계열 성능형 프로필 |

PatchCore 계열 모델은 `ckptFile`, `configFile`, `memoryBankFile`이 모두 있어야 실제 추론과 모델 버전 활성화가 가능하다.

같은 `ckpt/config`를 사용하더라도 `memory_bank`가 다르면 다른 모델 버전으로 관리한다.

생성된 `memory_bank`는 기존 `MODEL_VERSION / MODEL_ARTIFACT / MODEL_DEPLOYMENT` 구조에 연결한다.

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/models` | 모델 목록 조회 | SITE_ADMIN |
| POST | `/models` | 모델 기본 정보 등록 | SITE_ADMIN |
| GET | `/models/{modelId}` | 모델 상세 조회 | SITE_ADMIN |
| GET | `/models/{modelId}/versions` | 모델 버전 목록 조회 | SITE_ADMIN |
| POST | `/models/{modelId}/versions` | 모델 버전 및 산출물 수동 업로드 | SITE_ADMIN |
| POST | `/models/{modelId}/versions/from-normal-images` | 정상 이미지셋으로 memory bank를 생성하고 모델 버전/배포 자동 생성 | SITE_ADMIN |
| GET | `/model-versions/{versionId}` | 모델 버전 상세 조회 | SITE_ADMIN |
| GET | `/model-versions/{versionId}/artifacts` | 모델 버전 산출물 목록 조회 | SITE_ADMIN |
| PATCH | `/model-versions/{versionId}/activate` | 모델 버전 활성화 | SITE_ADMIN |
| PATCH | `/model-versions/{versionId}/deprecate` | 모델 버전 사용 중단 | SITE_ADMIN |
| GET | `/model-deployments` | 모델 배포 목록 조회 | SITE_ADMIN |
| POST | `/model-versions/{versionId}/deployments` | 조직/검사대상에 모델 배포 | SITE_ADMIN |
| PATCH | `/model-deployments/{deploymentId}/deactivate` | 모델 배포 비활성화 | SITE_ADMIN |
| PATCH | `/model-deployments/{deploymentId}/rollback` | 이전 모델 배포로 롤백 | SITE_ADMIN |

---

## 15.1 GET `/models`

| Query | Type | Required | 설명 |
| --- | --- | --- | --- |
| `modelType` | String | N | 모델 계열 또는 알고리즘 유형 |
| `page` | Int | N | 페이지 |
| `size` | Int | N | 크기 |
| `sort` | String | N | 정렬 |

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
| `sort` | String | N | 정렬 |

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
        "inputSize": "224x224",
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

모델 버전과 실행에 필요한 산출물 파일을 함께 등록한다.

이 API는 이미 생성된 `ckpt/config/memory_bank` 산출물을 직접 등록하는 수동 등록 API다.

정상 이미지셋을 업로드해 `memory_bank`를 자동 생성하는 경우에는 `POST /models/{modelId}/versions/from-normal-images`를 사용한다.

PatchCore 계열 모델은 정상 feature 기준 데이터인 `memoryBankFile`이 별도 산출물로 필요하다.

같은 `ckptFile`, `configFile`을 사용하더라도 `memoryBankFile`이 다르면 다른 모델 버전으로 관리한다.

`multipart/form-data`

| Field | Type | Required | 설명 |
| --- | --- | --- | --- |
| `ckptFile` | File | Y | 모델 가중치 또는 feature extractor/layer 산출물. 예: `model.ckpt` |
| `configFile` | File | Y | 모델 설정 파일. 예: `config.json` |
| `memoryBankFile` | File | Y | PatchCore memory bank 파일. 예: `memory_bank.pt`, `memory_bank.npy` |
| `labelsFile` | File | N | 라벨/클래스 매핑 파일 |
| `versionName` | String | Y | 모델 버전명 |
| `modelCategory` | String | Y | `OBJECT / TEXTURE` |
| `modelProfile` | String | Y | `SPEED / PERFORMANCE` |
| `framework` | String | N | 예: `PYTORCH` |
| `inputSize` | String | N | 예: `224x224` |
| `thresholdDefault` | Decimal | N | 기본 이상 점수 임계값 |
| `accuracy` | Decimal | N | 정확도 |
| `precisionScore` | Decimal | N | 정밀도 |
| `recallScore` | Decimal | N | 재현율 |
| `f1Score` | Decimal | N | F1 점수 |
| `aurocScore` | Decimal | N | AUROC 점수 |

### 요청 예시

```
ckptFile=model.ckpt
configFile=config.json
memoryBankFile=memory_bank.pt
versionName=v1.0.0-texture-performance
modelCategory=TEXTURE
modelProfile=PERFORMANCE
framework=PYTORCH
inputSize=224x224
thresholdDefault=0.7500
```

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
    "inputSize": "224x224",
    "thresholdDefault": 0.75,
    "deployStatus": "REGISTERED",
    "isActive": false,
    "artifacts": [
      {
        "modelArtifactId": 101,
        "artifactType": "CKPT",
        "fileId": 501
      },
      {
        "modelArtifactId": 102,
        "artifactType": "CONFIG",
        "fileId": 502
      },
      {
        "modelArtifactId": 103,
        "artifactType": "MEMORY_BANK",
        "fileId": 503
      }
    ]
  },
  "message": "모델 버전이 등록되었습니다."
}
```

### 검증 기준

| 조건 | 실패 처리 |
| --- | --- |
| `ckptFile` 누락 | 400 |
| `configFile` 누락 | 400 |
| `memoryBankFile` 누락 | 400 |
| `modelCategory`가 허용값이 아님 | 422 |
| `modelProfile`이 허용값이 아님 | 422 |
| `thresholdDefault`가 0~1 범위를 벗어남 | 422 |
| 파일 저장 실패 | 500 |
| 동일 모델 내 중복 `versionName` | 409 |

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
    "inputSize": "224x224",
    "thresholdDefault": 0.75,
    "accuracy": null,
    "precisionScore": null,
    "recallScore": null,
    "f1Score": null,
    "aurocScore": null,
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
      "modelArtifactId": 101,
      "artifactType": "CKPT",
      "fileId": 501,
      "fileName": "model.ckpt",
      "objectKey": "models/1/versions/10/model.ckpt",
      "checksum": "sha256:..."
    },
    {
      "modelArtifactId": 102,
      "artifactType": "CONFIG",
      "fileId": 502,
      "fileName": "config.json",
      "objectKey": "models/1/versions/10/config.json",
      "checksum": "sha256:..."
    },
    {
      "modelArtifactId": 103,
      "artifactType": "MEMORY_BANK",
      "fileId": 503,
      "fileName": "memory_bank.pt",
      "objectKey": "models/1/versions/10/memory_bank.pt",
      "checksum": "sha256:..."
    }
  ],
  "message": "모델 산출물 목록을 조회했습니다."
}
```

---

## 15.8 PATCH `/model-versions/{versionId}/activate`

모델 버전을 배포 가능한 상태로 활성화한다.

PatchCore 계열 모델 버전은 `CKPT`, `CONFIG`, `MEMORY_BANK` 산출물이 모두 존재해야 `VALIDATED` 상태로 활성화할 수 있다.

실제 고객사/검사대상 적용은 `/model-versions/{versionId}/deployments`에서 수행한다.

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

### 검증 기준

| 조건 | 실패 처리 |
| --- | --- |
| 존재하지 않는 `versionId` | 404 |
| `CKPT` 산출물 누락 | 422 |
| `CONFIG` 산출물 누락 | 422 |
| `MEMORY_BANK` 산출물 누락 | 422 |
| 이미 사용 중단된 모델 버전 | 409 |

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
| `sort` | String | N | 정렬 |

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

모델 버전을 조직 또는 특정 검사대상에 배포한다.

- `deploymentScope=ORGANIZATION`: 조직 기본 모델로 적용
- `deploymentScope=TARGET`: 특정 검사대상에만 적용
- 동일 조직/검사대상 범위에 기존 활성 배포가 있으면 기존 배포는 비활성화하고 신규 배포를 활성화한다.

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

### 검증 기준

| 조건 | 실패 처리 |
| --- | --- |
| 존재하지 않는 `versionId` | 404 |
| 비활성/사용중단 모델 버전 배포 | 409 |
| 존재하지 않는 `organizationId` | 404 |
| `deploymentScope=TARGET`인데 `targetId` 누락 | 422 |
| `targetId`가 해당 조직 소속이 아님 | 422 |
| 동일 범위 활성 배포 갱신 실패 | 500 |

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

현재 배포를 비활성화하고 이전 안정 배포를 다시 활성화한다.

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

이 API는 검사 업로드 API가 아니다.

검사용 이미지 업로드는 `/inspections/upload`를 사용한다.

처리 흐름은 다음과 같다.

```
정상 이미지셋 업로드
→ Spring이 정상 이미지 파일을 MinIO에 저장
→ modelCategory 기준으로 SPEED/PERFORMANCE 2개 프로필 대상 확정
→ 각 프로필별 고정 ckpt/config 선택
→ FastAPI memory-bank 생성 API를 SPEED, PERFORMANCE 각각 1회씩 호출
→ FastAPI가 memory_bank 생성 후 MinIO에 저장
→ Spring이 memory_bank 파일 메타를 FILE에 등록
→ MODEL_VERSION 생성
→ MODEL_ARTIFACT 생성
   - CKPT
   - CONFIG
   - MEMORY_BANK
→ MODEL_VERSION 활성화
→ MODEL_DEPLOYMENT 생성 또는 기존 활성 배포 비활성화 후 신규 배포
```

`multipart/form-data`

| Field | Type | Required | 설명 |
| --- | --- | --- | --- |
| `normalImages` | File[] | Y | memory bank 생성을 위한 정상 이미지 파일 목록 |
| `modelCategory` | String | Y | `OBJECT / TEXTURE` |
| `modelProfile` | String | N  | 특정 프로필만 재생성할 때 사용. 미전송 시 `SPEED`와 `PERFORMANCE`를 모두 생성 |
| `organizationId` | Long | Y | 모델을 적용할 조직 ID |
| `targetId` | Long | N | 특정 검사대상에 적용할 경우 사용 |
| `deploymentScope` | String | Y | `ORGANIZATION / TARGET` |
| `versionName` | String | N | 모델 버전명. 미전송 시 서버에서 자동 생성 가능 |
| `thresholdDefault` | Decimal | N | 기본 이상 점수 임계값 |
| `reason` | String | N | 모델 생성 및 배포 사유 |

### 요청 예시

```
normalImages=normal_001.jpg
normalImages=normal_002.jpg
normalImages=normal_003.jpg
modelCategory=TEXTURE
modelProfile=PERFORMANCE
organizationId=1001
targetId=10
deploymentScope=TARGET
versionName=v1.0.0-texture-performance-org1001-target10
thresholdDefault=0.7500
reason=고객사 A 프레스 검사대상 정상 이미지셋 기준 memory bank 생성
```

### Response

```json
{
  "success": true,
  "data": {
    "modelId": 1,
    "modelCategory": "TEXTURE",
    "normalImageCount": 1,
    "createdVersions": [
      {
        "modelProfile": "SPEED",
        "modelVersionId": 11,
        "versionName": "v1.0.0-texture-speed-org1001-target10",
        "deployStatus": "DEPLOYED",
        "isActive": true,
        "memoryBankFileId": 601,
        "deploymentId": 3002
      },
      {
        "modelProfile": "PERFORMANCE",
        "modelVersionId": 12,
        "versionName": "v1.0.0-texture-performance-org1001-target10",
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

### 검증 기준

| 조건 | 실패 처리 |
| --- | --- |
| `normalImages` 누락 | 400 |
| 정상 이미지 파일 개수가 1개 미만 | 422 |
| 지원하지 않는 이미지 MIME | 422 |
| 손상 이미지 포함 | 422 |
| `modelCategory`가 허용값이 아님 | 422 |
| `modelProfile`이 허용값이 아님 | 422 |
| `modelProfile` 미전송 시 SPEED/PERFORMANCE 둘 다 생성 실패 | 500  |
| 고정 `ckpt/config` 매핑을 찾을 수 없음 | 404 |
| 존재하지 않는 `modelId` | 404 |
| 존재하지 않는 `organizationId` | 404 |
| `deploymentScope=TARGET`인데 `targetId` 누락 | 422 |
| `targetId`가 해당 조직 소속이 아님 | 422 |
| `thresholdDefault`가 0~1 범위를 벗어남 | 422 |
| 동일 모델 내 중복 `versionName` | 409 |
| 정상 이미지 MinIO 저장 실패 | 500 |
| FastAPI memory bank 생성 실패 | 500 |
| memory bank 파일 등록 실패 | 500 |
| 모델 버전/산출물/배포 생성 실패 | 500 |

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
| POST | `/ai/v1/internal/models/memory-bank` | 정상 이미지 파일 키 목록과 고정 ckpt/config를 받아 memory_bank 생성 | Spring |

---

## 17.1 POST `/ai/v1/internal/vision/infer-image`

이미지 1장을 추론한다.

Spring이 검사 입력 파일과 활성 모델 배포 정보를 조회한 뒤, FastAPI에 이미지 파일 키와 모델 산출물 키를 전달한다.

FastAPI는 전달받은 `ckptFileKey`, `configFileKey`, `memoryBankFileKey`를 기준으로 모델 산출물을 로드하고 추론 결과를 반환한다.

현재 FastAPI 1차 구현은 API 계약, MinIO 다운로드, config 파싱, 전처리, 품질 검사, heatmap 업로드 흐름을 우선 제공한다. 실제 DINOv2/WideResNet PatchCore adapter는 `MEMORY_BANK` 산출물 구조와 모델 로딩 방식이 확정된 뒤 연결한다.

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
    "inputSize": "224x224",
    "ckptFileKey": "models/1/versions/10/model.ckpt",
    "configFileKey": "models/1/versions/10/config.json",
    "memoryBankFileKey": "models/1/versions/10/memory_bank.pt",
    "labelsFileKey": null
  },
  "roi": {
    "roiMode": "FULL_FRAME",
    "roiCoordinateType": "NORMALIZED",
    "roiX": null,
    "roiY": null,
    "roiWidth": null,
    "roiHeight": null
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
      "reason": null,
      "brightness": 128.4,
      "contrast": 42.7,
      "blurScore": 210.5,
      "saturation": 81.2
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
| 이미지 파일을 찾을 수 없음 | 404 |
| `ckptFileKey` 파일을 찾을 수 없음 | 404 |
| `configFileKey` 파일을 찾을 수 없음 | 404 |
| `memoryBankFileKey` 파일을 찾을 수 없음 | 404 |
| 모델 로드 실패 | 500 |
| memory bank 로드 실패 | 500 |
| 추론 실패 | 500 |

---

## 17.2 POST `/ai/v1/internal/vision/infer-video`

영상 파일을 추론한다.

이 API는 영상 파일 업로드 확장용 계약으로 유지한다.

현재 MVP 프론트에서는 사용하지 않는다.

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
    "inputSize": "224x224",
    "ckptFileKey": "models/1/versions/10/model.ckpt",
    "configFileKey": "models/1/versions/10/config.json",
    "memoryBankFileKey": "models/1/versions/10/memory_bank.pt",
    "labelsFileKey": null
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

---

## 17.3 POST `/ai/v1/internal/vision/infer-frame`

실시간 세션에서 프레임 1장을 추론한다.

이 API는 실시간 세션 기반 확장용 계약으로 유지한다.

현재 MVP 프론트에서는 사용하지 않는다.

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
    "inputSize": "224x224",
    "ckptFileKey": "models/1/versions/10/model.ckpt",
    "configFileKey": "models/1/versions/10/config.json",
    "memoryBankFileKey": "models/1/versions/10/memory_bank.pt",
    "labelsFileKey": null
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

---

## 17.4 POST `/ai/v1/internal/models/memory-bank`

정상 이미지 파일 키 목록과 고정 `ckpt/config` 파일 키를 받아 `memory_bank`를 생성한다.

이 API는 Spring 내부 연동용이다.

외부 사용자 또는 프론트엔드에서 직접 호출하지 않는다.

Spring은 정상 이미지 파일을 먼저 MinIO에 저장한 뒤, 해당 파일 키 목록을 이 API에 전달한다.

FastAPI는 전달받은 정상 이미지 파일과 고정 모델 산출물을 이용해 `memory_bank`를 생성하고, 생성된 `memory_bank`를 MinIO에 저장한 뒤 파일 키를 반환한다.

### Request

```json
{
  "modelCategory": "TEXTURE",
  "modelProfile": "PERFORMANCE",
  "normalImageFileKeys": [
    "models/tmp/normal/org-1001/target-10/normal_001.jpg",
    "models/tmp/normal/org-1001/target-10/normal_002.jpg",
    "models/tmp/normal/org-1001/target-10/normal_003.jpg"
  ],
  "configFileKey": "models/base/performance-texture/config.json",
  "ckptFileKey": "models/base/performance-texture/model.ckpt",
  "outputPrefix": "models/generated/org-1001/target-10/performance-texture"
}
```

### Response

```json
{
  "success": true,
  "data": {
    "memoryBankFileKey": "models/generated/org-1001/target-10/performance-texture/memory_bank.pt",
    "normalImageCount": 1,
    "modelCategory": "TEXTURE",
    "modelProfile": "PERFORMANCE",
    "createdAt": "2026-05-04T10:10:00"
  },
  "message": "메모리뱅크가 생성되었습니다."
}
```

### 검증 기준

| 조건 | 실패 처리 |
| --- | --- |
| `normalImageFileKeys` 누락 | 400 |
| 정상 이미지 파일 키 개수가 100개 미만 | 422 |
| `modelCategory`가 허용값이 아님 | 422 |
| `modelProfile`이 허용값이 아님 | 422 |
| `ckptFileKey` 누락 | 400 |
| `configFileKey` 누락 | 400 |
| `outputPrefix` 누락 | 400 |
| 정상 이미지 파일을 찾을 수 없음 | 404 |
| `ckptFileKey` 파일을 찾을 수 없음 | 404 |
| `configFileKey` 파일을 찾을 수 없음 | 404 |
| 이미지 로드 또는 전처리 실패 | 500 |
| ckpt/config 로드 실패 | 500 |
| memory bank 생성 실패 | 500 |
| memory bank MinIO 저장 실패 | 500 |