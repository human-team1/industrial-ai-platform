#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
INFRA_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
SEED_DIR="${1:-$INFRA_DIR/mariadb/seed}"

cd "$INFRA_DIR"
set -a
source .env
set +a

seed_order=(
  "seed-sample-organizations.sql"
  "seed-sample-users.sql"
  "seed-sample-documents.sql"
  "seed-sample-inspections.sql"
  "seed-sample-operation-admin.sql"
)

for seed in "${seed_order[@]}"; do
  path="$SEED_DIR/$seed"
  if [[ ! -f "$path" ]]; then
    echo "[db-seed] 파일 없음, 건너뜀: $seed"
    continue
  fi
  echo "[db-seed] APPLY $seed"
  docker compose --env-file .env exec -T mariadb sh -lc "mariadb -u${MARIADB_USER} -p${MARIADB_PASSWORD} ${MARIADB_DATABASE}" < "$path"
done

echo "[db-seed] 완료"
