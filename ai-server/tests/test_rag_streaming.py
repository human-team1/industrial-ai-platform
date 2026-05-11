from fastapi.testclient import TestClient

from container.dependencies import get_rag_graph_runner
from domain.rag.models import AnswerType, QuestionMode
from domain.rag.state import GraphState
from main import app


class FakeRagRunner:
    async def arun(self, *, state: GraphState, query_case_id=None, extra_metadata=None) -> GraphState:
        return state.model_copy(
            update={
                "answer": "첫 번째 조치입니다. 두 번째 조치입니다.",
                "answer_type": AnswerType.DOCUMENT_SEARCH,
                "question_mode": QuestionMode.DOCUMENT_SEARCH,
                "citation_ok": True,
                "llm_model": "test-llm",
                "route_path": ["validate_input", "finalize_response"],
            }
        )


def test_public_rag_query_streams_sse_events() -> None:
    app.dependency_overrides[get_rag_graph_runner] = lambda: FakeRagRunner()
    try:
        client = TestClient(app)

        with client.stream(
            "POST",
            "/ai/v1/rag/query",
            json={
                "question": "점검 절차 알려줘",
                "userId": 1,
                "organizationId": 1,
                "stream": True,
            },
        ) as response:
            body = response.read().decode("utf-8")

        assert response.status_code == 200
        assert response.headers["content-type"].startswith("text/event-stream")
        assert "event: metadata" in body
        assert "event: answer_delta" in body
        assert "event: done" in body
        assert "첫 번째 조치입니다" in body
    finally:
        app.dependency_overrides.pop(get_rag_graph_runner, None)


def test_internal_rag_query_streams_sse_events() -> None:
    app.dependency_overrides[get_rag_graph_runner] = lambda: FakeRagRunner()
    try:
        client = TestClient(app)

        with client.stream(
            "POST",
            "/ai/v1/internal/rag/query",
            json={
                "question": "점검 절차 알려줘",
                "userId": 1,
                "organizationId": 1,
                "stream": True,
            },
        ) as response:
            body = response.read().decode("utf-8")

        assert response.status_code == 200
        assert response.headers["content-type"].startswith("text/event-stream")
        assert "event: metadata" in body
        assert "event: answer_delta" in body
        assert "event: done" in body
    finally:
        app.dependency_overrides.pop(get_rag_graph_runner, None)
