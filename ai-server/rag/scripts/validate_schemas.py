import json
import sys
from pathlib import Path


sys.path.append(str(Path(__file__).resolve().parents[2]))

from rag.schemas import RetrievalConfig, RetrievalQuery, RetrievalResult, SourceChunk


def pretty_print(title: str, data) -> None:
    print(title)
    print(json.dumps(data.model_dump(), indent=2, ensure_ascii=False))


def main() -> None:
    config = RetrievalConfig()

    query = RetrievalQuery(
        query="컨베이어 정렬 이상 시 점검 순서 알려줘",
        filters={
            "organization_id": "org-001",
            "document_status": "PUBLISHED",
            "equipment_name": "FlexLink X65",
        },
        top_k=5,
        query_case_id="GQ-001",
        request_id="req-schema-smoke",
    )

    source = SourceChunk(
        chunk_id="chunk-flexlink-x65-001",
        document_id="doc-flexlink-x65",
        document_version_id="docver-flexlink-x65-v1",
        title="FlexLink X65 컨베이어 유지보수 매뉴얼",
        document_type="MAINTENANCE",
        category="conveyor",
        equipment_name="FlexLink X65",
        section_title="점검 순서",
        page=12,
        content="컨베이어 정렬 이상 시 가이드 레일, 체인 장력, 제품 위치 센서를 순서대로 확인한다.",
        score=0.82,
        rank=1,
        source_uri="rag/corpus/JE_md/FlexLink_X65_misalignment_rag_ko.md",
    )

    result = RetrievalResult(
        query=query.query,
        sources=[source],
        retrieval_config_id=config.retrieval_config_id,
        latency_ms=12.5,
        total_candidates=10,
        query_case_id=query.query_case_id,
        request_id=query.request_id,
    )

    pretty_print("RetrievalConfig", config)
    print()
    pretty_print("RetrievalQuery", query)
    print()
    pretty_print("RetrievalResult", result)
    print()
    print("Schema validation passed.")


if __name__ == "__main__":
    main()
