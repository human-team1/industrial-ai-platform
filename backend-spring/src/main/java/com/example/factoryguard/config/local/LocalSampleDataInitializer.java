package com.example.factoryguard.config.local;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;

@Component
@Profile("local")
@RequiredArgsConstructor
public class LocalSampleDataInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        if (!tableExists("INSPECTION_RESULT") || existsSampleResult()) {
            return;
        }

        jdbcTemplate.update("""
                INSERT INTO ORGANIZATION (organization_id, organization_name, status)
                VALUES (9001, '샘플 공장', 'ACTIVE')
                ON DUPLICATE KEY UPDATE organization_name = organization_name
                """);
        jdbcTemplate.update("""
                INSERT INTO USERS (user_id, organization_id, email, name, status, role)
                VALUES (9001, 9001, 'sample-admin@factoryguard.local', '샘플 관리자', 'ACTIVE', 'ADMIN')
                ON DUPLICATE KEY UPDATE email = email
                """);
        jdbcTemplate.update("""
                INSERT INTO ANALYSIS_TARGET (target_id, organization_id, target_name, equipment_name, product_name, target_type, target_status, created_by)
                VALUES (9001, 9001, '라인 A-1', '프레스 #1', '베어링', 'EQUIPMENT', 'ACTIVE', 9001)
                ON DUPLICATE KEY UPDATE target_name = target_name
                """);
        jdbcTemplate.update("""
                INSERT INTO CAMERA_SOURCE (camera_id, organization_id, user_id, camera_name, stream_url, status)
                VALUES (9001, 9001, 9001, '라인 A 카메라', 'local://sample-camera', 'ACTIVE')
                ON DUPLICATE KEY UPDATE camera_name = camera_name
                """);
        jdbcTemplate.update("""
                INSERT INTO `FILE` (file_id, storage_type, file_path, file_name, file_ext, mime_type, file_size, created_by)
                VALUES
                  (9001, 'LOCAL', 'sample/original.svg', '원본 이미지 샘플', 'svg', 'image/svg+xml', 1024, 9001),
                  (9002, 'LOCAL', 'sample/heatmap.svg', 'Heatmap 샘플', 'svg', 'image/svg+xml', 1024, 9001),
                  (9003, 'LOCAL', 'sample/normal.svg', '정상 샘플', 'svg', 'image/svg+xml', 1024, 9001)
                ON DUPLICATE KEY UPDATE file_name = file_name
                """);
        jdbcTemplate.update("""
                INSERT INTO INSPECTION_RUN (
                    inspection_id, organization_id, user_id, target_id, run_type, input_type,
                    source_type, source_id, run_status, applied_threshold, started_at, completed_at
                )
                VALUES
                  (9001, 9001, 9001, 9001, 'REALTIME', 'IMAGE', 'CAMERA', 9001, 'COMPLETED', 0.7000, NOW() - INTERVAL 30 MINUTE, NOW() - INTERVAL 29 MINUTE),
                  (9002, 9001, 9001, 9001, 'REALTIME', 'IMAGE', 'CAMERA', 9001, 'COMPLETED', 0.7000, NOW() - INTERVAL 90 MINUTE, NOW() - INTERVAL 89 MINUTE),
                  (9003, 9001, 9001, 9001, 'UPLOAD', 'IMAGE', 'FILE', 9003, 'COMPLETED', 0.7000, NOW() - INTERVAL 150 MINUTE, NOW() - INTERVAL 149 MINUTE)
                ON DUPLICATE KEY UPDATE inspection_id = inspection_id
                """);
        jdbcTemplate.update("""
                INSERT INTO INSPECTION_INPUT (inspection_input_id, inspection_id, file_id, camera_id, source_name, mime_type, frame_count)
                VALUES
                  (9001, 9001, 9001, 9001, 'sample-original', 'image/svg+xml', 1),
                  (9002, 9002, 9002, 9001, 'sample-heatmap', 'image/svg+xml', 1),
                  (9003, 9003, 9003, NULL, 'sample-normal', 'image/svg+xml', 1)
                ON DUPLICATE KEY UPDATE source_name = source_name
                """);
        jdbcTemplate.update("""
                INSERT INTO INSPECTION_EVENT_LOG (event_id, inspection_id, event_type, message, created_at)
                VALUES
                  (9001, 9001, 'INSPECTION_STARTED', '탐지 시작', NOW() - INTERVAL 30 MINUTE),
                  (9002, 9001, 'IMAGE_CAPTURED', '이미지 수집', NOW() - INTERVAL 29 MINUTE - INTERVAL 30 SECOND),
                  (9003, 9001, 'ANOMALY_DETECTED', '이상 탐지', NOW() - INTERVAL 29 MINUTE),
                  (9004, 9002, 'INSPECTION_STARTED', '탐지 시작', NOW() - INTERVAL 90 MINUTE),
                  (9005, 9003, 'INSPECTION_STARTED', '탐지 시작', NOW() - INTERVAL 150 MINUTE)
                ON DUPLICATE KEY UPDATE message = message
                """);
        jdbcTemplate.update("""
                INSERT INTO INSPECTION_RESULT (
                    result_id, inspection_id, score, confidence, decision_code, final_decision_code,
                    result_status, threshold_source, model_version_id, created_at
                )
                VALUES
                  (9001, 9001, 0.9240, 0.9100, 'DEFECT', 'DEFECT', 'SUCCESS', 'USER', NULL, NOW() - INTERVAL 29 MINUTE),
                  (9002, 9002, 0.7650, 0.7200, 'RECHECK', 'RECHECK', 'REVIEW_REQUIRED', 'USER', NULL, NOW() - INTERVAL 89 MINUTE),
                  (9003, 9003, 0.1230, 0.9500, 'NORMAL', 'NORMAL', 'SUCCESS', 'SYSTEM_DEFAULT', NULL, NOW() - INTERVAL 149 MINUTE)
                ON DUPLICATE KEY UPDATE result_id = result_id
                """);
        jdbcTemplate.update("""
                INSERT INTO RESULT_ARTIFACT (artifact_id, result_id, artifact_type, file_id)
                VALUES (9001, 9001, 'HEATMAP', 9002)
                ON DUPLICATE KEY UPDATE artifact_type = artifact_type
                """);
        jdbcTemplate.update("""
                INSERT INTO `IMAGE` (image_id, result_id, file_id, image_role)
                VALUES
                  (9001, 9001, 9001, 'ORIGINAL'),
                  (9002, 9002, 9002, 'ORIGINAL'),
                  (9003, 9003, 9003, 'ORIGINAL')
                ON DUPLICATE KEY UPDATE image_role = image_role
                """);
        jdbcTemplate.update("""
                INSERT INTO ANOMALY_REGION (region_id, image_id, label_code, bbox_x, bbox_y, bbox_w, bbox_h, score)
                VALUES (9001, 9001, 'WEAR', 120.0000, 80.0000, 100.0000, 90.0000, 0.8800)
                ON DUPLICATE KEY UPDATE label_code = label_code
                """);
        jdbcTemplate.update("""
                INSERT INTO REVIEW_QUEUE (review_queue_id, result_id, queue_status, queued_reason)
                VALUES (9001, 9001, 'WAITING', 'LOW_CONFIDENCE')
                ON DUPLICATE KEY UPDATE queue_status = queue_status
                """);
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = DATABASE()
                  AND table_name = ?
                """, Integer.class, tableName);
        return count != null && count > 0;
    }

    private boolean existsSampleResult() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INSPECTION_RESULT WHERE result_id IN (9001, 9002, 9003)",
                Integer.class
        );
        return count != null && count > 0;
    }
}
