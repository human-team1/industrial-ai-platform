ALTER TABLE model
  MODIFY COLUMN model_type VARCHAR(50) NOT NULL;

ALTER TABLE model
  ADD COLUMN IF NOT EXISTS description TEXT NULL AFTER model_type;

CREATE UNIQUE INDEX IF NOT EXISTS uk_model_name_type
  ON model(model_name, model_type);

ALTER TABLE model_version
  MODIFY COLUMN version_name VARCHAR(100) NOT NULL;

ALTER TABLE model_version
  ADD COLUMN IF NOT EXISTS model_category VARCHAR(20) NOT NULL DEFAULT 'OBJECT' AFTER version_name,
  ADD COLUMN IF NOT EXISTS model_profile VARCHAR(20) NOT NULL DEFAULT 'PERFORMANCE' AFTER model_category,
  ADD COLUMN IF NOT EXISTS framework VARCHAR(50) NULL AFTER model_profile,
  ADD COLUMN IF NOT EXISTS input_size VARCHAR(50) NULL AFTER framework,
  ADD COLUMN IF NOT EXISTS threshold_default DECIMAL(5,4) NULL AFTER input_size,
  ADD COLUMN IF NOT EXISTS f1_score DECIMAL(6,4) NULL AFTER recall_score,
  ADD COLUMN IF NOT EXISTS auroc_score DECIMAL(6,4) NULL AFTER f1_score;

UPDATE model_version
SET deploy_status = CASE deploy_status
  WHEN 'READY' THEN 'REGISTERED'
  WHEN 'VALIDATING' THEN 'REGISTERED'
  WHEN 'ACTIVE' THEN 'DEPLOYED'
  WHEN 'INACTIVE' THEN 'DEPRECATED'
  WHEN 'FAILED' THEN 'DEPRECATED'
  WHEN 'ROLLED_BACK' THEN 'DEPRECATED'
  ELSE 'REGISTERED'
END;

ALTER TABLE model_version
  MODIFY COLUMN deploy_status VARCHAR(20) NOT NULL DEFAULT 'REGISTERED';

CREATE UNIQUE INDEX IF NOT EXISTS uk_model_version_name
  ON model_version(model_id, version_name);

CREATE TABLE IF NOT EXISTS model_artifact (
  model_artifact_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  model_version_id BIGINT NOT NULL,
  file_id BIGINT NOT NULL,
  artifact_type VARCHAR(20) NOT NULL,
  checksum VARCHAR(128) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_model_artifact_version FOREIGN KEY (model_version_id) REFERENCES model_version(model_version_id),
  CONSTRAINT fk_model_artifact_file FOREIGN KEY (file_id) REFERENCES file(file_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE model_deployment
  ADD COLUMN IF NOT EXISTS organization_id BIGINT NULL AFTER deployment_id,
  ADD COLUMN IF NOT EXISTS target_id BIGINT NULL AFTER organization_id,
  ADD COLUMN IF NOT EXISTS deployment_scope VARCHAR(20) NOT NULL DEFAULT 'ORGANIZATION' AFTER model_version_id,
  ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE AFTER deploy_status,
  ADD COLUMN IF NOT EXISTS deployed_by BIGINT NULL AFTER deployed_at,
  ADD COLUMN IF NOT EXISTS rollback_from_deployment_id BIGINT NULL AFTER deployed_by,
  ADD COLUMN IF NOT EXISTS reason VARCHAR(255) NULL AFTER rollback_from_deployment_id;

UPDATE model_deployment
SET organization_id = 1
WHERE organization_id IS NULL;

ALTER TABLE model_deployment
  MODIFY COLUMN organization_id BIGINT NOT NULL;

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

CREATE INDEX IF NOT EXISTS idx_model_type_created ON model(model_type, created_at);
CREATE INDEX IF NOT EXISTS idx_model_version_model_status ON model_version(model_id, deploy_status, is_active, created_at);
CREATE INDEX IF NOT EXISTS idx_model_artifact_version_type ON model_artifact(model_version_id, artifact_type);
CREATE INDEX IF NOT EXISTS idx_model_deployment_scope ON model_deployment(organization_id, target_id, deployment_scope, is_active);
