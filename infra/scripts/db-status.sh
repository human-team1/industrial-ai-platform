#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
INFRA_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$INFRA_DIR"

set -a
source .env
set +a

run_sql() {
  local sql="$1"
  docker compose --env-file .env exec -T mariadb sh -lc "mariadb -u${MARIADB_USER} -p${MARIADB_PASSWORD} ${MARIADB_DATABASE} -e \"$sql\""
}

echo "== DB 접속 확인 =="
run_sql "SELECT NOW() AS server_time;"

echo
echo "== 주요 테이블 존재 여부 =="
run_sql "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name IN ('organization','users','document','document_version','inspection_run','inspection_result','chat_conversation','chat_message','schema_migration') ORDER BY table_name;"

echo
echo "== migration 적용 목록 =="
run_sql "SELECT filename, applied_at FROM schema_migration ORDER BY applied_at, filename;" || echo "schema_migration 테이블이 아직 없습니다."

echo
echo "== 샘플 데이터 건수 =="
run_sql "SELECT (SELECT COUNT(*) FROM organization) AS organizations, (SELECT COUNT(*) FROM users) AS users, (SELECT COUNT(*) FROM document) AS documents, (SELECT COUNT(*) FROM inspection_result) AS results, (SELECT COUNT(*) FROM chat_conversation) AS chat_conversations;"

echo
echo "== role 분포 =="
run_sql "SELECT role, COUNT(*) AS cnt FROM users GROUP BY role ORDER BY role;"

echo
echo "== 주요 컬럼 존재 여부 =="
run_sql "SELECT table_name, column_name FROM information_schema.columns WHERE table_schema = DATABASE() AND ((table_name='inspection_run' AND column_name IN ('payload_fingerprint','idempotency_key')) OR (table_name='document_version' AND column_name IN ('indexing_status','index_error_message')) OR (table_name='chat_message' AND column_name IN ('message_status','answer_status'))) ORDER BY table_name, column_name;"
