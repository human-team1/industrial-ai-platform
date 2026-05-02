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

## 성공 응답 예시

```json
{
  "success": true,
  "data": {},
  "message": "요청이 성공적으로 처리되었습니다."
}
```

## 에러 응답 예시

```json
{
  "type": "https://api.example.com/problems/validation-error",
  "title": "Validation failed",
  "status": 422,
  "detail": "요청 값이 올바르지 않습니다.",
  "instance": "/api/v1/users",
  "errorCode": "VALIDATION_ERROR",
  "errors": [
    {
      "field": "email",
      "reason": "올바른 이메일 형식이 아닙니다."
    }
  ]
}
```

---

# 1. Auth / Users

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| POST | `/auth/login` | 이메일/비밀번호 로그인 | Public |
| POST | `/auth/google` | Google OAuth 로그인 | Public |
| POST | `/auth/logout` | 로그아웃 | USER |
| POST | `/auth/refresh` | 토큰 재발급 | USER |
| GET | `/auth/me` | 내 로그인 정보 조회 | USER |
| GET | `/signup-requests/organizations/public` | 회원가입용 조직 목록 조회 (이름 위주) | Public |
| POST | `/signup-requests` | 회원가입 신청 | Public |
| GET | `/signup-requests` | 가입 신청 목록 조회 | ADMIN |
| PATCH | `/signup-requests/{requestId}/approve` | 가입 승인 | ADMIN |
| PATCH | `/signup-requests/{requestId}/reject` | 가입 거절 | ADMIN |
| GET | `/users/me` | 내 정보 조회 | USER |
| PATCH | `/users/me` | 내 정보 수정 | USER |
| GET | `/users` | 사용자 목록 조회 | ADMIN |
| GET | `/users/{userId}` | 사용자 상세 조회 | ADMIN |
| PATCH | `/users/{userId}/status` | 사용자 상태 변경 | ADMIN |

## POST `/auth/login`

### Request

```json
{
  "email": "user@example.com",
  "password": "password1234"
}
```

### Response

```json
{
  "success": true,
  "data": {
    "accessToken": "access-token",
    "refreshToken": "refresh-token",
    "user": {
      "userId": 1,
      "email": "user@example.com",
      "name": "홍길동",
      "role": "USER",
      "status": "ACTIVE"
    }
  },
  "message": "로그인되었습니다."
}
```

## POST `/auth/google`

### Request

```json
{
  "idToken": "google-id-token"
}
```

### Response

```json
{
  "success": true,
  "data": {
    "accessToken": "access-token",
    "refreshToken": "refresh-token",
    "user": {
      "userId": 1,
      "googleSub": "google-sub",
      "email": "user@gmail.com",
      "name": "홍길동",
      "picture": "https://...",
      "role": "USER",
      "status": "ACTIVE"
    }
  },
  "message": "Google 로그인이 완료되었습니다."
}
```

---

# 2. Organizations

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/organizations` | 조직 목록 조회 | ADMIN |
| POST | `/organizations` | 조직 생성 | ADMIN |
| GET | `/organizations/{organizationId}` | 조직 상세 조회 | ADMIN |
| PATCH | `/organizations/{organizationId}` | 조직 수정 | ADMIN |
| PATCH | `/organizations/{organizationId}/status` | 조직 상태 변경 | ADMIN |

---

# 3. Settings / Thresholds

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/users/me/settings` | 내 설정 조회 | USER |
| PATCH | `/users/me/settings` | 내 설정 수정 | USER |
| GET | `/users/me/thresholds` | 내 임계값 조회 | USER |
| POST | `/users/me/thresholds` | 내 임계값 생성 | USER |
| PATCH | `/users/me/thresholds/{thresholdId}` | 내 임계값 수정 | USER |
| GET | `/users/me/thresholds/{thresholdId}/histories` | 임계값 변경 이력 조회 | USER |

## PATCH `/users/me/thresholds/{thresholdId}`

```json
{
  "anomalyThreshold": 0.75,
  "lowConfidenceThreshold": 0.55,
  "applyScope": "DEFAULT",
  "changeReason": "공정 특성 반영"
}
```

---

# 4. Analysis Targets / Cameras

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/analysis-targets` | 검사 대상 목록 조회 | USER |
| POST | `/analysis-targets` | 검사 대상 등록 | USER |
| GET | `/analysis-targets/{targetId}` | 검사 대상 상세 조회 | USER |
| PATCH | `/analysis-targets/{targetId}` | 검사 대상 수정 | USER |
| DELETE | `/analysis-targets/{targetId}` | 검사 대상 삭제 | USER |
| GET | `/camera-sources` | 카메라 목록 조회 | USER |
| POST | `/camera-sources` | 카메라 등록 | USER |
| PATCH | `/camera-sources/{cameraId}` | 카메라 수정 | USER |
| DELETE | `/camera-sources/{cameraId}` | 카메라 삭제 | USER |

## POST `/analysis-targets`

### Request

```json
{
  "targetName": "프레스 검사 대상",
  "equipmentName": "프레스 #1",
  "productName": "금속 부품",
  "locationName": "라인 A-1",
  "targetType": "EQUIPMENT",
  "targetStatus": "ACTIVE"
}
```

### Response

```json
{
  "success": true,
  "data": {
    "targetId": 1,
    "targetName": "프레스 검사 대상",
    "equipmentName": "프레스 #1",
    "productName": "금속 부품",
    "locationName": "라인 A-1",
    "targetType": "EQUIPMENT",
    "targetStatus": "ACTIVE"
  },
  "message": "검사 대상이 등록되었습니다."
}
```

## PATCH `/analysis-targets/{targetId}`

### Request

```json
{
  "targetName":"프레스 검사 대상",
  "equipmentName":"프레스 #1",
  "productName":"금속 부품",
  "locationName":"라인 A-1",
  "targetType":"EQUIPMENT",
  "targetStatus":"ACTIVE"
}
```

---

# 5. Inspections

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| POST | `/inspections/upload` | 업로드 기반 검사 요청 | USER |
| POST | `/inspections/realtime` | 실시간 검사 시작 | USER |
| PATCH | `/inspections/{inspectionId}/stop` | 실시간 검사 중지 | USER |
| GET | `/inspections` | 검사 실행 목록 조회 | USER |
| GET | `/inspections/{inspectionId}` | 검사 실행 상세 조회 | USER |
| GET | `/inspections/{inspectionId}/events` | 검사 이벤트 로그 조회 | USER |

## POST `/inspections/upload`

`multipart/form-data`

| Field | Type | Required | 설명 |
| --- | --- | --- | --- |
| file | File | Y | 이미지/영상 파일 |
| targetId | Long | N | 검사 대상 ID |
| thresholdId | Long | N | 적용 임계값 ID |

### Response

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

## POST `/inspections/realtime`

```json
{
  "targetId": 1,
  "cameraId": 3,
  "thresholdId": 2
}
```

---

# 6. Results

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

### Query

| Query | 설명 |
| --- | --- |
| `startDate` | 시작일 |
| `endDate` | 종료일 |
| `decisionCode` | `NORMAL / DEFECT / RECHECK` |
| `targetId` | 검사 대상 |
| `page` | 페이지 |
| `size` | 크기 |

---

# 7. Review

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/reviews` | 재검토 큐 목록 조회 | ADMIN |
| GET | `/reviews/{reviewQueueId}` | 재검토 상세 조회 | ADMIN |
| PATCH | `/reviews/{reviewQueueId}` | 재검토 처리 | ADMIN |
| GET | `/results/{resultId}/review-histories` | 결과별 재검토 이력 조회 | ADMIN |
| POST | `/results/{resultId}/learning-candidates` | 학습 후보 등록 | ADMIN |

## PATCH `/reviews/{reviewQueueId}`

```json
{
  "afterDecision": "DEFECT",
  "reviewComment": "시각화 결과상 표면 결함 확인",
  "registerLearningCandidate": true
}
```

---

# 8. Documents / RAG

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/documents` | 문서 목록 조회 | USER |
| POST | `/documents` | 문서 업로드 | USER |
| GET | `/documents/{documentId}` | 문서 상세 조회 | USER |
| PATCH | `/documents/{documentId}` | 문서 정보 수정 | USER |
| DELETE | `/documents/{documentId}` | 문서 삭제 | USER |
| GET | `/documents/{documentId}/versions` | 문서 버전 목록 조회 | USER |
| POST | `/documents/{documentId}/versions` | 새 문서 버전 업로드 | USER |
| GET | `/document-versions/{versionId}/chunks` | 문서 청크 조회 | USER |
| GET | `/document-versions/{versionId}/index-jobs` | 인덱싱 작업 조회 | USER |
| POST | `/document-versions/{versionId}/index-jobs` | 인덱싱 재요청 | USER |

## POST `/documents`

`multipart/form-data`

| Field | Type | Required | 설명 |
| --- | --- | --- | --- |
| file | File | Y | 문서 파일 |
| title | String | Y | 문서 제목 |
| documentType | String | Y | `MANUAL / SOP / TROUBLESHOOTING` |

---

# 9. Chatbot

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

# 10. Notifications

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/notifications` | 알림 목록 조회 | USER |
| GET | `/notifications/{notificationId}` | 알림 상세 조회 | USER |
| PATCH | `/notifications/{notificationId}/read` | 알림 읽음 처리 | USER |
| PATCH | `/notifications/read-all` | 전체 읽음 처리 | USER |
| DELETE | `/notifications/{notificationId}` | 알림 삭제 | USER |

---

# 11. Reports

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/reports` | 보고서 목록 조회 | ADMIN |
| POST | `/reports` | 보고서 생성 요청 | ADMIN |
| GET | `/reports/{reportId}` | 보고서 상세 조회 | ADMIN |
| GET | `/reports/{reportId}/items` | 보고서 항목 조회 | ADMIN |
| GET | `/reports/{reportId}/files` | 보고서 파일 조회 | ADMIN |
| GET | `/reports/{reportId}/download` | 보고서 다운로드 | ADMIN |

---

# 12. Operation / Admin

운영 관리 API는 `ROLE_SITE_ADMIN` 전용이다. `ROLE_COMPANY_ADMIN`, `ROLE_COMPANY_WORKER`는 접근할 수 없다.

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/admin/audit-logs` | 감사 로그 조회 | ADMIN |
| GET | `/admin/action-logs` | 관리자 작업 로그 조회 | ADMIN |
| GET | `/admin/operation-logs` | 운영 로그 조회 | ADMIN |
| GET | `/admin/system-status` | 시스템 상태 조회 | ADMIN |
| GET | `/admin/system-components` | 시스템 컴포넌트별 상태 조회 | ADMIN |
| GET | `/admin/operation-policies` | 운영 정책 조회 | ADMIN |
| PATCH | `/admin/operation-policies/{policyId}` | 운영 정책 수정 | ADMIN |
| GET | `/admin/async-jobs` | 비동기 작업 목록 조회 | ADMIN |
| GET | `/admin/async-jobs/{jobId}` | 비동기 작업 상세 조회 | ADMIN |

## GET `/admin/system-status`

운영 모니터링 화면의 시스템 리소스 상태 요약을 조회한다.
Redis 최신 상태 캐시(`operation:system-status:spring:latest`)를 우선 사용하고, 없거나 장애가 있으면 MariaDB `system_status_snapshot` 최신 이력으로 fallback한다.

### Response

```json
{
  "success": true,
  "data": {
    "snapshotId": 1,
    "cpuUsage": 42.5,
    "memoryUsage": 68.1,
    "diskUsage": 73.4,
    "responseTimeMs": 128,
    "overallStatus": "NORMAL",
    "createdAt": "2025-05-20T09:30:00"
  },
  "message": "시스템 상태를 조회했습니다."
}
```

## GET `/admin/system-components`

운영 모니터링 화면의 컴포넌트별 상태를 조회한다.
Redis 최신 컴포넌트 캐시(`operation:component-status:{componentType}`)를 우선 사용하고, 없으면 MariaDB `system_component_status`의 컴포넌트별 최신 이력으로 fallback한다.

### Response

```json
{
  "success": true,
  "data": [
    {
      "componentStatusId": 1,
      "componentType": "SPRING_API",
      "componentName": "Spring API 서버",
      "status": "NORMAL",
      "message": "정상 응답 중",
      "cpuUsage": 42.5,
      "memoryUsage": 68.1,
      "diskUsage": 73.4,
      "hostName": "spring-host",
      "instanceId": "spring-local-1",
      "responseTimeMs": 42,
      "checkedAt": "2025-05-20T09:30:00",
      "createdAt": "2025-05-20T09:30:00"
    },
    {
      "componentStatusId": 2,
      "componentType": "AI_SERVER",
      "componentName": "AI 모델 서버",
      "status": "WARNING",
      "message": "응답 지연 발생",
      "cpuUsage": null,
      "memoryUsage": null,
      "diskUsage": 71.2,
      "hostName": "ai-server-local",
      "instanceId": "ai-server",
      "responseTimeMs": 850,
      "checkedAt": "2025-05-20T09:30:00",
      "createdAt": "2025-05-20T09:30:00"
    }
  ],
  "message": "시스템 컴포넌트 상태를 조회했습니다."
}
```

## GET `/admin/operation-logs`

운영 로그 목록을 조회한다.

## Internal AI Server Status API

Spring 운영 모니터링 스케줄러가 AI 서버 컴퓨터 상태를 수집하기 위해 호출한다.

### GET `/ai/v1/internal/system-status`

```json
{
  "success": true,
  "data": {
    "nodeType": "AI_SERVER",
    "nodeName": "AI 모델 서버",
    "hostName": "ai-server-local",
    "instanceId": "ai-server",
    "status": "NORMAL",
    "cpuUsage": null,
    "memoryUsage": null,
    "diskUsage": 70.1,
    "responseTimeMs": 48,
    "message": "AI 서버 상태 조회 정상",
    "checkedAt": "2026-05-02T12:00:00"
  },
  "message": "AI 서버 상태를 조회했습니다."
}
```

현재 AI 서버는 별도 의존성 추가 없이 표준 라이브러리로 디스크 사용률과 응답 시간을 반환한다. CPU/Memory는 수집 불가 시 `null`이다.

### Query

| Query | Type | Required | 설명 |
| --- | --- | --- | --- |
| `level` | `String` | N | 로그 레벨. `INFO / WARN / ERROR` |
| `sourceComponent` | `String` | N | 발생 컴포넌트. 예: `SPRING_API / AI_SERVER / MARIADB / REDIS / MINIO` |
| `eventStatus` | `String` | N | 이벤트 상태. 예: `SUCCESS / FAILED / PROCESSING` |
| `startDate` | `yyyy-MM-dd` | N | 조회 시작일 |
| `endDate` | `yyyy-MM-dd` | N | 조회 종료일 |
| `page` | `int` | N | 페이지 번호 |
| `size` | `int` | N | 페이지 크기 |

### Response

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "operationLogId": 1,
        "eventType": "INSPECTION_FAILED",
        "eventStatus": "FAILED",
        "logLevel": "ERROR",
        "sourceComponent": "AI_SERVER",
        "requestId": "req-20250520-0001",
        "actorUserId": 10,
        "detailMessage": "AI 서버 응답 지연으로 검사 요청이 실패했습니다.",
        "relatedPath": "/api/v1/inspections/upload",
        "createdAt": "2025-05-20T09:28:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1
  },
  "message": "운영 로그 목록을 조회했습니다."
}
```

## GET `/admin/operation-policies`

사이트 관리자 설정 화면의 운영 정책 목록을 조회한다.

### Query

| Query | Type | Required | 설명 |
| --- | --- | --- | --- |
| `category` | `String` | N | 정책 카테고리. `INSPECTION / NOTIFICATION / SECURITY / RETENTION / SYSTEM` |
| `activeOnly` | `Boolean` | N | 활성 정책만 조회 여부 |

### Response

```json
{
  "success": true,
  "data": [
    {
      "operationPolicyId": 1,
      "policyCategory": "INSPECTION",
      "policyKey": "default_anomaly_threshold",
      "policyName": "기본 이상 판정 임계값",
      "policyValue": "0.75",
      "valueType": "NUMBER",
      "description": "사용자 임계값이 없을 때 적용되는 기본 이상 판정 기준입니다.",
      "isActive": true,
      "updatedAt": "2025-05-20T09:30:00",
      "updatedBy": 1
    }
  ],
  "message": "운영 정책 목록을 조회했습니다."
}
```

## PATCH `/admin/operation-policies/{policyId}`

사이트 관리자 설정 화면에서 운영 정책 값을 수정한다.

### Request

```json
{
  "policyValue": "0.80",
  "isActive": true
}
```

### Response

```json
{
  "success": true,
  "data": {
    "operationPolicyId": 1,
    "policyCategory": "INSPECTION",
    "policyKey": "default_anomaly_threshold",
    "policyName": "기본 이상 판정 임계값",
    "policyValue": "0.80",
    "valueType": "NUMBER",
    "description": "사용자 임계값이 없을 때 적용되는 기본 이상 판정 기준입니다.",
    "isActive": true,
    "updatedAt": "2025-05-20T09:35:00",
    "updatedBy": 1
  },
  "message": "운영 정책을 수정했습니다."
}
```

---

# 13. Files

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/files/{fileId}` | 파일 메타데이터 조회 | USER |
| GET | `/files/{fileId}/download` | 파일 다운로드 | USER |
| DELETE | `/files/{fileId}` | 파일 삭제 | USER |

---

# 14. Models

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/models` | 모델 목록 조회 | ADMIN |
| POST | `/models` | 모델 등록 | ADMIN |
| GET | `/models/{modelId}/versions` | 모델 버전 목록 조회 | ADMIN |
| POST | `/models/{modelId}/versions` | 모델 버전 등록 | ADMIN |
| PATCH | `/model-versions/{versionId}/activate` | 모델 버전 활성화 | ADMIN |
| POST | `/model-versions/{versionId}/deployments` | 모델 배포 | ADMIN |
| PATCH | `/model-deployments/{deploymentId}/rollback` | 모델 롤백 | ADMIN |

---

# 15. Dashboard

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/dashboard/overview` | 대시보드 전체 요약 조회 | USER |

## GET `/dashboard/overview`

대시보드 화면에 필요한 KPI, 추이 차트, 설비별 이상률, 최근 결과, 최근 알림, 요약 정보, 시스템 상태를 한 번에 조회한다.

### Query

| Query | Type | Required | 설명 |
| --- | --- | --- | --- |
| `startDate` | `yyyy-MM-dd` | N | 조회 시작일. 없으면 최근 7일 기준 |
| `endDate` | `yyyy-MM-dd` | N | 조회 종료일. 없으면 오늘 기준 |
| `organizationId` | `Long` | N | 관리자 전용 조직 필터. 일반 사용자는 자기 조직 기준으로 조회 |

### Response

```json
{
  "success": true,
  "data": {
    "period": {
      "startDate": "2025-05-14",
      "endDate": "2025-05-20"
    },
    "kpis": {
      "totalInspectionCount": 12842,
      "totalInspectionChangeRate": 15.6,
      "anomalyCount": 386,
      "anomalyChangeRate": 22.1,
      "normalCount": 12456,
      "normalChangeRate": 14.3,
      "anomalyRate": 3.01,
      "anomalyRateChangePoint": 0.48
    },
    "trend": [
      {
        "date": "2025-05-14",
        "normalCount": 1540,
        "anomalyCount": 60,
        "recheckCount": 12,
        "anomalyRate": 3.7
      }
    ],
    "topEquipmentAnomalyRates": [
      {
        "targetId": 1,
        "equipmentName": "프레스 #1",
        "inspectionCount": 230,
        "anomalyCount": 10,
        "anomalyRate": 4.35
      }
    ],
    "recentResults": [
      {
        "resultId": 1001,
        "inspectionId": 2001,
        "inspectedAt": "2025-05-20T09:28:34",
        "equipmentName": "프레스 #1",
        "inspectionType": "실시간 탐지",
        "decision": "DEFECT",
        "decisionLabel": "이상",
        "anomalyScore": 0.924,
        "locationName": "라인 A-1"
      }
    ],
    "recentNotifications": [
      {
        "notificationId": 10,
        "severity": "WARNING",
        "title": "프레스 #1에서 이상이 감지되었습니다.",
        "createdAt": "2025-05-20T09:28:00",
        "targetUrl": "/results/1001"
      }
    ],
    "summary": {
      "registeredTargetCount": 32,
      "activeModelCount": 8,
      "totalDataSizeBytes": 1451355348664,
      "latestTrainingDate": "2025-05-18"
    },
    "systemStatus": {
      "overallStatus": "NORMAL",
      "modelServerStatus": "NORMAL",
      "streamServerStatus": "NORMAL",
      "storageStatus": "NORMAL",
      "lastUpdatedAt": "2025-05-20T09:30:00"
    }
  },
  "message": "대시보드 요약 정보를 조회했습니다."
}
```
