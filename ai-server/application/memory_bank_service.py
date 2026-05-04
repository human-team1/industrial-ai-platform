import tempfile
from datetime import datetime
from pathlib import Path

from api.schemas import MemoryBankApiResponse, MemoryBankRequest, MemoryBankResponseData
from infrastructure.minio_storage import MinioStorage


class MemoryBankService:
    def __init__(self, storage: MinioStorage):
        self.storage = storage

    async def create_memory_bank(
        self,
        payload: MemoryBankRequest,
        request_id: str,
    ) -> MemoryBankApiResponse:
        bucket_name = self.storage._settings.minio_bucket

        with tempfile.TemporaryDirectory(prefix=f"memory-bank-{request_id}-") as work_dir:
            work_path = Path(work_dir)

            image_dir = work_path / "normal_images"
            model_dir = work_path / "model"
            output_dir = work_path / "output"

            image_dir.mkdir(parents=True, exist_ok=True)
            model_dir.mkdir(parents=True, exist_ok=True)
            output_dir.mkdir(parents=True, exist_ok=True)

            local_image_paths: list[str] = []

            for index, file_key in enumerate(payload.normalImageFileKeys, start=1):
                suffix = Path(file_key).suffix or ".jpg"
                local_path = image_dir / f"normal_{index:04d}{suffix}"

                try:
                    print("DOWNLOAD TRY:", file_key)

                    self.storage.download_file(
                        bucket_name=bucket_name,
                        object_name=file_key,
                        local_path=str(local_path),
                    )

                except Exception as e:
                    print("🔥 FAILED FILE:", file_key)
                    raise e

                local_image_paths.append(str(local_path))

            local_ckpt_path = model_dir / "model.ckpt"
            local_config_path = model_dir / "config.json"

            self.storage.download_file(
                bucket_name=bucket_name,
                object_name=payload.ckptFileKey,
                local_path=str(local_ckpt_path),
            )

            self.storage.download_file(
                bucket_name=bucket_name,
                object_name=payload.configFileKey,
                local_path=str(local_config_path),
            )

            local_memory_bank_path = output_dir / "memory_bank.pt"

            with open(local_memory_bank_path, "wb") as f:
                f.write(b"dummy memory bank")

            memory_bank_file_key = f"{payload.outputPrefix.rstrip('/')}/memory_bank.pt"

            self.storage.upload_file(
                bucket_name=bucket_name,
                object_name=memory_bank_file_key,
                local_path=str(local_memory_bank_path),
            )

            return MemoryBankApiResponse(
                success=True,
                data=MemoryBankResponseData(
                    memoryBankFileKey=memory_bank_file_key,
                    normalImageCount=len(local_image_paths),
                    modelCategory=payload.modelCategory,
                    modelProfile=payload.modelProfile,
                    createdAt=datetime.now(),
                ),
                message="메모리뱅크가 생성되었습니다.",
            )