#!/usr/bin/env bash
set -euo pipefail

FORCE="${1:-}"
if [[ "$FORCE" != "--force" ]]; then
  echo "[db-reset] WARNING: destructive 작업입니다. 실행하려면 --force 옵션을 전달하세요."
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
INFRA_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$INFRA_DIR"

set -a
source .env
set +a

docker compose --env-file .env ps -q mariadb >/dev/null || {
  echo "mariadb 컨테이너가 실행 중이 아닙니다."
  exit 1
}

echo "[db-reset] database 초기화 시작: ${MARIADB_DATABASE}"
docker compose --env-file .env exec -T mariadb sh -lc "mariadb -uroot -p${MARIADB_ROOT_PASSWORD} -e \"DROP DATABASE IF EXISTS ${MARIADB_DATABASE}; CREATE DATABASE ${MARIADB_DATABASE} DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;\""

echo "[db-reset] init SQL 적용"
docker compose --env-file .env exec -T mariadb sh -lc "mariadb -uroot -p${MARIADB_ROOT_PASSWORD} ${MARIADB_DATABASE}" < "$INFRA_DIR/mariadb/init/industrial-ai-platform.sql"

echo "[db-reset] migration 적용"
"$SCRIPT_DIR/db-migrate.sh"

echo "[db-reset] seed 적용"
"$SCRIPT_DIR/db-seed.sh"

echo "[db-reset] 상태 확인"
"$SCRIPT_DIR/db-status.sh"
