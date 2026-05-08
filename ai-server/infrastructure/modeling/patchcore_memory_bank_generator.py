from __future__ import annotations

import os
import logging
from contextlib import contextmanager
from enum import Enum
from io import BytesIO
from pathlib import Path
from time import perf_counter
from typing import Any

import numpy as np
from PIL import Image, UnidentifiedImageError

from application.exceptions import AppException
from domain.models.memory_bank import MemoryBankGenerator, MemoryBankProfileSpec
from infrastructure.vision.preprocessing import preprocess_pil_image

try:
    import torch
except ImportError:  # pragma: no cover - exercised through runtime error mapping
    torch = None

log = logging.getLogger(__name__)


class PatchCoreMemoryBankGenerator(MemoryBankGenerator):
    def __init__(self, spec: MemoryBankProfileSpec) -> None:
        self._spec = spec

    def generate(self, *, image_bytes_list: list[bytes], config: dict, ckpt_bytes: bytes, request_id: str) -> bytes:
        log.info(
            "memory_bank_generation_started requestId=%s modelCategory=%s modelProfile=%s "
            "inputSize=%sx%s normalImageCount=%s shotPolicy=%s targetMemoryBankSize=%s",
            request_id,
            self._spec.category.value,
            self._spec.profile.value,
            self._spec.image_size[0], self._spec.image_size[1],
            len(image_bytes_list),
            self._spec.shot_policy,
            self._spec.target_memory_bank_size,
        )
        with self._stage(request_id, "ckpt_load_start", "ckpt_load_end", normalImageCount=len(image_bytes_list)):
            torch_module, model, device, backbone = self._load_checkpoint(ckpt_bytes, request_id)
        try:
            features = [self._extract_features(torch_module, model, image_bytes_list, device, request_id)]
        except AppException:
            raise
        except Exception as exc:
            raise AppException(500, "Feature extraction failed", "feature extraction failed.", "FEATURE_EXTRACTION_FAILED") from exc

        try:
            with self._stage(request_id, "memory_bank_build_start", "memory_bank_build_end"):
                memory_bank = np.concatenate(features, axis=0).astype(np.float32)
                candidate_count = int(memory_bank.shape[0])
                requested_size = int(self._spec.target_memory_bank_size)
                memory_bank = self._subsample(memory_bank, requested_size)
                actual_size = int(memory_bank.shape[0])
                feature_dim = int(memory_bank.shape[1]) if memory_bank.ndim == 2 else -1
                self._log_stage(
                    request_id,
                    "memory_bank_sampling_end",
                    0,
                    candidateFeatureCount=candidate_count,
                    requestedMemoryBankSize=requested_size,
                    actualMemoryBankSize=actual_size,
                    featureShape=tuple(memory_bank.shape),
                    featureDim=feature_dim,
                )
            with self._stage(request_id, "threshold_calibration_start", "threshold_calibration_end"):
                calibrated_threshold = self._calibrate_threshold(
                    torch_module, model, memory_bank, image_bytes_list, device, request_id
                )
            config["calibratedThreshold"] = calibrated_threshold
            log.info(
                "memory_bank_saved requestId=%s modelCategory=%s modelProfile=%s "
                "memoryBankShape=%s featureDim=%s backbone=%s layers=%s calibratedThreshold=%.4f",
                request_id,
                self._spec.category.value,
                self._spec.profile.value,
                tuple(memory_bank.shape),
                feature_dim,
                backbone,
                list(self._spec.layers),
                calibrated_threshold,
            )
            return self._serialize(torch_module, memory_bank, config, backbone=backbone, calibrated_threshold=calibrated_threshold)
        except AppException:
            raise
        except Exception as exc:
            raise AppException(500, "Memory bank generation failed", "memory_bank generation failed.", "MEMORY_BANK_GENERATION_FAILED") from exc

    def _load_checkpoint(self, ckpt_bytes: bytes, request_id: str):
        """base Anomalib ckpt를 full Patchcore 모델로 로드한다.

        raw timm backbone만 추출하면 generate_embedding(multi-layer concatenation) 경로를
        우회하게 되어 feature dimension이 불일치한다.
        추론(inference)과 동일한 PatchcoreModel을 사용해야 memory bank dimension이 일치한다.
        반드시 base Anomalib ckpt에 맞는 generate_embedding 경로로 feature를 추출해야
        injection 시 dimension mismatch가 발생하지 않는다.
        """
        if torch is None:
            raise AppException(500, "Model runtime not available", "torch is not available.", "MODEL_RUNTIME_NOT_AVAILABLE")

        try:
            self._prepare_checkpoint_runtime()
            device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
            log.info(
                "memory_bank_device_selected requestId=%s modelCategory=%s modelProfile=%s device=%s cudaAvailable=%s torchCuda=%s cudaDeviceName=%s",
                request_id,
                self._spec.category.value,
                self._spec.profile.value,
                device,
                torch.cuda.is_available(),
                torch.version.cuda,
                torch.cuda.get_device_name(0) if torch.cuda.is_available() else "no cuda",
            )
            # backbone을 ckpt에서 직접 읽어 메타데이터로 기록한다.
            from io import BytesIO as _BytesIO
            raw_ckpt = torch.load(_BytesIO(ckpt_bytes), map_location="cpu", weights_only=False)
            backbone = str(raw_ckpt.get("hyper_parameters", {}).get("backbone", "unknown"))
            ckpt_layers = list(raw_ckpt.get("hyper_parameters", {}).get("layers", []))
            state_dict = raw_ckpt.get("state_dict", {})
            base_mb_key = "model.memory_bank"
            base_mb_shape = tuple(state_dict[base_mb_key].shape) if base_mb_key in state_dict else None
            base_mb_dim = int(state_dict[base_mb_key].shape[1]) if base_mb_key in state_dict and state_dict[base_mb_key].ndim == 2 else None
            log.info(
                "memory_bank_ckpt_meta requestId=%s backbone=%s ckptLayers=%s baseMbShape=%s baseMbDim=%s "
                "generatorLayers=%s generatorInputSize=%s -- generation must match these dimensions",
                request_id,
                backbone,
                ckpt_layers,
                base_mb_shape,
                base_mb_dim,
                list(self._spec.layers),
                self._spec.image_size[0],
            )

            from infrastructure.vision.anomalib_patchcore_loader import load_patchcore_ckpt
            patchcore = load_patchcore_ckpt(ckpt_bytes, str(device))
            model = patchcore.model  # PatchcoreModel (feature_extractor + generate_embedding)
            model.eval()
            log.info(
                "memory_bank_model_loaded requestId=%s modelCategory=%s modelProfile=%s selectedDevice=%s modelDevice=%s backbone=%s cudaAllocatedBytes=%s cudaReservedBytes=%s",
                request_id,
                self._spec.category.value,
                self._spec.profile.value,
                device,
                self._model_device(model),
                backbone,
                torch.cuda.memory_allocated(0) if torch.cuda.is_available() else 0,
                torch.cuda.memory_reserved(0) if torch.cuda.is_available() else 0,
            )
        except AppException:
            raise
        except Exception as exc:
            raise AppException(
                500,
                "Backbone load failed",
                f"checkpoint load failed for {self._spec.category.value}/{self._spec.profile.value}.",
                "BACKBONE_LOAD_FAILED",
            ) from exc
        return torch, model, device, backbone

    def _prepare_checkpoint_runtime(self) -> None:
        cache_dir = Path(__file__).resolve().parents[2] / ".cache" / "huggingface"
        cache_dir.mkdir(parents=True, exist_ok=True)
        os.environ.setdefault("HF_HOME", str(cache_dir))
        try:
            import anomalib
        except ImportError:
            return
        if not hasattr(anomalib, "PrecisionType"):
            class PrecisionType(str, Enum):
                float16 = "float16"
                float32 = "float32"
                float64 = "float64"

            anomalib.PrecisionType = PrecisionType

    def _select_model(self, loaded: Any):
        if isinstance(loaded, dict) and "state_dict" in loaded:
            return self._build_model_from_state_dict(loaded["state_dict"])
        if hasattr(loaded, "model"):
            return loaded.model
        if hasattr(loaded, "eval") and callable(loaded):
            return loaded
        raise RuntimeError(f"unsupported checkpoint object: {type(loaded).__name__}")

    def _build_model_from_state_dict(self, state_dict: dict):
        try:
            import timm
        except ImportError as exc:
            raise RuntimeError("timm is required for checkpoint state_dict loading") from exc

        backbone = "vit_base_patch14_dinov2" if "DINO" in self._spec.pipeline.upper() else "wide_resnet50_2"
        if "DINO" in self._spec.pipeline.upper():
            model = timm.create_model(backbone, pretrained=False, img_size=self._spec.image_size[0])
        else:
            model = timm.create_model(backbone, pretrained=False)
        stripped_state_dict = {}
        for key, value in state_dict.items():
            if key.startswith("model.feature_extractor.feature_extractor.model."):
                stripped_state_dict[key.removeprefix("model.feature_extractor.feature_extractor.model.")] = value
            elif key.startswith("model.feature_extractor.feature_extractor."):
                stripped_state_dict[key.removeprefix("model.feature_extractor.feature_extractor.")] = value
            elif key.startswith("model.feature_extractor.model."):
                stripped_state_dict[key.removeprefix("model.feature_extractor.model.")] = value
        if not stripped_state_dict:
            raise RuntimeError("checkpoint state_dict did not contain feature extractor weights")
        incompatible = model.load_state_dict(stripped_state_dict, strict=False)
        if not stripped_state_dict or len(incompatible.missing_keys) == len(model.state_dict()):
            raise RuntimeError("checkpoint weights did not match backbone architecture")
        return model

    def _extract_features(self, torch, model, image_bytes_list: list[bytes], device, request_id: str) -> np.ndarray:
        tensors = []
        selected_images = self._select_images_by_shot_policy(image_bytes_list, request_id)
        vision_spec = self._to_vision_spec()
        with self._stage(request_id, "preprocess_start", "preprocess_end", normalImageCount=len(image_bytes_list)):
            try:
                for image_index, image_bytes in enumerate(selected_images):
                    with Image.open(BytesIO(image_bytes)) as image:
                        original_size = image.size  # (W, H)
                        processed = preprocess_pil_image(image, vision_spec)
                    tensor = torch.from_numpy(processed.normalized_chw)
                    tensors.append(tensor)
                    if image_index == 0:
                        # 첫 번째 이미지의 전처리 정보를 상세히 로그
                        log.info(
                            "memory_bank_preprocessing_info requestId=%s imageIndex=%s "
                            "originalSize=%sx%s modelInputSize=%sx%s colorMode=RGB "
                            "tensorShape=%s dtype=%s normalize=imagenet "
                            "mean=[0.485,0.456,0.406] std=[0.229,0.224,0.225]",
                            request_id,
                            image_index,
                            original_size[0], original_size[1],
                            vision_spec.input_size, vision_spec.input_size,
                            tuple(tensor.shape),
                            tensor.dtype,
                        )
                        self._log_stage(request_id, "preprocess_first_image", 0, imageIndex=image_index)
            except (UnidentifiedImageError, OSError) as exc:
                raise AppException(422, "Invalid normal image", "Invalid normal image.", "NORMAL_IMAGE_INVALID") from exc

            batch = torch.stack(tensors, dim=0).to(device)
            self._log_stage(
                request_id,
                "tensor_device_ready",
                0,
                selectedDevice=device,
                batchDevice=batch.device,
                batchShape=tuple(batch.shape),
                batchDtype=str(batch.dtype),
                cudaAllocatedBytes=torch.cuda.memory_allocated(0) if torch.cuda.is_available() else 0,
                cudaReservedBytes=torch.cuda.memory_reserved(0) if torch.cuda.is_available() else 0,
            )
        try:
            with self._stage(request_id, "feature_extraction_start", "feature_extraction_end", normalImageCount=len(image_bytes_list), selectedDevice=device):
                with torch.no_grad():
                    output = self._forward_model(model, batch)
            tensor = self._first_feature_tensor(output)
            if tensor is None:
                raise RuntimeError("model output did not contain a tensor")
            if tensor.ndim == 4:
                # (B, D, H, W) → (B*H*W, D)
                feature_dim = int(tensor.shape[1])
                feature_map = tensor.permute(0, 2, 3, 1).reshape(-1, feature_dim)
            elif tensor.ndim == 3:
                # (B, N, D) → (B*N, D)
                feature_dim = int(tensor.shape[-1])
                feature_map = tensor.reshape(-1, feature_dim)
            elif tensor.ndim == 2:
                # (B*N, D)
                feature_dim = int(tensor.shape[-1])
                feature_map = tensor
            else:
                raise RuntimeError(f"unsupported feature tensor shape: {tuple(tensor.shape)}")
            log.info(
                "memory_bank_feature_info requestId=%s modelCategory=%s modelProfile=%s "
                "forwardPath=generate_embedding rawOutputShape=%s featureMapShape=%s featureDim=%s "
                "layers=%s -- featureDim must match base ckpt memory_bank dim",
                request_id,
                self._spec.category.value,
                self._spec.profile.value,
                tuple(tensor.shape),
                tuple(feature_map.shape),
                feature_dim,
                list(self._spec.layers),
            )
            self._log_stage(
                request_id,
                "feature_tensor_ready",
                0,
                featureShape=tuple(tensor.shape),
                featureDim=feature_dim,
                featureDevice=tensor.device,
                cudaAllocatedBytes=torch.cuda.memory_allocated(0) if torch.cuda.is_available() else 0,
                cudaReservedBytes=torch.cuda.memory_reserved(0) if torch.cuda.is_available() else 0,
            )
            return feature_map.detach().cpu().numpy().astype(np.float32)
        except Exception as exc:
            raise AppException(500, "Feature extraction failed", "checkpoint feature extraction failed.", "FEATURE_EXTRACTION_FAILED") from exc

    def _forward_model(self, model, batch):
        """feature 추출 — 추론(inference)과 동일한 multi-layer concatenation 경로를 사용한다.

        PatchcoreModel(feature_extractor + generate_embedding) 경로가 가장 정확하다.
        이 경로를 사용해야 inference의 generate_embedding 출력과 동일한 feature dimension이 보장된다.
        """
        if hasattr(model, "feature_extractor") and hasattr(model, "generate_embedding"):
            # PatchcoreModel: inference와 동일한 feature extraction 경로
            features = model.feature_extractor(batch)
            return model.generate_embedding(features)
        if hasattr(model, "feature_extractor"):
            return model.feature_extractor(batch)
        if hasattr(model, "forward_features"):
            return model.forward_features(batch)
        return model(batch)

    def _first_feature_tensor(self, output):
        if torch is not None and isinstance(output, torch.Tensor):
            return output
        if isinstance(output, dict):
            for value in output.values():
                tensor = self._first_feature_tensor(value)
                if tensor is not None:
                    return tensor
        if isinstance(output, (list, tuple)):
            for value in output:
                tensor = self._first_feature_tensor(value)
                if tensor is not None:
                    return tensor
        for attribute in ("embedding", "features", "last_hidden_state"):
            if hasattr(output, attribute):
                tensor = self._first_feature_tensor(getattr(output, attribute))
                if tensor is not None:
                    return tensor
        return None

    def _model_device(self, model) -> str:
        try:
            return str(next(model.parameters()).device)
        except Exception:
            return "unknown"

    def _subsample(self, memory_bank: np.ndarray, target_size: int) -> np.ndarray:
        if memory_bank.shape[0] <= target_size:
            return memory_bank
        indices = np.linspace(0, memory_bank.shape[0] - 1, target_size, dtype=np.int64)
        return memory_bank[indices]

    def _select_images_by_shot_policy(self, image_bytes_list: list[bytes], request_id: str) -> list[bytes]:
        if self._spec.shot_policy != "50-shot":
            return image_bytes_list
        if len(image_bytes_list) <= 50:
            if len(image_bytes_list) < 50:
                self._log_stage(
                    request_id,
                    "memory_bank_shot_policy_warning",
                    0,
                    shotPolicy=self._spec.shot_policy,
                    requestedShotCount=50,
                    actualShotCount=len(image_bytes_list),
                )
            return image_bytes_list
        return image_bytes_list[:50]

    def _to_vision_spec(self):
        from domain.vision_model_profile import VisionModelProfileSpec

        return VisionModelProfileSpec(
            model_category=self._spec.category.value,
            model_profile=self._spec.profile.value,
            backbone="unknown",
            patchcore_layers=self._spec.layers,
            input_size=self._spec.image_size[0],
            shot_policy=self._spec.shot_policy,
            target_memory_bank_size=self._spec.target_memory_bank_size,
            image_threshold=self._spec.image_threshold,
            pixel_threshold=self._spec.pixel_threshold,
        )

    def _calibrate_threshold(
        self,
        torch_module,
        model,
        memory_bank: np.ndarray,
        image_bytes_list: list[bytes],
        device: str,
        request_id: str,
    ) -> float:
        """정상 이미지로 generated memory bank의 threshold를 재보정한다.

        공식: calibrated = percentile(normal_scores, 99) + margin
        margin = max(0.5, std * 3)

        base ckpt의 MVTec 기준 threshold는 고객 이미지 score 분포와 달라질 수 있으므로
        generated memory bank 생성 직후 정상 이미지를 기반으로 재보정이 필요하다.
        메모리뱅크마다 고유 threshold를 갖도록 policy threshold를 하한으로 고정하지 않는다.
        """
        mb_tensor = torch_module.from_numpy(memory_bank).to(device)
        model.memory_bank = mb_tensor

        calibrate_images = image_bytes_list[:20]
        vision_spec = self._to_vision_spec()
        scores: list[float] = []
        model.eval()
        with torch_module.no_grad():
            for img_bytes in calibrate_images:
                try:
                    with Image.open(BytesIO(img_bytes)) as pil:
                        processed = preprocess_pil_image(pil, vision_spec)
                    arr = processed.normalized_chw
                    tensor = torch_module.from_numpy(arr).unsqueeze(0).to(device)
                    pred = model(tensor)
                    if hasattr(pred, "pred_score"):
                        score = float(pred.pred_score.flatten()[0].detach().cpu().item())
                    elif isinstance(pred, dict) and "pred_score" in pred:
                        score = float(pred["pred_score"].flatten()[0].detach().cpu().item())
                    else:
                        continue
                    scores.append(score)
                except Exception as exc:
                    log.warning(
                        "memory_bank_calibration_skip requestId=%s error=%s",
                        request_id,
                        str(exc)[:200],
                    )

        if not scores:
            log.warning(
                "memory_bank_calibration_fallback requestId=%s noScores usingPolicyThreshold=%.4f",
                request_id,
                self._spec.image_threshold,
            )
            return float(self._spec.image_threshold)

        arr_scores = np.array(scores, dtype=np.float32)
        margin = float(max(0.5, float(arr_scores.std()) * 3))
        p99 = float(np.percentile(arr_scores, 99))
        calibrated = float(max(0.0, p99 + margin))
        log.info(
            "memory_bank_threshold_calibrated requestId=%s category=%s profile=%s "
            "normalSampleCount=%s mean=%.4f std=%.4f p99=%.4f margin=%.4f "
            "policyThreshold=%.4f calibratedThreshold=%.4f",
            request_id,
            self._spec.category.value,
            self._spec.profile.value,
            len(scores),
            float(arr_scores.mean()),
            float(arr_scores.std()),
            p99,
            margin,
            self._spec.image_threshold,
            calibrated,
        )
        return calibrated

    def _serialize(self, torch, memory_bank: np.ndarray, config: dict, *, backbone: str = "unknown", calibrated_threshold: float | None = None) -> bytes:
        from datetime import datetime as _dt
        feature_dim = int(memory_bank.shape[1]) if memory_bank.ndim == 2 else -1
        memory_bank_shape = list(memory_bank.shape)
        config["memoryBankMetadata"] = {
            "targetMemoryBankSize": self._spec.target_memory_bank_size,
            "actualMemoryBankSize": int(memory_bank.shape[0]),
            "shotPolicy": self._spec.shot_policy,
            "imageThreshold": self._spec.image_threshold,
            "pixelThreshold": self._spec.pixel_threshold,
            "featureDim": feature_dim,
            "memoryBankShape": memory_bank_shape,
            "calibratedThreshold": calibrated_threshold,
        }
        metadata = {
            "model_category": self._spec.category.value,
            "model_profile": self._spec.profile.value,
            "pipeline": self._spec.pipeline,
            "backbone": backbone,
            "image_size": list(self._spec.image_size),
            "layers": list(self._spec.layers),
            "feature_dim": feature_dim,
            "memory_bank_shape": memory_bank_shape,
            "target_memory_bank_size": self._spec.target_memory_bank_size,
            "actual_memory_bank_size": int(memory_bank.shape[0]),
            "shot_policy": self._spec.shot_policy,
            "image_threshold": self._spec.image_threshold,
            "pixel_threshold": self._spec.pixel_threshold,
            "calibrated_threshold": calibrated_threshold,
            "preprocessing": {
                "color": "RGB",
                "resize": f"{self._spec.image_size[0]}x{self._spec.image_size[1]}",
                "normalize": "imagenet",
                "mean": [0.485, 0.456, 0.406],
                "std": [0.229, 0.224, 0.225],
                "tensor_shape": "BCHW",
                "dtype": "float32",
            },
            "anomalib_version": "2.4.0",
            "created_at": _dt.now().isoformat(timespec="seconds"),
            "config": config,
        }
        try:
            buffer = BytesIO()
            torch.save({"memory_bank": memory_bank, "metadata": metadata}, buffer)
            return buffer.getvalue()
        except Exception as exc:
            raise AppException(500, "Memory bank generation failed", "memory_bank serialization failed.", "MEMORY_BANK_GENERATION_FAILED") from exc

    @contextmanager
    def _stage(self, request_id: str, start_stage: str, end_stage: str, **fields):
        started_at = perf_counter()
        self._log_stage(request_id, start_stage, 0, **fields)
        try:
            yield
        finally:
            elapsed_ms = int((perf_counter() - started_at) * 1000)
            self._log_stage(request_id, end_stage, elapsed_ms, **fields)

    def _log_stage(self, request_id: str, stage: str, elapsed_ms: int, **fields) -> None:
        extra = " ".join(f"{key}={value}" for key, value in fields.items() if value is not None)
        message = "memory_bank_stage requestId=%s stage=%s elapsedMs=%s modelCategory=%s modelProfile=%s %s"
        args = (request_id, stage, elapsed_ms, self._spec.category.value, self._spec.profile.value, extra)
        if elapsed_ms >= 60000:
            log.error("memory_bank_stage_slow requestId=%s stage=%s elapsedMs=%s modelCategory=%s modelProfile=%s %s", *args)
        elif elapsed_ms >= 30000:
            log.warning("memory_bank_stage_slow requestId=%s stage=%s elapsedMs=%s modelCategory=%s modelProfile=%s %s", *args)
        else:
            log.info(message, *args)
