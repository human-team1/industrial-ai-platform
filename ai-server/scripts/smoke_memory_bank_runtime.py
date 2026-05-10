from __future__ import annotations

import sys
import argparse
from io import BytesIO
from pathlib import Path
from urllib.parse import urlparse

import requests
import urllib3
from minio import Minio
from PIL import Image

SCRIPT_DIR = Path(__file__).resolve().parent
AI_SERVER_DIR = SCRIPT_DIR.parent
sys.path.insert(0, str(AI_SERVER_DIR))

from config.settings import get_settings


CONNECT_TIMEOUT_SECONDS = 3.0
READ_TIMEOUT_SECONDS = 5.0
FASTAPI_TIMEOUT_SECONDS = 900.0
REQUEST_ID = "memory-bank-runtime-smoke"


def main() -> int:
    args = parse_args()
    settings = get_settings()
    fastapi_base_url = f"http://localhost:{settings.app_port}"
    client = create_minio_client(settings)
    bucket = settings.minio_bucket_models

    try:
        step("MinIO health")
        health_check(settings.minio_endpoint)

        step("bucket exists")
        if not client.bucket_exists(bucket):
            raise RuntimeError(f"bucket not found: {bucket}")
        print(f"bucket ready: {bucket}")

        step("base ckpt/config object check")
        ckpt_key, config_key = base_artifact_keys(args.model_category, args.model_profile)
        client.stat_object(bucket, ckpt_key)
        client.stat_object(bucket, config_key)
        print(f"base ckpt ready: {bucket}/{ckpt_key}")
        print(f"base config ready: {bucket}/{config_key}")

        step("tiny normal images upload")
        normal_keys = []
        for index in range(10):
            key = f"models/tmp/smoke/{REQUEST_ID}/normal-{index:02d}.png"
            put_bytes(client, bucket, key, make_png(index), "image/png")
            normal_keys.append(key)
        print(f"uploaded normal images: {len(normal_keys)}")

        step("FastAPI memory-bank call")
        output_prefix = f"models/generated/smoke/{REQUEST_ID}-{args.model_category.lower()}-{args.model_profile.lower()}"
        response = requests.post(
            fastapi_base_url.rstrip("/") + "/ai/v1/internal/models/memory-bank",
            headers={"X-Request-Id": REQUEST_ID},
            json={
                "modelCategory": args.model_category,
                "modelProfile": args.model_profile,
                "ckptFileKey": ckpt_key,
                "configFileKey": config_key,
                "normalImageFileKeys": normal_keys,
                "outputPrefix": output_prefix,
            },
            timeout=FASTAPI_TIMEOUT_SECONDS,
        )
        print(f"FastAPI status: {response.status_code}")
        print_response(response)
        if not response.ok:
            return 1
        data = response.json()["data"]

        step("memory_bank.pt upload check")
        client.stat_object(bucket, data["memoryBankFileKey"])
        print(f"memory_bank exists: {bucket}/{data['memoryBankFileKey']}")

        step("config snapshot upload check")
        client.stat_object(bucket, data["configFileKey"])
        print(f"config snapshot exists: {bucket}/{data['configFileKey']}")
        return 0
    except Exception as exc:
        print(f"smoke failed: {type(exc).__name__}: {exc}", file=sys.stderr)
        return 1


def print_response(response: requests.Response) -> None:
    body = response.text
    print(body[:4000])
    if response.ok:
        return
    try:
        problem = response.json()
    except ValueError:
        return
    print(f"errorCode={problem.get('errorCode')}")
    print(f"detail={problem.get('detail')}")
    print(f"requestId={problem.get('requestId')}")


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


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("--model-category", "--category", dest="model_category", default="OBJECT", choices=["OBJECT", "TEXTURE"])
    parser.add_argument("--model-profile", "--profile", dest="model_profile", default="SPEED", choices=["SPEED", "PERFORMANCE"])
    return parser.parse_args()


def base_artifact_keys(category: str, profile: str) -> tuple[str, str]:
    mapping = {
        ("OBJECT", "SPEED"): ("models/base/speed-object/model.ckpt", "models/base/speed-object/config.json"),
        ("OBJECT", "PERFORMANCE"): ("models/base/performance-object/model.ckpt", "models/base/performance-object/config.json"),
        ("TEXTURE", "SPEED"): ("models/base/speed-texture/model.ckpt", "models/base/speed-texture/config.json"),
        ("TEXTURE", "PERFORMANCE"): ("models/base/performance-texture/model.ckpt", "models/base/performance-texture/config.json"),
    }
    return mapping[(category, profile)]


def put_bytes(client: Minio, bucket: str, object_key: str, data: bytes, content_type: str) -> None:
    client.put_object(bucket, object_key, BytesIO(data), length=len(data), content_type=content_type)


def make_png(index: int) -> bytes:
    buffer = BytesIO()
    color = (index * 17 % 255, index * 29 % 255, index * 43 % 255)
    Image.new("RGB", (32, 32), color=color).save(buffer, format="PNG")
    return buffer.getvalue()


def step(name: str) -> None:
    print(f"[memory-bank-smoke] {name}")


if __name__ == "__main__":
    raise SystemExit(main())
