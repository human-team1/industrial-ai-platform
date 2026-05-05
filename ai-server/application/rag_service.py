from domain.rag.ports import RagCollectionPort
from domain.rag.query_models import RagQueryCommand, RagQueryResult


class RagService:
    def __init__(self, rag_collection: RagCollectionPort) -> None:
        self._rag_collection = rag_collection

    def query(self, request: RagQueryCommand) -> RagQueryResult:
        self._rag_collection.collection_name()
        return RagQueryResult(
            answer=(
                "RAG pipeline skeleton is ready. Add an embedding model and LLM "
                "provider after model selection."
            ),
            answer_type="general",
            sources=[],
        )
