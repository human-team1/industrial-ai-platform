-- Expects 02-seed.sql applied. Expect: single row failed_checks = 0.
-- Does not verify MinIO objects (DB metadata only).
USE industrial_ai;

SELECT
  (
    SELECT CASE WHEN COUNT(*) = 1 THEN 0 ELSE 1 END
    FROM organization
    WHERE organization_id = 9001 AND status = 'ACTIVE'
  )
  + (
    SELECT CASE WHEN COUNT(*) = 1 THEN 0 ELSE 1 END
    FROM users
    WHERE user_id = 91001 AND email = 'site-admin@local.dev'
      AND status = 'ACTIVE' AND role = 'ROLE_SITE_ADMIN'
      AND password_hash IS NOT NULL AND password_hash LIKE '$2%'
  )
  + (
    SELECT CASE WHEN COUNT(*) >= 3 THEN 0 ELSE 1 END
    FROM operation_policy
    WHERE policy_key IN ('default_anomaly_threshold', 'low_confidence_threshold', 'maintenance_mode')
      AND is_active = TRUE
  )
  + (
    SELECT CASE WHEN COUNT(*) = 1 THEN 0 ELSE 1 END
    FROM model WHERE model_id = 93001
  )
  + (
    SELECT CASE WHEN COUNT(*) = 4 THEN 0 ELSE 1 END
    FROM model_version
    WHERE model_id = 93001
      AND deploy_status = 'VALIDATED'
      AND is_active = TRUE
  )
  + (
    SELECT CASE WHEN COUNT(*) = 4 THEN 0 ELSE 1 END
    FROM model_version
    WHERE model_id = 93001
      AND (model_category, model_profile) IN (
        ('OBJECT', 'SPEED'), ('OBJECT', 'PERFORMANCE'),
        ('TEXTURE', 'SPEED'), ('TEXTURE', 'PERFORMANCE')
      )
  )
  + (
    SELECT CASE WHEN COUNT(*) = 12 THEN 0 ELSE 1 END
    FROM model_artifact
    WHERE model_version_id IN (94001, 94002, 94003, 94004)
  )
  + (
    SELECT CASE WHEN COUNT(*) = 12 THEN 0 ELSE 1 END
    FROM model_artifact ma
    JOIN file f ON f.file_id = ma.file_id
    WHERE ma.model_version_id IN (94001, 94002, 94003, 94004)
  )
  + (
    SELECT CASE WHEN COUNT(*) = 4 THEN 0 ELSE 1 END
    FROM model_deployment
    WHERE organization_id = 9001 AND is_active = TRUE
      AND deploy_status = 'DEPLOYED' AND deployment_scope = 'ORGANIZATION'
  )
  + (
    SELECT CASE WHEN (
      SELECT COUNT(*) FROM (
        SELECT model_version_id, artifact_type
        FROM model_artifact
        WHERE model_version_id IN (94001, 94002, 94003, 94004)
        GROUP BY model_version_id, artifact_type
        HAVING COUNT(*) = 1
          AND artifact_type IN ('CKPT', 'CONFIG', 'MEMORY_BANK')
      ) g
    ) = 12 THEN 0 ELSE 1 END
  )
AS failed_checks;
