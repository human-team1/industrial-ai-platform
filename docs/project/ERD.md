
ERD 문서는 논리 테이블명 표기를 위해 대문자를 유지한다.
실제 MariaDB init SQL의 물리 테이블명 및 FK 참조는 소문자로 통일한다.

---

# ✅ 1. ORGANIZATION / USER

| Table | Columns | Description |
| --- | --- | --- |
| ORGANIZATION | organization_id (PK), organization_name, status, created_at, updated_at | 조직(회사) 정보 |
| USERS | user_id (PK), organization_id (FK), google_sub (UK, NULL), email (UK, NOT NULL), password_hash (NULL), name, picture, phone, status (NOT NULL), role, last_login_at, created_at, updated_at, deleted_at | 이메일 로그인 + Google OAuth 동시 지원 사용자 계정 |
| SIGNUP_REQUEST | signup_request_id (PK), user_id (FK), organization_id (FK), request_status, reject_reason, processed_by (FK), requested_at, processed_at | 가입 승인/거절 요청 |
| AUTH_SESSION | auth_session_id (PK), user_id (FK), access_token, refresh_token, ip_address, user_agent, login_at, expires_at, revoked_at | 로그인 세션 |
| USER_SETTING | user_setting_id (PK), user_id (FK), notification_enabled, default_dashboard_range, default_camera_id, created_at, updated_at | 사용자 설정 |
| USER_THRESHOLD | threshold_id (PK), user_id (FK), anomaly_threshold, low_confidence_threshold, min_allowed, max_allowed, apply_scope, is_active, created_at, updated_at | 개인 임계값 |
| USER_THRESHOLD_HISTORY | threshold_history_id (PK), threshold_id (FK), version,old_anomaly_threshold, new_anomaly_threshold,change_reason, changed_by (FK), changed_at | 임계값 변경 이력 |
| ORGANIZATION_PUBLIC  |  id, name | ACTIVE 상태의 조직만 외부/회원가입 화면에 제공하는 공개 조직 목록 View |

---

### USER_THRESHOLD_HISTORY 상세 타입 변경 ⇒ erd 페이지에서 수정 예정

| 컬럼 | 기존 | 수정 |
| --- | --- | --- |
| `version` | 없음 | `INT` |
| `old_anomaly_threshold` | `DOUBLE` | `DECIMAL(5,4)` |
| `new_anomaly_threshold` | `DOUBLE` | `DECIMAL(5,4)` |
| `change_reason` | `VARCHAR(255)` | `TEXT` |

# ✅ 2. INSPECTION DOMAIN

| Table | Columns | Description | 비고 |
| --- | --- | --- | --- |
| ANALYSIS_TARGET | target_id (PK), organization_id (FK), target_name, equipment_name, product_name, location_name, target_type, target_status, created_by (FK), created_at, updated_at | 검사 대상 |  |
| CAMERA_SOURCE | camera_id (PK), organization_id (FK), user_id (FK), camera_name, stream_url, status, created_at, updated_at | 카메라/스트림 입력 |  |
| INSPECTION_RUN | inspection_id (PK), organization_id (FK), user_id (FK), target_id (FK),run_type, input_type, source_type, source_id, run_status,applied_threshold, idempotency_key, payload_fingerprint,error_code, started_at, completed_at | 검사 실행 | UK: (organization_id, user_id, idempotency_key)
constraint: uk_inspection_run_org_user_idempotency |
| INSPECTION_INPUT | inspection_input_id (PK), inspection_id (FK), source_type,file_id, camera_id, stream_url, source_name, mime_type,duration_sec, frame_count, created_at | 검사 입력 데이터 |  |
| INSPECTION_EVENT_LOG | event_id (PK), inspection_id (FK), event_type, message, created_at | 검사 이벤트 로그 |  |
| INSPECTION_RESULT | result_id (PK), inspection_id (FK), score, confidence, decision_code, final_decision_code, result_status, threshold_source, threshold_id, threshold_version, model_version_id, failure_reason, created_at | 검사 결과 |  |
| RESULT_ARTIFACT | artifact_id (PK), result_id (FK), artifact_type, file_id, created_at | 결과 산출물 |  |
| IMAGE | image_id (PK), result_id (FK), file_id, image_role, created_at | 결과 이미지 |  |
| ANOMALY_REGION | region_id (PK), image_id (FK), label_code, bbox_x, bbox_y, bbox_w, bbox_h, score, created_at | 이상 영역 |  |

---

### ANALYSIS_TARGET 상세 컬럼 추가

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| `location_name` | `VARCHAR(100)` | 대시보드 및 결과 목록에서 표시할 설비/검사 위치명. 예: 라인 A-1, 라인 B-2 |

### INSPECTION_RUN 상세 타입 변경

| 컬럼/제약 | 기존 | 수정 |
| --- | --- | --- |
| `applied_threshold` | `DOUBLE` | `DECIMAL(5,4)` |
| `idempotency_key` | `VARCHAR(255) UNIQUE` | `VARCHAR(255) NOT NULL` |
| `payload_fingerprint` | 없음 | `VARCHAR(64)` |
| UNIQUE | `UNIQUE(idempotency_key)` | `UNIQUE(organization_id, user_id, idempotency_key)` |

### INSPECTION_INPUT 상세 타입

| 컬럼 | 기존 | 수정 |
| --- | --- | --- |
| `source_type` | 없음 | `VARCHAR(20) NOT NULL` |
| `stream_url` | `TEXT` | `TEXT` |
| `source_name` | `VARCHAR(255)` | `VARCHAR(255)` |
| `mime_type` | `VARCHAR(100)` | `VARCHAR(100)` |

### INSPECTION_RESULT 상세 타입

| 컬럼 | 기존 | 수정 |
| --- | --- | --- |
| threshold_version | `VARCHAR(50)` | `VARCHAR(int)` |

# ✅ 3. REVIEW DOMAIN

| Table | Columns | Description |
| --- | --- | --- |
| REVIEW_QUEUE | review_queue_id (PK), result_id (FK), queue_status, queued_reason, queued_at | 재검토 대상 |
| REVIEW_HISTORY | review_history_id (PK), result_id (FK), before_decision, after_decision, review_comment, reviewed_at, reviewed_by (FK) | 판정 수정 이력 |
| LEARNING_CANDIDATE | learning_candidate_id (PK), result_id (FK), candidate_status, selected_at | 학습 후보 데이터 |

---

# ✅ 4. DOCUMENT / RAG

| Table | Columns | Description |
| --- | --- | --- |
| DOCUMENT | document_id (PK), organization_id (FK), owner_user_id (FK), title, document_type, category, equipment_type, description, current_status, created_at, updated_at, deleted_at | 문서 기본 정보 및 문서 메타데이터 |
| DOCUMENT_TAG | document_tag_id (PK), document_id (FK), tag_name, created_at | 문서별 태그. DOCUMENT 1건에 여러 태그를 연결 |
| DOCUMENT_VERSION | document_version_id (PK), document_id (FK), version_no, file_id (FK), file_hash, indexing_status, indexed_chunk_count, index_error_message, indexed_at, created_at | 문서 파일 버전 및 인덱싱 상태 |
| DOCUMENT_INDEX_JOB | job_id (PK), document_version_id (FK), job_status, error_message, started_at, completed_at | 인덱싱 작업 |
| CHUNK | chunk_id (PK), document_version_id (FK), sequence_no, content, created_at | 문서 청크 |
| VECTOR_INDEX | vector_id (PK), chunk_id (FK), embedding_model, vector_ref, created_at | 벡터 데이터 |

---

# ✅ 5. CHAT DOMAIN

| Table | Columns | Description |
| --- | --- | --- |
| CHAT_CONVERSATION | conversation_id (PK), user_id (FK), title, created_at, updated_at, deleted_at | 챗봇 대화 |
| CHAT_MESSAGE | message_id (PK), conversation_id (FK), role, message_text, message_status, answer_status, error_code, model_name, created_at, updated_at | 메시지 |
| CHAT_SOURCE | chat_source_id (PK), message_id (FK), source_type, source_id, document_id (FK), document_title, document_type, chunk_id (FK), page_no, section, source_snippet, score, created_at | 답변 출처 |

---

### CHAT_MESSAGE 상세 컬럼

| 컬럼 | 타입 제안 | 설명 |
| --- | --- | --- |
| `message_status` | `VARCHAR(20)` | 메시지 처리 상태. `SUCCESS / FAILED` |
| `answer_status` | `VARCHAR(50)` | RAG 답변 상태. `ANSWERED / NO_RELEVANT_SOURCE / LLM_FAILED / VECTOR_STORE_FAILED / DOCUMENT_SCOPE_FORBIDDEN / VALIDATION_FAILED` |
| `error_code` | `VARCHAR(50)` | 답변 생성 실패 시 에러 코드 |
| `model_name` | `VARCHAR(100)` | 답변 생성에 사용한 모델명 |
| `updated_at` | `TIMESTAMP` | 메시지 상태 변경 시각 |

### CHAT_SOURCE 상세 컬럼

| 컬럼 | 타입 제안 | 설명 |
| --- | --- | --- |
| `document_id` | `BIGINT` | 출처 문서 ID |
| `document_title` | `VARCHAR(255)` | 답변 생성 당시 문서 제목 스냅샷 |
| `document_type` | `VARCHAR(50)` | PDF, DOCX, XLSX 등 문서 타입 |
| `chunk_id` | `BIGINT` | 검색된 문서 청크 ID |
| `page_no` | `INT` | 출처 페이지 번호 |
| `section` | `VARCHAR(255)` | 출처 섹션명 |
| `source_snippet` | `TEXT` | 출처 본문 일부 |
| `score` | `DECIMAL(5,4)` | RAG 검색 유사도 또는 신뢰도 점수 |
| `created_at` | `TIMESTAMP` | 출처 저장 시각 |

---

# ✅ 6. NOTIFICATION / REPORT

| Table | Columns | Description |
| --- | --- | --- |
| NOTIFICATION | notification_id (PK), user_id (FK), notification_type, severity, title, message, related_type, related_id, target_url, dedup_key, is_read, created_at | 알림 |
| REPORT | report_id (PK), organization_id (FK), report_type, period_start, period_end, report_status, snapshot_at, generated_at, generated_by (FK) | 보고서 |
| REPORT_ITEM | report_item_id (PK), report_id (FK), result_id (FK), summary_text | 보고서 항목 |
| REPORT_FILE | report_file_id (PK), report_id (FK), file_id | 보고서 파일 |

---

# ✅ 7. OPERATION DOMAIN

| Table | Columns | Description |
| --- | --- | --- |
| AUDIT_LOG | audit_log_id (PK), actor_user_id (FK), action_type, target_type, target_id, before_json, after_json, created_at | 감사 로그 |
| ADMIN_ACTION_LOG | admin_action_id (PK), actor_user_id (FK), action_type, target_type, target_id, reason, created_at | 관리자 작업 |
| OPERATION_LOG | operation_log_id (PK), event_type, event_status, log_level, source_component, request_id, actor_user_id (FK), detail_message, related_path, created_at | 운영 로그 |
| SYSTEM_STATUS_SNAPSHOT | snapshot_id (PK), cpu_usage, memory_usage, disk_usage, response_time_ms, created_at | 시스템 상태 스냅샷 |
| SYSTEM_COMPONENT_STATUS | component_status_id (PK), component_type, component_name, status, message, cpu_usage, memory_usage, disk_usage, host_name, instance_id, response_time_ms, checked_at, created_at | 시스템 컴포넌트별 상태 |
| OPERATION_POLICY | operation_policy_id (PK), policy_category, policy_key, policy_name, policy_value, value_type, description, is_active, updated_at, updated_by (FK) | 운영 정책 |
| ASYNC_JOB | job_id (PK), job_type, job_status, target_type, target_id, error_message, created_at, completed_at | 비동기 작업 |

---

### OPERATION_LOG 상세 컬럼 추가

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| `log_level` | `VARCHAR(20)` | 로그 레벨. 예: `INFO / WARN / ERROR` |
| `source_component` | `VARCHAR(50)` | 로그 발생 컴포넌트. 예: `SPRING_API / AI_SERVER / MARIADB / REDIS / MINIO` |
| `request_id` | `VARCHAR(100)` | 요청 추적 ID |
| `actor_user_id` | `BIGINT` | 관련 사용자 ID. 없을 수 있음 |

### SYSTEM_COMPONENT_STATUS 상세 테이블 추가

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| `component_status_id` | `BIGINT` | PK |
| `component_type` | `VARCHAR(50)` | 컴포넌트 타입. 예: `SPRING_API / AI_SERVER / MARIADB / REDIS / MINIO / CHROMA / STREAM_SERVER / STORAGE` |
| `component_name` | `VARCHAR(100)` | 화면 표시명. 예: Spring API 서버 |
| `status` | `VARCHAR(20)` | 상태. 예: `NORMAL / WARNING / ERROR / UNKNOWN` |
| `message` | `VARCHAR(255)` | 상태 설명 |
| `cpu_usage` | `DECIMAL(5,2)` | Spring/AI 서버 등 노드 CPU 사용률. 수집 불가 시 NULL |
| `memory_usage` | `DECIMAL(5,2)` | Spring/AI 서버 등 노드 메모리 사용률. 수집 불가 시 NULL |
| `disk_usage` | `DECIMAL(5,2)` | Spring/AI 서버 등 노드 디스크 사용률. 수집 불가 시 NULL |
| `host_name` | `VARCHAR(100)` | 상태를 수집한 호스트명 |
| `instance_id` | `VARCHAR(100)` | 상태를 수집한 인스턴스 식별자 |
| `response_time_ms` | `INT` | 응답 시간 |
| `checked_at` | `TIMESTAMP` | 점검 시각 |
| `created_at` | `TIMESTAMP` | 생성 시각 |

### OPERATION_POLICY 상세 컬럼 변경

| 컬럼 | 기존 | 수정 |
| --- | --- | --- |
| `policy_type` | `VARCHAR` | `policy_category VARCHAR(50)`, `policy_key VARCHAR(100)`, `policy_name VARCHAR(100)`로 분리 |
| `policy_value` | `TEXT` | 유지 |
| 없음 | - | `value_type VARCHAR(20)` 추가 |
| 없음 | - | `description VARCHAR(255)` 추가 |
| 없음 | - | `is_active BOOLEAN` 추가 |

---

# ✅ 8. FILE / MODEL

| Table | Columns | Description |
| --- | --- | --- |
| FILE | file_id (PK), storage_type, bucket_name, object_key, file_path, file_name, file_ext, mime_type, file_size, checksum, created_at, created_by (FK) | 파일 저장 |
| MODEL | model_id (PK), model_name, model_type, created_at | 모델 |
| MODEL_VERSION | model_version_id (PK), model_id (FK), file_id (FK), version_name, accuracy, precision_score, recall_score, deploy_status, is_active, validated_at, validated_by (FK), created_at | 모델 버전 |
| MODEL_DEPLOYMENT | deployment_id (PK), model_version_id (FK), deployed_at, rollback_flag, deploy_status | 모델 배포 |

---
