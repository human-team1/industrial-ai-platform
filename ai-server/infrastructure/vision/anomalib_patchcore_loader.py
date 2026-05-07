"""Custom Anomalib PatchCore checkpoint loader.

WideResNet-based (speed) ckpts work with ``Patchcore.load_from_checkpoint``
directly because their pre_processor is serialised with the correct image size.

DINOv2-based (performance) ckpts store ``pre_processor=True`` (a bare boolean),
so ``load_from_checkpoint`` falls back to the timm default img_size of 518 for
``vit_base_patch14_dinov2``.  That produces ``pos_embed (1, 1370, 768)`` which
conflicts with the checkpoint's ``pos_embed (1, 577, 768)`` (obj_perf, 336×336)
or ``(1, 1025, 768)`` (texture_perf, 448×448).  timm also asserts ``H == img_size``
inside ``PatchEmbed.forward``, so strict=False alone cannot fix this.

Fix:
  1. Read pos_embed from the raw checkpoint to derive the correct img_size.
  2. Create Patchcore normally (img_size=518 default).
  3. Replace ``model.feature_extractor.feature_extractor.model`` (the timm ViT)
     with a new instance built with the correct img_size.
  4. Load state_dict – all shapes now match exactly.
"""

from __future__ import annotations

import logging
import math
from io import BytesIO
from typing import TYPE_CHECKING

import timm
import torch
from anomalib.models.image import Patchcore

if TYPE_CHECKING:
    pass

logger = logging.getLogger(__name__)

# Backbones that need img_size correction (ViT / DINOv2-type)
_VIT_BACKBONE_PREFIXES = ("vit_", "deit_", "swin_", "dinov2_")

PATCH_SIZE_BY_BACKBONE: dict[str, int] = {
    "vit_base_patch14_dinov2": 14,
    "vit_small_patch14_dinov2": 14,
    "vit_large_patch14_dinov2": 14,
}


def _is_vit_backbone(backbone: str) -> bool:
    return any(backbone.startswith(p) for p in _VIT_BACKBONE_PREFIXES)


def _infer_img_size_from_pos_embed(
    state_dict: dict[str, torch.Tensor],
    backbone: str,
) -> int | None:
    """Derive img_size from pos_embed shape stored in checkpoint state_dict."""
    pos_key = "model.feature_extractor.feature_extractor.model.pos_embed"
    if pos_key not in state_dict:
        return None

    pos_embed = state_dict[pos_key]
    n_tokens = pos_embed.shape[1]
    n_patches = n_tokens - 1  # subtract CLS token
    grid = int(math.isqrt(n_patches))
    if grid * grid != n_patches:
        logger.warning(
            "pos_embed token count %d is not a perfect square; cannot derive img_size",
            n_patches,
        )
        return None

    patch_size = PATCH_SIZE_BY_BACKBONE.get(backbone, 14)
    img_size = grid * patch_size
    return img_size


def _replace_timm_vit_with_correct_img_size(
    model: Patchcore,
    backbone: str,
    img_size: int,
) -> None:
    """Replace the timm ViT inside the Patchcore with one sized to img_size.

    The nested path is:
      Patchcore.model                              (PatchcoreModel)
        .feature_extractor                         (TimmFeatureExtractor)
          .feature_extractor                       (timm.FeatureGetterNet)
            .model                                 (timm.VisionTransformer)

    We only replace ``.model`` inside ``FeatureGetterNet``, so all Anomalib
    feature-hook wiring on ``FeatureGetterNet`` itself is preserved.
    """
    new_vit = timm.create_model(backbone, pretrained=False, img_size=img_size)
    getter_net = model.model.feature_extractor.feature_extractor  # FeatureGetterNet
    old_vit = getter_net.model
    getter_net.model = new_vit
    logger.info(
        "anomalib_dino_vit_replaced backbone=%s old_img_size=%s new_img_size=%d "
        "old_pos_embed=%s new_pos_embed=%s",
        backbone,
        getattr(getattr(old_vit, "patch_embed", None), "img_size", "?"),
        img_size,
        tuple(old_vit.pos_embed.shape) if hasattr(old_vit, "pos_embed") else "?",
        tuple(new_vit.pos_embed.shape),
    )


def load_patchcore_ckpt(ckpt_bytes: bytes, device: str) -> Patchcore:
    """Load a Patchcore checkpoint, handling DINOv2 img_size mismatches.

    For WideResNet backbones the standard ``load_from_checkpoint`` is used.
    For ViT/DINOv2 backbones the img_size is derived from the checkpoint
    pos_embed and the timm backbone is rebuilt at the correct size before
    the state_dict is applied.
    """
    raw_ckpt = torch.load(BytesIO(ckpt_bytes), map_location="cpu", weights_only=False)
    hp: dict = raw_ckpt.get("hyper_parameters", {})
    state_dict: dict[str, torch.Tensor] = raw_ckpt.get("state_dict", {})

    backbone: str = hp.get("backbone", "wide_resnet50_2")
    layers: list[str] = list(hp.get("layers", ["layer2"]))
    pre_trained: bool = bool(hp.get("pre_trained", True))

    logger.info(
        "anomalib_ckpt_loading backbone=%s layers=%s is_vit=%s",
        backbone,
        layers,
        _is_vit_backbone(backbone),
    )

    if not _is_vit_backbone(backbone):
        # WideResNet: standard path works fine
        model = Patchcore.load_from_checkpoint(
            checkpoint_path=BytesIO(ckpt_bytes),
            map_location=device,
            strict=False,
        )
        model.to(device)
        model.eval()
        logger.info("anomalib_ckpt_loaded_standard backbone=%s", backbone)
        return model

    # ViT / DINOv2 path ----------------------------------------------------
    img_size = _infer_img_size_from_pos_embed(state_dict, backbone)
    if img_size is None:
        raise RuntimeError(
            f"Cannot derive img_size from pos_embed for backbone={backbone}. "
            "Manual inspection required."
        )

    logger.info(
        "anomalib_dino_img_size_inferred backbone=%s img_size=%d",
        backbone,
        img_size,
    )

    # Build Patchcore skeleton (no pre-trained download, no pre/post processor)
    model = Patchcore(
        backbone=backbone,
        layers=layers,
        pre_trained=False,
        pre_processor=False,
        post_processor=True,
        evaluator=False,
        visualizer=False,
    )

    # Swap the internal timm ViT to the correct img_size
    _replace_timm_vit_with_correct_img_size(model, backbone, img_size)

    # Load state dict with strict=False.
    # The checkpoint omits the final ViT norm layer (norm.weight/norm.bias),
    # which is not used during PatchCore feature extraction (intermediate blocks only).
    missing_keys, unexpected_keys = model.load_state_dict(state_dict, strict=False)

    # Validate that no *critical* keys are missing from the checkpoint.
    # Missing keys here means "in model but not in ckpt" — final norm is acceptable.
    # Unexpected keys means "in ckpt but not in model" — should be empty.
    critical_prefixes = (
        "model.memory_bank",
        "model.feature_extractor.feature_extractor.model.pos_embed",
        "model.feature_extractor.feature_extractor.model.blocks.",
        "post_processor._image_threshold",
        "post_processor._pixel_threshold",
    )
    missing_critical = [
        k for k in missing_keys
        if any(k.startswith(p) for p in critical_prefixes)
    ]
    if missing_critical:
        raise RuntimeError(
            f"Critical keys missing after loading DINO ckpt for backbone={backbone}: "
            f"{missing_critical}"
        )

    allowed_missing = {"model.feature_extractor.feature_extractor.model.norm.weight",
                       "model.feature_extractor.feature_extractor.model.norm.bias"}
    non_norm_missing = [k for k in missing_keys if k not in allowed_missing]
    if non_norm_missing:
        logger.warning("anomalib_dino_non_norm_missing_keys keys=%s", non_norm_missing)
    if unexpected_keys:
        logger.warning("anomalib_dino_unexpected_keys keys=%s", unexpected_keys)
    logger.debug(
        "anomalib_dino_state_dict_loaded missing=%d unexpected=%d "
        "(norm.weight/bias missing is expected for this checkpoint)",
        len(missing_keys),
        len(unexpected_keys),
    )

    model.to(device)
    model.eval()

    img_thr = state_dict.get("post_processor._image_threshold")
    pix_thr = state_dict.get("post_processor._pixel_threshold")
    logger.info(
        "anomalib_dino_loaded backbone=%s img_size=%d image_threshold=%.4f pixel_threshold=%.4f",
        backbone,
        img_size,
        img_thr.item() if img_thr is not None else float("nan"),
        pix_thr.item() if pix_thr is not None else float("nan"),
    )
    return model
