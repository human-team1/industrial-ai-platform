---

# ERD-TEXT

ERD 문서는 논리 테이블명 표기를 위해 대문자를 유지한다.

실제 MariaDB init SQL의 물리 테이블명 및 FK 참조는 소문자로 통일한다.

---

# ✅ 1. ORGANIZATION / USER

| Table | Columns | Description |
| --- | --- | --- |
| ORGANIZATION | organization_id (PK), organization_name, status, created_at, updated_at | 조직(회사) 정보 |
| USERS | user_id (PK), organization_id (FK), google_sub (UK, NULL), email (UK, NOT NULL), password_hash (NULL), name, picture, phone, status, role, last_login_at, created_at, updated_at, deleted_at | 사용자 계정 |
| SIGNUP_REQUEST | signup_request_id (PK), user_id (FK), organization_id (FK), request_status, reject_reason, processed_by (FK), requested_at, processed_at | 가입 승인/거절 요청 |
| AUTH_SESSION | auth_session_id (PK), user_id (FK), access_token, refresh_token, ip_address, user_agent, login_at, expires_at, revoked_at | 로그인 세션 |
| USER_SETTING | user_setting_id (PK), user_id (FK), notification_enabled, default_dashboard_range, default_camera_id, created_at, updated_at | 사용자 설정 |
| USER_THRESHOLD | threshold_id (PK), user_id (FK), anomaly_threshold, low_confidence_threshold, min_allowed, max_allowed, apply_scope, is_active, created_at, updated_at | 개인 임계값 |
| USER_THRESHOLD_HISTORY | threshold_history_id (PK), threshold_id (FK), version, old_anomaly_threshold, new_anomaly_threshold, change_reason, changed_by (FK), changed_at | 임계값 변경 이력 |
| ORGANIZATION_PUBLIC | id, name | ACTIVE 조직 공개 View |

---

### USER_THRESHOLD_HISTORY 상세 컬럼

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| version | INT | 임계값 변경 버전 |
| old_anomaly_threshold | DECIMAL(8,4) | 이전 이상 임계값 |
| new_anomaly_threshold | DECIMAL(8,4) | 변경 이상 임계값 |
| change_reason | TEXT | 변경 사유 |

---

# ✅ 2. INSPECTION DOMAIN

| Table | Columns | Description | 비고 |
| --- | --- | --- | --- |
| ANALYSIS_TARGET | target_id (PK), organization_id (FK), target_name, equipment_name, product_name, location_name, target_type, target_status, created_by (FK), created_at, updated_at | 검사 대상 |  |
| CAMERA_SOURCE | camera_id (PK), organization_id (FK), user_id (FK), camera_name, stream_url, status, created_at, updated_at | 카메라/스트림 입력 |  |
| INSPECTION_RUN | inspection_id (PK), organization_id (FK), user_id (FK), target_id (FK), run_type, input_type, source_type, source_id, run_status, applied_threshold, idempotency_key, payload_fingerprint, error_code, started_at, completed_at | 검사 실행 | UK: organization_id, user_id, idempotency_key |
| INSPECTION_INPUT | inspection_input_id (PK), inspection_id (FK), source_type, file_id, camera_id, stream_url, source_name, mime_type, duration_sec, frame_count, roi_mode, roi_coordinate_type, roi_x, roi_y, roi_width, roi_height, sampling_fps, max_frames, quality_gate_enabled, created_at | 검사 입력 데이터 | 이미지/영상/실시간 입력 조건 |
| INSPECTION_EVENT_LOG | event_id (PK), inspection_id (FK), event_type, message, created_at | 검사 이벤트 로그 |  |
| INSPECTION_RESULT | result_id (PK), inspection_id (FK), score, confidence, decision_code, final_decision_code, result_status, threshold_source, threshold_id, threshold_version, model_version_id (FK), image_path, category_type, category, model_profile, model_name, anomaly_score, image_threshold, predicted_label, heatmap_path, pixel_threshold, inference_time, analyzed_frame_count, skipped_frame_count, defect_frame_count, recheck_frame_count, max_frame_score, avg_frame_score, representative_frame_seq, input_quality_status, input_quality_reason, failure_reason, created_at | 검사 결과 | 이미지 단건 및 영상/세션 집계 |
| RESULT_ARTIFACT | artifact_id (PK), result_id (FK), artifact_type, file_id, created_at | 결과 산출물 |  |
| IMAGE | image_id (PK), result_id (FK), file_id, image_role, created_at | 결과 이미지 |  |
| ANOMALY_REGION | region_id (PK), image_id (FK), label_code, bbox_x, bbox_y, bbox_w, bbox_h, score, created_at | 이상 영역 |  |

---

### ANALYSIS_TARGET 상세 컬럼

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| location_name | VARCHAR(100) | 대시보드 및 결과 목록 표시용 위치명 |

---

### INSPECTION_RUN 상세 컬럼

| 컬럼/제약 | 타입/정책 | 설명 |
| --- | --- | --- |
| applied_threshold | DECIMAL(8,4) | 검사에 적용된 임계값 |
| idempotency_key | VARCHAR(255) NOT NULL | 중복 요청 방지 키 |
| payload_fingerprint | VARCHAR(64) | 동일 키의 다른 payload 충돌 검출 |
| UNIQUE | organization_id, user_id, idempotency_key | 사용자/조직 단위 멱등성 보장 |

---

### INSPECTION_INPUT 상세 컬럼

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| source_type | VARCHAR(30) NOT NULL | IMAGE / VIDEO / BROWSER_CAMERA / RTSP_STREAM |
| roi_mode | VARCHAR(20) | FULL_FRAME / FIXED |
| roi_coordinate_type | VARCHAR(20) | 기본 NORMALIZED |
| roi_x | DECIMAL(8,6) | 정규화 ROI 시작 x |
| roi_y | DECIMAL(8,6) | 정규화 ROI 시작 y |
| roi_width | DECIMAL(8,6) | 정규화 ROI 너비 |
| roi_height | DECIMAL(8,6) | 정규화 ROI 높이 |
| sampling_fps | DECIMAL(5,2) | 영상/실시간 분석 FPS |
| max_frames | INT | 영상 분석 최대 프레임 수 |
| quality_gate_enabled | BOOLEAN | 입력 품질 검사 사용 여부 |

---

### INSPECTION_RESULT 상세 컬럼

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| score | DECIMAL(8,4) | 이미지 또는 집계 결과 이상 점수 |
| confidence | DECIMAL(6,4) | 추론 신뢰도. 0~1 범위 기준 |
| threshold_version | INT | 적용 임계값 버전 |
| analyzed_frame_count | INT | 실제 추론한 프레임 수 |
| skipped_frame_count | INT | 건너뛴 프레임 수 |
| defect_frame_count | INT | 이상 판정 프레임 수 |
| recheck_frame_count | INT | 재검사 판정 프레임 수 |
| max_frame_score | DECIMAL(8,4) | 최대 프레임 이상 점수 |
| avg_frame_score | DECIMAL(8,4) | 평균 프레임 이상 점수 |
| representative_frame_seq | INT | 대표 프레임 번호 |
| input_quality_status | VARCHAR(30) | 입력 품질 요약 상태 |
| input_quality_reason | VARCHAR(100) | 입력 품질 대표 사유 |
| model_version_id  | BIGINT FK | 검사에 사용된 모델 버전 ID |
| image_path | VARCHAR(500) | 추론 입력 이미지 object key 스냅샷 |
| category_type | VARCHAR(50) | 모델 category/type 스냅샷 |
| category | VARCHAR(100) | 검사 카테고리 스냅샷 |
| model_profile | VARCHAR(20) | SPEED / PERFORMANCE 스냅샷 |
| model_name | VARCHAR(100) | 모델명 스냅샷 |
| anomaly_score | DECIMAL(12,6) | 원본 anomaly score 스냅샷 |
| image_threshold | DECIMAL(12,6) | 이미지 단위 threshold 스냅샷 |
| predicted_label | VARCHAR(50) | 저장된 최종 판정 라벨 |
| heatmap_path | VARCHAR(500) | heatmap object key 스냅샷 |
| pixel_threshold | DECIMAL(12,6) | 픽셀 단위 threshold 스냅샷 |
| inference_time | BIGINT | 추론 소요 시간(ms) |

---

### RESULT_ARTIFACT / IMAGE 역할 구분 (중요)

| 구분 | 값 | 설명 |
| --- | --- | --- |
| image.image_role | `ORIGINAL / VISUALIZED / THUMBNAIL` | 결과 상세 화면에서 원본/시각화/썸네일 구분 |
| result_artifact.artifact_type | `HEATMAP / ANOMALY_MAP / BOUNDING_BOX_IMAGE / THUMBNAIL / REPORT` | AI 산출물 유형(시각화/리포트 등). MVP 시각화는 주로 HEATMAP 사용 |

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
| DOCUMENT | document_id (PK), organization_id (FK), owner_user_id (FK), title, document_type, category, equipment_type, description, current_status, created_at, updated_at, deleted_at | 문서 기본 정보 |
| DOCUMENT_TAG | document_tag_id (PK), document_id (FK), tag_name, created_at | 문서 태그 |
| DOCUMENT_VERSION | document_version_id (PK), document_id (FK), version_no, file_id (FK), file_hash, indexing_status, indexed_chunk_count, index_error_message, indexed_at, created_at | 문서 버전 |
| DOCUMENT_INDEX_JOB | job_id (PK), document_version_id (FK), ai_job_id, job_status, error_message, started_at, completed_at | 인덱싱 작업 |
| CHUNK | chunk_id (PK), document_version_id (FK), sequence_no, content, created_at | 문서 청크 |
| VECTOR_INDEX | vector_id (PK), chunk_id (FK), embedding_model, vector_ref, created_at | 벡터 인덱스 |

---

### DOCUMENT_VERSION 상세 컬럼

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| indexing_status | VARCHAR(30) | PENDING / PROCESSING / COMPLETED / FAILED |
| indexed_chunk_count | INT | 인덱싱된 청크 수 |
| index_error_message | TEXT | 인덱싱 실패 사유 |

---

# ✅ 5. CHAT DOMAIN

| Table | Columns | Description |
| --- | --- | --- |
| CHAT_CONVERSATION | conversation_id (PK), user_id (FK), title, created_at, updated_at, deleted_at | 챗봇 대화 |
| CHAT_MESSAGE | message_id (PK), conversation_id (FK), role, message_text, message_status, answer_status, error_code, model_name, created_at, updated_at | 챗봇 메시지 |
| CHAT_SOURCE | chat_source_id (PK), message_id (FK), source_type, source_id, document_id (FK), document_title, document_type, chunk_id (FK), page_no, section, source_snippet, score, created_at | 답변 출처 |

---

### CHAT_MESSAGE 상세 컬럼

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| message_status | VARCHAR(20) | SUCCESS / FAILED |
| answer_status | VARCHAR(50) | ANSWERED / NO_RELEVANT_SOURCE / OUT_OF_SCOPE / LLM_FAILED / VECTOR_STORE_FAILED / DOCUMENT_SCOPE_FORBIDDEN / VALIDATION_FAILED |
| error_code | VARCHAR(50) | 답변 생성 실패 코드 |
| model_name | VARCHAR(100) | 사용 모델명 |

---

### CHAT_SOURCE 상세 컬럼

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| document_id | BIGINT | 출처 문서 ID |
| document_title | VARCHAR(255) | 답변 생성 당시 문서 제목 |
| document_type | VARCHAR(50) | 문서 타입 |
| chunk_id | BIGINT | 검색된 청크 ID |
| page_no | INT | 출처 페이지 |
| section | VARCHAR(255) | 출처 섹션 |
| source_snippet | TEXT | 출처 본문 일부 |
| score | DECIMAL(5,4) | 검색 유사도 또는 신뢰도 |

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
| ADMIN_ACTION_LOG | admin_action_id (PK), actor_user_id (FK), action_type, target_type, target_id, reason, created_at | 관리자 작업 로그 |
| OPERATION_LOG | operation_log_id (PK), event_type, event_status, log_level, source_component, request_id, actor_user_id (FK), detail_message, related_path, created_at | 운영 로그 |
| SYSTEM_STATUS_SNAPSHOT | snapshot_id (PK), cpu_usage, memory_usage, disk_usage, response_time_ms, created_at | 시스템 상태 스냅샷 |
| SYSTEM_COMPONENT_STATUS | component_status_id (PK), component_type, component_name, status, message, cpu_usage, memory_usage, disk_usage, host_name, instance_id, response_time_ms, checked_at, created_at | 컴포넌트 상태 |
| OPERATION_POLICY | operation_policy_id (PK), policy_category, policy_key, policy_name, policy_value, value_type, description, is_active, updated_at, updated_by (FK) | 운영 정책 |
| ASYNC_JOB | job_id (PK), job_type, job_status, target_type, target_id, error_message, created_at, completed_at | 비동기 작업 |

---

### OPERATION_LOG 상세 컬럼

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| log_level | VARCHAR(20) | INFO / WARN / ERROR |
| source_component | VARCHAR(50) | SPRING_API / AI_SERVER / MARIADB / REDIS / MINIO |
| request_id | VARCHAR(100) | 요청 추적 ID |
| actor_user_id | BIGINT | 관련 사용자 ID |

---

### SYSTEM_COMPONENT_STATUS 상세 컬럼

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| component_type | VARCHAR(50) | SPRING_API / AI_SERVER / MARIADB / REDIS / MINIO / CHROMA / STORAGE |
| status | VARCHAR(20) | NORMAL / WARNING / ERROR / UNKNOWN |
| cpu_usage | DECIMAL(5,2) | CPU 사용률 |
| memory_usage | DECIMAL(5,2) | 메모리 사용률 |
| disk_usage | DECIMAL(5,2) | 디스크 사용률 |
| response_time_ms | INT | 응답 시간 |
| checked_at | TIMESTAMP | 점검 시각 |

---

### OPERATION_POLICY 상세 컬럼

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| policy_category | VARCHAR(50) | 정책 카테고리 |
| policy_key | VARCHAR(100) | 정책 키 |
| policy_name | VARCHAR(100) | 정책명 |
| policy_value | TEXT | 정책 값 |
| value_type | VARCHAR(20) | STRING / NUMBER / BOOLEAN 등 |
| description | VARCHAR(255) | 정책 설명 |
| is_active | BOOLEAN | 활성 여부 |

---

# ✅ 8. FILE / MODEL

| Table | Columns | Description |
| --- | --- | --- |
| FILE | file_id (PK), storage_type, bucket_name, object_key, file_path, file_name, file_ext, mime_type, file_size, checksum, created_at, created_by (FK) | 파일 저장 |
| MODEL | model_id (PK), model_name, model_type, description, created_at | 모델 기본 정보 |
| MODEL_VERSION | model_version_id (PK), model_id (FK), version_name, model_category, model_profile, framework, input_size, threshold_default, accuracy, precision_score, recall_score, f1_score, auroc_score, deploy_status, is_active, validated_at, validated_by (FK), created_at, deleted_at, deleted_by (FK), delete_reason | 모델 버전 |
| MODEL_ARTIFACT | model_artifact_id (PK), model_version_id (FK), file_id (FK), artifact_type, checksum, created_at | 모델 산출물 파일 |
| MODEL_DEPLOYMENT | deployment_id (PK), organization_id (FK), target_id (FK, NULL), model_version_id (FK), deployment_scope, deploy_status, is_active, deployed_at, deployed_by (FK), rollback_from_deployment_id (FK, NULL), reason, rollback_flag, deleted_at, deleted_by (FK), delete_reason | 조직/검사대상별 모델 배포 |

---

### MODEL 상세 컬럼

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| model_name | VARCHAR(100) | 모델명 |
| model_type | VARCHAR(50) | 모델 계열 또는 알고리즘 유형 |
| description | TEXT | 모델 설명 |

---

### MODEL_VERSION 상세 컬럼

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| version_name | VARCHAR(100) | 모델 버전명 |
| model_category | VARCHAR(20) | OBJECT / TEXTURE |
| model_profile | VARCHAR(20) | SPEED / PERFORMANCE |
| framework | VARCHAR(50) | PYTORCH 등 모델 실행 프레임워크 |
| input_size | VARCHAR(50) | 모델 입력 크기. 예: 224x224 |
| threshold_default | DECIMAL(8,4) | 기본 이상 점수 임계값 |
| accuracy | DECIMAL 또는 FLOAT | 모델 정확도 |
| precision_score | DECIMAL 또는 FLOAT | 정밀도 |
| recall_score | DECIMAL 또는 FLOAT | 재현율 |
| f1_score | DECIMAL 또는 FLOAT | F1 점수 |
| auroc_score | DECIMAL 또는 FLOAT | AUROC 점수 |
| deploy_status | VARCHAR(30) | REGISTERED / VALIDATED / DEPLOYED / DEPRECATED |
| is_active | BOOLEAN | 활성 모델 버전 여부 |

> PatchCore 계열 모델 버전은 `CKPT`, `CONFIG`, `MEMORY_BANK` 산출물이 모두 존재해야 배포 가능 상태로 활성화할 수 있다.
> `CKPT`와 `CONFIG`는 base profile별 고정 산출물을 참조할 수 있고, `MEMORY_BANK`는 고객사/검사대상 정상 이미지셋 기준으로 생성된 별도 산출물이다.
> 같은 `CKPT`/`CONFIG`를 사용하더라도 `MEMORY_BANK`가 다르면 다른 `MODEL_VERSION`으로 관리한다.

---

### MODEL_ARTIFACT 상세 컬럼

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| model_version_id | BIGINT | 모델 버전 ID |
| file_id | BIGINT | FILE 테이블 참조 |
| artifact_type | VARCHAR(30) | CKPT / CONFIG / MEMORY_BANK / LABELS / EXTRA |
| checksum | VARCHAR(255) | 파일 무결성 확인용 체크섬 |

---

### MODEL_DEPLOYMENT 상세 컬럼

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| organization_id | BIGINT | 모델이 적용되는 조직 ID |
| target_id | BIGINT NULL | 특정 검사대상에만 적용할 경우 사용 |
| model_version_id | BIGINT | 배포된 모델 버전 ID |
| deployment_scope | VARCHAR(20) | ORGANIZATION / TARGET |
| deploy_status | VARCHAR(30) | DEPLOYED / ROLLED_BACK / DEACTIVATED |
| is_active | BOOLEAN | 현재 활성 배포 여부 |
| deployed_at | TIMESTAMP | 배포 시각 |
| deployed_by | BIGINT | 배포한 관리자 ID |
| rollback_from_deployment_id | BIGINT NULL | 롤백 기준이 된 배포 ID |
| reason | VARCHAR(255) | 배포 또는 교체 사유 |
| rollback_flag | BOOLEAN | 롤백으로 생성된 배포 여부 |
| deleted_at | TIMESTAMP NULL | 삭제 처리 시각 (soft delete) |
| deleted_by | BIGINT NULL | 삭제 처리 사용자 ID (`USERS.user_id`) |
| delete_reason | TEXT NULL | 삭제 사유 |

> MODEL_DEPLOYMENT의 활성 배포 단위(슬롯)는 `organizationId + targetId + deploymentScope + modelCategory + modelProfile` 조합이다.
> `modelCategory`, `modelProfile`은 `MODEL_DEPLOYMENT`에 직접 저장되지 않으며 `MODEL_VERSION`을 통해 참조하여 판별한다.
> 동일 조직/검사대상/배포범위라도 카테고리나 프로필이 다르면 서로 독립적으로 동시에 ACTIVE/DEPLOYED 상태를 유지할 수 있다.
