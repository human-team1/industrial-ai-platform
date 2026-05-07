"""Regression smoke test for WideResNet speed ckpts."""
from __future__ import annotations

import sys
import os

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

import numpy as np
import torch

from infrastructure.vision.anomalib_patchcore_loader import load_patchcore_ckpt

CASES = [
    {"name": "obj_speed",     "path": "config/model/obj_speed.ckpt",     "expected_img_size": 224},
    {"name": "texture_speed", "path": "config/model/texture_speed.ckpt", "expected_img_size": 256},
]

IMAGENET_MEAN = [0.485, 0.456, 0.406]
IMAGENET_STD  = [0.229, 0.224, 0.225]


def make_dummy(img_size: int, device: str) -> torch.Tensor:
    np.random.seed(42)
    arr = np.random.rand(3, img_size, img_size).astype(np.float32)
    t = torch.from_numpy(arr).to(device)
    mean = torch.tensor(IMAGENET_MEAN, device=device).view(3, 1, 1)
    std  = torch.tensor(IMAGENET_STD,  device=device).view(3, 1, 1)
    return ((t - mean) / std).unsqueeze(0)


def run():
    device = "cuda" if torch.cuda.is_available() else "cpu"
    all_pass = True

    for case in CASES:
        print(f"\n{'='*50}")
        print(f"Testing: {case['name']}")
        with open(case["path"], "rb") as f:
            ckpt_bytes = f.read()

        try:
            model = load_patchcore_ckpt(ckpt_bytes, device)
            print("  [OK] load")
        except Exception as e:
            print(f"  [FAIL] load: {e}")
            all_pass = False
            continue

        try:
            img = make_dummy(case["expected_img_size"], device)
            with torch.no_grad():
                pred = model.model(img)
            score = float(pred.pred_score.flatten()[0].detach().cpu().item())
            amap  = pred.anomaly_map[0, 0].detach().cpu().numpy()
            print(f"  [OK] predict  score={score:.4f}  anomaly_map={amap.shape}")
        except Exception as e:
            print(f"  [FAIL] predict: {e}")
            all_pass = False

    print(f"\n{'='*50}")
    print("SUMMARY:", "PASS" if all_pass else "FAIL")
    sys.exit(0 if all_pass else 1)


if __name__ == "__main__":
    run()
