-- (레거시) 테이블 존재만 확인. 강화 검증은 db_verify_schema.sql / db_verify_indexes.sql / db_verify_seed.sql
SELECT COUNT(*) AS table_cnt
FROM information_schema.tables
WHERE table_schema = 'industrial_ai';

SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'industrial_ai'
  AND table_name IN (
    'async_job', 'operation_log', 'system_status_snapshot', 'system_component_status',
    'operation_policy', 'users', 'organization', 'file', 'model', 'model_version',
    'model_artifact', 'model_deployment', 'inspection_run', 'inspection_result',
    'document', 'document_version', 'document_index_job', 'chat_conversation', 'chat_message'
  )
ORDER BY table_name;
