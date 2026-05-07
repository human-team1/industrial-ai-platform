"""Spring E2E inspection upload test.

1. JWT 생성 + Redis 세션 등록
2. /api/v1/inspections/upload 호출 (OBJECT/SPEED, deployment_id=10)
3. 결과 폴링
4. DB 검증 SQL 출력
"""
from __future__ import annotations

import io
import time
import uuid

import jwt
import redis
import requests
import numpy as np
from PIL import Image

# ── 설정 ────────────────────────────────────────────────────
SPRING_BASE = "http://localhost:8080"
SECRET = "local-dev-jwt-secret-key-must-be-at-least-32-characters-long"
USER_ID = 91003
SESSION_ID = str(uuid.uuid4())

# 4개 배포 ID (DB 확인한 값)
DEPLOYMENTS = {
    "OBJECT/SPEED": 10,
    "OBJECT/PERFORMANCE": 11,
    "TEXTURE/SPEED": 12,
    "TEXTURE/PERFORMANCE": 13,
}

# ── JWT 발급 ─────────────────────────────────────────────────
now = int(time.time())
TOKEN = jwt.encode(
    {
        "sub": str(USER_ID),
        "role": "ROLE_COMPANY_WORKER",
        "orgId": 9001,
        "sid": SESSION_ID,
        "iat": now,
        "exp": now + 7200,
    },
    SECRET,
    algorithm="HS256",
)

# ── Redis 세션 등록 ──────────────────────────────────────────
r = redis.Redis(host="localhost", port=6379, decode_responses=True)
r.setex(f"session:{USER_ID}:{SESSION_ID}", 7200, "1")
r.setex(f"active:session:{SESSION_ID}", 7200, str(USER_ID))
r.sadd("active:sessions", SESSION_ID)
print(f"[AUTH] Session registered: {SESSION_ID[:16]}...")

AUTH_HEADERS = {"Authorization": f"Bearer {TOKEN}"}

# ── Auth 확인 ────────────────────────────────────────────────
resp = requests.get(f"{SPRING_BASE}/api/v1/auth/me", headers=AUTH_HEADERS)
if resp.status_code != 200:
    print(f"[FAIL] Auth /me: {resp.status_code} {resp.text[:200]}")
    exit(1)
data_me = resp.json().get("data", {})
print(f"[OK] Auth as: {data_me.get('email', data_me)}")

# ── 테스트 이미지 생성 ────────────────────────────────────────
def make_test_image() -> tuple[bytes, str]:
    arr = (np.random.rand(200, 200, 3) * 255).astype(np.uint8)
    img = Image.fromarray(arr)
    buf = io.BytesIO()
    img.save(buf, format="PNG")
    return buf.getvalue(), "test_image.png"


def wait_for_result(inspection_id: int, max_wait: int = 120) -> dict | None:
    url = f"{SPRING_BASE}/api/v1/inspections/{inspection_id}/result"
    for i in range(max_wait):
        time.sleep(1)
        r = requests.get(url, headers=AUTH_HEADERS)
        if r.status_code == 200:
            d = r.json()
            status = d.get("data", {}).get("resultStatus")
            if status in ("SUCCESS", "REVIEW_REQUIRED", "FAILED"):
                return d.get("data")
        elif r.status_code not in (404, 202):
            print(f"  [WARN] poll {r.status_code}")
    return None


# ── E2E 실행 (OBJECT/SPEED 먼저, 나머지는 요약) ────────────────
print("\n" + "=" * 60)
print("E2E TEST: OBJECT/SPEED (deployment_id=10)")
print("=" * 60)

img_bytes, img_name = make_test_image()
upload_resp = requests.post(
    f"{SPRING_BASE}/api/v1/inspections/upload",
    headers=AUTH_HEADERS,
    files={"file": (img_name, img_bytes, "image/png")},
    data={"deploymentId": 10},
)
print(f"POST /inspections/upload → {upload_resp.status_code}")
if upload_resp.status_code not in (200, 201, 202):
    print(f"[FAIL] {upload_resp.text[:400]}")
    exit(1)

upload_data = upload_resp.json().get("data", {})
inspection_id = upload_data.get("inspectionId")
print(f"  inspectionId: {inspection_id}")

if inspection_id:
    print(f"  Waiting for result (up to 120s)...")
    result = wait_for_result(inspection_id)
    if result:
        print(f"  [RESULT]")
        print(f"    score:              {result.get('score')}")
        print(f"    decisionCode:       {result.get('decisionCode')}")
        print(f"    finalDecisionCode:  {result.get('finalDecisionCode')}")
        print(f"    resultStatus:       {result.get('resultStatus')}")
        print(f"    appliedThreshold:   {result.get('appliedThreshold')}")
        print(f"    thresholdSource:    {result.get('thresholdSource')}")
        print(f"    modelVersionId:     {result.get('modelVersionId')}")
    else:
        print("  [WARN] Result not ready within 120s")

print("\n[DB] 검증 SQL (수동 실행용):")
print("""
docker compose exec -T mariadb mariadb -uroot -pchange_me_root_password industrial_ai -e "
SELECT r.result_id, r.score, r.decision_code, r.final_decision_code,
       r.result_status, mv.model_category, mv.model_profile,
       mv.input_size, mv.threshold_default, ir.applied_threshold
FROM inspection_result r
JOIN inspection_run ir ON ir.inspection_id = r.inspection_id
JOIN model_version mv ON mv.model_version_id = r.model_version_id
WHERE r.inspection_id = {inspection_id};"
""")
