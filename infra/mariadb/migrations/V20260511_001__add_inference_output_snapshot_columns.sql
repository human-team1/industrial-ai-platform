ALTER TABLE inspection_result
  ADD COLUMN IF NOT EXISTS image_path VARCHAR(500) NULL AFTER model_version_id,
  ADD COLUMN IF NOT EXISTS category_type VARCHAR(50) NULL AFTER image_path,
  ADD COLUMN IF NOT EXISTS category VARCHAR(100) NULL AFTER category_type,
  ADD COLUMN IF NOT EXISTS model_profile VARCHAR(20) NULL AFTER category,
  ADD COLUMN IF NOT EXISTS model_name VARCHAR(100) NULL AFTER model_profile,
  ADD COLUMN IF NOT EXISTS anomaly_score DECIMAL(12,6) NULL AFTER model_name,
  ADD COLUMN IF NOT EXISTS image_threshold DECIMAL(12,6) NULL AFTER anomaly_score,
  ADD COLUMN IF NOT EXISTS predicted_label VARCHAR(50) NULL AFTER image_threshold,
  ADD COLUMN IF NOT EXISTS heatmap_path VARCHAR(500) NULL AFTER predicted_label,
  ADD COLUMN IF NOT EXISTS pixel_threshold DECIMAL(12,6) NULL AFTER heatmap_path,
  ADD COLUMN IF NOT EXISTS inference_time BIGINT NULL AFTER pixel_threshold;
