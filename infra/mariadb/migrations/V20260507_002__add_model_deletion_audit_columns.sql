ALTER TABLE model_deployment
    ADD COLUMN deleted_at TIMESTAMP NULL AFTER rollback_flag,
    ADD COLUMN deleted_by BIGINT NULL AFTER deleted_at,
    ADD COLUMN delete_reason TEXT NULL AFTER deleted_by;

ALTER TABLE model_version
    ADD COLUMN deleted_at TIMESTAMP NULL AFTER created_at,
    ADD COLUMN deleted_by BIGINT NULL AFTER deleted_at,
    ADD COLUMN delete_reason TEXT NULL AFTER deleted_by;

ALTER TABLE model_deployment
    ADD CONSTRAINT fk_model_deployment_deleted_by FOREIGN KEY (deleted_by) REFERENCES users(user_id);

ALTER TABLE model_version
    ADD CONSTRAINT fk_model_version_deleted_by FOREIGN KEY (deleted_by) REFERENCES users(user_id);

CREATE INDEX idx_model_deployment_deleted_at ON model_deployment (deleted_at);
CREATE INDEX idx_model_version_deleted_at ON model_version (deleted_at);
