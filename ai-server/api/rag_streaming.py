from __future__ import annotations

import json
from collections.abc import AsyncIterator
from typing import Any

from api.schemas import RagQueryResponse


def encode_sse(event: str, data: dict[str, Any]) -> str:
    payload = json.dumps(data, ensure_ascii=False, default=str)
    return f"event: {event}\ndata: {payload}\n\n"


async def stream_rag_response(response: RagQueryResponse) -> AsyncIterator[str]:
    yield encode_sse(
        "metadata",
        {
            "answerStatus": response.answer_status,
            "questionMode": response.question_mode,
            "llmModel": response.llm_model,
            "sourceCount": len(response.sources),
            "resultId": response.result_id,
        },
    )

    answer = response.answer or ""
    for chunk in _split_answer(answer):
        yield encode_sse("answer_delta", {"delta": chunk})

    yield encode_sse(
        "done",
        {
            "answer": response.answer,
            "answerStatus": response.answer_status,
            "sources": [source.model_dump(by_alias=True) for source in response.sources],
            "metadata": response.metadata,
            "errors": response.errors,
        },
    )


def _split_answer(answer: str, chunk_size: int = 120) -> list[str]:
    if not answer:
        return []
    return [answer[index : index + chunk_size] for index in range(0, len(answer), chunk_size)]
