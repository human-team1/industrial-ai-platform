ALTER TABLE document_index_job
  ADD COLUMN IF NOT EXISTS ai_job_id VARCHAR(100) NULL AFTER document_version_id;

CREATE INDEX IF NOT EXISTS idx_document_index_job_ai_job_id
  ON document_index_job(ai_job_id);
