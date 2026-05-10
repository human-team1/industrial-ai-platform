-- Idempotent: baseline(init) 에 이미 audit 컬럼이 있으면 스킵 (fresh + 레거시 DB 공존).
ALTER TABLE model_deployment
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL AFTER rollback_flag,
    ADD COLUMN IF NOT EXISTS deleted_by BIGINT NULL AFTER deleted_at,
    ADD COLUMN IF NOT EXISTS delete_reason TEXT NULL AFTER deleted_by;

ALTER TABLE model_version
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL AFTER created_at,
    ADD COLUMN IF NOT EXISTS deleted_by BIGINT NULL AFTER deleted_at,
    ADD COLUMN IF NOT EXISTS delete_reason TEXT NULL AFTER deleted_by;

SET @fk_md_del :=
  IF (
    (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
      WHERE CONSTRAINT_SCHEMA = DATABASE()
        AND TABLE_NAME = 'model_deployment'
        AND CONSTRAINT_NAME = 'fk_model_deployment_deleted_by') = 0,
    'ALTER TABLE model_deployment ADD CONSTRAINT fk_model_deployment_deleted_by FOREIGN KEY (deleted_by) REFERENCES users(user_id)',
    'SELECT 1'
  );
PREPARE stmt FROM @fk_md_del;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @fk_mv_del :=
  IF (
    (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
      WHERE CONSTRAINT_SCHEMA = DATABASE()
        AND TABLE_NAME = 'model_version'
        AND CONSTRAINT_NAME = 'fk_model_version_deleted_by') = 0,
    'ALTER TABLE model_version ADD CONSTRAINT fk_model_version_deleted_by FOREIGN KEY (deleted_by) REFERENCES users(user_id)',
    'SELECT 1'
  );
PREPARE stmt FROM @fk_mv_del;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE INDEX IF NOT EXISTS idx_model_deployment_deleted_at ON model_deployment (deleted_at);
CREATE INDEX IF NOT EXISTS idx_model_version_deleted_at ON model_version (deleted_at);
