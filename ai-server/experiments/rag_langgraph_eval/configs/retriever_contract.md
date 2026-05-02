# Retriever Contract

A는 최종 답변을 만들지 않고 문서 검색 결과인 `SourceChunk[]`만 책임진다. B는 이 결과를 LangGraph `retrieve_documents` 노드에서 받아 prompt 구성, LLM 호출, source verification, 최종 `RagQueryResponse` 생성을 수행한다.

## Python Port

```python
class RetrieverPort:
    def search(
        self,
        query: str,
        filters: dict,
        top_k: int = 5,
    ) -> list[SourceChunk]:
        ...
```

Mock 사용:

```python
from rag.retrievers import MockRetriever

retriever = MockRetriever()
sources = retriever.search(
    query="컨베이어 정렬 문제 조치 방법",
    filters={
        "organization_id": "org-001",
        "document_status": "PUBLISHED",
        "equipment_name": "FlexLink X65"
    },
    top_k=5,
)
```

## Internal API Option

`POST /internal/rag/search`

요청:

```json
{
  "query": "컨베이어 정렬 문제 조치 방법",
  "filters": {
    "organization_id": "org-001",
    "document_status": "PUBLISHED",
    "equipment_name": "FlexLink X65"
  },
  "top_k": 5,
  "query_case_id": "GQ-001",
  "request_id": "req-001"
}
```

성공 응답:

```json
{
  "success": true,
  "data": {
    "query": "컨베이어 정렬 문제 조치 방법",
    "sources": [
      {
        "chunk_id": "chunk-001",
        "document_id": "doc-flexlink-x65",
        "document_version_id": "docver-flexlink-x65-v1",
        "title": "FlexLink X65 컨베이어 유지보수 매뉴얼",
        "section_title": "점검 순서",
        "page": 12,
        "content": "...",
        "score": 0.82,
        "rank": 1
      }
    ],
    "retrieval_config_id": "R3_HYBRID_TK10_S04",
    "latency_ms": 12.5
  },
  "message": "검색이 완료되었습니다."
}
```

검색 결과 없음:

```json
{
  "success": true,
  "data": {
    "query": "커피머신 물탱크 청소 방법",
    "sources": [],
    "retrieval_config_id": "R6"
  },
  "message": "검색 결과가 없습니다."
}
```

실패 응답은 RFC 9457 Problem Details 형식을 따른다.

## Required Source Fields

- `chunk_id`
- `document_id`
- `document_version_id`
- `title`
- `content`
- `score`
- `rank`
- `section_title` 또는 `page` 중 최소 하나

## Frozen Config

```yaml
retrieval_config_id: R3_HYBRID_TK10_S04
collection_name: industrial_rag_chunks_a_v1
embedding_model: BAAI/bge-m3
chunk_size: 800
chunk_overlap: 100
search_mode: hybrid
internal_top_k: 10
answer_top_k: 5
visible_source_limit: 3
organization_filter_required: true
document_status_filter: PUBLISHED
document_version_policy: latest_only
min_score: 0.4
deduplicate_by_document: true
diversify_by_section: true
mmr_enabled: false
reranker_enabled: false
```

Freeze 이후 B 통합 테스트는 `industrial_rag_chunks_a_v1`만 바라본다. A가 실험 collection을 추가하더라도 이 통합 collection은 변경하지 않는다.

> 참고: 초기 계획의 `R6 / text-embedding-3-small / vector_with_metadata_filter / min_score=0.35` 설정은 실제 로컬 실험 결과와 맞지 않아 폐기했다. 45개 golden question 기준 최종 리포트에서 `R3_HYBRID_TK10_S04`가 `hit@5=1.0`, `visible_hit@3=1.0`, `mrr=1.0`, `no_answer_accuracy=0.7`, `project_score=0.94`로 가장 높았다.

## Score / No-answer Policy

- `score`는 검색 방식별 정규화 similarity로 취급한다.
- A는 `min_score=0.4` 미만 후보를 제거하고, 남은 후보가 없으면 `sources: []`를 반환한다.
- 단, no_answer/out_of_scope 질문은 score threshold만으로 완전히 차단되지 않는다.
- B는 `sources: []` 또는 query router / service scope check 실패 시 LLM 호출을 차단하고 제한 응답을 생성한다.
- A 검색 결과가 있더라도 B의 source verifier가 출처 필수 필드와 질문 범위 적합성을 최종 확인한다.

## Reranker Decision

cross-encoder reranker 1차 실험은 수행했다.

```text
config_id=R5_HYBRID_RERANKER
reranker_model=cross-encoder/ms-marco-MiniLM-L-6-v2
question_count=45
hit_at_5=1.0
visible_hit_at_3=1.0
mrr=0.981
no_answer_accuracy=0.0
```

정상 질문 검색 품질은 유지됐지만 no_answer/out_of_scope 질문에 억지 source를 모두 붙였으므로 최종 config에는 포함하지 않는다. reranker는 후속으로 한국어/다국어 reranker 후보를 별도 비교할 때 다시 검토한다.

## Trace Metadata

A-side retrieval trace는 LangSmith project `industrial-rag-eval`에 남긴다. 공통 metadata:

```json
{
  "team": "A",
  "stage": "retrieval_eval",
  "query_case_id": "GQ-001",
  "retrieval_config_id": "R3_HYBRID_TK10_S04",
  "embedding_model": "BAAI/bge-m3",
  "chunk_size": 800,
  "chunk_overlap": 100,
  "search_mode": "hybrid",
  "top_k": 10,
  "min_score": 0.4,
  "expected_doc_hit": true,
  "source_count": 3
}
```

Trace output에는 `query`, `filters`, `sources`, `latency_ms`가 포함된다. `LANGSMITH_TRACING=true`와 `LANGSMITH_API_KEY`가 설정된 경우에만 전송한다.

## A/B Boundary

A:

- 검색 config 선택 및 freeze
- `SourceChunk[]` 반환
- 검색 실패 시 `sources: []` 반환
- LangSmith metadata: query, filter, top-k, retrieved chunks, score, latency, expected hit

B:

- `/api/v1/rag/query`
- LangGraph routing
- prompt, LLM, source verifier
- sources 없음 처리 및 제한 응답 생성
