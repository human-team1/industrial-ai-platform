-- Sample inspection/result rows for result list and dashboard verification.
-- Apply after seed-sample-organizations.sql and seed-sample-users.sql.

INSERT INTO file (
  file_id, storage_type, bucket_name, object_key, file_name, file_ext, mime_type, file_size, created_by
)
VALUES
  (92001, 'MINIO', 'inspection', 'samples/press-001.jpg', 'press-001.jpg', 'jpg', 'image/jpeg', 1843200, 91003),
  (92002, 'MINIO', 'inspection', 'samples/motor-001.jpg', 'motor-001.jpg', 'jpg', 'image/jpeg', 2050048, 91003),
  (92003, 'MINIO', 'inspection', 'samples/pump-001.jpg', 'pump-001.jpg', 'jpg', 'image/jpeg', 1679360, 91003),
  (92004, 'MINIO', 'inspection', 'samples/conveyor-001.jpg', 'conveyor-001.jpg', 'jpg', 'image/jpeg', 2498560, 91003),
  (92005, 'MINIO', 'inspection', 'samples/fan-001.jpg', 'fan-001.jpg', 'jpg', 'image/jpeg', 1392640, 91003)
ON DUPLICATE KEY UPDATE
  file_size = VALUES(file_size),
  created_by = VALUES(created_by);

INSERT INTO model (model_id, model_name, model_type, description, created_at)
VALUES
  (93001, 'visual-anomaly-detector', 'PATCHCORE', '샘플 결과 확인용 텍스처 이상 탐지 모델', '2025-05-01 09:00:00')
ON DUPLICATE KEY UPDATE
  model_name = VALUES(model_name),
  model_type = VALUES(model_type),
  description = VALUES(description);

INSERT INTO model_version (
  model_version_id, model_id, file_id, version_name, model_category, model_profile, framework, input_size,
  threshold_default, accuracy, precision_score, recall_score, f1_score, auroc_score,
  deploy_status, is_active, validated_at, validated_by, created_at
)
VALUES
  (94001, 93001, 92001, 'v1.2.0', 'TEXTURE', 'PERFORMANCE', 'PYTORCH', '256x256',
   0.6500, 0.9820, 0.9610, 0.9540, 0.9570, 0.9910,
   'DEPLOYED', TRUE, '2025-05-18 10:30:00', 91002, '2025-05-18 09:00:00')
ON DUPLICATE KEY UPDATE
  version_name = VALUES(version_name),
  model_category = VALUES(model_category),
  model_profile = VALUES(model_profile),
  framework = VALUES(framework),
  input_size = VALUES(input_size),
  threshold_default = VALUES(threshold_default),
  deploy_status = VALUES(deploy_status),
  is_active = VALUES(is_active),
  validated_at = VALUES(validated_at);

INSERT INTO analysis_target (
  target_id, organization_id, target_name, equipment_name, product_name, location_name,
  target_type, target_status, created_by, created_at, updated_at
)
VALUES
  (95001, 9001, '프레스 #1 검사 대상', '프레스 #1', '금속 부품', '라인 A-1', 'EQUIPMENT', 'ACTIVE', 91002, '2025-05-01 09:00:00', '2025-05-01 09:00:00'),
  (95002, 9001, '모터 #3 검사 대상', '모터 #3', '구동 모터', '라인 B-2', 'EQUIPMENT', 'ACTIVE', 91002, '2025-05-01 09:00:00', '2025-05-01 09:00:00'),
  (95003, 9001, '펌프 #2 검사 대상', '펌프 #2', '순환 펌프', '라인 A-3', 'EQUIPMENT', 'ACTIVE', 91002, '2025-05-01 09:00:00', '2025-05-01 09:00:00'),
  (95004, 9001, '컨베이어 #1 검사 대상', '컨베이어 #1', '이송 장치', '라인 C-1', 'EQUIPMENT', 'ACTIVE', 91002, '2025-05-01 09:00:00', '2025-05-01 09:00:00'),
  (95005, 9001, '팬 #4 검사 대상', '팬 #4', '냉각 팬', '라인 B-1', 'EQUIPMENT', 'ACTIVE', 91002, '2025-05-01 09:00:00', '2025-05-01 09:00:00')
ON DUPLICATE KEY UPDATE
  target_name = VALUES(target_name),
  equipment_name = VALUES(equipment_name),
  product_name = VALUES(product_name),
  location_name = VALUES(location_name),
  target_status = VALUES(target_status),
  updated_at = VALUES(updated_at);

INSERT INTO inspection_run (
  inspection_id, organization_id, user_id, target_id, run_type, input_type, source_type, source_id,
  run_status, applied_threshold, idempotency_key, payload_fingerprint, started_at, completed_at
)
VALUES
  (96001, 9001, 91003, 95001, 'UPLOAD', 'IMAGE', 'FILE', '92001', 'COMPLETED', 0.6500, 'sample-inspection-96001', 'sample-fp-96001', '2025-05-14 08:00:00', '2025-05-14 08:01:00'),
  (96002, 9001, 91003, 95002, 'UPLOAD', 'IMAGE', 'FILE', '92002', 'COMPLETED', 0.6500, 'sample-inspection-96002', 'sample-fp-96002', '2025-05-14 09:00:00', '2025-05-14 09:01:00'),
  (96003, 9001, 91003, 95003, 'REALTIME', 'IMAGE', 'CAMERA', '1', 'COMPLETED', 0.6500, 'sample-inspection-96003', 'sample-fp-96003', '2025-05-15 10:00:00', '2025-05-15 10:01:00'),
  (96004, 9001, 91003, 95004, 'UPLOAD', 'IMAGE', 'FILE', '92004', 'COMPLETED', 0.6500, 'sample-inspection-96004', 'sample-fp-96004', '2025-05-15 11:00:00', '2025-05-15 11:01:00'),
  (96005, 9001, 91003, 95005, 'REALTIME', 'IMAGE', 'CAMERA', '2', 'COMPLETED', 0.6500, 'sample-inspection-96005', 'sample-fp-96005', '2025-05-16 08:30:00', '2025-05-16 08:31:00'),
  (96006, 9001, 91003, 95001, 'UPLOAD', 'IMAGE', 'FILE', '92001', 'COMPLETED', 0.6500, 'sample-inspection-96006', 'sample-fp-96006', '2025-05-16 13:00:00', '2025-05-16 13:01:00'),
  (96007, 9001, 91003, 95002, 'REALTIME', 'IMAGE', 'CAMERA', '3', 'COMPLETED', 0.6500, 'sample-inspection-96007', 'sample-fp-96007', '2025-05-17 14:00:00', '2025-05-17 14:01:00'),
  (96008, 9001, 91003, 95003, 'UPLOAD', 'IMAGE', 'FILE', '92003', 'COMPLETED', 0.6500, 'sample-inspection-96008', 'sample-fp-96008', '2025-05-18 08:00:00', '2025-05-18 08:01:00'),
  (96009, 9001, 91003, 95004, 'REALTIME', 'IMAGE', 'CAMERA', '4', 'COMPLETED', 0.6500, 'sample-inspection-96009', 'sample-fp-96009', '2025-05-19 09:00:00', '2025-05-19 09:01:00'),
  (96010, 9001, 91003, 95005, 'UPLOAD', 'IMAGE', 'FILE', '92005', 'COMPLETED', 0.6500, 'sample-inspection-96010', 'sample-fp-96010', '2025-05-19 16:00:00', '2025-05-19 16:01:00'),
  (96011, 9001, 91003, 95001, 'REALTIME', 'IMAGE', 'CAMERA', '5', 'COMPLETED', 0.6500, 'sample-inspection-96011', 'sample-fp-96011', '2025-05-20 09:20:00', '2025-05-20 09:21:00'),
  (96012, 9001, 91003, 95002, 'UPLOAD', 'IMAGE', 'FILE', '92002', 'COMPLETED', 0.6500, 'sample-inspection-96012', 'sample-fp-96012', '2025-05-20 09:28:00', '2025-05-20 09:29:00')
ON DUPLICATE KEY UPDATE
  target_id = VALUES(target_id),
  run_type = VALUES(run_type),
  input_type = VALUES(input_type),
  source_type = VALUES(source_type),
  source_id = VALUES(source_id),
  run_status = VALUES(run_status),
  applied_threshold = VALUES(applied_threshold),
  payload_fingerprint = VALUES(payload_fingerprint),
  started_at = VALUES(started_at),
  completed_at = VALUES(completed_at);

INSERT INTO inspection_result (
  result_id, inspection_id, score, confidence, decision_code, final_decision_code,
  result_status, threshold_source, threshold_version, model_version_id, failure_reason, created_at
)
VALUES
  (97001, 96001, 0.1200, 0.9400, 'NORMAL', 'NORMAL', 'SUCCESS', 'USER', 1, 94001, NULL, '2025-05-14 08:01:05'),
  (97002, 96002, 0.8800, 0.9100, 'DEFECT', 'DEFECT', 'SUCCESS', 'USER', 1, 94001, NULL, '2025-05-14 09:01:05'),
  (97003, 96003, 0.5400, 0.7800, 'RECHECK', 'RECHECK', 'REVIEW_REQUIRED', 'USER', 1, 94001, NULL, '2025-05-15 10:01:05'),
  (97004, 96004, 0.1800, 0.9600, 'NORMAL', 'NORMAL', 'SUCCESS', 'USER', 1, 94001, NULL, '2025-05-15 11:01:05'),
  (97005, 96005, 0.7300, 0.8900, 'DEFECT', 'DEFECT', 'SUCCESS', 'USER', 1, 94001, NULL, '2025-05-16 08:31:05'),
  (97006, 96006, 0.9200, 0.9300, 'DEFECT', 'DEFECT', 'SUCCESS', 'USER', 1, 94001, NULL, '2025-05-16 13:01:05'),
  (97007, 96007, 0.2400, 0.9500, 'NORMAL', 'NORMAL', 'SUCCESS', 'USER', 1, 94001, NULL, '2025-05-17 14:01:05'),
  (97008, 96008, 0.6600, 0.8100, 'RECHECK', 'RECHECK', 'REVIEW_REQUIRED', 'USER', 1, 94001, NULL, '2025-05-18 08:01:05'),
  (97009, 96009, 0.3100, 0.9300, 'NORMAL', 'NORMAL', 'SUCCESS', 'USER', 1, 94001, NULL, '2025-05-19 09:01:05'),
  (97010, 96010, 0.8200, 0.9000, 'DEFECT', 'DEFECT', 'SUCCESS', 'USER', 1, 94001, NULL, '2025-05-19 16:01:05'),
  (97011, 96011, 0.9240, 0.9400, 'DEFECT', 'DEFECT', 'SUCCESS', 'USER', 1, 94001, NULL, '2025-05-20 09:21:05'),
  (97012, 96012, 0.1400, 0.9700, 'NORMAL', 'NORMAL', 'SUCCESS', 'USER', 1, 94001, NULL, '2025-05-20 09:29:05')
ON DUPLICATE KEY UPDATE
  score = VALUES(score),
  confidence = VALUES(confidence),
  decision_code = VALUES(decision_code),
  final_decision_code = VALUES(final_decision_code),
  result_status = VALUES(result_status),
  threshold_source = VALUES(threshold_source),
  threshold_version = VALUES(threshold_version),
  model_version_id = VALUES(model_version_id),
  failure_reason = VALUES(failure_reason),
  created_at = VALUES(created_at);

INSERT INTO notification (
  notification_id, user_id, notification_type, severity, title, message,
  related_type, related_id, target_url, dedup_key, is_read, created_at
)
VALUES
  (98001, 91003, 'INSPECTION_RESULT', 'CRITICAL', '프레스 #1에서 이상이 감지되었습니다.', '최근 검사에서 이상 점수가 기준치를 초과했습니다.', 'RESULT', 97011, '/results/97011', 'sample-noti-98001', FALSE, '2025-05-20 09:30:00'),
  (98002, 91003, 'INSPECTION_RESULT', 'WARNING', '펌프 #2 재검사가 필요합니다.', '경계 구간 결과가 발생하여 재검사를 권장합니다.', 'RESULT', 97008, '/results/97008', 'sample-noti-98002', FALSE, '2025-05-18 08:05:00'),
  (98003, 91003, 'INSPECTION_RESULT', 'INFO', '팬 #4 검사 결과가 저장되었습니다.', '검사 결과와 시각화 산출물이 저장되었습니다.', 'RESULT', 97010, '/results/97010', 'sample-noti-98003', TRUE, '2025-05-19 16:05:00')
ON DUPLICATE KEY UPDATE
  severity = VALUES(severity),
  title = VALUES(title),
  message = VALUES(message),
  target_url = VALUES(target_url),
  is_read = VALUES(is_read),
  created_at = VALUES(created_at);

INSERT INTO system_status_snapshot (
  snapshot_id, cpu_usage, memory_usage, disk_usage, response_time_ms, created_at
)
VALUES
  (99001, 32.50, 58.20, 64.10, 128, '2025-05-20 09:30:00')
ON DUPLICATE KEY UPDATE
  cpu_usage = VALUES(cpu_usage),
  memory_usage = VALUES(memory_usage),
  disk_usage = VALUES(disk_usage),
  response_time_ms = VALUES(response_time_ms),
  created_at = VALUES(created_at);
