from __future__ import annotations

import logging

from fastapi import APIRouter, Depends, Request
from pydantic import BaseModel, ConfigDict, Field

from application.exceptions import AppException
from application.models.generate_memory_bank_usecase import GenerateMemoryBankUseCase
from container.model_container import create_generate_memory_bank_usecase
from domain.models.memory_bank import GenerateMemoryBankCommand, ModelCategory, ModelProfile

router = APIRouter()
log = logging.getLogger(__name__)


class MemoryBankGenerateRequest(BaseModel):
    model_config = ConfigDict(protected_namespaces=())

    model_category: str | None = Field(default=None, alias="modelCategory")
    model_profile: str | None = Field(default=None, alias="modelProfile")
    normal_image_file_keys: list[str] | None = Field(default=None, alias="normalImageFileKeys")
    config_file_key: str | None = Field(default=None, alias="configFileKey")
    ckpt_file_key: str | None = Field(default=None, alias="ckptFileKey")
    output_prefix: str | None = Field(default=None, alias="outputPrefix")

    def to_command(self, request_id: str) -> GenerateMemoryBankCommand:
        if self.model_category is None:
            raise AppException(400, "modelCategory is required", "modelCategory is required.", "MODEL_CATEGORY_REQUIRED")
        if self.model_profile is None:
            raise AppException(400, "modelProfile is required", "modelProfile is required.", "MODEL_PROFILE_REQUIRED")
        if self.normal_image_file_keys is None:
            raise AppException(400, "normalImageFileKeys is required", "normalImageFileKeys is required.", "NORMAL_IMAGE_FILE_KEYS_REQUIRED")
        if self.config_file_key is None:
            raise AppException(400, "configFileKey is required", "configFileKey is required.", "MODEL_CONFIG_REQUIRED")
        if self.ckpt_file_key is None:
            raise AppException(400, "ckptFileKey is required", "ckptFileKey is required.", "MODEL_CKPT_REQUIRED")
        if self.output_prefix is None:
            raise AppException(400, "outputPrefix is required", "outputPrefix is required.", "OUTPUT_PREFIX_REQUIRED")
        try:
            category = ModelCategory(str(self.model_category).upper())
        except ValueError as exc:
            raise AppException(422, "Invalid modelCategory", "modelCategory must be OBJECT or TEXTURE.", "INVALID_MODEL_CATEGORY") from exc
        try:
            profile = ModelProfile(str(self.model_profile).upper())
        except ValueError as exc:
            raise AppException(422, "Invalid modelProfile", "modelProfile must be SPEED or PERFORMANCE.", "INVALID_MODEL_PROFILE") from exc
        return GenerateMemoryBankCommand(
            model_category=category,
            model_profile=profile,
            normal_image_file_keys=self.normal_image_file_keys,
            config_file_key=self.config_file_key,
            ckpt_file_key=self.ckpt_file_key,
            output_prefix=self.output_prefix,
            request_id=request_id,
        )


@router.post("/models/memory-bank")
async def generate_memory_bank(
    request_body: MemoryBankGenerateRequest,
    request: Request,
    usecase: GenerateMemoryBankUseCase = Depends(create_generate_memory_bank_usecase),
) -> dict:
    request_id = getattr(request.state, "request_id", None) or request.headers.get("X-Request-Id") or ""
    log.info(
        "memory_bank_request_received requestId=%s modelCategory=%s modelProfile=%s normalImageCount=%s ckptFileKey=%s configFileKey=%s outputPrefix=%s",
        request_id,
        request_body.model_category,
        request_body.model_profile,
        len(request_body.normal_image_file_keys or []),
        request_body.ckpt_file_key,
        request_body.config_file_key,
        request_body.output_prefix,
    )
    command = request_body.to_command(request_id)
    result = usecase.execute(command)
    log.info(
        "memory_bank generation completed requestId=%s modelCategory=%s modelProfile=%s normalImageCount=%s memoryBankFileKey=%s configFileKey=%s",
        request_id,
        result.model_category.value,
        result.model_profile.value,
        result.normal_image_count,
        result.memory_bank_file_key,
        result.config_file_key,
    )
    return {
        "success": True,
        "data": {
            "memoryBankFileKey": result.memory_bank_file_key,
            "configFileKey": result.config_file_key,
            "ckptFileKey": result.ckpt_file_key,
            "normalImageCount": result.normal_image_count,
            "modelCategory": result.model_category.value,
            "modelProfile": result.model_profile.value,
            "inputSize": result.input_size,
            "framework": result.framework,
            "createdAt": result.created_at,
        },
        "message": "memory_bank generated.",
    }
