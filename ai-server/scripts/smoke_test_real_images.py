"""FastAPI 4-model real image inference smoke test.

Uses real-ish images from scipy/PIL to test all 4 model profiles.
Images are loaded from local .ckpt files without MinIO/Spring.
"""
from __future__ import annotations

import math
import sys
import os

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

import io
import numpy as np
import torch
from PIL import Image

from infrastructure.vision.anomalib_patchcore_loader import load_patchcore_ckpt

IMAGENET_MEAN = np.array([0.485, 0.456, 0.406], dtype=np.float32)
IMAGENET_STD = np.array([0.229, 0.224, 0.225], dtype=np.float32)

CASES = [
    {
        "name": "OBJECT / SPEED",
        "ckpt": "config/model/obj_speed.ckpt",
        "image_threshold": 36.8688,
        "pixel_threshold": 28.4368,
        "input_size": 224,
    },
    {
        "name": "TEXTURE / SPEED",
        "ckpt": "config/model/texture_speed.ckpt",
        "image_threshold": 40.0642,
        "pixel_threshold": 36.5779,
        "input_size": 256,
    },
    {
        "name": "OBJECT / PERFORMANCE",
        "ckpt": "config/model/obj_perf.ckpt",
        "image_threshold": 7.4762,
        "pixel_threshold": 6.6040,
        "input_size": 336,
    },
    {
        "name": "TEXTURE / PERFORMANCE",
        "ckpt": "config/model/texture_perf.ckpt",
        "image_threshold": 22.2103,
        "pixel_threshold": 25.8270,
        "input_size": 448,
    },
]


def make_normal_image(size: int) -> np.ndarray:
    """Simulate a 'normal' flat texture: smooth gradient."""
    x = np.linspace(0, 1, size, dtype=np.float32)
    y = np.linspace(0, 1, size, dtype=np.float32)
    xx, yy = np.meshgrid(x, y)
    r = (xx * 0.5 + 0.3).clip(0, 1)
    g = (yy * 0.4 + 0.3).clip(0, 1)
    b = ((1 - xx) * 0.3 + 0.2).clip(0, 1)
    return np.stack([r, g, b], axis=2)  # HWC float32 [0,1]


def make_anomaly_image(size: int) -> np.ndarray:
    """Simulate an 'anomalous' image: normal with a bright patch."""
    arr = make_normal_image(size).copy()
    patch = size // 4
    start = size // 3
    arr[start:start + patch, start:start + patch, :] = 1.0  # bright white patch
    return arr


def preprocess(arr_hwc_float: np.ndarray) -> torch.Tensor:
    """Normalize HWC float32[0,1] → tensor NCHW."""
    normalized = (arr_hwc_float - IMAGENET_MEAN) / IMAGENET_STD
    chw = normalized.transpose(2, 0, 1).astype(np.float32)
    return torch.from_numpy(chw).unsqueeze(0)


def run():
    device = "cuda" if torch.cuda.is_available() else "cpu"
    results = []

    for case in CASES:
        print(f"\n{'='*60}")
        print(f"  {case['name']}")

        with open(case["ckpt"], "rb") as f:
            ckpt_bytes = f.read()

        try:
            model = load_patchcore_ckpt(ckpt_bytes, device)
        except Exception as e:
            print(f"  [FAIL] load: {e}")
            results.append({"name": case["name"], "ok": False, "error": str(e)})
            continue

        img_thr = float(model.post_processor._image_threshold.item())
        pix_thr = float(model.post_processor._pixel_threshold.item())

        # Validate thresholds from ckpt
        img_thr_ok = abs(img_thr - case["image_threshold"]) < 0.002
        pix_thr_ok = abs(pix_thr - case["pixel_threshold"]) < 0.1
        if not img_thr_ok:
            print(f"  [FAIL] imageThreshold mismatch: ckpt={img_thr:.4f} expected={case['image_threshold']}")
        if not pix_thr_ok:
            print(f"  [FAIL] pixelThreshold mismatch: ckpt={pix_thr:.4f} expected={case['pixel_threshold']}")

        all_ok = True
        for label, img_fn in [("normal", make_normal_image), ("anomaly", make_anomaly_image)]:
            img_arr = img_fn(case["input_size"])
            tensor = preprocess(img_arr).to(device)
            try:
                model.eval()
                with torch.no_grad():
                    prediction = model.model(tensor)
                pred_score = float(prediction.pred_score.flatten()[0].detach().cpu().item())
                anomaly_map = prediction.anomaly_map[0, 0].detach().cpu().numpy()
                amap_ok = anomaly_map.shape == (case["input_size"], case["input_size"])
                decision = "DEFECT" if pred_score >= img_thr else "NORMAL"
                print(f"  [{label}]  pred_score={pred_score:.4f}  threshold={img_thr:.4f}  "
                      f"decision={decision}  anomaly_map={anomaly_map.shape}  {'OK' if amap_ok else 'FAIL'}")
                if not amap_ok:
                    all_ok = False
            except Exception as e:
                print(f"  [{label}] FAIL: {e}")
                all_ok = False

        # Summary for this case
        ok = all_ok and img_thr_ok and pix_thr_ok
        print(f"  imageThreshold={img_thr:.4f}  pixelThreshold={pix_thr:.4f}")
        print(f"  RESULT: [{'PASS' if ok else 'FAIL'}]")
        results.append({"name": case["name"], "ok": ok,
                        "image_threshold": img_thr, "pixel_threshold": pix_thr})

    print(f"\n{'='*60}")
    print("SUMMARY")
    all_pass = all(r["ok"] for r in results)
    for r in results:
        status = "PASS" if r["ok"] else "FAIL"
        thr = r.get("image_threshold", "?")
        print(f"  [{status}] {r['name']}  imageThreshold={thr}")
    sys.exit(0 if all_pass else 1)


if __name__ == "__main__":
    run()
