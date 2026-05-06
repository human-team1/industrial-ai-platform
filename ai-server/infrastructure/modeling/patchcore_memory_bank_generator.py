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

try:
    import torch
except ImportError:  # pragma: no cover - exercised through runtime error mapping
    torch = None

log = logging.getLogger(__name__)


class PatchCoreMemoryBankGenerator(MemoryBankGenerator):
    def __init__(self, spec: MemoryBankProfileSpec) -> None:
        self._spec = spec

    def generate(self, *, image_bytes_list: list[bytes], config: dict, ckpt_bytes: bytes, request_id: str) -> bytes:
        with self._stage(request_id, "ckpt_load_start", "ckpt_load_end", normalImageCount=len(image_bytes_list)):
            torch_module, model, device = self._load_checkpoint(ckpt_bytes, request_id)
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
                self._log_stage(
                    request_id,
                    "memory_bank_sampling_end",
                    0,
                    candidateFeatureCount=candidate_count,
                    requestedMemoryBankSize=requested_size,
                    actualMemoryBankSize=actual_size,
                    featureShape=tuple(memory_bank.shape),
                )
            return self._serialize(torch_module, memory_bank, config)
        except AppException:
            raise
        except Exception as exc:
            raise AppException(500, "Memory bank generation failed", "memory_bank generation failed.", "MEMORY_BANK_GENERATION_FAILED") from exc

    def _load_checkpoint(self, ckpt_bytes: bytes, request_id: str):
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
            loaded = torch.load(BytesIO(ckpt_bytes), map_location=device, weights_only=False)
            model = self._select_model(loaded)
            if hasattr(model, "to"):
                model = model.to(device)
            model.eval()
            log.info(
                "memory_bank_model_device requestId=%s modelCategory=%s modelProfile=%s selectedDevice=%s modelDevice=%s cudaAllocatedBytes=%s cudaReservedBytes=%s",
                request_id,
                self._spec.category.value,
                self._spec.profile.value,
                device,
                self._model_device(model),
                torch.cuda.memory_allocated(0) if torch.cuda.is_available() else 0,
                torch.cuda.memory_reserved(0) if torch.cuda.is_available() else 0,
            )
        except Exception as exc:
            raise AppException(
                500,
                "Backbone load failed",
                f"checkpoint load failed for {self._spec.category.value}/{self._spec.profile.value}.",
                "BACKBONE_LOAD_FAILED",
            ) from exc
        return torch, model, device

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
        with self._stage(request_id, "preprocess_start", "preprocess_end", normalImageCount=len(image_bytes_list)):
            try:
                for image_index, image_bytes in enumerate(image_bytes_list):
                    with Image.open(BytesIO(image_bytes)) as image:
                        image = image.convert("RGB").resize(self._spec.image_size)
                        array = np.asarray(image, dtype=np.float32) / 255.0
                    tensor = torch.from_numpy(array).permute(2, 0, 1)
                    tensors.append(tensor)
                    if image_index == 0:
                        self._log_stage(request_id, "preprocess_first_image", 0, imageIndex=image_index)
            except (UnidentifiedImageError, OSError) as exc:
                raise AppException(422, "Invalid normal image", "Invalid normal image.", "NORMAL_IMAGE_INVALID") from exc

            batch = torch.stack(tensors, dim=0).to(device)
            mean = torch.tensor([0.485, 0.456, 0.406], device=device).view(1, 3, 1, 1)
            std = torch.tensor([0.229, 0.224, 0.225], device=device).view(1, 3, 1, 1)
            batch = (batch - mean) / std
            self._log_stage(
                request_id,
                "tensor_device_ready",
                0,
                selectedDevice=device,
                batchDevice=batch.device,
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
                feature_map = tensor.permute(0, 2, 3, 1).reshape(-1, tensor.shape[1])
            elif tensor.ndim == 3:
                feature_map = tensor.reshape(-1, tensor.shape[-1])
            elif tensor.ndim == 2:
                feature_map = tensor
            else:
                raise RuntimeError(f"unsupported feature tensor shape: {tuple(tensor.shape)}")
            self._log_stage(
                request_id,
                "feature_tensor_ready",
                0,
                featureShape=tuple(tensor.shape),
                featureDevice=tensor.device,
                cudaAllocatedBytes=torch.cuda.memory_allocated(0) if torch.cuda.is_available() else 0,
                cudaReservedBytes=torch.cuda.memory_reserved(0) if torch.cuda.is_available() else 0,
            )
            return feature_map.detach().cpu().numpy().astype(np.float32)
        except Exception as exc:
            raise AppException(500, "Feature extraction failed", "checkpoint feature extraction failed.", "FEATURE_EXTRACTION_FAILED") from exc

    def _forward_model(self, model, batch):
        if hasattr(model, "feature_extractor"):
            features = model.feature_extractor(batch)
            if hasattr(model, "generate_embedding"):
                return model.generate_embedding(features)
            return features
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

    def _serialize(self, torch, memory_bank: np.ndarray, config: dict) -> bytes:
        metadata = {
            "model_category": self._spec.category.value,
            "model_profile": self._spec.profile.value,
            "pipeline": self._spec.pipeline,
            "image_size": list(self._spec.image_size),
            "layers": list(self._spec.layers),
            "target_memory_bank_size": self._spec.target_memory_bank_size,
            "actual_memory_bank_size": int(memory_bank.shape[0]),
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
