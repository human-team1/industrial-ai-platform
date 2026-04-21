from domain.schemas import RagQueryRequest, RagQueryResponse
from infrastructure.chroma_client import ChromaClientWrapper


class RagService:
    def __init__(self, chroma_client: ChromaClientWrapper) -> None:
        self._chroma_client = chroma_client

    def query(self, request: RagQueryRequest) -> RagQueryResponse:
        self._chroma_client.collection_name()
        return RagQueryResponse(
            answer=(
                "RAG pipeline skeleton is ready. Add an embedding model and LLM "
                "provider after model selection."
            ),
            sources=[],
        )
