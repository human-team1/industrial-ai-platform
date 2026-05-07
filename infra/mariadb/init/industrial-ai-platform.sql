  CREATE DATABASE IF NOT EXISTS industrial_ai
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

  USE industrial_ai;

  CREATE TABLE organization (
    organization_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    organization_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE OR REPLACE VIEW organization_public AS
    SELECT
      organization_id AS id,
      organization_name AS name
    FROM organization
    WHERE status = 'ACTIVE';

  CREATE TABLE users (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    organization_id BIGINT NULL,
    google_sub VARCHAR(255) UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    name VARCHAR(100),
    picture VARCHAR(255),
    phone VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    role VARCHAR(20) NOT NULL DEFAULT 'ROLE_COMPANY_WORKER',
    last_login_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    CONSTRAINT fk_users_organization FOREIGN KEY (organization_id)
      REFERENCES organization(organization_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE file (
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
      REFERENCES users(user_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE signup_request (
    signup_request_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    organization_id BIGINT,
    request_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reject_reason VARCHAR(255),
    processed_by BIGINT,
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL,
    CONSTRAINT fk_signup_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_signup_organization FOREIGN KEY (organization_id) REFERENCES organization(organization_id),
    CONSTRAINT fk_signup_processed_by FOREIGN KEY (processed_by) REFERENCES users(user_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE auth_session (
    auth_session_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    access_token TEXT,
    refresh_token TEXT,
    ip_address VARCHAR(50),
    user_agent TEXT,
    login_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NULL,
    revoked_at TIMESTAMP NULL,
    CONSTRAINT fk_auth_session_user FOREIGN KEY (user_id) REFERENCES users(user_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE user_setting (
    user_setting_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    notification_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    default_dashboard_range VARCHAR(20),
    default_camera_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_setting_user FOREIGN KEY (user_id) REFERENCES users(user_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE user_threshold (
    threshold_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    anomaly_threshold DECIMAL(8,4),
    low_confidence_threshold DOUBLE,
    min_allowed DECIMAL(8,4),
    max_allowed DECIMAL(8,4),
    apply_scope VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_threshold_user FOREIGN KEY (user_id) REFERENCES users(user_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE user_threshold_history (
    threshold_history_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    threshold_id BIGINT NOT NULL,
    version INT,
    old_anomaly_threshold DECIMAL(8,4),
    new_anomaly_threshold DECIMAL(8,4),
    change_reason TEXT,
    changed_by BIGINT,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_threshold_history_threshold FOREIGN KEY (threshold_id) REFERENCES user_threshold(threshold_id),
    CONSTRAINT fk_threshold_history_user FOREIGN KEY (changed_by) REFERENCES users(user_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE analysis_target (
    target_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    organization_id BIGINT NOT NULL,
    target_name VARCHAR(100),
    equipment_name VARCHAR(100),
    product_name VARCHAR(100),
    location_name VARCHAR(100),
    target_type VARCHAR(50),
    target_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_target_organization FOREIGN KEY (organization_id) REFERENCES organization(organization_id),
    CONSTRAINT fk_target_created_by FOREIGN KEY (created_by) REFERENCES users(user_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE camera_source (
    camera_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    organization_id BIGINT NOT NULL,
    user_id BIGINT,
    camera_name VARCHAR(100),
    stream_url TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_camera_organization FOREIGN KEY (organization_id) REFERENCES organization(organization_id),
    CONSTRAINT fk_camera_user FOREIGN KEY (user_id) REFERENCES users(user_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE model (
    model_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    model_name VARCHAR(100) NOT NULL,
    model_type VARCHAR(50) NOT NULL,
    description TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_model_name_type UNIQUE (model_name, model_type)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE model_version (
    model_version_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    model_id BIGINT NOT NULL,
    file_id BIGINT,
    version_name VARCHAR(100) NOT NULL,
    model_category VARCHAR(20) NOT NULL,
    model_profile VARCHAR(20) NOT NULL,
    framework VARCHAR(50),
    input_size VARCHAR(50),
    threshold_default DECIMAL(8,4),
    accuracy DECIMAL(6,4),
    precision_score DECIMAL(6,4),
    recall_score DECIMAL(6,4),
    f1_score DECIMAL(6,4),
    auroc_score DECIMAL(6,4),
    deploy_status VARCHAR(20) NOT NULL DEFAULT 'REGISTERED',
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    validated_at TIMESTAMP NULL,
    validated_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    deleted_by BIGINT NULL,
    delete_reason TEXT NULL,
    CONSTRAINT fk_model_version_model FOREIGN KEY (model_id) REFERENCES model(model_id),
    CONSTRAINT fk_model_version_file FOREIGN KEY (file_id) REFERENCES file(file_id),
    CONSTRAINT fk_model_version_validated_by FOREIGN KEY (validated_by) REFERENCES users(user_id),
    CONSTRAINT fk_model_version_deleted_by FOREIGN KEY (deleted_by) REFERENCES users(user_id),
    CONSTRAINT uk_model_version_name UNIQUE (model_id, version_name)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE model_artifact (
    model_artifact_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    model_version_id BIGINT NOT NULL,
    file_id BIGINT NOT NULL,
    artifact_type VARCHAR(30) NOT NULL,
    checksum VARCHAR(128),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_model_artifact_version FOREIGN KEY (model_version_id) REFERENCES model_version(model_version_id),
    CONSTRAINT fk_model_artifact_file FOREIGN KEY (file_id) REFERENCES file(file_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE model_deployment (
    deployment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    organization_id BIGINT NOT NULL,
    target_id BIGINT NULL,
    model_version_id BIGINT NOT NULL,
    deployment_scope VARCHAR(20) NOT NULL,
    deploy_status VARCHAR(20) NOT NULL DEFAULT 'DEPLOYED',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    deployed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deployed_by BIGINT NULL,
    rollback_from_deployment_id BIGINT NULL,
    reason VARCHAR(255) NULL,
    rollback_flag BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,
    deleted_by BIGINT NULL,
    delete_reason TEXT NULL,
    CONSTRAINT fk_model_deployment_version FOREIGN KEY (model_version_id) REFERENCES model_version(model_version_id),
    CONSTRAINT fk_model_deployment_organization FOREIGN KEY (organization_id) REFERENCES organization(organization_id),
    CONSTRAINT fk_model_deployment_target FOREIGN KEY (target_id) REFERENCES analysis_target(target_id),
    CONSTRAINT fk_model_deployment_user FOREIGN KEY (deployed_by) REFERENCES users(user_id),
    CONSTRAINT fk_model_deployment_deleted_by FOREIGN KEY (deleted_by) REFERENCES users(user_id),
    CONSTRAINT fk_model_deployment_rollback FOREIGN KEY (rollback_from_deployment_id) REFERENCES model_deployment(deployment_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE inspection_run (
    inspection_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    organization_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    target_id BIGINT,
    run_type VARCHAR(20) NOT NULL,
    input_type VARCHAR(20) NOT NULL,
    source_type VARCHAR(20),
    source_id VARCHAR(255),
    run_status VARCHAR(20) NOT NULL DEFAULT 'REQUESTED',
    applied_threshold DECIMAL(8,4),
    idempotency_key VARCHAR(255) NOT NULL,
    payload_fingerprint VARCHAR(64),
    error_code VARCHAR(50),
    started_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    CONSTRAINT uk_inspection_run_org_user_idempotency UNIQUE (organization_id, user_id, idempotency_key),
    CONSTRAINT fk_inspection_run_organization FOREIGN KEY (organization_id) REFERENCES organization(organization_id),
    CONSTRAINT fk_inspection_run_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_inspection_run_target FOREIGN KEY (target_id) REFERENCES analysis_target(target_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE inspection_input (
    inspection_input_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    inspection_id BIGINT NOT NULL,
    source_type VARCHAR(20) NOT NULL,
    file_id BIGINT,
    camera_id BIGINT,
    stream_url TEXT,
    source_name VARCHAR(255),
    mime_type VARCHAR(100),
    duration_sec INT,
    frame_count INT,
    roi_mode VARCHAR(20),
    roi_coordinate_type VARCHAR(20),
    roi_x DECIMAL(8,6),
    roi_y DECIMAL(8,6),
    roi_width DECIMAL(8,6),
    roi_height DECIMAL(8,6),
    sampling_fps DECIMAL(5,2),
    max_frames INT,
    quality_gate_enabled TINYINT(1),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inspection_input_run FOREIGN KEY (inspection_id) REFERENCES inspection_run(inspection_id),
    CONSTRAINT fk_inspection_input_file FOREIGN KEY (file_id) REFERENCES file(file_id),
    CONSTRAINT fk_inspection_input_camera FOREIGN KEY (camera_id) REFERENCES camera_source(camera_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE inspection_event_log (
    event_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    inspection_id BIGINT NOT NULL,
    event_type VARCHAR(50),
    message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inspection_event_run FOREIGN KEY (inspection_id) REFERENCES inspection_run(inspection_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE inspection_result (
    result_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    inspection_id BIGINT NOT NULL UNIQUE,
    score DECIMAL(8,4),
    confidence DECIMAL(6,4),
    decision_code VARCHAR(20),
    final_decision_code VARCHAR(20),
    result_status VARCHAR(20),
    threshold_source VARCHAR(20),
    threshold_id BIGINT,
    threshold_version INT,
    model_version_id BIGINT,
    failure_reason VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_result_inspection FOREIGN KEY (inspection_id) REFERENCES inspection_run(inspection_id),
    CONSTRAINT fk_result_threshold FOREIGN KEY (threshold_id) REFERENCES user_threshold(threshold_id),
    CONSTRAINT fk_result_model_version FOREIGN KEY (model_version_id) REFERENCES model_version(model_version_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE result_artifact (
    artifact_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    result_id BIGINT NOT NULL,
    artifact_type VARCHAR(50),
    file_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_artifact_result FOREIGN KEY (result_id) REFERENCES inspection_result(result_id),
    CONSTRAINT fk_artifact_file FOREIGN KEY (file_id) REFERENCES file(file_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE image (
    image_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    result_id BIGINT NOT NULL,
    file_id BIGINT,
    image_role VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_image_result FOREIGN KEY (result_id) REFERENCES inspection_result(result_id),
    CONSTRAINT fk_image_file FOREIGN KEY (file_id) REFERENCES file(file_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE anomaly_region (
    region_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    image_id BIGINT NOT NULL,
    label_code VARCHAR(50),
    bbox_x DECIMAL(10,4),
    bbox_y DECIMAL(10,4),
    bbox_w DECIMAL(10,4),
    bbox_h DECIMAL(10,4),
    score DECIMAL(6,4),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_region_image FOREIGN KEY (image_id) REFERENCES image(image_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE review_queue (
    review_queue_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    result_id BIGINT NOT NULL UNIQUE,
    queue_status VARCHAR(20) NOT NULL DEFAULT 'WAITING',
    queued_reason VARCHAR(255),
    queued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_review_queue_result FOREIGN KEY (result_id) REFERENCES inspection_result(result_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE review_history (
    review_history_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    result_id BIGINT NOT NULL,
    before_decision VARCHAR(20),
    after_decision VARCHAR(20),
    review_comment TEXT,
    reviewed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_by BIGINT,
    CONSTRAINT fk_review_history_result FOREIGN KEY (result_id) REFERENCES inspection_result(result_id),
    CONSTRAINT fk_review_history_user FOREIGN KEY (reviewed_by) REFERENCES users(user_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE learning_candidate (
    learning_candidate_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    result_id BIGINT NOT NULL UNIQUE,
    candidate_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    selected_at TIMESTAMP NULL,
    CONSTRAINT fk_learning_candidate_result FOREIGN KEY (result_id) REFERENCES inspection_result(result_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE document (
    document_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    organization_id BIGINT NOT NULL,
    owner_user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    document_type VARCHAR(50),
    category VARCHAR(50),
    equipment_type VARCHAR(100),
    description TEXT,
    current_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    CONSTRAINT fk_document_organization FOREIGN KEY (organization_id) REFERENCES organization(organization_id),
    CONSTRAINT fk_document_owner FOREIGN KEY (owner_user_id) REFERENCES users(user_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE document_tag (
    document_tag_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    document_id BIGINT NOT NULL,
    tag_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_document_tag_document FOREIGN KEY (document_id) REFERENCES document(document_id),
    UNIQUE KEY uk_document_tag (document_id, tag_name)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE document_version (
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
    CONSTRAINT fk_document_version_document FOREIGN KEY (document_id) REFERENCES document(document_id),
    CONSTRAINT fk_document_version_file FOREIGN KEY (file_id) REFERENCES file(file_id),
    UNIQUE KEY uk_document_version (document_id, version_no)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE document_index_job (
    job_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    document_version_id BIGINT NOT NULL,
    ai_job_id VARCHAR(100),
    job_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    error_message TEXT,
    started_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    CONSTRAINT fk_index_job_version FOREIGN KEY (document_version_id) REFERENCES document_version(document_version_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE chunk (
    chunk_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    document_version_id BIGINT NOT NULL,
    sequence_no INT NOT NULL,
    content TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_chunk_version FOREIGN KEY (document_version_id) REFERENCES document_version(document_version_id),
    UNIQUE KEY uk_chunk_sequence (document_version_id, sequence_no)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE vector_index (
    vector_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    chunk_id BIGINT NOT NULL UNIQUE,
    embedding_model VARCHAR(100),
    vector_ref TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_vector_chunk FOREIGN KEY (chunk_id) REFERENCES chunk(chunk_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE chat_conversation (
    conversation_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    CONSTRAINT fk_chat_conversation_user FOREIGN KEY (user_id) REFERENCES users(user_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE chat_message (
    message_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    message_text TEXT NOT NULL,
    message_status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    answer_status VARCHAR(50) NULL,
    error_code VARCHAR(50) NULL,
    model_name VARCHAR(100) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_chat_message_conversation FOREIGN KEY (conversation_id) REFERENCES chat_conversation(conversation_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE chat_source (
    chat_source_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    message_id BIGINT NOT NULL,
    source_type VARCHAR(50) NOT NULL,
    source_id BIGINT,
    document_id BIGINT,
    document_title VARCHAR(255),
    document_type VARCHAR(50),
    chunk_id BIGINT,
    page_no INT,
    section VARCHAR(255),
    source_snippet TEXT,
    score DECIMAL(5,4),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_chat_source_message FOREIGN KEY (message_id) REFERENCES chat_message(message_id),
    CONSTRAINT fk_chat_source_document FOREIGN KEY (document_id) REFERENCES document(document_id),
    CONSTRAINT fk_chat_source_chunk FOREIGN KEY (chunk_id) REFERENCES chunk(chunk_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE notification (
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
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(user_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE report (
    report_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    organization_id BIGINT NOT NULL,
    report_type VARCHAR(50),
    period_start TIMESTAMP NULL,
    period_end TIMESTAMP NULL,
    report_status VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    snapshot_at TIMESTAMP NULL,
    generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    generated_by BIGINT,
    CONSTRAINT fk_report_organization FOREIGN KEY (organization_id) REFERENCES organization(organization_id),
    CONSTRAINT fk_report_generated_by FOREIGN KEY (generated_by) REFERENCES users(user_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE report_item (
    report_item_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    report_id BIGINT NOT NULL,
    result_id BIGINT,
    summary_text TEXT,
    CONSTRAINT fk_report_item_report FOREIGN KEY (report_id) REFERENCES report(report_id),
    CONSTRAINT fk_report_item_result FOREIGN KEY (result_id) REFERENCES inspection_result(result_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE report_file (
    report_file_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    report_id BIGINT NOT NULL,
    file_id BIGINT NOT NULL,
    CONSTRAINT fk_report_file_report FOREIGN KEY (report_id) REFERENCES report(report_id),
    CONSTRAINT fk_report_file_file FOREIGN KEY (file_id) REFERENCES file(file_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE audit_log (
    audit_log_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    actor_user_id BIGINT,
    action_type VARCHAR(50),
    target_type VARCHAR(50),
    target_id BIGINT,
    before_json LONGTEXT,
    after_json LONGTEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_log_user FOREIGN KEY (actor_user_id) REFERENCES users(user_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE admin_action_log (
    admin_action_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    actor_user_id BIGINT,
    action_type VARCHAR(50),
    target_type VARCHAR(50),
    target_id BIGINT,
    reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_admin_action_user FOREIGN KEY (actor_user_id) REFERENCES users(user_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE operation_log (
    operation_log_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_type VARCHAR(50),
    event_status VARCHAR(20),
    log_level VARCHAR(20),
    source_component VARCHAR(50),
    request_id VARCHAR(100),
    actor_user_id BIGINT,
    detail_message TEXT,
    related_path TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_operation_log_actor FOREIGN KEY (actor_user_id) REFERENCES users(user_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE system_status_snapshot (
    snapshot_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cpu_usage DECIMAL(6,2),
    memory_usage DECIMAL(6,2),
    disk_usage DECIMAL(6,2),
    response_time_ms INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE system_component_status (
    component_status_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    component_type VARCHAR(50),
    component_name VARCHAR(100),
    status VARCHAR(20),
    message VARCHAR(255),
    cpu_usage DECIMAL(5,2),
    memory_usage DECIMAL(5,2),
    disk_usage DECIMAL(5,2),
    host_name VARCHAR(100),
    instance_id VARCHAR(100),
    response_time_ms INT,
    checked_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE operation_policy (
    operation_policy_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    policy_type VARCHAR(50),
    policy_category VARCHAR(50),
    policy_key VARCHAR(100),
    policy_name VARCHAR(100),
    policy_value TEXT,
    value_type VARCHAR(20),
    description VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by BIGINT,
    CONSTRAINT fk_operation_policy_user FOREIGN KEY (updated_by) REFERENCES users(user_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE TABLE async_job (
    job_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    job_type VARCHAR(50),
    job_status VARCHAR(20),
    target_type VARCHAR(50),
    target_id BIGINT,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  CREATE INDEX idx_users_email ON users(email);
  CREATE INDEX idx_users_google_sub ON users(google_sub);
  CREATE INDEX idx_inspection_run_org_user ON inspection_run(organization_id, user_id);
  CREATE INDEX idx_inspection_run_status ON inspection_run(run_status);
  CREATE INDEX idx_inspection_run_org_completed ON inspection_run(organization_id, completed_at);
  CREATE INDEX idx_inspection_run_org_status_completed ON inspection_run(organization_id, run_status, completed_at);
  CREATE INDEX idx_result_decision ON inspection_result(decision_code);
  CREATE INDEX idx_result_inspection_decision ON inspection_result(inspection_id, decision_code, final_decision_code, created_at);
  CREATE INDEX idx_model_type_created ON model(model_type, created_at);
  CREATE INDEX idx_model_version_model_status ON model_version(model_id, deploy_status, is_active, created_at);
  CREATE INDEX idx_model_version_available ON model_version(model_id, model_category, deploy_status, is_active, created_at);
  CREATE INDEX idx_model_artifact_version_type ON model_artifact(model_version_id, artifact_type);
  CREATE INDEX idx_model_deployment_scope ON model_deployment(organization_id, target_id, deployment_scope, is_active);
  CREATE INDEX idx_model_deployment_available ON model_deployment(organization_id, target_id, deployment_scope, deploy_status, is_active, deployed_at);
  CREATE INDEX idx_model_deployment_version_active ON model_deployment(model_version_id, is_active, deploy_status);
  CREATE INDEX idx_model_deployment_deleted_at ON model_deployment(deleted_at);
  CREATE INDEX idx_model_version_deleted_at ON model_version(deleted_at);
  CREATE INDEX idx_analysis_target_org_status ON analysis_target(organization_id, target_status);
  CREATE INDEX idx_notification_user_created ON notification(user_id, created_at);
  CREATE INDEX idx_operation_log_created ON operation_log(created_at);
  CREATE INDEX idx_operation_log_level ON operation_log(log_level);
  CREATE INDEX idx_operation_log_source ON operation_log(source_component);
  CREATE INDEX idx_operation_log_status ON operation_log(event_status);
  CREATE INDEX idx_operation_log_request ON operation_log(request_id);
  CREATE INDEX idx_audit_log_created ON audit_log(created_at);
  CREATE INDEX idx_admin_action_log_created ON admin_action_log(created_at);
  CREATE INDEX idx_system_component_type ON system_component_status(component_type);
  CREATE INDEX idx_system_component_checked ON system_component_status(checked_at);
  CREATE INDEX idx_operation_policy_category ON operation_policy(policy_category);
  CREATE INDEX idx_operation_policy_key ON operation_policy(policy_key);
  CREATE INDEX idx_async_job_status ON async_job(job_status);
  CREATE INDEX idx_async_job_type_status_created ON async_job(job_type, job_status, created_at);
  CREATE INDEX idx_file_storage_object ON file(storage_type, bucket_name, object_key);
  CREATE INDEX idx_file_created_by ON file(created_by, created_at);
  CREATE INDEX idx_inspection_input_file ON inspection_input(file_id);
  CREATE INDEX idx_document_org_owner ON document(organization_id, owner_user_id);
  CREATE INDEX idx_document_version_status_created ON document_version(indexing_status, created_at);
  CREATE INDEX idx_document_index_job_status ON document_index_job(job_status, document_version_id);
  CREATE INDEX idx_notification_user_read ON notification(user_id, is_read);
  CREATE INDEX idx_chat_conversation_user ON chat_conversation(user_id);
  CREATE INDEX idx_chat_conversation_user_updated ON chat_conversation(user_id, updated_at);
  CREATE INDEX idx_chat_message_conversation_created ON chat_message(conversation_id, created_at);
  CREATE INDEX idx_chat_source_message ON chat_source(message_id);
  CREATE INDEX idx_chat_source_document ON chat_source(document_id);
  CREATE INDEX idx_chat_source_chunk ON chat_source(chunk_id);
  CREATE INDEX idx_document_category ON document(category);
  CREATE INDEX idx_document_equipment_type ON document(equipment_type);
  CREATE INDEX idx_document_tag_name ON document_tag(tag_name);
