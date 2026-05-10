-- Main tables / columns aligned with latest migration + 01-schema.sql.
-- Expect: single row failed_checks = 0. Anything else => investigate.
USE industrial_ai;

SELECT
  (
    SELECT CASE WHEN COUNT(*) = 1 THEN 0 ELSE 1 END
    FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'async_job'
  )
  + (
    SELECT CASE WHEN COUNT(*) = 1 THEN 0 ELSE 1 END
    FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'document_index_job'
  )
  + (
    SELECT CASE WHEN COUNT(*) = 1 THEN 0 ELSE 1 END
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'document_index_job'
      AND column_name = 'ai_job_id' AND data_type = 'varchar'
  )
  + (
    SELECT CASE WHEN COUNT(*) = 1 THEN 0 ELSE 1 END
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'inspection_run'
      AND column_name = 'applied_threshold'
      AND column_type = 'decimal(8,4)' AND is_nullable = 'NO'
  )
  + (
    SELECT CASE WHEN COUNT(*) = 1 THEN 0 ELSE 1 END
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'user_threshold'
      AND column_name = 'anomaly_threshold'
      AND column_type = 'decimal(8,4)' AND is_nullable = 'NO'
  )
  + (
    SELECT CASE WHEN COUNT(*) = 1 THEN 0 ELSE 1 END
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'user_threshold'
      AND column_name = 'min_allowed'
      AND column_type = 'decimal(8,4)' AND is_nullable = 'NO'
  )
  + (
    SELECT CASE WHEN COUNT(*) = 1 THEN 0 ELSE 1 END
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'user_threshold'
      AND column_name = 'max_allowed'
      AND column_type = 'decimal(8,4)' AND is_nullable = 'NO'
  )
  + (
    SELECT CASE WHEN COUNT(*) = 1 THEN 0 ELSE 1 END
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'user_threshold_history'
      AND column_name = 'old_anomaly_threshold'
      AND column_type = 'decimal(8,4)' AND is_nullable = 'NO'
  )
  + (
    SELECT CASE WHEN COUNT(*) = 1 THEN 0 ELSE 1 END
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'user_threshold_history'
      AND column_name = 'new_anomaly_threshold'
      AND column_type = 'decimal(8,4)' AND is_nullable = 'NO'
  )
  + (
    SELECT CASE WHEN COUNT(*) = 1 THEN 0 ELSE 1 END
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'model_version'
      AND column_name = 'threshold_default'
      AND column_type = 'decimal(8,4)' AND is_nullable = 'YES'
  )
  + (
    SELECT CASE WHEN COUNT(*) = 1 THEN 0 ELSE 1 END
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'model_version'
      AND column_name = 'deleted_at' AND data_type = 'timestamp'
  )
  + (
    SELECT CASE WHEN COUNT(*) = 1 THEN 0 ELSE 1 END
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'model_deployment'
      AND column_name = 'deleted_at' AND data_type = 'timestamp'
  )
  + (
    SELECT CASE WHEN COUNT(*) = 1 THEN 0 ELSE 1 END
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'inspection_input'
      AND column_name = 'quality_gate_enabled'
  )
  + (
    SELECT CASE WHEN COUNT(*) = 1 THEN 0 ELSE 1 END
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'inspection_result'
      AND column_name = 'score'
      AND column_type = 'decimal(8,4)' AND is_nullable = 'YES'
  )
  + (
    SELECT CASE WHEN COUNT(*) = 1 THEN 0 ELSE 1 END
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'inspection_result'
      AND column_name = 'confidence'
      AND column_type = 'decimal(6,4)' AND is_nullable = 'YES'
  )
  + (
    SELECT CASE WHEN COUNT(*) = 1 THEN 0 ELSE 1 END
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'chat_message'
      AND column_name = 'answer_status'
  )
  + (
    SELECT CASE WHEN COUNT(*) = 1 THEN 0 ELSE 1 END
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'chat_message'
      AND column_name = 'error_code'
  )
  + (
    SELECT CASE WHEN COUNT(*) = 1 THEN 0 ELSE 1 END
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'chat_source'
      AND column_name = 'document_title'
  )
AS failed_checks;
