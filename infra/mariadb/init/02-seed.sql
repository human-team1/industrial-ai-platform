-- Minimal seed: 조직/관리자/정책 + OBJECT·TEXTURE × SPEED·PERFORMANCE 모델 버전 및 아티팩트 메타.
-- MinIO 실제 객체는 포함하지 않음( object_key 만). 추론 전 스토리지 업로드 필요.
-- BCrypt 해시만 저장(평문 없음).

USE industrial_ai;

INSERT INTO organization (organization_id, organization_name, status)
VALUES (9001, 'Default Organization', 'ACTIVE')
ON DUPLICATE KEY UPDATE
  organization_name = VALUES(organization_name),
  status = VALUES(status);

INSERT INTO users (
  user_id, organization_id, email, name, password_hash, status, role, created_at, updated_at
)
VALUES
  (
    91001, 9001, 'site-admin@local.dev', 'Site Admin',
    '$2b$10$Fk9lmpEHQdhdNafJRJlCh.TPaKuqD/mga3AuYEkwPTqfxgI3jXIUu',
    'ACTIVE', 'ROLE_SITE_ADMIN', NOW(), NOW()
  ),
  (
    91002, 9001, 'company-admin@local.dev', 'Company Admin', NULL,
    'ACTIVE', 'ROLE_COMPANY_ADMIN', NOW(), NOW()
  ),
  (
    91003, 9001, 'worker@local.dev', 'Worker', NULL,
    'ACTIVE', 'ROLE_COMPANY_WORKER', NOW(), NOW()
  )
ON DUPLICATE KEY UPDATE
  organization_id = VALUES(organization_id),
  name = VALUES(name),
  password_hash = COALESCE(VALUES(password_hash), password_hash),
  status = VALUES(status),
  role = VALUES(role),
  updated_at = NOW();

-- 파일 메타(12): 버전당 CKPT/CONFIG/MEMORY_BANK
INSERT INTO file (
  file_id, storage_type, bucket_name, object_key, file_name, mime_type, file_size, created_by
) VALUES
  (92001, 'MINIO', 'models', 'seed/object-speed/model.ckpt', 'model.ckpt', 'application/octet-stream', 1, 91001),
  (92002, 'MINIO', 'models', 'seed/object-speed/config.json', 'config.json', 'application/json', 1, 91001),
  (92003, 'MINIO', 'models', 'seed/object-speed/memory_bank.npy', 'memory_bank.npy', 'application/octet-stream', 1, 91001),
  (92004, 'MINIO', 'models', 'seed/object-perf/model.ckpt', 'model.ckpt', 'application/octet-stream', 1, 91001),
  (92005, 'MINIO', 'models', 'seed/object-perf/config.json', 'config.json', 'application/json', 1, 91001),
  (92006, 'MINIO', 'models', 'seed/object-perf/memory_bank.npy', 'memory_bank.npy', 'application/octet-stream', 1, 91001),
  (92007, 'MINIO', 'models', 'seed/texture-speed/model.ckpt', 'model.ckpt', 'application/octet-stream', 1, 91001),
  (92008, 'MINIO', 'models', 'seed/texture-speed/config.json', 'config.json', 'application/json', 1, 91001),
  (92009, 'MINIO', 'models', 'seed/texture-speed/memory_bank.npy', 'memory_bank.npy', 'application/octet-stream', 1, 91001),
  (92010, 'MINIO', 'models', 'seed/texture-perf/model.ckpt', 'model.ckpt', 'application/octet-stream', 1, 91001),
  (92011, 'MINIO', 'models', 'seed/texture-perf/config.json', 'config.json', 'application/json', 1, 91001),
  (92012, 'MINIO', 'models', 'seed/texture-perf/memory_bank.npy', 'memory_bank.npy', 'application/octet-stream', 1, 91001)
ON DUPLICATE KEY UPDATE
  bucket_name = VALUES(bucket_name),
  object_key = VALUES(object_key);

INSERT INTO model (model_id, model_name, model_type, description)
VALUES (93001, 'Seed PatchCore Family', 'VISUAL_ANOMALY', 'init seed — replace via API')
ON DUPLICATE KEY UPDATE description = VALUES(description);

INSERT INTO model_version (
  model_version_id, model_id, file_id, version_name, model_category, model_profile,
  framework, input_size, threshold_default, deploy_status, is_active, created_at
) VALUES
  (94001, 93001, 92001, 'v1-object-speed', 'OBJECT', 'SPEED', 'PYTORCH', '224x224', 0.5000, 'VALIDATED', TRUE, NOW()),
  (94002, 93001, 92004, 'v1-object-perf', 'OBJECT', 'PERFORMANCE', 'PYTORCH', '448x448', 0.5000, 'VALIDATED', TRUE, NOW()),
  (94003, 93001, 92007, 'v1-texture-speed', 'TEXTURE', 'SPEED', 'PYTORCH', '256x256', 0.5000, 'VALIDATED', TRUE, NOW()),
  (94004, 93001, 92010, 'v1-texture-perf', 'TEXTURE', 'PERFORMANCE', 'PYTORCH', '448x448', 0.5000, 'VALIDATED', TRUE, NOW())
ON DUPLICATE KEY UPDATE
  model_category = VALUES(model_category),
  model_profile = VALUES(model_profile),
  deploy_status = VALUES(deploy_status);

INSERT INTO model_artifact (model_artifact_id, model_version_id, file_id, artifact_type, checksum) VALUES
  (95001, 94001, 92001, 'CKPT', 'seed'), (95002, 94001, 92002, 'CONFIG', 'seed'), (95003, 94001, 92003, 'MEMORY_BANK', 'seed'),
  (95004, 94002, 92004, 'CKPT', 'seed'), (95005, 94002, 92005, 'CONFIG', 'seed'), (95006, 94002, 92006, 'MEMORY_BANK', 'seed'),
  (95007, 94003, 92007, 'CKPT', 'seed'), (95008, 94003, 92008, 'CONFIG', 'seed'), (95009, 94003, 92009, 'MEMORY_BANK', 'seed'),
  (95010, 94004, 92010, 'CKPT', 'seed'), (95011, 94004, 92011, 'CONFIG', 'seed'), (95012, 94004, 92012, 'MEMORY_BANK', 'seed')
ON DUPLICATE KEY UPDATE checksum = VALUES(checksum);

INSERT INTO model_deployment (
  deployment_id, organization_id, target_id, model_version_id, deployment_scope,
  deploy_status, is_active, deployed_at, deployed_by, rollback_flag
) VALUES
  (96001, 9001, NULL, 94001, 'ORGANIZATION', 'DEPLOYED', TRUE, NOW(), 91001, FALSE),
  (96002, 9001, NULL, 94002, 'ORGANIZATION', 'DEPLOYED', TRUE, NOW(), 91001, FALSE),
  (96003, 9001, NULL, 94003, 'ORGANIZATION', 'DEPLOYED', TRUE, NOW(), 91001, FALSE),
  (96004, 9001, NULL, 94004, 'ORGANIZATION', 'DEPLOYED', TRUE, NOW(), 91001, FALSE)
ON DUPLICATE KEY UPDATE
  deploy_status = VALUES(deploy_status),
  is_active = VALUES(is_active);

INSERT INTO operation_policy (
  operation_policy_id, policy_type, policy_category, policy_key, policy_name,
  policy_value, value_type, description, is_active, updated_at, updated_by
)
VALUES
  (99501, 'DEFAULT_ANOMALY_THRESHOLD', 'INSPECTION', 'default_anomaly_threshold', '기본 이상 임계값', '0.70', 'NUMBER', 'seed', TRUE, NOW(), 91001),
  (99502, 'LOW_CONFIDENCE_THRESHOLD', 'INSPECTION', 'low_confidence_threshold', '저신뢰 임계값', '0.55', 'NUMBER', 'seed', TRUE, NOW(), 91001),
  (99503, 'MAINTENANCE_MODE', 'SYSTEM', 'maintenance_mode', '점검 모드', 'false', 'BOOLEAN', 'seed', TRUE, NOW(), 91001)
ON DUPLICATE KEY UPDATE
  policy_value = VALUES(policy_value),
  is_active = VALUES(is_active),
  updated_at = VALUES(updated_at);

INSERT INTO system_status_snapshot (snapshot_id, cpu_usage, memory_usage, disk_usage, response_time_ms, created_at)
VALUES (1, 1.0, 1.0, 1.0, 1, NOW())
ON DUPLICATE KEY UPDATE cpu_usage = VALUES(cpu_usage);
