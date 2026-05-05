from typing import Protocol


class InfraHealthPort(Protocol):
    def health_check(self) -> bool: ...
