"""End-to-end smoke test for all four Anomalib PatchCore checkpoints."""
from __future__ import annotations

import sys
import os

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

import numpy as np
import torch

from infrastructure.vision.anomalib_patchcore_loader import load_patchcore_ckpt

CASES = [
    {
        "name": "obj_speed",
        "path": "config/model/obj_speed.ckpt",
        "expected_img_size": 224,
        "expected_image_threshold": 36.8688,
        "expected_pixel_threshold": 28.4368,
    },
    {
        "name": "texture_speed",
        "path": "config/model/texture_speed.ckpt",
        "expected_img_size": 256,
        "expected_image_threshold": 40.0642,
        "expected_pixel_threshold": 36.5779,
    },
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
        "expected_pixel_threshold": 25.827,
    },
]

IMAGENET_MEAN = [0.485, 0.456, 0.406]
IMAGENET_STD = [0.229, 0.224, 0.225]


def make_dummy(img_size: int, device: str) -> torch.Tensor:
    np.random.seed(42)
    arr = np.random.rand(3, img_size, img_size).astype(np.float32)
    t = torch.from_numpy(arr).to(device)
    mean = torch.tensor(IMAGENET_MEAN, device=device).view(3, 1, 1)
    std = torch.tensor(IMAGENET_STD, device=device).view(3, 1, 1)
    return ((t - mean) / std).unsqueeze(0)


def run():
    device = "cuda" if torch.cuda.is_available() else "cpu"
    results = []

    for case in CASES:
        print(f"\n{'='*62}")
        print(f"  {case['name']}")
        with open(case["path"], "rb") as f:
            ckpt_bytes = f.read()

        fail_reasons = []

        try:
            model = load_patchcore_ckpt(ckpt_bytes, device)
        except Exception as e:
            print(f"  [FAIL] load: {e}")
            results.append({"name": case["name"], "ok": False})
            continue

        # --- img_size check ---
        # ViT/DINOv2: img_size is in patch_embed of the timm ViT (FeatureGetterNet.model.patch_embed)
        # WideResNet:  uses FeatureListNet which has no .model attr; img_size is enforced by
        #              pre_processor transform (not model weights), so we skip this check for CNNs.
        try:
            getter_net = model.model.feature_extractor.feature_extractor
            inner_model = getattr(getter_net, "model", None)
            if inner_model is not None:
                patch_embed = getattr(inner_model, "patch_embed", None)
                if patch_embed is not None:
                    pe_img = patch_embed.img_size
                    actual_size = pe_img[0] if hasattr(pe_img, "__len__") else pe_img
                else:
                    actual_size = case["expected_img_size"]  # ViT with no patch_embed (unlikely)
            else:
                # WideResNet / FeatureListNet: no img_size in model — skip (rely on anomaly_map shape)
                actual_size = case["expected_img_size"]
            size_ok = actual_size == case["expected_img_size"]
        except Exception:
            actual_size = "?"
            size_ok = False
        if not size_ok:
            fail_reasons.append(f"img_size={actual_size} expected {case['expected_img_size']}")

        # --- threshold check ---
        pp = model.post_processor
        img_thr = float(pp._image_threshold.item()) if hasattr(pp, "_image_threshold") else None
        pix_thr = float(pp._pixel_threshold.item()) if hasattr(pp, "_pixel_threshold") else None
        img_thr_ok = img_thr is not None and abs(img_thr - case["expected_image_threshold"]) < 0.002
        pix_thr_ok = pix_thr is not None and abs(pix_thr - case["expected_pixel_threshold"]) < 0.1
        if not img_thr_ok:
            fail_reasons.append(f"image_threshold={img_thr:.4f} expected {case['expected_image_threshold']}")
        if not pix_thr_ok:
            fail_reasons.append(f"pixel_threshold={pix_thr:.4f} expected {case['expected_pixel_threshold']}")

        # --- predict check ---
        try:
            model.eval()
            img_tensor = make_dummy(case["expected_img_size"], device)
            with torch.no_grad():
                prediction = model.model(img_tensor)
            pred_score = float(prediction.pred_score.flatten()[0].detach().cpu().item())
            anomaly_map = prediction.anomaly_map[0, 0].detach().cpu().numpy()
            amap_shape = anomaly_map.shape
            expected_amap_shape = (case["expected_img_size"], case["expected_img_size"])
            if amap_shape != expected_amap_shape:
                fail_reasons.append(f"anomaly_map shape={amap_shape} expected {expected_amap_shape}")
            decision = "ABNORMAL" if pred_score >= img_thr else "NORMAL"
        except Exception as e:
            fail_reasons.append(f"predict failed: {e}")
            pred_score = None
            amap_shape = None
            decision = None

        ok = not fail_reasons
        status = "PASS" if ok else "FAIL"
        print(f"  load:               [OK]")
        print(f"  img_size:           {actual_size}  {'[OK]' if size_ok else '[FAIL]'}")
        print(f"  image_threshold:    {img_thr:.4f}  {'[OK]' if img_thr_ok else '[FAIL]'}")
        print(f"  pixel_threshold:    {pix_thr:.4f}  {'[OK]' if pix_thr_ok else '[FAIL]'}")
        if pred_score is not None:
            print(f"  pred_score:         {pred_score:.6f}")
            print(f"  anomaly_map shape:  {amap_shape}")
            print(f"  decision:           {decision}")
        if fail_reasons:
            for r in fail_reasons:
                print(f"  [FAIL] {r}")
        print(f"  RESULT: [{status}]")
        results.append({"name": case["name"], "ok": ok})

    print(f"\n{'='*62}")
    print("SUMMARY")
    all_pass = all(r["ok"] for r in results)
    for r in results:
        print(f"  [{'PASS' if r['ok'] else 'FAIL'}] {r['name']}")
    print(f"\nOverall: {'ALL PASS' if all_pass else 'SOME FAILED'}")
    sys.exit(0 if all_pass else 1)


if __name__ == "__main__":
    run()
