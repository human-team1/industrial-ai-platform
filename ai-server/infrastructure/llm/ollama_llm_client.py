from __future__ import annotations

import time
from typing import Any

import httpx

from domain.rag.models import LLMGenerateResult


class OllamaLLMClient:
    """
    Ollama Local LLM 호출 클라이언트.

    Step 11에서는 /api/generate + stream=false 기준으로 사용한다.
    """

    def __init__(
        self,
        *,
        base_url: str,
        model_name: str,
        temperature: float,
        max_tokens: int,
        timeout_seconds: int,
    ) -> None:
        self.base_url = base_url.rstrip("/")
        self.model_name = model_name
        self.temperature = temperature
        self.max_tokens = max_tokens
        self.timeout_seconds = timeout_seconds

    def generate(self, *, prompt: str) -> LLMGenerateResult:
        if not prompt.strip():
            raise ValueError("prompt is empty")

        started_at = time.perf_counter()

        payload: dict[str, Any] = {
            "model": self.model_name,
            "prompt": prompt,
            "stream": False,
            "options": {
                "temperature": self.temperature,
                "num_predict": self.max_tokens,
            },
        }

        with httpx.Client(timeout=self.timeout_seconds) as client:
            response = client.post(
                f"{self.base_url}/api/generate",
                json=payload,
            )
            response.raise_for_status()
            data = response.json()

        latency_ms = int((time.perf_counter() - started_at) * 1000)

        answer = str(data.get("response") or "").strip()

        if not answer:
            raise RuntimeError("ollama returned empty response")

        prompt_tokens = data.get("prompt_eval_count")
        completion_tokens = data.get("eval_count")

        total_tokens = None
        if isinstance(prompt_tokens, int) and isinstance(completion_tokens, int):
            total_tokens = prompt_tokens + completion_tokens

        return LLMGenerateResult(
            answer=answer,
            model_name=self.model_name,
            prompt_tokens=prompt_tokens,
            completion_tokens=completion_tokens,
            total_tokens=total_tokens,
            latency_ms=latency_ms,
            raw_response={
                "done": data.get("done"),
                "total_duration": data.get("total_duration"),
                "load_duration": data.get("load_duration"),
                "prompt_eval_duration": data.get("prompt_eval_duration"),
                "eval_duration": data.get("eval_duration"),
            },
        )