"""FastAPI /infer-image 응답 필드 검증 스크립트."""
import io
import json
import numpy as np
import requests
from PIL import Image

img_arr = (np.random.rand(200, 200, 3) * 255).astype(np.uint8)
img = Image.fromarray(img_arr)
buf = io.BytesIO()
img.save(buf, format="PNG")
buf.seek(0)

resp = requests.post(
    "http://localhost:8001/ai/v1/internal/vision/infer-image",
    files={"image": ("test.png", buf, "image/png")},
    data={
        "modelVersionId": "94010",
        "modelCategory": "OBJECT",
        "modelProfile": "SPEED",
        "framework": "PYTORCH",
        "inputSize": "224x224",
        "ckptFileKey": "models/base/speed-object/model.ckpt",
        "configFileKey": "models/generated/org-9001/organization/speed-object/job-5b80efc9-b3d5-4092-8993-a5db1c9d5f22/config.json",
        "memoryBankFileKey": "models/generated/org-9001/organization/speed-object/job-5b80efc9-b3d5-4092-8993-a5db1c9d5f22/memory_bank.pt",
        "anomalyThreshold": "36.8688",
        "lowConfidenceThreshold": "0.55",
        "roiMode": "FULL_FRAME",
        "roiCoordinateType": "NORMALIZED",
        "qualityGateEnabled": "true",
    },
)

print(f"Status: {resp.status_code}")
if resp.status_code == 200:
    data = resp.json().get("data", {})
    for key in ["score", "scoreType", "scoreSource", "imageThreshold",
                "pixelThreshold", "decisionCode"]:
        print(f"  {key}: {data.get(key)}")
    print(f"  metadata:")
    for k, v in (data.get("metadata") or {}).items():
        print(f"    {k}: {v}")

    score = data.get("score", 0)
    img_thr = data.get("imageThreshold", 0)
    expected_decision = "DEFECT" if score >= img_thr else "NORMAL"
    actual_decision = data.get("decisionCode")
    ok = expected_decision == actual_decision
    print(f"\n  [{'PASS' if ok else 'FAIL'}] decision: score({score}) >= threshold({img_thr}) → {expected_decision}, got {actual_decision}")
else:
    print(resp.text[:500])
