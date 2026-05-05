from __future__ import annotations

import json
from datetime import datetime

from application.exceptions import AppException
from domain.models.memory_bank import GenerateMemoryBankCommand, GenerateMemoryBankResult, ModelStoragePort
from infrastructure.modeling.memory_bank_generator_factory import MemoryBankGeneratorFactory


class GenerateMemoryBankUseCase:
    def __init__(self, storage: ModelStoragePort, generator_factory: MemoryBankGeneratorFactory) -> None:
        self._storage = storage
        self._generator_factory = generator_factory

    def execute(self, command: GenerateMemoryBankCommand) -> GenerateMemoryBankResult:
        self._validate_command(command)
        config_bytes = self._storage.download_model_object(command.config_file_key)
        ckpt_bytes = self._storage.download_model_object(command.ckpt_file_key)
        config = self._parse_config(config_bytes)
        self._validate_config(config, command)
        image_bytes_list = [self._storage.download_model_object(key) for key in command.normal_image_file_keys]

        generator = self._generator_factory.create(command.model_category, command.model_profile)
        memory_bank_bytes = generator.generate(
            image_bytes_list=image_bytes_list,
            config=config,
            ckpt_bytes=ckpt_bytes,
        )

        memory_bank_file_key = command.output_prefix.rstrip("/") + "/memory_bank.pt"
        self._storage.upload_model_object(memory_bank_file_key, memory_bank_bytes, "application/octet-stream")

        return GenerateMemoryBankResult(
            memory_bank_file_key=memory_bank_file_key,
            normal_image_count=len(command.normal_image_file_keys),
            model_category=command.model_category,
            model_profile=command.model_profile,
            created_at=datetime.now().isoformat(timespec="seconds"),
        )

    def _validate_command(self, command: GenerateMemoryBankCommand) -> None:
        if len(command.normal_image_file_keys) < 100:
            raise AppException(422, "Not enough normal images", "memory_bank 생성을 위해 정상 이미지가 최소 100개 필요합니다.", "NORMAL_IMAGE_COUNT_TOO_SMALL")
        if not command.config_file_key.strip():
            raise AppException(400, "configFileKey is required", "configFileKey는 필수입니다.", "CONFIG_FILE_KEY_REQUIRED")
        if not command.ckpt_file_key.strip():
            raise AppException(400, "ckptFileKey is required", "ckptFileKey는 필수입니다.", "CKPT_FILE_KEY_REQUIRED")
        if not command.output_prefix.strip():
            raise AppException(400, "outputPrefix is required", "outputPrefix는 필수입니다.", "OUTPUT_PREFIX_REQUIRED")

    def _parse_config(self, config_bytes: bytes) -> dict:
        try:
            return json.loads(config_bytes.decode("utf-8"))
        except Exception as exc:
            raise AppException(422, "Invalid model config", "config 파일을 JSON으로 해석할 수 없습니다.", "INVALID_MODEL_CONFIG") from exc

    def _validate_config(self, config: dict, command: GenerateMemoryBankCommand) -> None:
        category = config.get("modelCategory") or config.get("model_category")
        profile = config.get("modelProfile") or config.get("model_profile")
        if category is not None and str(category).upper() != command.model_category.value:
            raise AppException(422, "Config mismatch", "config의 modelCategory가 요청값과 일치하지 않습니다.", "CONFIG_CATEGORY_MISMATCH")
        if profile is not None and str(profile).upper() != command.model_profile.value:
            raise AppException(422, "Config mismatch", "config의 modelProfile이 요청값과 일치하지 않습니다.", "CONFIG_PROFILE_MISMATCH")
