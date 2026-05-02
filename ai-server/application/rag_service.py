from domain.schemas import RagQueryRequest, RagQueryResponse
from application.rag_graph import InspectionRagGraph


class RagService:
    def __init__(self, rag_graph: InspectionRagGraph) -> None:
        self._rag_graph = rag_graph

    def query(self, request: RagQueryRequest) -> RagQueryResponse:
        state = self._rag_graph.invoke(request.question, request.top_k)
        return RagQueryResponse(
            answer=state.get("answer", ""),
            sources=[
                self._format_source(source)
                for source in state.get("sources", [])
            ],
        )

    def _format_source(self, source) -> str:
        locator = source.section_title or (f"p.{source.page}" if source.page else "위치 미상")
        return f"{source.title} - {locator} ({source.chunk_id})"
