#!/bin/sh
mc alias set local http://127.0.0.1:9000 "$MINIO_ROOT_USER" "$MINIO_ROOT_PASSWORD" >/dev/null
echo "=== buckets (mc ls local) ==="
mc ls local || true
echo "=== seed objects ==="
for p in models/seed/object-speed/model.ckpt models/seed/object-speed/config.json models/seed/object-speed/memory_bank.npy models/seed/object-perf/model.ckpt models/seed/object-perf/config.json models/seed/object-perf/memory_bank.npy models/seed/texture-speed/model.ckpt models/seed/texture-speed/config.json models/seed/texture-speed/memory_bank.npy models/seed/texture-perf/model.ckpt models/seed/texture-perf/config.json models/seed/texture-perf/memory_bank.npy; do
  if mc stat "local/$p" >/dev/null 2>&1; then echo "OK $p"; else echo "MISSING $p"; fi
done