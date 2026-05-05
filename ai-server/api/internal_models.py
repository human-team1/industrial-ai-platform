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
        if self.normal_image_file_keys is None:
            raise AppException(400, "normalImageFileKeys is required", "normalImageFileKeys는 필수입니다.", "NORMAL_IMAGE_FILE_KEYS_REQUIRED")
        try:
            category = ModelCategory(str(self.model_category or "").upper())
        except ValueError as exc:
            raise AppException(422, "Invalid modelCategory", "modelCategory는 OBJECT 또는 TEXTURE만 허용됩니다.", "INVALID_MODEL_CATEGORY") from exc
        try:
            profile = ModelProfile(str(self.model_profile or "").upper())
        except ValueError as exc:
            raise AppException(422, "Invalid modelProfile", "modelProfile은 SPEED 또는 PERFORMANCE만 허용됩니다.", "INVALID_MODEL_PROFILE") from exc
        return GenerateMemoryBankCommand(
            model_category=category,
            model_profile=profile,
            normal_image_file_keys=self.normal_image_file_keys,
            config_file_key=self.config_file_key or "",
            ckpt_file_key=self.ckpt_file_key or "",
            output_prefix=self.output_prefix or "",
            request_id=request_id,
        )


@router.post("/models/memory-bank")
async def generate_memory_bank(
    request_body: MemoryBankGenerateRequest,
    request: Request,
    usecase: GenerateMemoryBankUseCase = Depends(create_generate_memory_bank_usecase),
) -> dict:
    request_id = getattr(request.state, "request_id", None) or request.headers.get("X-Request-Id") or ""
    command = request_body.to_command(request_id)
    log.info(
        "memory_bank generation requested, requestId=%s, modelCategory=%s, modelProfile=%s, normalImageCount=%s",
        request_id,
        command.model_category.value,
        command.model_profile.value,
        len(command.normal_image_file_keys),
    )
    result = usecase.execute(command)
    log.info(
        "memory_bank generation completed, requestId=%s, modelCategory=%s, modelProfile=%s, normalImageCount=%s, memoryBankFileKey=%s",
        request_id,
        result.model_category.value,
        result.model_profile.value,
        result.normal_image_count,
        result.memory_bank_file_key,
    )
    return {
        "success": True,
        "data": {
            "memoryBankFileKey": result.memory_bank_file_key,
            "normalImageCount": result.normal_image_count,
            "modelCategory": result.model_category.value,
            "modelProfile": result.model_profile.value,
            "createdAt": result.created_at,
        },
        "message": "메모리뱅크가 생성되었습니다.",
    }
