from typing import Protocol

from domain.vision_models import InferenceOutput, LoadedMemoryBank, LoadedVisionModel, PreprocessedImage, QualityMetrics


class StoragePort(Protocol):
    def download_object(self, bucket_name: str, object_name: str) -> bytes: ...

    def put_object(self, bucket_name: str, object_name: str, content: bytes, content_type: str = "application/octet-stream") -> None: ...


class VisionConfigLoaderPort(Protocol):
    def load(self, config_bytes: bytes) -> dict: ...


class VisionModelLoaderPort(Protocol):
    def load(self, model_version_id: int, ckpt_file_key: str, config_file_key: str, memory_bank_file_key: str, ckpt_bytes: bytes, config: dict) -> LoadedVisionModel: ...


class MemoryBankLoaderPort(Protocol):
    def load(self, file_key: str, memory_bank_bytes: bytes, config: dict) -> LoadedMemoryBank: ...


class ImagePreprocessorPort(Protocol):
    def preprocess(self, image_bytes: bytes, roi: dict | None, input_size: str | None, config: dict) -> PreprocessedImage: ...


class QualityEvaluatorPort(Protocol):
    def evaluate(self, image_array) -> QualityMetrics: ...


class VisionInferencerPort(Protocol):
    def infer(self, image: PreprocessedImage, model: LoadedVisionModel, config: dict, memory_bank: LoadedMemoryBank) -> InferenceOutput: ...


class HeatmapGeneratorPort(Protocol):
    def render(self, image, anomaly_map) -> bytes: ...
