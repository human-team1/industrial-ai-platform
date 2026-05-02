# A Step-by-Step Generation

## Step 2. Schemas

- `rag/schemas/source_chunk.py`
- `rag/schemas/retrieval_result.py`
- `rag/schemas/retrieval_config.py`

생성/정의 대상:

- `SourceChunk`
- `RetrievalQuery`
- `RetrievalResult`
- `RetrievalConfig`

A는 최종 `RagQueryResponse`가 아니라 그 안의 `sources` 계약만 책임진다.

## Step 3. LangSmith Settings / Tracer

- `rag/tracing/langsmith_tracer.py`

공통 project는 `industrial-rag-eval`을 사용하고, A trace metadata는 `team=A`, `stage=retrieval_eval`로 구분한다.

## Step 4. Corpus / Mock Sources / Query Cases

- `rag/outputs/document_catalog.csv`
- `experiments/rag_langgraph_eval/datasets/mock_sources_v1.json`
- `experiments/rag_langgraph_eval/datasets/golden_questions_v1.csv`
- `experiments/rag_langgraph_eval/datasets/result_linked_query_cases.json`

B는 실제 Chroma 없이도 `mock_sources_v1.json`으로 `MockRetriever`를 실행할 수 있다.

## Step 5. Markdown Loader

- `rag/loaders/markdown_loader.py`

초기 실험은 Markdown 문서를 기준으로 한다.

## Step 6. Chunker

- `rag/chunking/token_chunker.py`
- `rag/chunking/section_chunker.py`

산업 매뉴얼 특성상 기본 실험은 section chunker를 우선한다.

## Step 7. Baseline Chroma Index

- `rag/scripts/build_chroma_baseline_index.py`

Day 1 baseline:

```yaml
retrieval_config_id: R0_BASELINE
collection_name: industrial_rag_chunks_a_baseline
embedding_model: text-embedding-3-small
chunk_size: 800
chunk_overlap: 100
search_mode: vector
top_k: 5
document_status_filter: PUBLISHED
```

## Step 8. Validate Index

- `rag/scripts/validate_index.py`

검증 질문:

- `FlexLink X65 컨베이어 정렬 문제 조치 방법`
- `조명 반사 때문에 heatmap이 넓게 나올 때 확인할 항목`
- `커피머신 물탱크 청소 방법`

## Step 9. Retrieval Eval Metrics

- `rag/eval/retrieval_metrics.py`
- `rag/scripts/run_retrieval_experiment.py`

기본 지표:

- Hit@k
- Recall@k
- Precision@k
- MRR
- latency_ms

프로젝트 지표:

- Source Completeness
- Section Hit Rate
- Metadata Filter Accuracy
- No-answer Accuracy

## Step 10. 검색 실험 1차

- `experiments/rag_langgraph_eval/results/retrieval_eval/retrieval_eval_summary.csv`

비교 축:

- R1: chunk 500 / overlap 100 / vector / top-k 5
- R2: chunk 800 / overlap 100 / vector / top-k 5
- R3: chunk 1000 / overlap 150 / vector / top-k 5

## Step 11. 검색 실험 2차

- `experiments/rag_langgraph_eval/results/topk_threshold_eval/topk_eval.csv`

비교 축:

- top-k 3 / 5 / 10
- metadata filter
- hybrid
- reranker 후보

## Step 12. Source Verifier 대응 테스트

- `rag/scripts/validate_source_contract.py`

검증 조건:

- `sources`는 빈 배열 가능
- `score`, `rank` 포함
- `document_id`, `document_version_id`, `chunk_id` 포함
- `title` 포함
- `section_title` 또는 `page` 중 최소 하나 포함

## Step 13. Best Retrieval Config Freeze

- `experiments/rag_langgraph_eval/configs/best_retrieval_config.yaml`

현재 freeze:

```yaml
retrieval_config_id: R6
collection_name: industrial_rag_chunks_a_v1
embedding_model: text-embedding-3-small
search_mode: vector_with_metadata_filter
```

## Step 14. B 통합용 Retriever 계약

- `experiments/rag_langgraph_eval/configs/retriever_contract.md`

B 소유 API:

- `POST /api/v1/rag/query`

A 소유 검색 계약:

- `RetrieverPort.search()`
- 또는 `POST /internal/rag/search`
