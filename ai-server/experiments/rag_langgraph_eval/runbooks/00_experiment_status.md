# Experiment Status

현재 A 역할은 검색 품질 검증과 B 통합용 검색 계약 제공에 집중한다.

## 완료

| Task | 상태 | 산출물 |
|---|---|---|
| RAG-A-001 corpus 구조 및 문서 카탈로그 | 완료 | `rag/outputs/*` |
| RAG-A-002 SourceChunk / RetrievalConfig schema | 완료 | `rag/schemas/*` |
| RAG-A-003 Markdown loader 및 section parser | 완료 | `rag/loaders/markdown_loader.py`, `rag/chunking/section_chunker.py` |
| RAG-A-004 token chunker / section chunker | 완료 | `rag/chunking/token_chunker.py`, `rag/chunking/section_chunker.py` |
| RAG-A-005 Chroma baseline index 생성 스크립트 | 스크립트 완료 / 실행 미완료 | `rag/scripts/build_chroma_baseline_index.py`, `results/retrieval_eval/chroma_baseline_validation_status.md` |
| RAG-A-006 golden questions | 완료 | `experiments/rag_langgraph_eval/datasets/golden_questions_v1.csv` |
| RAG-A-007 retrieval eval metric | 완료 | `rag/scripts/run_retrieval_experiment.py` |
| RAG-A-008 chunk size / overlap 비교 | 완료 | `results/chunking_matrix/` |
| RAG-A-009 embedding model 비교 | 완료 | `results/embedding_eval/` |
| RAG-A-010 top-k / metadata filter 비교 | 완료 | `results/topk_threshold_eval/`, `results/retrieval_eval/` |
| RAG-A-011 hybrid / reranker 선택 실험 | 완료 / reranker 미채택 | `results/retrieval_eval/`, `results/reranker_eval/` |
| RAG-A-012 retrieval_failed_cases.md | 완료 | `results/retrieval_eval/retrieval_failed_cases.md` |
| RAG-A-013 best_retrieval_config.yaml freeze | 완료 | `configs/best_retrieval_config.yaml` |
| RAG-A-014 RetrieverPort/API 계약 | 완료 | `configs/retriever_contract.md` |
| RAG-A-015 B LangGraph 통합 테스트 지원 | 진행 가능 | `mock_sources_v1.json`, `retriever_contract.md` |

## Freeze Config

```text
retrieval_config_id=R3_HYBRID_TK10_S04
collection_name=industrial_rag_chunks_a_v1
embedding_model=BAAI/bge-m3
search_mode=hybrid
chunk_size=800
chunk_overlap=100
internal_top_k=10
answer_top_k=5
visible_source_limit=3
min_score=0.4
document_status_filter=PUBLISHED
document_version_policy=latest_only
```

## 보완 기록

- `R6 / text-embedding-3-small / vector_with_metadata_filter` 초기 freeze 값은 실제 retrieval 평가 best와 불일치하여 폐기했다.
- 최종 freeze는 `retrieval_eval_report.md` 기준 best인 `R3_HYBRID_TK10_S04`로 맞췄다.
- LangSmith tracer wrapper를 retrieval/top-k 실험 루프에 연결했다. 실제 전송은 `LANGSMITH_TRACING=true`와 `LANGSMITH_API_KEY`가 설정된 경우에만 수행된다.
- no_answer/out_of_scope 차단은 A threshold와 cross-encoder reranker만으로 부족하므로 B query router/source verifier 보완이 필요하다.
- Chroma baseline index build는 Gemini key 기준으로 전환했으며, 현재 Chroma 서버 미접속으로 실행되지 않았다.
- cross-encoder reranker는 `cross-encoder/ms-marco-MiniLM-L-6-v2`로 실행 완료했다. 정상 질문 `hit@5=1.0`은 유지했지만 `no_answer_accuracy=0.0`이라 최종 config에는 채택하지 않는다.
