ALTER TABLE operation_log
  ADD COLUMN IF NOT EXISTS log_level VARCHAR(20) NULL AFTER event_status,
  ADD COLUMN IF NOT EXISTS source_component VARCHAR(50) NULL AFTER log_level,
  ADD COLUMN IF NOT EXISTS request_id VARCHAR(100) NULL AFTER source_component,
  ADD COLUMN IF NOT EXISTS actor_user_id BIGINT NULL AFTER request_id;

CREATE TABLE IF NOT EXISTS system_component_status (
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

ALTER TABLE system_component_status
  ADD COLUMN IF NOT EXISTS cpu_usage DECIMAL(5,2) NULL AFTER message,
  ADD COLUMN IF NOT EXISTS memory_usage DECIMAL(5,2) NULL AFTER cpu_usage,
  ADD COLUMN IF NOT EXISTS disk_usage DECIMAL(5,2) NULL AFTER memory_usage,
  ADD COLUMN IF NOT EXISTS host_name VARCHAR(100) NULL AFTER disk_usage,
  ADD COLUMN IF NOT EXISTS instance_id VARCHAR(100) NULL AFTER host_name;

ALTER TABLE operation_policy
  ADD COLUMN IF NOT EXISTS policy_category VARCHAR(50) NULL AFTER policy_type,
  ADD COLUMN IF NOT EXISTS policy_key VARCHAR(100) NULL AFTER policy_category,
  ADD COLUMN IF NOT EXISTS policy_name VARCHAR(100) NULL AFTER policy_key,
  ADD COLUMN IF NOT EXISTS value_type VARCHAR(20) NULL AFTER policy_value,
  ADD COLUMN IF NOT EXISTS description VARCHAR(255) NULL AFTER value_type,
  ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE AFTER description;

CREATE INDEX IF NOT EXISTS idx_operation_log_created ON operation_log(created_at);
CREATE INDEX IF NOT EXISTS idx_operation_log_level ON operation_log(log_level);
CREATE INDEX IF NOT EXISTS idx_operation_log_source ON operation_log(source_component);
CREATE INDEX IF NOT EXISTS idx_operation_log_status ON operation_log(event_status);
CREATE INDEX IF NOT EXISTS idx_operation_log_request ON operation_log(request_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_created ON audit_log(created_at);
CREATE INDEX IF NOT EXISTS idx_admin_action_log_created ON admin_action_log(created_at);
CREATE INDEX IF NOT EXISTS idx_system_component_type ON system_component_status(component_type);
CREATE INDEX IF NOT EXISTS idx_system_component_checked ON system_component_status(checked_at);
CREATE INDEX IF NOT EXISTS idx_operation_policy_category ON operation_policy(policy_category);
CREATE INDEX IF NOT EXISTS idx_operation_policy_key ON operation_policy(policy_key);
CREATE INDEX IF NOT EXISTS idx_async_job_status ON async_job(job_status);
