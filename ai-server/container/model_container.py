from __future__ import annotations

from application.models.generate_memory_bank_usecase import GenerateMemoryBankUseCase
from config.settings import get_settings
from infrastructure.minio_storage import MinioStorage
from infrastructure.modeling.memory_bank_generator_factory import MemoryBankGeneratorFactory
from infrastructure.storage.model_storage import MinioModelStorage


def create_generate_memory_bank_usecase() -> GenerateMemoryBankUseCase:
    settings = get_settings()
    storage = MinioModelStorage(settings, MinioStorage(settings))
    return GenerateMemoryBankUseCase(storage, MemoryBankGeneratorFactory())
