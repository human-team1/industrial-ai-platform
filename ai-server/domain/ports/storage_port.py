from abc import ABC, abstractmethod


class StoragePort(ABC):
    @abstractmethod
    def download_file(self, file_key: str, local_path: str) -> str:
        pass

    @abstractmethod
    def upload_file(self, local_path: str, file_key: str) -> str:
        pass