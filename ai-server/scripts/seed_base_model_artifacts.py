from __future__ import annotations

import sys
from dataclasses import dataclass
from pathlib import Path
from urllib.parse import urlparse

import requests
import urllib3
from minio import Minio


CONNECT_TIMEOUT_SECONDS = 3.0
READ_TIMEOUT_SECONDS = 5.0
SCRIPT_DIR = Path(__file__).resolve().parent
AI_SERVER_DIR = SCRIPT_DIR.parent
MODEL_DIR = AI_SERVER_DIR / "config" / "model"
sys.path.insert(0, str(AI_SERVER_DIR))

from config.settings import get_settings


@dataclass(frozen=True)
class BaseArtifact:
    profile_key: str
    local_name: str
    object_key: str
    content_type: str


BASE_ARTIFACTS: tuple[BaseArtifact, ...] = (
    BaseArtifact(
        "speed-object",
        "obj_speed.ckpt",
        "models/base/speed-object/model.ckpt",
        "application/octet-stream",
    ),
    BaseArtifact(
        "speed-object",
        "object_speed_wrn50_layer2_224_full_mb10000.json",
        "models/base/speed-object/config.json",
        "application/json",
    ),
    BaseArtifact(
        "performance-object",
        "obj_perf.ckpt",
        "models/base/performance-object/model.ckpt",
        "application/octet-stream",
    ),
    BaseArtifact(
        "performance-object",
        "object_perf_dinov2_base_3layers_336_50shot_mb2000.json",
        "models/base/performance-object/config.json",
        "application/json",
    ),
    BaseArtifact(
        "speed-texture",
        "texture_speed.ckpt",
        "models/base/speed-texture/model.ckpt",
        "application/octet-stream",
    ),
    BaseArtifact(
        "speed-texture",
        "texture_speed_wrn50_layer2_256_full_mb5000.json",
        "models/base/speed-texture/config.json",
        "application/json",
    ),
    BaseArtifact(
        "performance-texture",
        "texture_perf.ckpt",
        "models/base/performance-texture/model.ckpt",
        "application/octet-stream",
    ),
    BaseArtifact(
        "performance-texture",
        "texture_perf_dinov2_base_3layers_448_50shot_mb2000.json",
        "models/base/performance-texture/config.json",
        "application/json",
    ),
)


def main() -> int:
    settings = get_settings()
    bucket = settings.minio_bucket_models

    try:
        print_step("local base artifact check")
        local_paths = validate_local_files()

        print_step("MinIO health")
        health_check(settings.minio_endpoint)

        print_step("MinIO client")
        client = create_minio_client(settings)

        print_step("bucket exists")
        ensure_bucket(client, bucket)

        print_step("base ckpt/config upload")
        uploaded_keys: list[str] = []
        for artifact in BASE_ARTIFACTS:
            local_path = local_paths[artifact.object_key]
            upload_file(client, bucket, artifact, local_path)
            uploaded_keys.append(artifact.object_key)

        print_step("uploaded object existence check")
        for object_key in uploaded_keys:
            client.stat_object(bucket, object_key)
            print(f"exists: {bucket}/{object_key}")

        print_step("seed completed")
        for object_key in uploaded_keys:
            print(object_key)
        return 0
    except Exception as exc:
        print(f"seed failed: {type(exc).__name__}: {exc}", file=sys.stderr)
        return 1


def validate_local_files() -> dict[str, Path]:
    local_paths: dict[str, Path] = {}
    missing: list[str] = []
    for artifact in BASE_ARTIFACTS:
        local_path = MODEL_DIR / artifact.local_name
        if not local_path.is_file():
            missing.append(str(local_path))
            continue
        assert_not_lfs_pointer(local_path)
        local_paths[artifact.object_key] = local_path
        print(f"ready: {artifact.profile_key} {local_path.name} ({local_path.stat().st_size} bytes)")

    if missing:
        raise FileNotFoundError("missing local base artifact(s): " + ", ".join(missing))
    return local_paths


def assert_not_lfs_pointer(path: Path) -> None:
    try:
        head = path.read_bytes()[:128]
    except OSError:
        return
    if b"version https://git-lfs.github.com/spec/v1" in head:
        raise RuntimeError(
            f"Git LFS file is not pulled: {path}\nRun:\n  git lfs install\n  git lfs pull"
        )


def health_check(endpoint: str) -> None:
    url = endpoint.rstrip("/") + "/minio/health/live"
    response = requests.get(url, timeout=(CONNECT_TIMEOUT_SECONDS, READ_TIMEOUT_SECONDS))
    response.raise_for_status()
    print(f"health ok: {url}")


def create_minio_client(settings) -> Minio:
    parsed = urlparse(settings.minio_endpoint)
    endpoint = parsed.netloc if parsed.scheme else settings.minio_endpoint
    return Minio(
        endpoint,
        access_key=settings.minio_access_key,
        secret_key=settings.minio_secret_key,
        secure=settings.minio_secure,
        http_client=urllib3.PoolManager(
            timeout=urllib3.Timeout(connect=CONNECT_TIMEOUT_SECONDS, read=READ_TIMEOUT_SECONDS),
            retries=False,
        ),
    )


def ensure_bucket(client: Minio, bucket: str) -> None:
    if client.bucket_exists(bucket):
        print(f"bucket ready: {bucket}")
        return
    client.make_bucket(bucket)
    print(f"created bucket: {bucket}")


def upload_file(client: Minio, bucket: str, artifact: BaseArtifact, local_path: Path) -> None:
    client.fput_object(
        bucket,
        artifact.object_key,
        str(local_path),
        content_type=artifact.content_type,
    )
    print(f"uploaded: {local_path.name} -> {bucket}/{artifact.object_key}")


def print_step(name: str) -> None:
    print(f"[seed-base-model-artifacts] {name}")


if __name__ == "__main__":
    raise SystemExit(main())
