-- Sample operation/admin data for monitoring and site settings screens.
-- Apply after organization/user seeds.

INSERT INTO system_status_snapshot (snapshot_id, cpu_usage, memory_usage, disk_usage, response_time_ms, created_at)
VALUES
  (99101, 42.50, 61.20, 68.40, 132, '2025-05-20 10:00:00')
ON DUPLICATE KEY UPDATE
  cpu_usage = VALUES(cpu_usage),
  memory_usage = VALUES(memory_usage),
  disk_usage = VALUES(disk_usage),
  response_time_ms = VALUES(response_time_ms),
  created_at = VALUES(created_at);

INSERT INTO system_component_status (
  component_status_id, component_type, component_name, status, message,
  cpu_usage, memory_usage, disk_usage, host_name, instance_id,
  response_time_ms, checked_at, created_at
)
VALUES
  (99201, 'SPRING_API', 'Spring API 서버', 'NORMAL', '[seed fallback] 정상 응답', 42.50, 61.20, 68.40, 'spring-local', 'spring-local-1', 126, '2025-05-20 10:00:00', '2025-05-20 10:00:00'),
  (99202, 'AI_SERVER', 'AI 모델 서버', 'WARNING', '[seed fallback] 응답 시간이 평소보다 높습니다.', 35.20, 54.80, 70.10, 'ai-server-local', 'ai-local-1', 820, '2025-05-20 10:00:00', '2025-05-20 10:00:00'),
  (99203, 'MARIADB', 'MariaDB', 'NORMAL', '[seed fallback] 연결 정상', NULL, NULL, NULL, 'db-local', 'mariadb', 18, '2025-05-20 10:00:00', '2025-05-20 10:00:00'),
  (99204, 'REDIS', 'Redis', 'NORMAL', '[seed fallback] 연결 정상', NULL, NULL, NULL, 'redis-local', 'redis', 11, '2025-05-20 10:00:00', '2025-05-20 10:00:00'),
  (99205, 'MINIO', 'MinIO', 'NORMAL', '[seed fallback] 스토리지 접근 정상', NULL, NULL, NULL, 'minio-local', 'minio', 35, '2025-05-20 10:00:00', '2025-05-20 10:00:00'),
  (99206, 'CHROMA', 'ChromaDB', 'NORMAL', '[seed fallback] 벡터 검색 정상', NULL, NULL, NULL, 'chroma-local', 'chroma', 52, '2025-05-20 10:00:00', '2025-05-20 10:00:00'),
  (99207, 'STREAM_SERVER', 'Stream Server', 'UNKNOWN', '[seed fallback] 실시간 스트림 상태 수집 대기', NULL, NULL, NULL, NULL, 'stream', NULL, '2025-05-20 10:00:00', '2025-05-20 10:00:00'),
  (99208, 'STORAGE', 'Storage', 'NORMAL', '[seed fallback] 디스크 사용률 안정', 42.50, 61.20, 68.40, 'spring-local', 'storage', 24, '2025-05-20 10:00:00', '2025-05-20 10:00:00')
ON DUPLICATE KEY UPDATE
  status = VALUES(status),
  message = VALUES(message),
  response_time_ms = VALUES(response_time_ms),
  checked_at = VALUES(checked_at);

INSERT INTO operation_log (
  operation_log_id, event_type, event_status, log_level, source_component, request_id,
  actor_user_id, detail_message, related_path, created_at
)
VALUES
  (99301, 'SYSTEM_HEALTH_CHECK', 'SUCCESS', 'INFO', 'SPRING_API', 'seed-req-001', 91001, '시스템 상태 확인 완료', '/api/v1/admin/system-status', '2025-05-20 09:50:00'),
  (99302, 'AI_SERVER_CALL', 'SLOW', 'WARN', 'AI_SERVER', 'seed-req-002', 91003, '[seed fallback] AI 모델 서버 응답 지연', '/api/v1/inspections/upload', '2025-05-20 09:42:00'),
  (99303, 'DOCUMENT_INDEX', 'FAILED', 'ERROR', 'CHROMA', 'seed-req-003', 91002, '[seed fallback] 문서 인덱싱 실패', '/api/v1/document-versions/12/index-jobs', '2025-05-20 09:30:00'),
  (99304, 'INSPECTION_REQUEST', 'SUCCESS', 'INFO', 'SPRING_API', 'seed-req-004', 91003, '검사 요청 접수', '/api/v1/inspections/upload', '2025-05-20 09:20:00'),
  (99305, 'STORAGE_WRITE', 'SUCCESS', 'INFO', 'MINIO', 'seed-req-005', 91003, '검사 이미지 저장 완료', '/api/v1/files/92001', '2025-05-20 09:10:00'),
  (99306, 'CACHE_ACCESS', 'SUCCESS', 'INFO', 'REDIS', 'seed-req-006', 91001, '세션 캐시 접근 정상', '/api/v1/auth/me', '2025-05-20 09:00:00'),
  (99307, 'DB_QUERY', 'SUCCESS', 'INFO', 'MARIADB', 'seed-req-007', 91001, '대시보드 집계 조회 완료', '/api/v1/dashboard/overview', '2025-05-20 08:50:00'),
  (99308, 'ASYNC_JOB', 'FAILED', 'ERROR', 'SPRING_API', 'seed-req-008', 91001, '비동기 보고서 생성 실패', '/api/v1/admin/async-jobs/99402', '2025-05-20 08:40:00'),
  (99309, 'STREAM_CHECK', 'UNKNOWN', 'WARN', 'STREAM_SERVER', 'seed-req-009', 91001, '스트림 서버 상태 수집 대기', '/api/v1/admin/system-components', '2025-05-20 08:30:00'),
  (99310, 'POLICY_UPDATE', 'SUCCESS', 'INFO', 'SPRING_API', 'seed-req-010', 91001, '운영 정책 변경 기록', '/api/v1/admin/operation-policies/99501', '2025-05-20 08:20:00')
ON DUPLICATE KEY UPDATE
  event_status = VALUES(event_status),
  log_level = VALUES(log_level),
  detail_message = VALUES(detail_message),
  created_at = VALUES(created_at);

INSERT INTO audit_log (audit_log_id, actor_user_id, action_type, target_type, target_id, before_json, after_json, created_at)
VALUES
  (99601, 91001, 'UPDATE', 'OPERATION_POLICY', 99501, '{"policyValue":"0.65"}', '{"policyValue":"0.70"}', '2025-05-20 08:20:00'),
  (99602, 91001, 'APPROVE', 'SIGNUP_REQUEST', 1, NULL, '{"status":"APPROVED"}', '2025-05-19 14:30:00')
ON DUPLICATE KEY UPDATE
  action_type = VALUES(action_type),
  before_json = VALUES(before_json),
  after_json = VALUES(after_json);

INSERT INTO admin_action_log (admin_action_id, actor_user_id, action_type, target_type, target_id, reason, created_at)
VALUES
  (99701, 91001, 'UPDATE_OPERATION_POLICY', 'OPERATION_POLICY', 99501, '기본 이상 임계값 조정', '2025-05-20 08:20:00'),
  (99702, 91001, 'APPROVE_SIGNUP', 'SIGNUP_REQUEST', 1, '가입 승인', '2025-05-19 14:30:00')
ON DUPLICATE KEY UPDATE
  action_type = VALUES(action_type),
  reason = VALUES(reason);

INSERT INTO operation_policy (
  operation_policy_id, policy_type, policy_category, policy_key, policy_name,
  policy_value, value_type, description, is_active, updated_at, updated_by
)
VALUES
  (99501, 'DEFAULT_ANOMALY_THRESHOLD', 'INSPECTION', 'default_anomaly_threshold', '기본 이상 임계값', '0.70', 'NUMBER', '회사 기본값이 없을 때 적용되는 이상 판정 기준', TRUE, '2025-05-20 08:20:00', 91001),
  (99502, 'LOW_CONFIDENCE_THRESHOLD', 'INSPECTION', 'low_confidence_threshold', '저신뢰 임계값', '0.55', 'NUMBER', '재검사 분류에 활용하는 신뢰도 기준', TRUE, '2025-05-20 08:20:00', 91001),
  (99503, 'ANOMALY_ALERT_ENABLED', 'NOTIFICATION', 'anomaly_alert_enabled', '이상 알림 사용', 'true', 'BOOLEAN', '이상 탐지 알림 발송 여부', TRUE, '2025-05-20 08:20:00', 91001),
  (99504, 'SYSTEM_ERROR_ALERT_ENABLED', 'NOTIFICATION', 'system_error_alert_enabled', '시스템 오류 알림 사용', 'true', 'BOOLEAN', '운영 오류 알림 발송 여부', TRUE, '2025-05-20 08:20:00', 91001),
  (99505, 'SESSION_TIMEOUT_MINUTES', 'SECURITY', 'session_timeout_minutes', '세션 만료 시간', '60', 'NUMBER', '관리자 세션 만료 시간(분)', TRUE, '2025-05-20 08:20:00', 91001),
  (99506, 'OPERATION_LOG_RETENTION_DAYS', 'RETENTION', 'operation_log_retention_days', '운영 로그 보존 기간', '90', 'NUMBER', '운영 로그 보존 기간(일)', TRUE, '2025-05-20 08:20:00', 91001),
  (99507, 'MAINTENANCE_MODE', 'SYSTEM', 'maintenance_mode', '점검 모드', 'false', 'BOOLEAN', '서비스 점검 모드 활성 여부', TRUE, '2025-05-20 08:20:00', 91001)
ON DUPLICATE KEY UPDATE
  policy_value = VALUES(policy_value),
  value_type = VALUES(value_type),
  description = VALUES(description),
  is_active = VALUES(is_active),
  updated_at = VALUES(updated_at),
  updated_by = VALUES(updated_by);

INSERT INTO async_job (job_id, job_type, job_status, target_type, target_id, error_message, created_at, completed_at)
VALUES
  (99401, 'DOCUMENT_INDEXING', 'COMPLETED', 'DOCUMENT_VERSION', 101, NULL, '2025-05-20 08:00:00', '2025-05-20 08:02:00'),
  (99402, 'REPORT_GENERATION', 'FAILED', 'REPORT', 201, '보고서 템플릿 파일을 찾을 수 없습니다.', '2025-05-20 08:30:00', '2025-05-20 08:31:00'),
  (99403, 'MODEL_VALIDATION', 'RUNNING', 'MODEL_VERSION', 94001, NULL, '2025-05-20 09:00:00', NULL)
ON DUPLICATE KEY UPDATE
  job_status = VALUES(job_status),
  error_message = VALUES(error_message),
  completed_at = VALUES(completed_at);
