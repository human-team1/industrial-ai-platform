from datetime import datetime

from fastapi.testclient import TestClient

from container.dependencies import (
    get_deindex_document_version_usecase,
    get_process_document_index_job_usecase,
    get_rag_service,
)
from domain.document_models import DocumentDeindexCommand, DocumentDeindexResult
from domain.rag.query_models import RagQueryResult
from main import app


class NoopProcessUseCase:
    async def process(self, job, request_id: str) -> None:
        return None


class FakeDeindexUseCase:
    def deindex(self, command: DocumentDeindexCommand) -> DocumentDeindexResult:
        return DocumentDeindexResult(
            document_id=command.document_id,
            document_version_id=command.document_version_id,
            organization_id=command.organization_id,
            collection_name=f"documents_org_{command.organization_id}",
            deleted_vector_count=0,
            deindexed_at=datetime(2026, 5, 5, 10, 30, 0),
        )


class FakeRagService:
    def query(self, request) -> RagQueryResult:
        return RagQueryResult(
            answer="먼저 컨베이어 가이드 위치와 체인 장력을 확인하세요.",
            answer_type="ANSWERED",
            question_mode="DOCUMENT_SEARCH",
            metadata={"answerStatus": "ANSWERED", "llmModel": "test-llm", "createdAt": "2026-05-05T10:40:00"},
        )


def _valid_index_payload() -> dict:
    return {
        "indexJobId": 3001,
        "documentId": 1001,
        "documentVersionId": 2001,
        "organizationId": 1001,
        "file": {
            "fileId": 501,
            "fileKey": "documents/org-1001/doc-1001/v1/manual.pdf",
            "fileName": "manual.pdf",
            "mimeType": "application/pdf",
        },
        "metadata": {
            "title": "프레스 설비 점검 매뉴얼",
            "documentType": "MANUAL",
            "category": "MAINTENANCE",
            "equipmentType": "PRESS",
        },
        "chunking": {"chunkSize": 800, "chunkOverlap": 120},
        "embedding": {"embeddingModel": "default"},
    }


def test_internal_document_index_enqueue_returns_processing() -> None:
    app.dependency_overrides[get_process_document_index_job_usecase] = lambda: NoopProcessUseCase()
    client = TestClient(app)

    response = client.post(
        "/ai/v1/internal/documents/index",
        headers={"X-Request-Id": "test-index-job"},
        json=_valid_index_payload(),
    )

    body = response.json()
    assert response.status_code == 200
    assert body["success"] is True
    assert body["data"]["aiJobId"] == "doc-index-3001"
    assert body["data"]["indexingStatus"] == "PENDING"
    assert body["data"]["collectionName"] == "documents_org_1001"
    app.dependency_overrides.clear()


def test_internal_document_index_requires_file_key() -> None:
    client = TestClient(app)
    payload = _valid_index_payload()
    payload["file"]["fileKey"] = None

    response = client.post(
        "/ai/v1/internal/documents/index",
        headers={"X-Request-Id": "test-missing-file-key"},
        json=payload,
    )

    body = response.json()
    assert response.status_code == 400
    assert body["status"] == 400
    assert body["errorCode"] == "DOCUMENT-JOB-400"
    assert body["requestId"] == "test-missing-file-key"


def test_internal_document_index_rejects_unsupported_mime() -> None:
    client = TestClient(app)
    payload = _valid_index_payload()
    payload["file"]["mimeType"] = "application/x-msdownload"

    response = client.post(
        "/ai/v1/internal/documents/index",
        headers={"X-Request-Id": "test-unsupported-type"},
        json=payload,
    )

    body = response.json()
    assert response.status_code == 422
    assert body["status"] == 422
    assert body["errorCode"] == "DOCUMENT-JOB-422"


def test_internal_document_index_rejects_invalid_chunk_overlap() -> None:
    client = TestClient(app)
    payload = _valid_index_payload()
    payload["chunking"] = {"chunkSize": 100, "chunkOverlap": 100}

    response = client.post("/ai/v1/internal/documents/index", json=payload)

    assert response.status_code == 422
    assert response.json()["errorCode"] == "DOCUMENT-JOB-422"


def test_get_document_index_job_status() -> None:
    app.dependency_overrides[get_process_document_index_job_usecase] = lambda: NoopProcessUseCase()
    client = TestClient(app)
    client.post("/ai/v1/internal/documents/index", json=_valid_index_payload())

    response = client.get("/ai/v1/internal/document-index-jobs/doc-index-3001")

    assert response.status_code == 200
    assert response.json()["data"]["aiJobId"] == "doc-index-3001"
    app.dependency_overrides.clear()


def test_deindex_document_version() -> None:
    app.dependency_overrides[get_deindex_document_version_usecase] = lambda: FakeDeindexUseCase()
    client = TestClient(app)

    response = client.request(
        "DELETE",
        "/ai/v1/internal/document-versions/2001/index",
        json={"organizationId": 1001, "documentId": 1001},
    )

    assert response.status_code == 200
    assert response.json()["data"]["collectionName"] == "documents_org_1001"
    app.dependency_overrides.clear()


def test_internal_rag_query() -> None:
    app.dependency_overrides[get_rag_service] = lambda: FakeRagService()
    client = TestClient(app)

    response = client.post(
        "/ai/v1/internal/rag/query",
        json={
            "question": "컨베이어 정렬이 틀어졌을 때 점검 순서는?",
            "userId": 1,
            "organizationId": 1001,
            "topK": 5,
        },
    )

    assert response.status_code == 200
    assert response.json()["data"]["answerStatus"] == "ANSWERED"
    app.dependency_overrides.clear()


def test_internal_rag_query_rejects_blank_question() -> None:
    client = TestClient(app)

    response = client.post(
        "/ai/v1/internal/rag/query",
        json={"question": "", "userId": 1, "organizationId": 1001},
    )

    assert response.status_code == 422
