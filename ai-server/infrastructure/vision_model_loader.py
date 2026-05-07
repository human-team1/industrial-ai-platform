from __future__ import annotations

import logging
from datetime import datetime
from io import BytesIO

import torch

from application.exceptions import AppException
from domain.vision_models import LoadedVisionModel
from infrastructure.memory_bank_loader import MemoryBankLoader
from infrastructure.vision.anomalib_patchcore_loader import load_patchcore_ckpt

logger = logging.getLogger(__name__)

_memory_bank_loader = MemoryBankLoader()


class VisionModelLoader:
    def __init__(self) -> None:
        self._cache: dict[str, LoadedVisionModel] = {}

    def load(
        self,
        model_version_id: int,
        ckpt_file_key: str,
        config_file_key: str,
        memory_bank_file_key: str,
        ckpt_bytes: bytes,
        config: dict,
        memory_bank_bytes: bytes | None = None,
    ) -> LoadedVisionModel:
        cache_key = f"{model_version_id}:{ckpt_file_key}:{config_file_key}:{memory_bank_file_key}"
        cached = self._cache.get(cache_key)
        if cached is not None:
            return cached

        device = "cuda" if torch.cuda.is_available() else "cpu"
        runtime_model = self._load_anomalib_model(ckpt_bytes, device)

        if memory_bank_bytes:
            self._inject_memory_bank(
                runtime_model,
                memory_bank_file_key,
                memory_bank_bytes,
                model_version_id,
                device,
            )

        loaded = LoadedVisionModel(
            model_version_id=model_version_id,
            ckpt_file_key=ckpt_file_key,
            config_file_key=config_file_key,
            memory_bank_file_key=memory_bank_file_key,
            ckpt_bytes=ckpt_bytes,
            config=config,
            loaded_at=datetime.now(),
            runtime_model=runtime_model,
            runtime_metadata={
                "backend": "anomalib",
                "anomalibVersion": "2.4.0",
                "device": device,
            },
        )
        self._cache[cache_key] = loaded
        return loaded

    def _load_anomalib_model(self, ckpt_bytes: bytes, device: str):
        # 커스텀 memory bank 파일({memory_bank, metadata})을 ckpt로 잘못 넘기는 경우를 빠르게 감지
        try:
            raw = torch.load(BytesIO(ckpt_bytes), map_location="cpu", weights_only=False)
            if isinstance(raw, dict) and "memory_bank" in raw and "hyper_parameters" not in raw:
                raise AppException(
                    500,
                    "Anomalib model load failed",
                    "ckptFileKey가 Anomalib 표준 checkpoint가 아닌 memory bank 파일입니다. "
                    "DB artifact 매핑을 확인하세요. (CKPT artifact에 base ckpt를 연결해야 합니다.)",
                    "ANOMALIB_CKPT_IS_MEMORY_BANK",
                )
        except AppException:
            raise
        except Exception:
            pass  # torch.load 실패 시 load_patchcore_ckpt에서 처리

        try:
            return load_patchcore_ckpt(ckpt_bytes, device)
        except AppException:
            raise
        except Exception as exc:
            raise AppException(
                500,
                "Anomalib model load failed",
                "Anomalib 체크포인트 로드에 실패했습니다.",
                "ANOMALIB_LOAD_FAILED",
            ) from exc

    def _inject_memory_bank(
        self,
        runtime_model,
        memory_bank_file_key: str,
        memory_bank_bytes: bytes,
        model_version_id: int,
        device: str,
    ) -> None:
        """generated memory bank를 base ckpt에 주입한다.

        PatchcoreModel.memory_bank는 nn.Buffer이므로 직접 교체 가능하다.
        주입 전 feature dimension 일치 여부를 검증한다.

        dimension mismatch가 발생하면 AppException으로 중단한다.
        silent skip은 base ckpt 내장 memory bank(MVTec 등 훈련 데이터)를
        그대로 사용하게 되어 정상 이미지도 높은 score를 만들기 때문에 허용하지 않는다.
        """
        try:
            loaded_mb = _memory_bank_loader.load(memory_bank_file_key, memory_bank_bytes, {})
        except AppException:
            raise
        except Exception as exc:
            raise AppException(
                500,
                "Memory bank load failed",
                "메모리뱅크 로드 중 오류가 발생했습니다.",
                "AI_MEMORY_BANK_LOAD_FAILED",
            ) from exc

        payload = loaded_mb.payload
        if payload is None:
            raise AppException(
                500,
                "Memory bank payload is None",
                "메모리뱅크 파일이 비어 있거나 payload를 추출할 수 없습니다.",
                "AI_MEMORY_BANK_LOAD_FAILED",
            )

        import numpy as np

        if isinstance(payload, torch.Tensor):
            mb_tensor = payload.to(dtype=torch.float32, device=device)
        else:
            try:
                arr = np.asarray(payload, dtype=np.float32)
            except Exception as exc:
                raise AppException(
                    500,
                    "Memory bank conversion failed",
                    "메모리뱅크를 Tensor로 변환하는 데 실패했습니다.",
                    "AI_MEMORY_BANK_LOAD_FAILED",
                ) from exc
            mb_tensor = torch.from_numpy(arr).to(device)

        patchcore_model = runtime_model.model
        existing_mb = getattr(patchcore_model, "memory_bank", None)
        existing_dim: int | None = None
        if existing_mb is not None and hasattr(existing_mb, "shape") and len(existing_mb.shape) == 2:
            existing_dim = int(existing_mb.shape[1])

        injected_dim = int(mb_tensor.shape[1]) if mb_tensor.ndim == 2 else None

        # metadata에서 category, profile, featureDim 등을 추출해 로깅
        mb_meta = loaded_mb.metadata or {}
        meta_category = mb_meta.get("model_category") or mb_meta.get("modelCategory", "unknown")
        meta_profile = mb_meta.get("model_profile") or mb_meta.get("modelProfile", "unknown")
        meta_feature_dim = mb_meta.get("feature_dim") or mb_meta.get("featureDim")
        meta_input_size = mb_meta.get("image_size") or mb_meta.get("inputSize", "unknown")
        meta_backbone = mb_meta.get("backbone", "unknown")
        meta_layers = mb_meta.get("layers", [])
        meta_preprocessing = mb_meta.get("preprocessing", {})

        logger.info(
            "memory_bank_loaded modelVersionId=%s memoryBankFileKey=%s memoryBankShape=%s "
            "metadataCategory=%s metadataProfile=%s metadataFeatureDim=%s metadataInputSize=%s "
            "metadataBackbone=%s metadataLayers=%s metadataColor=%s metadataResize=%s",
            model_version_id,
            memory_bank_file_key,
            tuple(mb_tensor.shape),
            meta_category,
            meta_profile,
            meta_feature_dim,
            meta_input_size,
            meta_backbone,
            meta_layers,
            meta_preprocessing.get("color", "unknown"),
            meta_preprocessing.get("resize", "unknown"),
        )

        if existing_dim is not None and injected_dim is not None and existing_dim != injected_dim:
            # dimension mismatch 시 base ckpt memory bank를 사용하면 정상 이미지도 높은 score를 만든다.
            # silent skip 없이 명확한 오류로 처리한다.
            raise AppException(
                500,
                "Memory bank dimension mismatch",
                (
                    f"generated memory bank feature dimension({injected_dim})이 "
                    f"base ckpt feature dimension({existing_dim})과 다릅니다. "
                    "from-normal-images를 재실행하여 memory bank를 재생성하세요. "
                    f"modelVersionId={model_version_id} "
                    f"memoryBankShape={tuple(mb_tensor.shape)} "
                    f"metadataCategory={meta_category} metadataProfile={meta_profile}"
                ),
                "AI_MEMORY_BANK_DIM_MISMATCH",
            )

        patchcore_model.memory_bank = mb_tensor

        logger.info(
            "memory_bank_injected modelVersionId=%s expectedDim=%s actualDim=%s memoryBankShape=%s "
            "metadataCategory=%s metadataProfile=%s",
            model_version_id,
            existing_dim,
            injected_dim,
            tuple(mb_tensor.shape),
            meta_category,
            meta_profile,
        )
