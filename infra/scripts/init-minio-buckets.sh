#!/bin/sh
# MinIO 필수 버킷: models, documents, inspection-artifacts, reports
# 실행: cd infra && docker compose --env-file .env exec -i -T minio sh < scripts/init-minio-buckets.sh
# prod: docker compose -f docker-compose.prod.yml --env-file .env.prod exec -i -T minio sh < scripts/init-minio-buckets.sh
set -e
mc alias set s http://127.0.0.1:9000 "$MINIO_ROOT_USER" "$MINIO_ROOT_PASSWORD" >/dev/null
for b in models documents inspection-artifacts reports; do
  mc mb "s/$b" --ignore-existing 2>/dev/null || true
  echo "bucket ok: $b"
done
echo "--- mc ls (root) ---"
mc ls s
