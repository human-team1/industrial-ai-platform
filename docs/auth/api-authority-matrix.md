# API 권한 매트릭스 (SSOT)

이 문서는 본 프로젝트의 API 권한 기준을 정의하는 단일 출처(Single Source of Truth)입니다.

- 백엔드의 `SecurityConfig`, 컨트롤러 단 권한 검사, 서비스 단 조직 스코프 검증, 프론트의 라우트 가드/메뉴 필터는 모두 본 문서를 기준으로 정합화합니다.
- 실제 코드 변경 시 본 문서도 함께 갱신해야 합니다.
- 본 문서와 코드가 충돌하면 본 문서를 우선 정정 대상으로 삼고, 정합화 PR로 동시 수정합니다.

---

## 1. Role 정의

| Role | 책임 범위 |
| --- | --- |
| `ROLE_SITE_ADMIN` | 플랫폼 전역 관리자. 회사 관리, 가입 승인/거절, 운영 모니터링, 감사 로그, 전체 데이터 조회/처리. |
| `ROLE_COMPANY_ADMIN` | 자기 회사의 관리자. 자기 회사 운영 데이터(검사/결과/문서/리포트 등) 조회·관리. |
| `ROLE_COMPANY_WORKER` | 자기 회사의 일반 작업자. 검사 수행, 결과 조회, 챗봇, 자기 회사 문서 조회, 본인 임계값/설정 관리. |

신규 가입자는 승인 시 `ROLE_COMPANY_WORKER`로 생성됩니다 (`SignupService`).

권한 표기 매핑:

| 구분 | 표기 |
| --- | --- |
| 정책명/화면 표시명 | 사이트 관리자 |
| Spring authority 문자열 | `ROLE_SITE_ADMIN` |
| DB 저장값 (`users.role`) | `ROLE_SITE_ADMIN` |
| 본 문서 표의 축약 컬럼명 | `SITE_ADMIN` |

---

## 2. Status 정의

| Status | 보호 API 접근 |
| --- | --- |
| `ACTIVE` | 가능 (본 매트릭스의 권한 검사를 통과해야 함) |
| `PENDING` | 불가 — 프론트는 `/pending` 으로 리다이렉트, 백엔드는 인증 자체는 통과해도 도메인 API에서 차단 정책 필요 |
| `REJECTED` | 불가 — 프론트는 `/rejected` 으로 리다이렉트 |
| `INACTIVE` | 불가 — 프론트는 `/auth?inactive=1` 으로 리다이렉트 |

> 본 매트릭스의 모든 보호 API는 별도 명시가 없으면 `Status = ACTIVE` 를 전제로 합니다.

---

## 3. Scope 정의

| Scope | 의미 |
| --- | --- |
| `GLOBAL` | 전 조직의 데이터 접근. 현재 정책상 `ROLE_SITE_ADMIN`만 허용. |
| `ORGANIZATION` | `currentUser.organizationId` 와 일치하는 리소스만 접근. 위반 시 403. 백엔드는 `SecurityUtils.assertSameOrganization(targetOrganizationId)` 로 검증. |
| `OWNER` | `currentUser.userId` 와 일치하는 리소스만 접근(예: `/users/me/...`, 본인 임계값/설정/알림). |

`ROLE_SITE_ADMIN`은 `ORGANIZATION` 스코프 리소스에 대해 전역 조회/처리 권한을 가질 수 있습니다.
단, `OWNER` 스코프 API(`/users/me`, `/users/me/settings`, `/notifications`, `/chat-conversations` 등)는 현재 인증 주체 본인 기준으로 동작합니다.
SITE_ADMIN이 전체 사용자 소유 데이터에 접근해야 하는 경우 별도의 `GLOBAL` 관리자 API를 정의합니다.

---

## 4. 매트릭스 표기 규칙

각 도메인 표의 컬럼:

- **Method / Endpoint / 설명**: api.txt 와 일치
- **Scope**: `GLOBAL` / `ORGANIZATION` / `OWNER` / `PUBLIC`
- **SITE_ADMIN / COMPANY_ADMIN / COMPANY_WORKER**: 권한 부여 여부
  - `✅` 허용
  - `자기 회사만` — `ORGANIZATION` 스코프 내에서만 허용
  - `본인만` — `OWNER` 스코프 내에서만 허용
  - `❌` 차단
  - `정책 확정 필요` — 회사 내 권한 분리(예: 등록/수정 권한이 COMPANY_ADMIN 전용인지 WORKER도 가능한지)가 미정
  - `-` 또는 `N/A` — PUBLIC API로 Role 검사 대상이 아님
- **상태**: 현재 코드 구현 상태
  - `✅ 구현` — 컨트롤러·서비스·SecurityConfig 매처 존재 및 동작 확인됨
  - `🟡 부분` — 일부만 존재(예: 컨트롤러는 있으나 스코프 검증 미적용)
  - `⏳ 예정` — 컨트롤러 자체 미구현. api.txt 명세만 존재

---

## 5. Auth / Users (signup-requests 포함)

| Method | Endpoint | Scope | SITE_ADMIN | COMPANY_ADMIN | COMPANY_WORKER | 상태 | 비고 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| POST | `/auth/google` | PUBLIC | - | - | - | ✅ 구현 | `permitAll` |
| POST | `/auth/refresh` | PUBLIC | - | - | - | ✅ 구현 | `permitAll`. Refresh Token은 HttpOnly Cookie |
| POST | `/auth/logout` | OWNER | ✅ | ✅ | ✅ | ⏳ 예정 | 본인 세션 무효화 |
| GET | `/auth/me` | OWNER | ✅ | ✅ | ✅ | ✅ 구현 | 본인 인증 정보 |
| GET | `/users/me` | OWNER | ✅ | ✅ | ✅ | ✅ 구현 | 본인 프로필 |
| PATCH | `/users/me` | OWNER | ✅ | ✅ | ✅ | ⏳ 예정 | 본인 프로필 수정 (MY-002) |
| GET | `/users` | GLOBAL / ORGANIZATION | ✅ | 자기 회사만 | ❌ | ⏳ 예정 | COMPANY_ADMIN은 자기 회사 사용자 목록만 조회 |
| GET | `/users/{userId}` | GLOBAL / ORGANIZATION | ✅ | 자기 회사만 | ❌ | ⏳ 예정 | COMPANY_ADMIN은 자기 회사 사용자만 조회 |
| PATCH | `/users/{userId}/status` | GLOBAL | ✅ | 정책 확정 필요 | ❌ | ⏳ 예정 | COMPANY_ADMIN의 사용자 상태 변경 권한 여부 확정 필요 |
| POST | `/signup-requests` | PUBLIC | - | - | - | ✅ 구현 | `permitAll` |
| GET | `/signup-requests/organizations/public` | PUBLIC | - | - | - | ✅ 구현 | 가입 페이지용 조직 목록 (이름 위주) |
| GET | `/signup-requests` | GLOBAL | ✅ | ❌ | ❌ | ✅ 구현 | `hasRole("SITE_ADMIN")` |
| PATCH | `/signup-requests/{requestId}/approve` | GLOBAL | ✅ | ❌ | ❌ | ✅ 구현 | `hasRole("SITE_ADMIN")` |
| PATCH | `/signup-requests/{requestId}/reject` | GLOBAL | ✅ | ❌ | ❌ | ✅ 구현 | `hasRole("SITE_ADMIN")` |

---

## 6. Organizations

| Method | Endpoint | Scope | SITE_ADMIN | COMPANY_ADMIN | COMPANY_WORKER | 상태 | 비고 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| GET | `/organizations` | GLOBAL | ✅ | ❌ | ❌ | ⏳ 예정 | 전체 조직 목록은 SITE_ADMIN 전용 |
| POST | `/organizations` | GLOBAL | ✅ | ❌ | ❌ | ⏳ 예정 | 회사 생성은 SITE_ADMIN 전용 |
| GET | `/organizations/{organizationId}` | GLOBAL / ORGANIZATION | ✅ | 자기 회사만 | 정책 확정 필요 | ⏳ 예정 | COMPANY_WORKER도 자기 회사 정보를 조회 가능한지 확정 필요 |
| PATCH | `/organizations/{organizationId}` | GLOBAL / ORGANIZATION | ✅ | 자기 회사만 | ❌ | ⏳ 예정 | 회사 정보 수정은 회사 관리자 이상 |
| PATCH | `/organizations/{organizationId}/status` | GLOBAL | ✅ | ❌ | ❌ | ⏳ 예정 | 조직 상태 변경은 SITE_ADMIN 전용 |

---

## 7. Settings / Thresholds

| Method | Endpoint | Scope | SITE_ADMIN | COMPANY_ADMIN | COMPANY_WORKER | 상태 | 비고 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| GET | `/users/me/settings` | OWNER | ✅ | ✅ | ✅ | ⏳ 예정 | ST-001 |
| PATCH | `/users/me/settings` | OWNER | ✅ | ✅ | ✅ | ⏳ 예정 | ST-001 |
| GET | `/users/me/thresholds` | OWNER | ✅ | ✅ | ✅ | ⏳ 예정 | ST-002 |
| POST | `/users/me/thresholds` | OWNER | ✅ | ✅ | ✅ | ⏳ 예정 | ST-002 |
| PATCH | `/users/me/thresholds/{thresholdId}` | OWNER | ✅ | ✅ | ✅ | ⏳ 예정 | ST-002 |
| GET | `/users/me/thresholds/{thresholdId}/histories` | OWNER | ✅ | ✅ | ✅ | ⏳ 예정 | ST-002 |

---

## 8. Analysis Targets / Cameras

| Method | Endpoint | Scope | SITE_ADMIN | COMPANY_ADMIN | COMPANY_WORKER | 상태 | 비고 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| GET | `/analysis-targets` | ORGANIZATION | ✅ | 자기 회사만 | 자기 회사만 | ⏳ 예정 |  |
| POST | `/analysis-targets` | ORGANIZATION | ✅ | 자기 회사만 | 정책 확정 필요 | ⏳ 예정 | 검사 대상 등록을 WORKER도 할 수 있는지 확정 필요 |
| GET | `/analysis-targets/{targetId}` | ORGANIZATION | ✅ | 자기 회사만 | 자기 회사만 | ⏳ 예정 |  |
| PATCH | `/analysis-targets/{targetId}` | ORGANIZATION | ✅ | 자기 회사만 | 정책 확정 필요 | ⏳ 예정 |  |
| DELETE | `/analysis-targets/{targetId}` | ORGANIZATION | ✅ | 자기 회사만 | ❌ | ⏳ 예정 | 삭제는 회사 관리자 이상 권장 |
| GET | `/camera-sources` | ORGANIZATION | ✅ | 자기 회사만 | 자기 회사만 | ⏳ 예정 |  |
| POST | `/camera-sources` | ORGANIZATION | ✅ | 자기 회사만 | 정책 확정 필요 | ⏳ 예정 |  |
| PATCH | `/camera-sources/{cameraId}` | ORGANIZATION | ✅ | 자기 회사만 | 정책 확정 필요 | ⏳ 예정 |  |
| DELETE | `/camera-sources/{cameraId}` | ORGANIZATION | ✅ | 자기 회사만 | ❌ | ⏳ 예정 |  |

---

## 9. Inspections

| Method | Endpoint | Scope | SITE_ADMIN | COMPANY_ADMIN | COMPANY_WORKER | 상태 | 비고 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| POST | `/inspections/upload` | ORGANIZATION | ✅ | 자기 회사만 | 자기 회사만 | 🟡 부분 | 컨트롤러 존재. 스코프 검증 미적용 — 후속 적용 필요 |
| POST | `/inspections/realtime` | ORGANIZATION | ✅ | 자기 회사만 | 자기 회사만 | 🟡 부분 | 동일 |
| PATCH | `/inspections/{inspectionId}/stop` | ORGANIZATION | ✅ | 자기 회사만 | 자기 회사만 | 🟡 부분 | 동일 |
| GET | `/inspections` | ORGANIZATION | ✅ | 자기 회사만 | 자기 회사만 | 🟡 부분 | 동일 |
| GET | `/inspections/{inspectionId}` | ORGANIZATION | ✅ | 자기 회사만 | 자기 회사만 | 🟡 부분 | 동일 |
| GET | `/inspections/{inspectionId}/events` | ORGANIZATION | ✅ | 자기 회사만 | 자기 회사만 | ⏳ 예정 |  |

---

## 10. Results

| Method | Endpoint | Scope | SITE_ADMIN | COMPANY_ADMIN | COMPANY_WORKER | 상태 | 비고 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| GET | `/results` | GLOBAL / ORGANIZATION | ✅ | 자기 회사만 | 자기 회사만 | ✅ 구현 | 비-admin은 자기 회사 organizationId 로 자동 필터 |
| GET | `/results/{resultId}` | GLOBAL / ORGANIZATION | ✅ | 자기 회사만 | 자기 회사만 | ✅ 구현 | `assertSameOrganization` 로 타회사 접근 시 403 |
| GET | `/results/{resultId}/artifacts` | ORGANIZATION | ✅ | 자기 회사만 | 자기 회사만 | ⏳ 예정 |  |
| GET | `/results/{resultId}/images` | ORGANIZATION | ✅ | 자기 회사만 | 자기 회사만 | ⏳ 예정 |  |
| GET | `/results/{resultId}/regions` | ORGANIZATION | ✅ | 자기 회사만 | 자기 회사만 | ⏳ 예정 |  |
| GET | `/results/{resultId}/explanation` | ORGANIZATION | ✅ | 자기 회사만 | 자기 회사만 | ⏳ 예정 |  |
| GET | `/results/{resultId}/report` | ORGANIZATION | ✅ | 자기 회사만 | 자기 회사만 | ⏳ 예정 |  |

---

## 11. Review (재검토)

| Method | Endpoint | Scope | SITE_ADMIN | COMPANY_ADMIN | COMPANY_WORKER | 상태 | 비고 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| GET | `/reviews` | GLOBAL / ORGANIZATION | ✅ | 자기 회사만 | ❌ | ⏳ 예정 | COMPANY_ADMIN은 자기 회사 재검토 큐만 조회 |
| GET | `/reviews/{reviewQueueId}` | GLOBAL / ORGANIZATION | ✅ | 자기 회사만 | ❌ | ⏳ 예정 | 동일 |
| PATCH | `/reviews/{reviewQueueId}` | GLOBAL / ORGANIZATION | ✅ | 자기 회사만 | ❌ | ⏳ 예정 | 동일 |
| GET | `/results/{resultId}/review-histories` | ORGANIZATION | ✅ | 자기 회사만 | 자기 회사만 | ⏳ 예정 | 결과 조회 권한과 동일 정책 권장 |
| POST | `/results/{resultId}/learning-candidates` | GLOBAL | ✅ | ❌ | ❌ | ⏳ 예정 | 학습 후보 등록은 SITE_ADMIN 전용 권장 |

---

## 12. Documents / RAG

| Method | Endpoint | Scope | SITE_ADMIN | COMPANY_ADMIN | COMPANY_WORKER | 상태 | 비고 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| GET | `/documents` | ORGANIZATION | ✅ | 자기 회사만 | 자기 회사만 | ⏳ 예정 | 자기 회사 문서 조회 |
| POST | `/documents` | ORGANIZATION | ✅ | 자기 회사만 | 정책 확정 필요 | ⏳ 예정 | 업로드를 WORKER도 할 수 있는지 확정 필요 |
| GET | `/documents/{documentId}` | ORGANIZATION | ✅ | 자기 회사만 | 자기 회사만 | ⏳ 예정 |  |
| PATCH | `/documents/{documentId}` | ORGANIZATION | ✅ | 자기 회사만 | 정책 확정 필요 | ⏳ 예정 |  |
| DELETE | `/documents/{documentId}` | ORGANIZATION | ✅ | 자기 회사만 | ❌ | ⏳ 예정 | 삭제는 회사 관리자 이상 권장 |
| GET | `/documents/{documentId}/versions` | ORGANIZATION | ✅ | 자기 회사만 | 자기 회사만 | ⏳ 예정 |  |
| POST | `/documents/{documentId}/versions` | ORGANIZATION | ✅ | 자기 회사만 | 정책 확정 필요 | ⏳ 예정 |  |
| GET | `/document-versions/{versionId}/chunks` | ORGANIZATION | ✅ | 자기 회사만 | 자기 회사만 | ⏳ 예정 |  |
| GET | `/document-versions/{versionId}/index-jobs` | ORGANIZATION | ✅ | 자기 회사만 | 자기 회사만 | ⏳ 예정 |  |
| POST | `/document-versions/{versionId}/index-jobs` | ORGANIZATION | ✅ | 자기 회사만 | 정책 확정 필요 | ⏳ 예정 | 인덱싱 재요청 권한 확정 필요 |

---

## 13. Chatbot

| Method | Endpoint | Scope | SITE_ADMIN | COMPANY_ADMIN | COMPANY_WORKER | 상태 | 비고 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| GET | `/chat-conversations` | OWNER | ✅ | ✅ | ✅ | ⏳ 예정 | 본인 대화 목록 |
| POST | `/chat-conversations` | OWNER | ✅ | ✅ | ✅ | ⏳ 예정 |  |
| GET | `/chat-conversations/{conversationId}` | OWNER | ✅ | ✅ | ✅ | ⏳ 예정 | 대화 소유자 검증 필요 |
| DELETE | `/chat-conversations/{conversationId}` | OWNER | ✅ | ✅ | ✅ | ⏳ 예정 | 동일 |
| GET | `/chat-conversations/{conversationId}/messages` | OWNER | ✅ | ✅ | ✅ | ⏳ 예정 | 동일 |
| POST | `/chat-conversations/{conversationId}/messages` | OWNER | ✅ | ✅ | ✅ | ⏳ 예정 | 동일. RAG 컨텍스트의 documentIds는 자기 회사 문서로 제한 필요 |
| GET | `/chat-messages/{messageId}/sources` | OWNER | ✅ | ✅ | ✅ | ⏳ 예정 |  |

---

## 14. Notifications

`/notifications` 계열은 본인 알림(OWNER scope) 기준입니다. SITE_ADMIN이 본 API로 타 사용자 알림을 조회할 수 없으며, 회사/전체 알림 운영 조회가 필요한 경우 아래의 `/organizations/{organizationId}/notifications` 또는 `/admin/notifications` 를 사용합니다.

| Method | Endpoint | Scope | SITE_ADMIN | COMPANY_ADMIN | COMPANY_WORKER | 상태 | 비고 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| GET | `/notifications` | OWNER | 본인만 | 본인만 | 본인만 | ⏳ 예정 | 본인 알림 목록 |
| GET | `/notifications/{notificationId}` | OWNER | 본인만 | 본인만 | 본인만 | ⏳ 예정 |  |
| PATCH | `/notifications/{notificationId}/read` | OWNER | 본인만 | 본인만 | 본인만 | ⏳ 예정 |  |
| PATCH | `/notifications/read-all` | OWNER | 본인만 | 본인만 | 본인만 | ⏳ 예정 |  |
| DELETE | `/notifications/{notificationId}` | OWNER | 본인만 | 본인만 | 본인만 | ⏳ 예정 |  |
| GET | `/organizations/{organizationId}/notifications` | ORGANIZATION | ✅ | 자기 회사만 | ❌ | ⏳ 예정 | 회사 관리자용 알림 현황 조회 |
| GET | `/admin/notifications` | GLOBAL | ✅ | ❌ | ❌ | ⏳ 예정 | 전체 알림 운영 조회 |

---

## 15. Reports

| Method | Endpoint | Scope | SITE_ADMIN | COMPANY_ADMIN | COMPANY_WORKER | 상태 | 비고 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| GET | `/reports` | GLOBAL / ORGANIZATION | ✅ | 자기 회사만 | ❌ | ⏳ 예정 | COMPANY_ADMIN은 자기 회사 보고서만 조회 |
| POST | `/reports` | GLOBAL / ORGANIZATION | ✅ | 자기 회사만 | ❌ | ⏳ 예정 |  |
| GET | `/reports/{reportId}` | GLOBAL / ORGANIZATION | ✅ | 자기 회사만 | ❌ | ⏳ 예정 |  |
| GET | `/reports/{reportId}/items` | GLOBAL / ORGANIZATION | ✅ | 자기 회사만 | ❌ | ⏳ 예정 |  |
| GET | `/reports/{reportId}/files` | GLOBAL / ORGANIZATION | ✅ | 자기 회사만 | ❌ | ⏳ 예정 |  |
| GET | `/reports/{reportId}/download` | GLOBAL / ORGANIZATION | ✅ | 자기 회사만 | ❌ | ⏳ 예정 |  |

---

## 16. Operation / Admin

| Method | Endpoint | Scope | SITE_ADMIN | COMPANY_ADMIN | COMPANY_WORKER | 상태 | 비고 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| GET | `/admin/audit-logs` | GLOBAL | ✅ | ❌ | ❌ | ✅ 구현 | `/admin/**` SITE_ADMIN 매처 적용 |
| GET | `/admin/action-logs` | GLOBAL | ✅ | ❌ | ❌ | ✅ 구현 | 동일 |
| GET | `/admin/operation-logs` | GLOBAL | ✅ | ❌ | ❌ | ✅ 구현 | 동일 |
| GET | `/admin/system-status` | GLOBAL | ✅ | ❌ | ❌ | ✅ 구현 | 동일 |
| GET | `/admin/system-components` | GLOBAL | ✅ | ❌ | ❌ | ✅ 구현 | 동일 |
| GET | `/admin/operation-policies` | GLOBAL | ✅ | ❌ | ❌ | ✅ 구현 | 동일 |
| PATCH | `/admin/operation-policies/{policyId}` | GLOBAL | ✅ | ❌ | ❌ | ✅ 구현 | 정책 수정 시 감사/관리자 작업/운영 로그 기록 |
| GET | `/admin/async-jobs` | GLOBAL | ✅ | ❌ | ❌ | ✅ 구현 | 동일 |
| GET | `/admin/async-jobs/{jobId}` | GLOBAL | ✅ | ❌ | ❌ | ✅ 구현 | 동일 |
| (`/operations/**`) | (api.txt 외 운영 영역) | GLOBAL | ✅ | ❌ | ❌ | 🟡 부분 | `SecurityConfig`에 `/operations/**` SITE_ADMIN 매처 적용 |

---

## 17. Files

파일 단독 `organization_id`가 없는 경우, 파일을 참조하는 연결 리소스 기준으로 권한을 검증합니다. 예시:

- `RESULT_ARTIFACT` → `INSPECTION_RESULT` → `INSPECTION_RUN.organization_id`
- `DOCUMENT_VERSION` → `DOCUMENT.organization_id`
- `REPORT_FILE` → `REPORT.organization_id`

| Method | Endpoint | Scope | SITE_ADMIN | COMPANY_ADMIN | COMPANY_WORKER | 상태 | 비고 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| GET | `/files/{fileId}` | ORGANIZATION | ✅ | 자기 회사만 | 자기 회사만 | 🟡 부분 | 연결 리소스의 `organization_id` 기준 검증. 컨트롤러 존재, 스코프 검증 미적용 — 후속 적용 필요 |
| GET | `/files/{fileId}/download` | ORGANIZATION | ✅ | 자기 회사만 | 자기 회사만 | 🟡 부분 | 동일 |
| DELETE | `/files/{fileId}` | ORGANIZATION | ✅ | 자기 회사만 | 정책 확정 필요 | 🟡 부분 | 동일. 삭제 권한 확정 필요 |

---

## 18. Models

| Method | Endpoint | Scope | SITE_ADMIN | COMPANY_ADMIN | COMPANY_WORKER | 상태 | 비고 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| GET | `/models` | GLOBAL | ✅ | ❌ | ❌ | ⏳ 예정 | 모델 관리 SITE_ADMIN 전용 |
| POST | `/models` | GLOBAL | ✅ | ❌ | ❌ | ⏳ 예정 |  |
| GET | `/models/{modelId}/versions` | GLOBAL | ✅ | ❌ | ❌ | ⏳ 예정 |  |
| POST | `/models/{modelId}/versions` | GLOBAL | ✅ | ❌ | ❌ | ⏳ 예정 |  |
| PATCH | `/model-versions/{versionId}/activate` | GLOBAL | ✅ | ❌ | ❌ | ⏳ 예정 |  |
| POST | `/model-versions/{versionId}/deployments` | GLOBAL | ✅ | ❌ | ❌ | ⏳ 예정 |  |
| PATCH | `/model-deployments/{deploymentId}/rollback` | GLOBAL | ✅ | ❌ | ❌ | ⏳ 예정 |  |

---

## 19. Dashboard

| Method | Endpoint | Scope | SITE_ADMIN | COMPANY_ADMIN | COMPANY_WORKER | 상태 | 비고 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| GET | `/dashboard/summary` | GLOBAL / ORGANIZATION | ✅ | 자기 회사만 | 자기 회사만 | ⏳ 예정 |  |
| GET | `/dashboard/recent-results` | GLOBAL / ORGANIZATION | ✅ | 자기 회사만 | 자기 회사만 | ⏳ 예정 |  |
| GET | `/dashboard/statistics/decision-trend` | GLOBAL / ORGANIZATION | ✅ | 자기 회사만 | 자기 회사만 | ⏳ 예정 |  |
| GET | `/dashboard/statistics/defect-types` | GLOBAL / ORGANIZATION | ✅ | 자기 회사만 | 자기 회사만 | ⏳ 예정 |  |
| GET | `/dashboard/statistics/targets` | GLOBAL / ORGANIZATION | ✅ | 자기 회사만 | 자기 회사만 | ⏳ 예정 |  |

---

## 20. 정책 확정 필요 항목 모음

본 매트릭스에서 `정책 확정 필요`로 표시된 항목은 후속 의사결정이 필요합니다.

- `PATCH /users/{userId}/status` — COMPANY_ADMIN의 사용자 상태 변경 권한 여부
- `GET /organizations/{organizationId}` — COMPANY_WORKER가 자기 회사 정보를 조회할 수 있는지
- `POST/PATCH/DELETE /analysis-targets`, `/camera-sources` — COMPANY_WORKER의 등록/수정/삭제 권한 범위
- `POST/PATCH /documents`, `/document-versions/{versionId}/index-jobs` — COMPANY_WORKER의 문서 등록/수정/인덱싱 권한
- `DELETE /files/{fileId}` — COMPANY_WORKER의 파일 삭제 권한

---

## 21. 백엔드 적용 가이드

- 컨트롤러 단 권한: `SecurityConfig`의 `antMatchers(...).hasRole("SITE_ADMIN")` 또는 `.authenticated()` 로 1차 차단
- 서비스 단 조직 스코프: `SecurityUtils.assertSameOrganization(targetOrganizationId)` (또는 메시지 오버로드) 호출로 일관 검증
- 페이지 조회처럼 organizationId 를 쿼리 파라미터로 자동 주입해야 하는 경우: `securityUtils.isSiteAdmin() ? null : securityUtils.requireOrganizationId()` 패턴 사용
- 새 컨트롤러 추가 시 본 매트릭스에 행을 먼저 추가하고, 행과 동일한 권한 검사를 코드에 반영합니다.

---

## Dashboard 정합성 메모

대시보드의 현재 기준 엔드포인트는 `GET /dashboard/overview` 하나로 통합한다. 기존 문서에 `/dashboard/summary`, `/dashboard/recent-results`, `/dashboard/statistics/*`가 남아 있으면 레거시 예정 항목으로 보고 신규 구현/프론트 연동 기준에서는 사용하지 않는다.

| Method | Endpoint | Scope | SITE_ADMIN | COMPANY_ADMIN | COMPANY_WORKER | 상태 | 비고 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| GET | `/dashboard/overview` | GLOBAL / ORGANIZATION | 허용 | 자기 회사만 | 자기 회사만 | 구현 | SITE_ADMIN은 `organizationId` 필터 가능, 일반 사용자는 자기 `organization_id` 범위로 자동 제한 |
