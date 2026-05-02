ALTER TABLE analysis_target
  ADD COLUMN IF NOT EXISTS location_name VARCHAR(100) NULL AFTER product_name;

CREATE INDEX IF NOT EXISTS idx_inspection_run_org_completed
  ON inspection_run(organization_id, completed_at);

CREATE INDEX IF NOT EXISTS idx_inspection_run_org_status_completed
  ON inspection_run(organization_id, run_status, completed_at);

CREATE INDEX IF NOT EXISTS idx_result_inspection_decision
  ON inspection_result(inspection_id, decision_code, final_decision_code, created_at);

CREATE INDEX IF NOT EXISTS idx_analysis_target_org_status
  ON analysis_target(organization_id, target_status);

CREATE INDEX IF NOT EXISTS idx_notification_user_created
  ON notification(user_id, created_at);
