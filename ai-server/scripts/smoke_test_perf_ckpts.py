"""Smoke test for DINO performance ckpts.

Validates:
- obj_perf.ckpt  loads  (336x336, image_threshold=7.4762)
- texture_perf.ckpt loads (448x448, image_threshold=22.2103)
- predict() returns pred_score and anomaly_map for a synthetic normal image
"""
from __future__ import annotations

import math
import sys
import os

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

import numpy as np
import torch
from torchvision import transforms

from infrastructure.vision.anomalib_patchcore_loader import load_patchcore_ckpt

CASES = [
    {
        "name": "obj_perf",
        "path": "config/model/obj_perf.ckpt",
        "expected_img_size": 336,
        "expected_image_threshold": 7.4762,
        "expected_pixel_threshold": 6.6040,
    },
    {
        "name": "texture_perf",
        "path": "config/model/texture_perf.ckpt",
        "expected_img_size": 448,
        "expected_image_threshold": 22.2103,
        "expected_pixel_threshold": 25.8270,
    },
]

IMAGENET_MEAN = [0.485, 0.456, 0.406]
IMAGENET_STD = [0.229, 0.224, 0.225]


def make_dummy_tensor(img_size: int, device: str) -> torch.Tensor:
    """Create a 'normal-looking' image tensor (random noise, ImageNet-normalized)."""
    np.random.seed(42)
    arr = (np.random.rand(3, img_size, img_size).astype(np.float32))
    t = torch.from_numpy(arr).to(device)
    mean = torch.tensor(IMAGENET_MEAN, device=device).view(3, 1, 1)
    std = torch.tensor(IMAGENET_STD, device=device).view(3, 1, 1)
    return ((t - mean) / std).unsqueeze(0)


def run():
    device = "cuda" if torch.cuda.is_available() else "cpu"
    results = []

    for case in CASES:
        print(f"\n{'='*60}")
        print(f"Testing: {case['name']}")

        with open(case["path"], "rb") as f:
            ckpt_bytes = f.read()

        # --- Load ---
        try:
            model = load_patchcore_ckpt(ckpt_bytes, device)
            load_ok = True
            print(f"  [OK] load")
        except Exception as e:
            print(f"  [FAIL] load: {e}")
            results.append({"name": case["name"], "ok": False, "error": str(e)})
            continue

        # --- Check img_size ---
        getter_net = model.model.feature_extractor.feature_extractor
        vit = getter_net.model
        patch_embed_img_size = getattr(vit.patch_embed, "img_size", (None,))
        actual_img_size = patch_embed_img_size[0] if hasattr(patch_embed_img_size, "__len__") else patch_embed_img_size
        size_ok = actual_img_size == case["expected_img_size"]
        print(f"  [{'OK' if size_ok else 'FAIL'}] img_size: {actual_img_size} (expected {case['expected_img_size']})")

        # --- Check thresholds from post_processor ---
        pp = model.post_processor
        img_thr = float(pp._image_threshold.item()) if hasattr(pp, "_image_threshold") else None
        pix_thr = float(pp._pixel_threshold.item()) if hasattr(pp, "_pixel_threshold") else None
        img_thr_ok = img_thr is not None and abs(img_thr - case["expected_image_threshold"]) < 0.001
        pix_thr_ok = pix_thr is not None and abs(pix_thr - case["expected_pixel_threshold"]) < 0.1
        print(f"  [{'OK' if img_thr_ok else 'FAIL'}] image_threshold: {img_thr:.4f} (expected {case['expected_image_threshold']})")
        print(f"  [{'OK' if pix_thr_ok else 'FAIL'}] pixel_threshold: {pix_thr:.4f} (expected {case['expected_pixel_threshold']})")

        # --- Predict ---
        try:
            model.eval()
            img_tensor = make_dummy_tensor(case["expected_img_size"], device)
            with torch.no_grad():
                prediction = model.model(img_tensor)

            pred_score = float(prediction.pred_score.flatten()[0].detach().cpu().item())
            anomaly_map = prediction.anomaly_map[0, 0].detach().cpu().numpy()
            amap_ok = anomaly_map.shape == (case["expected_img_size"], case["expected_img_size"])
            decision = "ABNORMAL" if pred_score >= img_thr else "NORMAL"

            print(f"  [OK] predict")
            print(f"  pred_score:        {pred_score:.6f}")
            print(f"  anomaly_map shape: {anomaly_map.shape}")
            print(f"  decision:          {decision} (score={pred_score:.4f} >= threshold={img_thr:.4f})")
            predict_ok = True
        except Exception as e:
            print(f"  [FAIL] predict: {e}")
            predict_ok = False
            pred_score = None
            anomaly_map = None

        all_ok = load_ok and size_ok and img_thr_ok and pix_thr_ok and predict_ok
        results.append({
            "name": case["name"],
            "ok": all_ok,
            "img_size": actual_img_size,
            "pred_score": pred_score,
            "image_threshold": img_thr,
            "pixel_threshold": pix_thr,
            "anomaly_map_shape": anomaly_map.shape if anomaly_map is not None else None,
        })

    print(f"\n{'='*60}")
    print("SUMMARY")
    all_pass = True
    for r in results:
        status = "PASS" if r["ok"] else "FAIL"
        print(f"  [{status}] {r['name']}")
        if not r["ok"]:
            all_pass = False

    sys.exit(0 if all_pass else 1)


if __name__ == "__main__":
    run()
