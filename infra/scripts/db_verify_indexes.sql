-- Named indexes / uniques critical for idempotency, model slot lookup, RAG/job join.
-- Expect: single row failed_checks = 0.
USE industrial_ai;

SELECT
  (
    SELECT CASE WHEN COUNT(*) >= 1 THEN 0 ELSE 1 END
    FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'inspection_run'
      AND index_name = 'uk_inspection_run_org_user_idempotency'
  )
  + (
    SELECT CASE WHEN COUNT(*) >= 1 THEN 0 ELSE 1 END
    FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'model'
      AND index_name = 'uk_model_name_type'
  )
  + (
    SELECT CASE WHEN COUNT(*) >= 1 THEN 0 ELSE 1 END
    FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'model_version'
      AND index_name = 'uk_model_version_name'
  )
  + (
    SELECT CASE WHEN COUNT(*) >= 1 THEN 0 ELSE 1 END
    FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'document_index_job'
      AND index_name = 'idx_document_index_job_ai_job_id'
  )
  + (
    SELECT CASE WHEN COUNT(*) >= 1 THEN 0 ELSE 1 END
    FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'model_deployment'
      AND index_name = 'idx_model_deployment_slot_deactivate'
  )
  + (
    SELECT CASE WHEN COUNT(*) >= 1 THEN 0 ELSE 1 END
    FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'model_version'
      AND index_name = 'idx_model_version_slot_lookup'
  )
  + (
    SELECT CASE WHEN COUNT(*) >= 1 THEN 0 ELSE 1 END
    FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'notification'
      AND index_name = 'idx_notification_user_dedup'
  )
  + (
    SELECT CASE WHEN COUNT(*) >= 1 THEN 0 ELSE 1 END
    FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'file'
      AND index_name = 'idx_file_storage_object'
  )
  + (
    SELECT CASE WHEN COUNT(*) >= 1 THEN 0 ELSE 1 END
    FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'model_artifact'
      AND index_name = 'idx_model_artifact_version_type'
  )
  + (
    SELECT CASE WHEN COUNT(*) >= 1 THEN 0 ELSE 1 END
    FROM information_schema.table_constraints
    WHERE table_schema = DATABASE() AND table_name = 'model_deployment'
      AND constraint_name = 'fk_model_deployment_deleted_by'
      AND constraint_type = 'FOREIGN KEY'
  )
  + (
    SELECT CASE WHEN COUNT(*) >= 1 THEN 0 ELSE 1 END
    FROM information_schema.table_constraints
    WHERE table_schema = DATABASE() AND table_name = 'model_version'
      AND constraint_name = 'fk_model_version_deleted_by'
      AND constraint_type = 'FOREIGN KEY'
  )
AS failed_checks;
