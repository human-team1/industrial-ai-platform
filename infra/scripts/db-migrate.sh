#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
INFRA_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
MIGRATIONS_DIR="${1:-$INFRA_DIR/mariadb/migrations}"

cd "$INFRA_DIR"

if [[ ! -f .env ]]; then
  echo "infra/.env 파일이 없습니다. 먼저 .env.example을 복사하세요."
  exit 1
fi

set -a
source .env
set +a

if [[ -z "${MARIADB_DATABASE:-}" || -z "${MARIADB_USER:-}" || -z "${MARIADB_PASSWORD:-}" ]]; then
  echo "MARIADB_DATABASE / MARIADB_USER / MARIADB_PASSWORD 확인이 필요합니다."
  exit 1
fi

docker compose --env-file .env ps -q mariadb >/dev/null || {
  echo "mariadb 컨테이너가 실행 중이 아닙니다."
  exit 1
}

docker compose --env-file .env exec -T mariadb sh -lc "mariadb -u${MARIADB_USER} -p${MARIADB_PASSWORD} ${MARIADB_DATABASE} -e \"CREATE TABLE IF NOT EXISTS schema_migration (migration_id BIGINT PRIMARY KEY AUTO_INCREMENT, filename VARCHAR(255) NOT NULL UNIQUE, applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);\""

shopt -s nullglob
files=("$MIGRATIONS_DIR"/*.sql)
if (( ${#files[@]} == 0 )); then
  echo "[db-migrate] 적용할 migration SQL이 없습니다."
  exit 0
fi

for file in "${files[@]}"; do
  filename="$(basename "$file")"
  applied="$(docker compose --env-file .env exec -T mariadb sh -lc "mariadb -N -u${MARIADB_USER} -p${MARIADB_PASSWORD} ${MARIADB_DATABASE} -e \"SELECT COUNT(*) FROM schema_migration WHERE filename='${filename}';\"")"
  if [[ "$applied" != "0" ]]; then
    echo "[db-migrate] SKIP $filename"
    continue
  fi

  echo "[db-migrate] APPLY $filename"
  docker compose --env-file .env exec -T mariadb sh -lc "mariadb -u${MARIADB_USER} -p${MARIADB_PASSWORD} ${MARIADB_DATABASE}" < "$file"
  docker compose --env-file .env exec -T mariadb sh -lc "mariadb -u${MARIADB_USER} -p${MARIADB_PASSWORD} ${MARIADB_DATABASE} -e \"INSERT INTO schema_migration(filename) VALUES('${filename}');\""
done

echo "[db-migrate] 완료"
