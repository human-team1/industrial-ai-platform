"""Inspect all four ckpt files to understand structure."""
import os
import torch

CKPTS = [
    "config/model/obj_speed.ckpt",
    "config/model/texture_speed.ckpt",
    "config/model/obj_perf.ckpt",
    "config/model/texture_perf.ckpt",
]

for p in CKPTS:
    ck = torch.load(p, map_location="cpu", weights_only=False)
    hp = ck.get("hyper_parameters", {})
    sd = ck.get("state_dict", {})
    print("=" * 70)
    print(os.path.basename(p))
    print(f"  backbone:    {hp.get('backbone')}")
    print(f"  layers:      {hp.get('layers')}")
    print(f"  pre_trained: {hp.get('pre_trained')}")

    pp = hp.get("pre_processor")
    print(f"  pre_processor type: {type(pp).__name__}")
    if hasattr(pp, "transform"):
        print(f"  pre_processor.transform: {pp.transform}")

    print(f"  tiler: {hp.get('tiler')}")
    print(f"  state_dict keys count: {len(sd)}")

    print("  [relevant state_dict keys]")
    for k, v in sd.items():
        low = k.lower()
        if any(w in low for w in ["memory", "bank", "threshold", "norm", "pos_embed", "img_size", "patch_embed"]):
            shape = tuple(v.shape) if hasattr(v, "shape") else type(v).__name__
            print(f"    {k}: {shape}")

    print("  [state_dict first 20 keys]")
    for k in list(sd.keys())[:20]:
        shape = tuple(sd[k].shape) if hasattr(sd[k], "shape") else type(sd[k]).__name__
        print(f"    {k}: {shape}")
