-- Align model deployment/artifact schema with Spring model management runtime contract.
-- This migration is intentionally idempotent for local/dev databases that may have
-- partially applied earlier model-management migrations.

CREATE TABLE IF NOT EXISTS model_artifact (
  model_artifact_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  model_version_id BIGINT NOT NULL,
  file_id BIGINT NOT NULL,
  artifact_type VARCHAR(30) NOT NULL,
  checksum VARCHAR(128) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_model_artifact_version FOREIGN KEY (model_version_id) REFERENCES model_version(model_version_id),
  CONSTRAINT fk_model_artifact_file FOREIGN KEY (file_id) REFERENCES file(file_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE model_artifact
  MODIFY COLUMN artifact_type VARCHAR(30) NOT NULL;

ALTER TABLE model_deployment
  ADD COLUMN IF NOT EXISTS organization_id BIGINT NULL AFTER deployment_id,
  ADD COLUMN IF NOT EXISTS target_id BIGINT NULL AFTER organization_id,
  ADD COLUMN IF NOT EXISTS deployment_scope VARCHAR(20) NOT NULL DEFAULT 'ORGANIZATION' AFTER model_version_id,
  ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE AFTER deploy_status,
  ADD COLUMN IF NOT EXISTS deployed_by BIGINT NULL AFTER deployed_at,
  ADD COLUMN IF NOT EXISTS rollback_from_deployment_id BIGINT NULL AFTER deployed_by,
  ADD COLUMN IF NOT EXISTS reason VARCHAR(255) NULL AFTER rollback_from_deployment_id;

ALTER TABLE model_deployment
  ADD COLUMN IF NOT EXISTS rollback_flag BOOLEAN NOT NULL DEFAULT FALSE AFTER reason;

UPDATE model_deployment md
JOIN (SELECT MIN(organization_id) AS organization_id FROM organization) seed
SET md.organization_id = seed.organization_id
WHERE md.organization_id IS NULL
  AND seed.organization_id IS NOT NULL;

UPDATE model_deployment md
LEFT JOIN organization org ON org.organization_id = md.organization_id
JOIN (SELECT MIN(organization_id) AS organization_id FROM organization) seed
SET md.organization_id = seed.organization_id
WHERE md.organization_id IS NOT NULL
  AND org.organization_id IS NULL
  AND seed.organization_id IS NOT NULL;

ALTER TABLE model_deployment
  MODIFY COLUMN organization_id BIGINT NOT NULL,
  MODIFY COLUMN deployment_scope VARCHAR(20) NOT NULL DEFAULT 'ORGANIZATION',
  MODIFY COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE,
  MODIFY COLUMN rollback_flag BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE model_version
SET deploy_status = CASE deploy_status
  WHEN 'READY' THEN 'REGISTERED'
  WHEN 'VALIDATING' THEN 'REGISTERED'
  WHEN 'ACTIVE' THEN 'DEPLOYED'
  WHEN 'INACTIVE' THEN 'DEPRECATED'
  WHEN 'FAILED' THEN 'DEPRECATED'
  WHEN 'ROLLED_BACK' THEN 'DEPRECATED'
  WHEN 'REGISTERED' THEN 'REGISTERED'
  WHEN 'VALIDATED' THEN 'VALIDATED'
  WHEN 'DEPLOYED' THEN 'DEPLOYED'
  WHEN 'DEPRECATED' THEN 'DEPRECATED'
  ELSE 'REGISTERED'
END;

ALTER TABLE model_version
  MODIFY COLUMN deploy_status VARCHAR(20) NOT NULL DEFAULT 'REGISTERED';

UPDATE model_deployment
SET deploy_status = CASE deploy_status
  WHEN 'ACTIVE' THEN 'DEPLOYED'
  WHEN 'INACTIVE' THEN 'DEACTIVATED'
  WHEN 'FAILED' THEN 'DEACTIVATED'
  WHEN 'DEPLOYED' THEN 'DEPLOYED'
  WHEN 'ROLLED_BACK' THEN 'ROLLED_BACK'
  WHEN 'DEACTIVATED' THEN 'DEACTIVATED'
  ELSE 'DEPLOYED'
END;

ALTER TABLE model_deployment
  MODIFY COLUMN deploy_status VARCHAR(20) NOT NULL DEFAULT 'DEPLOYED';

SET @fk_model_deployment_version :=
  IF (
    (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
      WHERE CONSTRAINT_SCHEMA = DATABASE()
        AND TABLE_NAME = 'model_deployment'
        AND CONSTRAINT_NAME = 'fk_model_deployment_version') = 0,
    'ALTER TABLE model_deployment ADD CONSTRAINT fk_model_deployment_version FOREIGN KEY (model_version_id) REFERENCES model_version(model_version_id)',
    'SELECT 1'
  );
PREPARE stmt FROM @fk_model_deployment_version;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @fk_model_deployment_organization :=
  IF (
    (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
      WHERE CONSTRAINT_SCHEMA = DATABASE()
        AND TABLE_NAME = 'model_deployment'
        AND CONSTRAINT_NAME = 'fk_model_deployment_organization') = 0,
    'ALTER TABLE model_deployment ADD CONSTRAINT fk_model_deployment_organization FOREIGN KEY (organization_id) REFERENCES organization(organization_id)',
    'SELECT 1'
  );
PREPARE stmt FROM @fk_model_deployment_organization;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @fk_model_deployment_target :=
  IF (
    (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
      WHERE CONSTRAINT_SCHEMA = DATABASE()
        AND TABLE_NAME = 'model_deployment'
        AND CONSTRAINT_NAME = 'fk_model_deployment_target') = 0,
    'ALTER TABLE model_deployment ADD CONSTRAINT fk_model_deployment_target FOREIGN KEY (target_id) REFERENCES analysis_target(target_id)',
    'SELECT 1'
  );
PREPARE stmt FROM @fk_model_deployment_target;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @fk_model_deployment_user :=
  IF (
    (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
      WHERE CONSTRAINT_SCHEMA = DATABASE()
        AND TABLE_NAME = 'model_deployment'
        AND CONSTRAINT_NAME = 'fk_model_deployment_user') = 0,
    'ALTER TABLE model_deployment ADD CONSTRAINT fk_model_deployment_user FOREIGN KEY (deployed_by) REFERENCES users(user_id)',
    'SELECT 1'
  );
PREPARE stmt FROM @fk_model_deployment_user;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @fk_model_deployment_rollback :=
  IF (
    (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
      WHERE CONSTRAINT_SCHEMA = DATABASE()
        AND TABLE_NAME = 'model_deployment'
        AND CONSTRAINT_NAME = 'fk_model_deployment_rollback') = 0,
    'ALTER TABLE model_deployment ADD CONSTRAINT fk_model_deployment_rollback FOREIGN KEY (rollback_from_deployment_id) REFERENCES model_deployment(deployment_id)',
    'SELECT 1'
  );
PREPARE stmt FROM @fk_model_deployment_rollback;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @fk_model_artifact_version :=
  IF (
    (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
      WHERE CONSTRAINT_SCHEMA = DATABASE()
        AND TABLE_NAME = 'model_artifact'
        AND CONSTRAINT_NAME = 'fk_model_artifact_version') = 0,
    'ALTER TABLE model_artifact ADD CONSTRAINT fk_model_artifact_version FOREIGN KEY (model_version_id) REFERENCES model_version(model_version_id)',
    'SELECT 1'
  );
PREPARE stmt FROM @fk_model_artifact_version;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @fk_model_artifact_file :=
  IF (
    (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
      WHERE CONSTRAINT_SCHEMA = DATABASE()
        AND TABLE_NAME = 'model_artifact'
        AND CONSTRAINT_NAME = 'fk_model_artifact_file') = 0,
    'ALTER TABLE model_artifact ADD CONSTRAINT fk_model_artifact_file FOREIGN KEY (file_id) REFERENCES file(file_id)',
    'SELECT 1'
  );
PREPARE stmt FROM @fk_model_artifact_file;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE INDEX IF NOT EXISTS idx_model_deployment_available
  ON model_deployment(organization_id, target_id, deployment_scope, deploy_status, is_active, deployed_at);

CREATE INDEX IF NOT EXISTS idx_model_deployment_version_active
  ON model_deployment(model_version_id, is_active, deploy_status);

CREATE INDEX IF NOT EXISTS idx_model_version_available
  ON model_version(model_id, model_category, deploy_status, is_active, created_at);

CREATE INDEX IF NOT EXISTS idx_model_artifact_version_type
  ON model_artifact(model_version_id, artifact_type);
