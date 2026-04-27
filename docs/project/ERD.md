
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
| USER_THRESHOLD_HISTORY | threshold_history_id (PK), threshold_id (FK), old_anomaly_threshold, new_anomaly_threshold, change_reason, changed_by (FK), changed_at | 임계값 변경 이력 |

---

# ✅ 2. INSPECTION DOMAIN

| Table | Columns | Description |
| --- | --- | --- |
| ANALYSIS_TARGET | target_id (PK), organization_id (FK), target_name, equipment_name, product_name, target_type, target_status, created_by (FK), created_at, updated_at | 검사 대상 |
| CAMERA_SOURCE | camera_id (PK), organization_id (FK), user_id (FK), camera_name, stream_url, status, created_at, updated_at | 카메라/스트림 입력 |
| INSPECTION_RUN | inspection_id (PK), organization_id (FK), user_id (FK), target_id (FK), run_type, input_type, source_type, source_id, run_status, applied_threshold, idempotency_key (UK), error_code, started_at, completed_at | 검사 실행 |
| INSPECTION_INPUT | inspection_input_id (PK), inspection_id (FK), file_id, camera_id, stream_url, source_name, mime_type, duration_sec, frame_count, created_at | 검사 입력 데이터 |
| INSPECTION_EVENT_LOG | event_id (PK), inspection_id (FK), event_type, message, created_at | 검사 이벤트 로그 |
| INSPECTION_RESULT | result_id (PK), inspection_id (FK), score, confidence, decision_code, final_decision_code, result_status, threshold_source, threshold_id, threshold_version, model_version_id, failure_reason, created_at | 검사 결과 |
| RESULT_ARTIFACT | artifact_id (PK), result_id (FK), artifact_type, file_id, created_at | 결과 산출물 |
| IMAGE | image_id (PK), result_id (FK), file_id, image_role, created_at | 결과 이미지 |
| ANOMALY_REGION | region_id (PK), image_id (FK), label_code, bbox_x, bbox_y, bbox_w, bbox_h, score, created_at | 이상 영역 |

---

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
| DOCUMENT | document_id (PK), organization_id (FK), owner_user_id (FK), title, document_type, current_status, created_at, updated_at, deleted_at | 문서 |
| DOCUMENT_VERSION | document_version_id (PK), document_id (FK), version_no, file_id, file_hash, indexing_status, indexed_chunk_count, index_error_message, indexed_at, created_at | 문서 버전 |
| DOCUMENT_INDEX_JOB | job_id (PK), document_version_id (FK), job_status, error_message, started_at, completed_at | 인덱싱 작업 |
| CHUNK | chunk_id (PK), document_version_id (FK), sequence_no, content, created_at | 문서 청크 |
| VECTOR_INDEX | vector_id (PK), chunk_id (FK), embedding_model, vector_ref, created_at | 벡터 데이터 |

---

# ✅ 5. CHAT DOMAIN

| Table | Columns | Description |
| --- | --- | --- |
| CHAT_CONVERSATION | conversation_id (PK), user_id (FK), title, created_at, updated_at, deleted_at | 챗봇 대화 |
| CHAT_MESSAGE | message_id (PK), conversation_id (FK), role, message_text, created_at | 메시지 |
| CHAT_SOURCE | chat_source_id (PK), message_id (FK), source_type, source_id, chunk_id, source_snippet | 답변 출처 |

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
| OPERATION_LOG | operation_log_id (PK), event_type, event_status, detail_message, related_path, created_at | 운영 로그 |
| SYSTEM_STATUS_SNAPSHOT | snapshot_id (PK), cpu_usage, memory_usage, disk_usage, response_time_ms, created_at | 시스템 상태 |
| OPERATION_POLICY | operation_policy_id (PK), policy_type, policy_value, updated_at, updated_by (FK) | 운영 정책 |
| ASYNC_JOB | job_id (PK), job_type, job_status, target_type, target_id, error_message, created_at, completed_at | 비동기 작업 |

---

# ✅ 8. FILE / MODEL

| Table | Columns | Description |
| --- | --- | --- |
| FILE | file_id (PK), storage_type, bucket_name, object_key, file_path, file_name, file_ext, mime_type, file_size, checksum, created_at, created_by (FK) | 파일 저장 |
| MODEL | model_id (PK), model_name, model_type, created_at | 모델 |
| MODEL_VERSION | model_version_id (PK), model_id (FK), file_id (FK), version_name, accuracy, precision_score, recall_score, deploy_status, is_active, validated_at, validated_by (FK), created_at | 모델 버전 |
| MODEL_DEPLOYMENT | deployment_id (PK), model_version_id (FK), deployed_at, rollback_flag, deploy_status | 모델 배포 |

---