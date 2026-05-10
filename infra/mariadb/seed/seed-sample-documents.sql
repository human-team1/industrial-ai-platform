-- Sample document metadata and version records
INSERT INTO file (
  file_id, storage_type, bucket_name, object_key, file_name, file_ext, mime_type, file_size, created_by, created_at
)
VALUES
  (92101, 'MINIO', 'documents', 'samples/safety-manual.pdf', 'safety-manual.pdf', 'pdf', 'application/pdf', 204800, 91001, NOW())
ON DUPLICATE KEY UPDATE
  bucket_name = VALUES(bucket_name),
  object_key = VALUES(object_key),
  file_name = VALUES(file_name),
  file_ext = VALUES(file_ext),
  mime_type = VALUES(mime_type),
  file_size = VALUES(file_size);

INSERT INTO document (
  document_id, organization_id, owner_user_id, title, document_type, category, equipment_type, description, current_status, created_at, updated_at
)
VALUES
  (93101, 9001, 91001, '샘플 설비 안전 매뉴얼', 'MANUAL', '안전', '프레스', '로컬 검증용 샘플 문서', 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE
  owner_user_id = VALUES(owner_user_id),
  title = VALUES(title),
  document_type = VALUES(document_type),
  category = VALUES(category),
  equipment_type = VALUES(equipment_type),
  description = VALUES(description),
  current_status = VALUES(current_status),
  updated_at = NOW();

INSERT INTO document_tag (document_id, tag_name)
VALUES
  (93101, '안전'),
  (93101, '점검')
ON DUPLICATE KEY UPDATE
  tag_name = VALUES(tag_name);

INSERT INTO document_version (
  document_version_id, document_id, version_no, file_id, file_hash, indexing_status, indexed_chunk_count, indexed_at, created_at
)
VALUES
  (94101, 93101, 1, 92101, 'sample-hash-v1', 'COMPLETED', 12, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  file_id = VALUES(file_id),
  file_hash = VALUES(file_hash),
  indexing_status = VALUES(indexing_status),
  indexed_chunk_count = VALUES(indexed_chunk_count),
  indexed_at = VALUES(indexed_at);
