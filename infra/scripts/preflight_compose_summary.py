"""docker compose ... config 출력을 JSON으로 받아 요약합니다. 값은 비밀 가능성 있으므로 허용된 키만 형태 검증합니다."""

from __future__ import annotations

import json
import subprocess
from pathlib import Path


def main() -> int:
    infra = Path(__file__).resolve().parent.parent
    r = subprocess.run(
        [
            "docker",
            "compose",
            "-f",
            "docker-compose.prod.yml",
            "--env-file",
            ".env.prod",
            "config",
            "--format",
            "json",
        ],
        cwd=infra,
        capture_output=True,
        text=True,
        encoding="utf-8",
    )
    print("compose_exit", r.returncode)
    if r.returncode != 0:
        err = (r.stderr or "").splitlines()[0][:200] if r.stderr else ""
        print("stderr_head", err.replace("\r", ""))
        return r.returncode

    doc = json.loads(r.stdout or "{}")
    services = doc.get("services") or {}
    print("SERVICES", ",".join(sorted(services)))

    for name in sorted(services):
        ports = services[name].get("ports")
        if not ports:
            continue
        published: list[str] = []
        for item in ports:
            if isinstance(item, dict):
                published.append(str(item.get("published", "?")))
            elif isinstance(item, str):
                published.append(item)
        print("HOST_PORTS", name, published)

    fe = services.get("frontend") or {}
    build = fe.get("build") or {}
    args = build.get("args") or {}
    if isinstance(args, dict):
        print("FRONTEND_BUILD_ARG_KEYS", sorted(args.keys()))
        print(
            "VITE_API_BASE_URL_IN_BUILD_ARGS",
            "VITE_API_BASE_URL" in {k.upper() for k in args},
        )

    spring = services.get("spring") or {}
    env_raw = spring.get("environment")

    env: dict[str, str]
    if isinstance(env_raw, dict):
        env = {str(k): str(v) for k, v in env_raw.items()}
    elif isinstance(env_raw, list):
        env = {}
        for row in env_raw:
            if isinstance(row, str) and "=" in row:
                key, _, val = row.partition("=")
                env[key] = val
    else:
        env = {}

    def val(key: str) -> str | None:
        for k, v in env.items():
            if k.upper() == key.upper():
                return str(v)
        return None

    avs = ("AI_SERVER_BASE_URL", "DB_HOST", "REDIS_HOST", "MINIO_ENDPOINT", "CHROMA_HOST", "CHROMA_URL")
    for key in avs:
        v = val(key)
        if v is None:
            print(key, "SPRING_ABSENT")
            continue
        sensitive = ("PASSWORD", "SECRET", "JWT")
        kup = key.upper()
        if kup.endswith(sensitive) or "TOKEN" in kup:
            print(key, "PRESENT_SUPPRESSED_SHAPE_CHECK")
            continue
        if key == "AI_SERVER_BASE_URL":
            ok = v.rstrip("/") == "http://ai-server:8001"
            print(key, "OK" if ok else "CHECK")
        elif key == "DB_HOST" and v == "mariadb":
            print(key, "OK")
        elif key == "REDIS_HOST" and v == "redis":
            print(key, "OK")
        elif key == "MINIO_ENDPOINT":
            ok = v.startswith("http://minio:") and ":9000" in v
            print(key, "OK" if ok else "CHECK")
        elif key == "CHROMA_HOST" and v == "chroma":
            print(key, "OK")
        elif key == "CHROMA_URL":
            ok = v.rstrip("/") == "http://chroma:8000"
            print(key, "OK(SPRING_EXTRA)" if ok else "SPRING_OPTIONAL_SPRING_IGNORES_THIS_KEY_COMMON")
        else:
            print(key, "CHECK")

    ai_raw = services.get("ai-server", {}).get("environment")
    if isinstance(ai_raw, dict):
        ai_env = {str(k): str(v) for k, v in ai_raw.items()}
    elif isinstance(ai_raw, list):
        ai_env = {}
        for row in ai_raw:
            if isinstance(row, str) and "=" in row:
                kk, _, vv = row.partition("=")
                ai_env[kk] = vv
    else:
        ai_env = {}

    curl = ai_env.get("CHROMA_URL") or ai_env.get("chroma_url")
    host = ai_env.get("CHROMA_HOST") or ai_env.get("chroma_host")
    curl_ok = str(curl or "").startswith("http://chroma:")
    host_ok = str(host or "") == "chroma"
    print("FASTAPI_CHROMA", "HOST_" + ("OK" if host_ok else "MISSING"), "URL_" + ("OK" if curl_ok else "MISSING"))

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
