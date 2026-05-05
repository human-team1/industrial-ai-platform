from __future__ import annotations

from typing import Generic, TypeVar

from pydantic import BaseModel

from domain.rag.response_schema import RagQueryResponse

T = TypeVar("T")


class ApiSuccessResponse(BaseModel, Generic[T]):
    """프로젝트 공통 성공 응답 래퍼."""

    success: bool = True
    data: T
    message: str = "요청이 성공적으로 처리되었습니다."


class ProblemDetail(BaseModel):
    """RFC 9457 Problem Details 기반 실패 응답 스키마."""

    type: str = "about:blank"
    title: str
    status: int
    detail: str
    instance: str | None = None
    errorCode: str | None = None
    errors: list[dict[str, str]] | None = None
    requestId: str | None = None


class RagQuerySuccessResponse(ApiSuccessResponse[RagQueryResponse]):
    """RAG API 성공 응답 래퍼."""

    pass
