-- TEXTURE 모델 버전 및 배포 생성
-- TEXTURE/SPEED (input_size=256, imageThreshold=40.0642)
-- TEXTURE/PERFORMANCE (input_size=448, imageThreshold=22.2103)
-- 기존 deleted TEXTURE 버전(94006, 94007)은 그대로 유지하고 새 버전을 추가한다.
-- 기존 MinIO 파일은 이미 존재하므로 file 레코드만 재활용 한다.

-- ────────────────────────────────────────────────────────────
-- 1. TEXTURE / SPEED  model_version
-- ────────────────────────────────────────────────────────────
INSERT INTO model_version (
    model_id, version_name, model_category, model_profile,
    framework, input_size, threshold_default,
    deploy_status, is_active,
    created_at
) VALUES (
    93004,
    CONCAT('v', DATE_FORMAT(NOW(), '%Y%m%d%H%i%S'), '-texture-speed-org9001-org'),
    'TEXTURE', 'SPEED',
    'PYTORCH', '256x256', 40.0642,
    'DEPLOYED', 1,
    NOW()
);

SET @texture_speed_ver_id = LAST_INSERT_ID();

-- model_artifact: CKPT (base ckpt)
INSERT INTO model_artifact (model_version_id, artifact_type, file_id)
VALUES (@texture_speed_ver_id, 'CKPT', 92989);

-- model_artifact: CONFIG (generated config)
INSERT INTO model_artifact (model_version_id, artifact_type, file_id)
VALUES (@texture_speed_ver_id, 'CONFIG', 92988);

-- model_artifact: MEMORY_BANK (generated memory bank)
INSERT INTO model_artifact (model_version_id, artifact_type, file_id)
VALUES (@texture_speed_ver_id, 'MEMORY_BANK', 92987);

-- model_deployment
INSERT INTO model_deployment (
    model_version_id, organization_id, target_id,
    deployment_scope, deploy_status, is_active,
    deployed_at
) VALUES (
    @texture_speed_ver_id, 9001, NULL,
    'ORGANIZATION', 'DEPLOYED', 1,
    NOW()
);

-- ────────────────────────────────────────────────────────────
-- 2. TEXTURE / PERFORMANCE  model_version
-- ────────────────────────────────────────────────────────────
INSERT INTO model_version (
    model_id, version_name, model_category, model_profile,
    framework, input_size, threshold_default,
    deploy_status, is_active,
    created_at
) VALUES (
    93004,
    CONCAT('v', DATE_FORMAT(NOW(), '%Y%m%d%H%i%S'), '-texture-performance-org9001-org'),
    'TEXTURE', 'PERFORMANCE',
    'PYTORCH', '448x448', 22.2103,
    'DEPLOYED', 1,
    NOW()
);

SET @texture_perf_ver_id = LAST_INSERT_ID();

-- model_artifact: CKPT (base ckpt)
INSERT INTO model_artifact (model_version_id, artifact_type, file_id)
VALUES (@texture_perf_ver_id, 'CKPT', 92992);

-- model_artifact: CONFIG (generated config)
INSERT INTO model_artifact (model_version_id, artifact_type, file_id)
VALUES (@texture_perf_ver_id, 'CONFIG', 92991);

-- model_artifact: MEMORY_BANK (generated memory bank)
INSERT INTO model_artifact (model_version_id, artifact_type, file_id)
VALUES (@texture_perf_ver_id, 'MEMORY_BANK', 92990);

-- model_deployment
INSERT INTO model_deployment (
    model_version_id, organization_id, target_id,
    deployment_scope, deploy_status, is_active,
    deployed_at
) VALUES (
    @texture_perf_ver_id, 9001, NULL,
    'ORGANIZATION', 'DEPLOYED', 1,
    NOW()
);
