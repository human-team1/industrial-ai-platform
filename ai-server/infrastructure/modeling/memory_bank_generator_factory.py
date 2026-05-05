from __future__ import annotations

from application.exceptions import AppException
from domain.models.memory_bank import MemoryBankProfileSpec, ModelCategory, ModelProfile
from infrastructure.modeling.patchcore_memory_bank_generator import PatchCoreMemoryBankGenerator


PROFILE_SPECS: dict[tuple[ModelCategory, ModelProfile], MemoryBankProfileSpec] = {
    (ModelCategory.OBJECT, ModelProfile.PERFORMANCE): MemoryBankProfileSpec(
        category=ModelCategory.OBJECT,
        profile=ModelProfile.PERFORMANCE,
        pipeline="DINOv2-base + PatchCore",
        image_size=(336, 336),
        target_memory_bank_size=2000,
        layers=("blocks.3", "blocks.7", "blocks.11"),
    ),
    (ModelCategory.TEXTURE, ModelProfile.PERFORMANCE): MemoryBankProfileSpec(
        category=ModelCategory.TEXTURE,
        profile=ModelProfile.PERFORMANCE,
        pipeline="DINOv2-base + PatchCore",
        image_size=(448, 448),
        target_memory_bank_size=2000,
        layers=("blocks.3", "blocks.7", "blocks.11"),
    ),
    (ModelCategory.OBJECT, ModelProfile.SPEED): MemoryBankProfileSpec(
        category=ModelCategory.OBJECT,
        profile=ModelProfile.SPEED,
        pipeline="WideResNet50 + PatchCore",
        image_size=(224, 224),
        target_memory_bank_size=10000,
        layers=("layer2",),
    ),
    (ModelCategory.TEXTURE, ModelProfile.SPEED): MemoryBankProfileSpec(
        category=ModelCategory.TEXTURE,
        profile=ModelProfile.SPEED,
        pipeline="WideResNet50 + PatchCore",
        image_size=(256, 256),
        target_memory_bank_size=5000,
        layers=("layer2",),
    ),
}


class MemoryBankGeneratorFactory:
    def create(self, category: ModelCategory, profile: ModelProfile) -> PatchCoreMemoryBankGenerator:
        spec = PROFILE_SPECS.get((category, profile))
        if spec is None:
            raise AppException(404, "Model profile not found", "고정 모델 프로필 매핑을 찾을 수 없습니다.", "MODEL_PROFILE_NOT_FOUND")
        return PatchCoreMemoryBankGenerator(spec)
