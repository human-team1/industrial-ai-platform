-- Idempotent migration: add query performance indexes
CREATE INDEX IF NOT EXISTS idx_document_status ON document(current_status);
CREATE INDEX IF NOT EXISTS idx_document_version_status ON document_version(indexing_status);
CREATE INDEX IF NOT EXISTS idx_chat_message_status ON chat_message(message_status, answer_status);
