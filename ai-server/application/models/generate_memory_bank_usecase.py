from __future__ import annotations

import json
import logging
from contextlib import contextmanager
from datetime import datetime
from time import perf_counter

from application.exceptions import AppException
from domain.models.memory_bank import GenerateMemoryBankCommand, GenerateMemoryBankResult, ModelStoragePort
from infrastructure.modeling.memory_bank_generator_factory import MemoryBankGeneratorFactory

log = logging.getLogger(__name__)


class GenerateMemoryBankUseCase:
    def __init__(self, storage: ModelStoragePort, generator_factory: MemoryBankGeneratorFactory) -> None:
        self._storage = storage
        self._generator_factory = generator_factory

    def execute(self, command: GenerateMemoryBankCommand) -> GenerateMemoryBankResult:
        try:
            with self._stage(command, "validate_request_start", "validate_request_end"):
                self._validate_command(command)

            with self._stage(command, "ckpt_exists_start", "ckpt_exists_end", objectKey=command.ckpt_file_key):
                pass
            with self._stage(command, "config_exists_start", "config_exists_end", objectKey=command.config_file_key):
                pass
            with self._stage(command, "normal_images_exists_start", "normal_images_exists_end", normalImageCount=len(command.normal_image_file_keys)):
                pass

            with self._stage(command, "config_download_start", "config_download_end", objectKey=command.config_file_key):
                config_bytes = self._storage.download_config_object(command.config_file_key)
            with self._stage(command, "ckpt_download_start", "ckpt_download_end", objectKey=command.ckpt_file_key):
                ckpt_bytes = self._storage.download_ckpt_object(command.ckpt_file_key)
            with self._stage(command, "config_parse_start", "config_parse_end", fileSizeBytes=len(config_bytes)):
                config = self._parse_config(config_bytes)
                spec = self._generator_factory.to_spec(command.model_category, command.model_profile, config)
                self._validate_config(config, command)

            image_bytes_list = []
            with self._stage(command, "normal_images_download_start", "normal_images_download_end", normalImageCount=len(command.normal_image_file_keys)):
                for image_index, key in enumerate(command.normal_image_file_keys):
                    started_at = perf_counter()
                    self._log_stage(command, "normal_image_download_start", 0, imageIndex=image_index, objectKey=key)
                    try:
                        image_bytes = self._storage.download_normal_image_object(key)
                        elapsed_ms = int((perf_counter() - started_at) * 1000)
                        self._log_stage(
                            command,
                            "normal_image_download_end",
                            elapsed_ms,
                            imageIndex=image_index,
                            objectKey=key,
                            fileSizeBytes=len(image_bytes),
                        )
                        image_bytes_list.append(image_bytes)
                    except AppException as exc:
                        elapsed_ms = int((perf_counter() - started_at) * 1000)
                        self._log_stage(
                            command,
                            "normal_image_download_failed",
                            elapsed_ms,
                            imageIndex=image_index,
                            objectKey=key,
                            errorCode=exc.code,
                            exceptionClass=exc.__class__.__name__,
                            exceptionMessage=exc.detail,
                        )
                        raise
                    except Exception as exc:
                        elapsed_ms = int((perf_counter() - started_at) * 1000)
                        self._log_stage(
                            command,
                            "normal_image_download_failed",
                            elapsed_ms,
                            imageIndex=image_index,
                            objectKey=key,
                            errorCode="NORMAL_IMAGE_DOWNLOAD_FAILED",
                            exceptionClass=exc.__class__.__name__,
                            exceptionMessage=str(exc),
                        )
                        raise

            generator = self._generator_factory.create_from_spec(spec)
            memory_bank_bytes = generator.generate(
                image_bytes_list=image_bytes_list,
                config=config,
                ckpt_bytes=ckpt_bytes,
                request_id=command.request_id,
            )

            output_prefix = command.output_prefix.rstrip("/")
            memory_bank_file_key = output_prefix + "/memory_bank.pt"
            config_file_key = output_prefix + "/config.json"
            with self._stage(command, "memory_bank_upload_start", "memory_bank_upload_end", objectKey=memory_bank_file_key, fileSizeBytes=len(memory_bank_bytes)):
                self._storage.upload_model_object(memory_bank_file_key, memory_bank_bytes, "application/octet-stream")
            config_snapshot_bytes = json.dumps(config, ensure_ascii=False, indent=2).encode("utf-8")
            with self._stage(command, "config_snapshot_upload_start", "config_snapshot_upload_end", objectKey=config_file_key, fileSizeBytes=len(config_snapshot_bytes)):
                self._storage.upload_model_object(config_file_key, config_snapshot_bytes, "application/json")

            self._log_stage(command, "response_success", 0)
            return GenerateMemoryBankResult(
                memory_bank_file_key=memory_bank_file_key,
                config_file_key=config_file_key,
                ckpt_file_key=command.ckpt_file_key,
                normal_image_count=len(command.normal_image_file_keys),
                model_category=command.model_category,
                model_profile=command.model_profile,
                input_size=f"{spec.image_size[0]}x{spec.image_size[1]}",
                framework=spec.framework,
                created_at=datetime.now().isoformat(timespec="seconds"),
            )
        except Exception:
            self._log_stage(command, "response_failed", 0)
            raise

    def _validate_command(self, command: GenerateMemoryBankCommand) -> None:
        if not command.config_file_key.strip():
            raise AppException(400, "configFileKey is required", "configFileKey is required.", "MODEL_CONFIG_REQUIRED")
        if not command.ckpt_file_key.strip():
            raise AppException(400, "ckptFileKey is required", "ckptFileKey is required.", "MODEL_CKPT_REQUIRED")
        if not command.output_prefix.strip():
            raise AppException(400, "outputPrefix is required", "outputPrefix is required.", "OUTPUT_PREFIX_REQUIRED")
        if len(command.normal_image_file_keys) < 10:
            raise AppException(
                422,
                "Not enough normal images",
                "memory_bank generation requires at least 10 normal images.",
                "NORMAL_IMAGE_COUNT_TOO_SMALL",
            )

    def _parse_config(self, config_bytes: bytes) -> dict:
        try:
            return json.loads(config_bytes.decode("utf-8"))
        except Exception as exc:
            raise AppException(422, "Invalid model config", "config json is invalid.", "MODEL_CONFIG_INVALID") from exc

    def _validate_config(self, config: dict, command: GenerateMemoryBankCommand) -> None:
        category = config.get("modelCategory") or config.get("model_category")
        profile = config.get("modelProfile") or config.get("model_profile")
        if category is not None and str(category).upper() != command.model_category.value:
            raise AppException(422, "Config mismatch", "config modelCategory does not match request.", "MODEL_CONFIG_INVALID")
        if profile is not None and str(profile).upper() != command.model_profile.value:
            raise AppException(422, "Config mismatch", "config modelProfile does not match request.", "MODEL_CONFIG_INVALID")

    @contextmanager
    def _stage(self, command: GenerateMemoryBankCommand, start_stage: str, end_stage: str, **fields):
        started_at = perf_counter()
        self._log_stage(command, start_stage, 0, **fields)
        try:
            yield
        finally:
            elapsed_ms = int((perf_counter() - started_at) * 1000)
            self._log_stage(command, end_stage, elapsed_ms, **fields)

    def _log_stage(self, command: GenerateMemoryBankCommand, stage: str, elapsed_ms: int, **fields) -> None:
        extra = " ".join(f"{key}={value}" for key, value in fields.items() if value is not None)
        message = (
            "memory_bank_stage requestId=%s stage=%s elapsedMs=%s modelCategory=%s modelProfile=%s %s"
        )
        args = (command.request_id, stage, elapsed_ms, command.model_category.value, command.model_profile.value, extra)
        if elapsed_ms >= 60000:
            log.error("memory_bank_stage_slow requestId=%s stage=%s elapsedMs=%s modelCategory=%s modelProfile=%s %s", *args)
        elif elapsed_ms >= 30000:
            log.warning("memory_bank_stage_slow requestId=%s stage=%s elapsedMs=%s modelCategory=%s modelProfile=%s %s", *args)
        else:
            log.info(message, *args)
