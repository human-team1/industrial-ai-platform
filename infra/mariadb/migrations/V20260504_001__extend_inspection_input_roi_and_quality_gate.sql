ALTER TABLE inspection_input
  ADD COLUMN IF NOT EXISTS roi_mode             VARCHAR(20)  NULL AFTER frame_count,
  ADD COLUMN IF NOT EXISTS roi_coordinate_type  VARCHAR(20)  NULL AFTER roi_mode,
  ADD COLUMN IF NOT EXISTS roi_x                DECIMAL(8,6) NULL AFTER roi_coordinate_type,
  ADD COLUMN IF NOT EXISTS roi_y                DECIMAL(8,6) NULL AFTER roi_x,
  ADD COLUMN IF NOT EXISTS roi_width            DECIMAL(8,6) NULL AFTER roi_y,
  ADD COLUMN IF NOT EXISTS roi_height           DECIMAL(8,6) NULL AFTER roi_width,
  ADD COLUMN IF NOT EXISTS sampling_fps         DECIMAL(5,2) NULL AFTER roi_height,
  ADD COLUMN IF NOT EXISTS max_frames           INT          NULL AFTER sampling_fps,
  ADD COLUMN IF NOT EXISTS quality_gate_enabled TINYINT(1)   NULL AFTER max_frames;
