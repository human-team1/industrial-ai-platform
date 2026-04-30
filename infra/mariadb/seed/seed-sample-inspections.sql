-- Sample inspection/result rows for result list and dashboard verification
INSERT INTO analysis_target (
  target_id, organization_id, target_name, equipment_name, product_name, target_type, target_status, created_by, created_at, updated_at
)
VALUES
  (95001, 9001, '프레스 #1 라인', '프레스 #1', '모터 부품', 'EQUIPMENT', 'ACTIVE', 91002, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  target_name = VALUES(target_name),
  equipment_name = VALUES(equipment_name),
  product_name = VALUES(product_name),
  target_status = VALUES(target_status),
  updated_at = NOW();

INSERT INTO inspection_run (
  inspection_id, organization_id, user_id, target_id, run_type, input_type, source_type, source_id,
  run_status, applied_threshold, idempotency_key, payload_fingerprint, started_at, completed_at
)
VALUES
  (96001, 9001, 91003, 95001, 'UPLOAD', 'IMAGE', 'FILE', '92001',
   'COMPLETED', 0.65, 'sample-inspection-96001', 'sample-fp-96001', NOW(), NOW())
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
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
  result_status, threshold_source, threshold_version, failure_reason, created_at
)
VALUES
  (97001, 96001, 0.82, 0.91, 'DEFECT', 'DEFECT', 'SUCCESS', 'USER', 1, NULL, NOW())
ON DUPLICATE KEY UPDATE
  score = VALUES(score),
  confidence = VALUES(confidence),
  decision_code = VALUES(decision_code),
  final_decision_code = VALUES(final_decision_code),
  result_status = VALUES(result_status),
  threshold_source = VALUES(threshold_source),
  threshold_version = VALUES(threshold_version),
  failure_reason = VALUES(failure_reason);
