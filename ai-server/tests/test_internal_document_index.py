from fastapi.testclient import TestClient

from main import app


def test_internal_document_index_requires_file_key() -> None:
    client = TestClient(app)

    response = client.post(
        "/ai/v1/internal/documents/index",
        headers={"X-Request-Id": "test-missing-file-key"},
        json={
            "documentId": 101,
            "documentVersionId": 1001,
            "fileId": 501,
            "documentType": "PDF",
        },
    )

    body = response.json()
    assert response.status_code == 400
    assert body["status"] == 400
    assert body["errorCode"] == "DOCUMENT-400"
    assert body["requestId"] == "test-missing-file-key"


def test_internal_document_index_rejects_unsupported_document_type() -> None:
    client = TestClient(app)

    response = client.post(
        "/ai/v1/internal/documents/index",
        headers={"X-Request-Id": "test-unsupported-type"},
        json={
            "documentId": 101,
            "documentVersionId": 1001,
            "fileId": 501,
            "fileKey": "documents/malware.exe",
            "documentType": "EXE",
        },
    )

    body = response.json()
    assert response.status_code == 422
    assert body["status"] == 422
    assert body["errorCode"] == "DOCUMENT-422"
    assert body["requestId"] == "test-unsupported-type"
