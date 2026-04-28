  CREATE DATABASE IF NOT EXISTS industrial_ai
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

  USE industrial_ai;

  CREATE TABLE ORGANIZATION (
    organization_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    organization_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE USERS (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    organization_id BIGINT NULL,
    google_sub VARCHAR(255) UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    name VARCHAR(100),
    picture TEXT,
    phone VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    last_login_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    CONSTRAINT fk_users_organization FOREIGN KEY (organization_id)
      REFERENCES ORGANIZATION(organization_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE `FILE` (
    file_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    storage_type VARCHAR(20) NOT NULL DEFAULT 'MINIO',
    bucket_name VARCHAR(100),
    object_key VARCHAR(500),
    file_path TEXT,
    file_name VARCHAR(255),
    file_ext VARCHAR(20),
    mime_type VARCHAR(100),
    file_size BIGINT,
    checksum VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    CONSTRAINT fk_file_created_by FOREIGN KEY (created_by)
      REFERENCES USERS(user_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE SIGNUP_REQUEST (
    signup_request_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    organization_id BIGINT,
    request_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reject_reason TEXT,
    processed_by BIGINT,
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL,
    CONSTRAINT fk_signup_user FOREIGN KEY (user_id) REFERENCES USERS(user_id),
    CONSTRAINT fk_signup_organization FOREIGN KEY (organization_id) REFERENCES ORGANIZATION(organization_id),
    CONSTRAINT fk_signup_processed_by FOREIGN KEY (processed_by) REFERENCES USERS(user_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE AUTH_SESSION (
    auth_session_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    access_token TEXT,
    refresh_token TEXT,
    ip_address VARCHAR(50),
    user_agent TEXT,
    login_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NULL,
    revoked_at TIMESTAMP NULL,
    CONSTRAINT fk_auth_session_user FOREIGN KEY (user_id) REFERENCES USERS(user_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE USER_SETTING (
    user_setting_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    notification_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    default_dashboard_range VARCHAR(20),
    default_camera_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_setting_user FOREIGN KEY (user_id) REFERENCES USERS(user_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE USER_THRESHOLD (
    threshold_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    anomaly_threshold DECIMAL(5,4),
    low_confidence_threshold DECIMAL(5,4),
    min_allowed DECIMAL(5,4),
    max_allowed DECIMAL(5,4),
    apply_scope VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_threshold_user FOREIGN KEY (user_id) REFERENCES USERS(user_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE USER_THRESHOLD_HISTORY (
    threshold_history_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    threshold_id BIGINT NOT NULL,
    version INT,
    old_anomaly_threshold DECIMAL(5,4),
    new_anomaly_threshold DECIMAL(5,4),
    change_reason TEXT,
    changed_by BIGINT,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_threshold_history_threshold FOREIGN KEY (threshold_id) REFERENCES USER_THRESHOLD(threshold_id),
    CONSTRAINT fk_threshold_history_user FOREIGN KEY (changed_by) REFERENCES USERS(user_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE ANALYSIS_TARGET (
    target_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    organization_id BIGINT NOT NULL,
    target_name VARCHAR(100),
    equipment_name VARCHAR(100),
    product_name VARCHAR(100),
    target_type VARCHAR(50),
    target_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_target_organization FOREIGN KEY (organization_id) REFERENCES ORGANIZATION(organization_id),
    CONSTRAINT fk_target_created_by FOREIGN KEY (created_by) REFERENCES USERS(user_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE CAMERA_SOURCE (
    camera_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    organization_id BIGINT NOT NULL,
    user_id BIGINT,
    camera_name VARCHAR(100),
    stream_url TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_camera_organization FOREIGN KEY (organization_id) REFERENCES ORGANIZATION(organization_id),
    CONSTRAINT fk_camera_user FOREIGN KEY (user_id) REFERENCES USERS(user_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE MODEL (
    model_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    model_name VARCHAR(100) NOT NULL,
    model_type VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE MODEL_VERSION (
    model_version_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    model_id BIGINT NOT NULL,
    file_id BIGINT,
    version_name VARCHAR(100),
    accuracy DECIMAL(6,4),
    precision_score DECIMAL(6,4),
    recall_score DECIMAL(6,4),
    deploy_status VARCHAR(20) NOT NULL DEFAULT 'READY',
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    validated_at TIMESTAMP NULL,
    validated_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_model_version_model FOREIGN KEY (model_id) REFERENCES MODEL(model_id),
    CONSTRAINT fk_model_version_file FOREIGN KEY (file_id) REFERENCES `FILE`(file_id),
    CONSTRAINT fk_model_version_validated_by FOREIGN KEY (validated_by) REFERENCES USERS(user_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE MODEL_DEPLOYMENT (
    deployment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    model_version_id BIGINT NOT NULL,
    deployed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    rollback_flag BOOLEAN NOT NULL DEFAULT FALSE,
    deploy_status VARCHAR(20) NOT NULL DEFAULT 'DEPLOYED',
    CONSTRAINT fk_model_deployment_version FOREIGN KEY (model_version_id) REFERENCES MODEL_VERSION(model_version_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE INSPECTION_RUN (
    inspection_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    organization_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    target_id BIGINT,
    run_type VARCHAR(20) NOT NULL,
    input_type VARCHAR(20) NOT NULL,
    source_type VARCHAR(20),
    source_id BIGINT,
    run_status VARCHAR(20) NOT NULL DEFAULT 'REQUESTED',
    applied_threshold DECIMAL(5,4),
    idempotency_key VARCHAR(255) NOT NULL,
    payload_fingerprint VARCHAR(64),
    error_code VARCHAR(50),
    started_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    CONSTRAINT uk_inspection_run_org_user_idempotency UNIQUE (organization_id, user_id, idempotency_key),
    CONSTRAINT fk_inspection_run_organization FOREIGN KEY (organization_id) REFERENCES ORGANIZATION(organization_id),
    CONSTRAINT fk_inspection_run_user FOREIGN KEY (user_id) REFERENCES USERS(user_id),
    CONSTRAINT fk_inspection_run_target FOREIGN KEY (target_id) REFERENCES ANALYSIS_TARGET(target_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE INSPECTION_INPUT (
    inspection_input_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    inspection_id BIGINT NOT NULL,
    file_id BIGINT,
    camera_id BIGINT,
    stream_url TEXT,
    source_name VARCHAR(255),
    mime_type VARCHAR(100),
    duration_sec INT,
    frame_count INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inspection_input_run FOREIGN KEY (inspection_id) REFERENCES INSPECTION_RUN(inspection_id),
    CONSTRAINT fk_inspection_input_file FOREIGN KEY (file_id) REFERENCES `FILE`(file_id),
    CONSTRAINT fk_inspection_input_camera FOREIGN KEY (camera_id) REFERENCES CAMERA_SOURCE(camera_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE INSPECTION_EVENT_LOG (
    event_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    inspection_id BIGINT NOT NULL,
    event_type VARCHAR(50),
    message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inspection_event_run FOREIGN KEY (inspection_id) REFERENCES INSPECTION_RUN(inspection_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE INSPECTION_RESULT (
    result_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    inspection_id BIGINT NOT NULL UNIQUE,
    score DECIMAL(6,4),
    confidence DECIMAL(6,4),
    decision_code VARCHAR(20),
    final_decision_code VARCHAR(20),
    result_status VARCHAR(20),
    threshold_source VARCHAR(20),
    threshold_id BIGINT,
    threshold_version INT,
    model_version_id BIGINT,
    failure_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_result_inspection FOREIGN KEY (inspection_id) REFERENCES INSPECTION_RUN(inspection_id),
    CONSTRAINT fk_result_threshold FOREIGN KEY (threshold_id) REFERENCES USER_THRESHOLD(threshold_id),
    CONSTRAINT fk_result_model_version FOREIGN KEY (model_version_id) REFERENCES MODEL_VERSION(model_version_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE RESULT_ARTIFACT (
    artifact_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    result_id BIGINT NOT NULL,
    artifact_type VARCHAR(50),
    file_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_artifact_result FOREIGN KEY (result_id) REFERENCES INSPECTION_RESULT(result_id),
    CONSTRAINT fk_artifact_file FOREIGN KEY (file_id) REFERENCES `FILE`(file_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE `IMAGE` (
    image_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    result_id BIGINT NOT NULL,
    file_id BIGINT,
    image_role VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_image_result FOREIGN KEY (result_id) REFERENCES INSPECTION_RESULT(result_id),
    CONSTRAINT fk_image_file FOREIGN KEY (file_id) REFERENCES `FILE`(file_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE ANOMALY_REGION (
    region_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    image_id BIGINT NOT NULL,
    label_code VARCHAR(50),
    bbox_x DECIMAL(10,4),
    bbox_y DECIMAL(10,4),
    bbox_w DECIMAL(10,4),
    bbox_h DECIMAL(10,4),
    score DECIMAL(6,4),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_region_image FOREIGN KEY (image_id) REFERENCES `IMAGE`(image_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE REVIEW_QUEUE (
    review_queue_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    result_id BIGINT NOT NULL UNIQUE,
    queue_status VARCHAR(20) NOT NULL DEFAULT 'WAITING',
    queued_reason TEXT,
    queued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_review_queue_result FOREIGN KEY (result_id) REFERENCES INSPECTION_RESULT(result_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE REVIEW_HISTORY (
    review_history_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    result_id BIGINT NOT NULL,
    before_decision VARCHAR(20),
    after_decision VARCHAR(20),
    review_comment TEXT,
    reviewed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_by BIGINT,
    CONSTRAINT fk_review_history_result FOREIGN KEY (result_id) REFERENCES INSPECTION_RESULT(result_id),
    CONSTRAINT fk_review_history_user FOREIGN KEY (reviewed_by) REFERENCES USERS(user_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE LEARNING_CANDIDATE (
    learning_candidate_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    result_id BIGINT NOT NULL UNIQUE,
    candidate_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    selected_at TIMESTAMP NULL,
    CONSTRAINT fk_learning_candidate_result FOREIGN KEY (result_id) REFERENCES INSPECTION_RESULT(result_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE DOCUMENT (
    document_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    organization_id BIGINT NOT NULL,
    owner_user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    document_type VARCHAR(50),
    current_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    CONSTRAINT fk_document_organization FOREIGN KEY (organization_id) REFERENCES ORGANIZATION(organization_id),
    CONSTRAINT fk_document_owner FOREIGN KEY (owner_user_id) REFERENCES USERS(user_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE DOCUMENT_VERSION (
    document_version_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    document_id BIGINT NOT NULL,
    version_no INT NOT NULL,
    file_id BIGINT,
    file_hash VARCHAR(255),
    indexing_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    indexed_chunk_count INT DEFAULT 0,
    index_error_message TEXT,
    indexed_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_document_version_document FOREIGN KEY (document_id) REFERENCES DOCUMENT(document_id),
    CONSTRAINT fk_document_version_file FOREIGN KEY (file_id) REFERENCES `FILE`(file_id),
    UNIQUE KEY uk_document_version (document_id, version_no)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE DOCUMENT_INDEX_JOB (
    job_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    document_version_id BIGINT NOT NULL,
    job_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    error_message TEXT,
    started_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    CONSTRAINT fk_index_job_version FOREIGN KEY (document_version_id) REFERENCES DOCUMENT_VERSION(document_version_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE CHUNK (
    chunk_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    document_version_id BIGINT NOT NULL,
    sequence_no INT NOT NULL,
    content TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_chunk_version FOREIGN KEY (document_version_id) REFERENCES DOCUMENT_VERSION(document_version_id),
    UNIQUE KEY uk_chunk_sequence (document_version_id, sequence_no)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE VECTOR_INDEX (
    vector_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    chunk_id BIGINT NOT NULL UNIQUE,
    embedding_model VARCHAR(100),
    vector_ref TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_vector_chunk FOREIGN KEY (chunk_id) REFERENCES CHUNK(chunk_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE CHAT_CONVERSATION (
    conversation_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    CONSTRAINT fk_chat_conversation_user FOREIGN KEY (user_id) REFERENCES USERS(user_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE CHAT_MESSAGE (
    message_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    message_text TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_chat_message_conversation FOREIGN KEY (conversation_id) REFERENCES CHAT_CONVERSATION(conversation_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE CHAT_SOURCE (
    chat_source_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    message_id BIGINT NOT NULL,
    source_type VARCHAR(50),
    source_id BIGINT,
    chunk_id BIGINT,
    source_snippet TEXT,
    CONSTRAINT fk_chat_source_message FOREIGN KEY (message_id) REFERENCES CHAT_MESSAGE(message_id),
    CONSTRAINT fk_chat_source_chunk FOREIGN KEY (chunk_id) REFERENCES CHUNK(chunk_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE NOTIFICATION (
    notification_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    notification_type VARCHAR(50),
    severity VARCHAR(20),
    title VARCHAR(255),
    message TEXT,
    related_type VARCHAR(50),
    related_id BIGINT,
    target_url TEXT,
    dedup_key VARCHAR(255),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES USERS(user_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE REPORT (
    report_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    organization_id BIGINT NOT NULL,
    report_type VARCHAR(50),
    period_start TIMESTAMP NULL,
    period_end TIMESTAMP NULL,
    report_status VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    snapshot_at TIMESTAMP NULL,
    generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    generated_by BIGINT,
    CONSTRAINT fk_report_organization FOREIGN KEY (organization_id) REFERENCES ORGANIZATION(organization_id),
    CONSTRAINT fk_report_generated_by FOREIGN KEY (generated_by) REFERENCES USERS(user_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE REPORT_ITEM (
    report_item_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    report_id BIGINT NOT NULL,
    result_id BIGINT,
    summary_text TEXT,
    CONSTRAINT fk_report_item_report FOREIGN KEY (report_id) REFERENCES REPORT(report_id),
    CONSTRAINT fk_report_item_result FOREIGN KEY (result_id) REFERENCES INSPECTION_RESULT(result_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE REPORT_FILE (
    report_file_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    report_id BIGINT NOT NULL,
    file_id BIGINT NOT NULL,
    CONSTRAINT fk_report_file_report FOREIGN KEY (report_id) REFERENCES REPORT(report_id),
    CONSTRAINT fk_report_file_file FOREIGN KEY (file_id) REFERENCES `FILE`(file_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE AUDIT_LOG (
    audit_log_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    actor_user_id BIGINT,
    action_type VARCHAR(50),
    target_type VARCHAR(50),
    target_id BIGINT,
    before_json LONGTEXT,
    after_json LONGTEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_log_user FOREIGN KEY (actor_user_id) REFERENCES USERS(user_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE ADMIN_ACTION_LOG (
    admin_action_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    actor_user_id BIGINT,
    action_type VARCHAR(50),
    target_type VARCHAR(50),
    target_id BIGINT,
    reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_admin_action_user FOREIGN KEY (actor_user_id) REFERENCES USERS(user_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE OPERATION_LOG (
    operation_log_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_type VARCHAR(50),
    event_status VARCHAR(20),
    detail_message TEXT,
    related_path TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE SYSTEM_STATUS_SNAPSHOT (
    snapshot_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cpu_usage DECIMAL(6,2),
    memory_usage DECIMAL(6,2),
    disk_usage DECIMAL(6,2),
    response_time_ms INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE OPERATION_POLICY (
    operation_policy_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    policy_type VARCHAR(50),
    policy_value TEXT,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by BIGINT,
    CONSTRAINT fk_operation_policy_user FOREIGN KEY (updated_by) REFERENCES USERS(user_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE ASYNC_JOB (
    job_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    job_type VARCHAR(50),
    job_status VARCHAR(20),
    target_type VARCHAR(50),
    target_id BIGINT,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE INDEX idx_users_email ON USERS(email);
  CREATE INDEX idx_users_google_sub ON USERS(google_sub);
  CREATE INDEX idx_inspection_run_org_user ON INSPECTION_RUN(organization_id, user_id);
  CREATE INDEX idx_inspection_run_status ON INSPECTION_RUN(run_status);
  CREATE INDEX idx_result_decision ON INSPECTION_RESULT(decision_code);
  CREATE INDEX idx_document_org_owner ON DOCUMENT(organization_id, owner_user_id);
  CREATE INDEX idx_notification_user_read ON NOTIFICATION(user_id, is_read);
  CREATE INDEX idx_chat_conversation_user ON CHAT_CONVERSATION(user_id);