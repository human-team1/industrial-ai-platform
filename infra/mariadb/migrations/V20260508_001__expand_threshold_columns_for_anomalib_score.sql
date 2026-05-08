ALTER TABLE inspection_run
    MODIFY COLUMN applied_threshold DECIMAL(8,4) NOT NULL;

ALTER TABLE user_threshold
    MODIFY COLUMN anomaly_threshold DECIMAL(8,4) NOT NULL,
    MODIFY COLUMN min_allowed DECIMAL(8,4) NOT NULL,
    MODIFY COLUMN max_allowed DECIMAL(8,4) NOT NULL;

ALTER TABLE user_threshold_history
    MODIFY COLUMN old_anomaly_threshold DECIMAL(8,4) NOT NULL,
    MODIFY COLUMN new_anomaly_threshold DECIMAL(8,4) NOT NULL;
