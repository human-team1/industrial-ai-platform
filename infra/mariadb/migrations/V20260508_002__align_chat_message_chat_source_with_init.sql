-- Align chat_message / chat_source schema with init.sql for existing local/dev databases.
-- Latest init.sql and ERD include several columns and FKs that were never shipped as
-- explicit ALTER migrations, so teammates with pre-existing DBs are missing them and
-- chat INSERTs break. This migration is intentionally idempotent: ADD COLUMN IF NOT
-- EXISTS / CREATE INDEX IF NOT EXISTS are used, and FKs are added only when missing
-- via INFORMATION_SCHEMA checks. No destructive DDL.
--
-- Pre-flight orphan check (run manually before applying if you want to be safe — FK
-- creation will fail if any rows below are returned):
--
--   SELECT cs.chat_source_id, cs.document_id
--   FROM chat_source cs
--   LEFT JOIN document d ON cs.document_id = d.document_id
--   WHERE cs.document_id IS NOT NULL
--     AND d.document_id IS NULL;
--
--   SELECT cs.chat_source_id, cs.chunk_id
--   FROM chat_source cs
--   LEFT JOIN chunk c ON cs.chunk_id = c.chunk_id
--   WHERE cs.chunk_id IS NOT NULL
--     AND c.chunk_id IS NULL;

ALTER TABLE chat_message
  ADD COLUMN IF NOT EXISTS message_status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS' AFTER message_text,
  ADD COLUMN IF NOT EXISTS answer_status VARCHAR(50) NULL AFTER message_status,
  ADD COLUMN IF NOT EXISTS error_code VARCHAR(50) NULL AFTER answer_status,
  ADD COLUMN IF NOT EXISTS model_name VARCHAR(100) NULL AFTER error_code,
  ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP AFTER created_at;

ALTER TABLE chat_source
  ADD COLUMN IF NOT EXISTS document_id BIGINT NULL AFTER source_id,
  ADD COLUMN IF NOT EXISTS document_title VARCHAR(255) NULL AFTER document_id,
  ADD COLUMN IF NOT EXISTS document_type VARCHAR(50) NULL AFTER document_title,
  ADD COLUMN IF NOT EXISTS page_no INT NULL AFTER chunk_id,
  ADD COLUMN IF NOT EXISTS section VARCHAR(255) NULL AFTER page_no,
  ADD COLUMN IF NOT EXISTS score DECIMAL(5,4) NULL AFTER source_snippet,
  ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER score;

CREATE INDEX IF NOT EXISTS idx_chat_source_message ON chat_source(message_id);
CREATE INDEX IF NOT EXISTS idx_chat_source_document ON chat_source(document_id);
CREATE INDEX IF NOT EXISTS idx_chat_source_chunk ON chat_source(chunk_id);

SET @fk_chat_source_document :=
  IF (
    (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
      WHERE CONSTRAINT_SCHEMA = DATABASE()
        AND TABLE_NAME = 'chat_source'
        AND CONSTRAINT_NAME = 'fk_chat_source_document') = 0,
    'ALTER TABLE chat_source ADD CONSTRAINT fk_chat_source_document FOREIGN KEY (document_id) REFERENCES document(document_id)',
    'SELECT 1'
  );
PREPARE stmt FROM @fk_chat_source_document;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @fk_chat_source_chunk :=
  IF (
    (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
      WHERE CONSTRAINT_SCHEMA = DATABASE()
        AND TABLE_NAME = 'chat_source'
        AND CONSTRAINT_NAME = 'fk_chat_source_chunk') = 0,
    'ALTER TABLE chat_source ADD CONSTRAINT fk_chat_source_chunk FOREIGN KEY (chunk_id) REFERENCES chunk(chunk_id)',
    'SELECT 1'
  );
PREPARE stmt FROM @fk_chat_source_chunk;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
