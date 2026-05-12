from types import SimpleNamespace

from application.rag.nodes import retrieve_documents, validate_input
from domain.rag.models import AnswerType
from domain.rag.state import GraphState
from infrastructure.retriever.chroma_retriever import ChromaRetriever


def test_validate_input_allows_general_question_without_organization_id() -> None:
    state = GraphState(question="사용법 알려줘", organization_id=None)

    result = validate_input(state)

    assert result["normalized_question"]
    assert result.get("answer_type") is None


def test_retrieve_documents_returns_validation_error_without_organization_id() -> None:
    runtime = SimpleNamespace(
        rag_default_document_status="PUBLISHED",
        rag_top_k=5,
        retriever_type="chroma",
        retrieval_config_id="cfg-1",
        retriever=SimpleNamespace(search=lambda **kwargs: []),
    )
    state = GraphState(
        question="정비 문서 찾아줘",
        normalized_question="정비 문서 찾아줘",
        organization_id=None,
        need_retrieval=True,
    )

    result = retrieve_documents(state, runtime=runtime)

    assert result["answer_type"] == AnswerType.VALIDATION_ERROR
    assert "organization_id_missing" in result["errors"]
    assert result["retriever_called"] is False


def test_chroma_retriever_uses_filter_organization_id_as_metadata_fallback() -> None:
    settings = SimpleNamespace(
        rag_top_k=5,
        retrieval_internal_top_k=10,
        rag_default_document_status="PUBLISHED",
        rag_min_score=0.0,
    )
    retriever = ChromaRetriever(settings, SimpleNamespace(), SimpleNamespace())
    raw_result = {
        "ids": [["chunk-1"]],
        "documents": [["본문"]],
        "metadatas": [[{"chunkId": "chunk-1", "documentId": "doc-1", "documentVersionId": "ver-1", "title": "문서"}]],
        "distances": [[0.2]],
    }

    sources = retriever._to_source_chunks(raw_result, 5, fallback_organization_id="101")

    assert len(sources) == 1
    assert sources[0].metadata["organization_id"] == "101"
