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

권한 표기 규칙:

- 본 문서의 관리자 권한은 `ROLE_SITE_ADMIN`을 기본값으로 표기한다.
- 정책명/화면 문구는 "사이트 관리자"를 사용한다.
- 상세 권한 매트릭스(회사 관리자/작업자 포함)는 `docs/auth/api-authority-matrix.md`를 단일 기준으로 본다.

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
| GET | `/signup-requests` | 가입 신청 목록 조회 | ROLE_SITE_ADMIN |
| PATCH | `/signup-requests/{requestId}/approve` | 가입 승인 | ROLE_SITE_ADMIN |
| PATCH | `/signup-requests/{requestId}/reject` | 가입 거절 | ROLE_SITE_ADMIN |
| GET | `/users/me` | 내 정보 조회 | USER |
| PATCH | `/users/me` | 내 정보 수정 | USER |
| GET | `/users` | 사용자 목록 조회 | ROLE_SITE_ADMIN |
| GET | `/users/{userId}` | 사용자 상세 조회 | ROLE_SITE_ADMIN |
| PATCH | `/users/{userId}/status` | 사용자 상태 변경 | ROLE_SITE_ADMIN |

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
| GET | `/organizations` | 조직 목록 조회 | ROLE_SITE_ADMIN |
| POST | `/organizations` | 조직 생성 | ROLE_SITE_ADMIN |
| GET | `/organizations/{organizationId}` | 조직 상세 조회 | ROLE_SITE_ADMIN |
| PATCH | `/organizations/{organizationId}` | 조직 수정 | ROLE_SITE_ADMIN |
| PATCH | `/organizations/{organizationId}/status` | 조직 상태 변경 | ROLE_SITE_ADMIN |

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
| GET | `/reviews` | 재검토 큐 목록 조회 | ROLE_SITE_ADMIN |
| GET | `/reviews/{reviewQueueId}` | 재검토 상세 조회 | ROLE_SITE_ADMIN |
| PATCH | `/reviews/{reviewQueueId}` | 재검토 처리 | ROLE_SITE_ADMIN |
| GET | `/results/{resultId}/review-histories` | 결과별 재검토 이력 조회 | ROLE_SITE_ADMIN |
| POST | `/results/{resultId}/learning-candidates` | 학습 후보 등록 | ROLE_SITE_ADMIN |

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
| GET | `/reports` | 보고서 목록 조회 | ROLE_SITE_ADMIN |
| POST | `/reports` | 보고서 생성 요청 | ROLE_SITE_ADMIN |
| GET | `/reports/{reportId}` | 보고서 상세 조회 | ROLE_SITE_ADMIN |
| GET | `/reports/{reportId}/items` | 보고서 항목 조회 | ROLE_SITE_ADMIN |
| GET | `/reports/{reportId}/files` | 보고서 파일 조회 | ROLE_SITE_ADMIN |
| GET | `/reports/{reportId}/download` | 보고서 다운로드 | ROLE_SITE_ADMIN |

---

# 12. Operation / Admin

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/admin/audit-logs` | 감사 로그 조회 | ROLE_SITE_ADMIN |
| GET | `/admin/action-logs` | 관리자 작업 로그 조회 | ROLE_SITE_ADMIN |
| GET | `/admin/operation-logs` | 운영 로그 조회 | ROLE_SITE_ADMIN |
| GET | `/admin/system-status` | 시스템 상태 조회 | ROLE_SITE_ADMIN |
| GET | `/admin/operation-policies` | 운영 정책 조회 | ROLE_SITE_ADMIN |
| PATCH | `/admin/operation-policies/{policyId}` | 운영 정책 수정 | ROLE_SITE_ADMIN |
| GET | `/admin/async-jobs` | 비동기 작업 목록 조회 | ROLE_SITE_ADMIN |
| GET | `/admin/async-jobs/{jobId}` | 비동기 작업 상세 조회 | ROLE_SITE_ADMIN |

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
| GET | `/models` | 모델 목록 조회 | ROLE_SITE_ADMIN |
| POST | `/models` | 모델 등록 | ROLE_SITE_ADMIN |
| GET | `/models/{modelId}/versions` | 모델 버전 목록 조회 | ROLE_SITE_ADMIN |
| POST | `/models/{modelId}/versions` | 모델 버전 등록 | ROLE_SITE_ADMIN |
| PATCH | `/model-versions/{versionId}/activate` | 모델 버전 활성화 | ROLE_SITE_ADMIN |
| POST | `/model-versions/{versionId}/deployments` | 모델 배포 | ROLE_SITE_ADMIN |
| PATCH | `/model-deployments/{deploymentId}/rollback` | 모델 롤백 | ROLE_SITE_ADMIN |

---

# 15. Dashboard

| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/dashboard/summary` | KPI 요약 조회 | USER |
| GET | `/dashboard/recent-results` | 최근 검사 결과 조회 | USER |
| GET | `/dashboard/statistics/decision-trend` | 판정 추이 조회 | USER |
| GET | `/dashboard/statistics/defect-types` | 불량 유형 통계 조회 | USER |
| GET | `/dashboard/statistics/targets` | 설비/대상별 통계 조회 | USER |

---