CREATE INDEX IF NOT EXISTS idx_file_storage_object
  ON file(storage_type, bucket_name, object_key);

CREATE INDEX IF NOT EXISTS idx_file_created_by
  ON file(created_by, created_at);

CREATE INDEX IF NOT EXISTS idx_inspection_input_file
  ON inspection_input(file_id);

CREATE INDEX IF NOT EXISTS idx_document_version_status_created
  ON document_version(indexing_status, created_at);

CREATE INDEX IF NOT EXISTS idx_document_index_job_status
  ON document_index_job(job_status, document_version_id);

CREATE INDEX IF NOT EXISTS idx_async_job_type_status_created
  ON async_job(job_type, job_status, created_at);
