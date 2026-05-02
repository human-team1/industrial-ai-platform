# RAG-A Final Verification Report

## 검증 일자

- 2026-04-30

## 검증 범위

A 역할인 RAG 검색 품질 검증과 B 통합용 검색 계약을 확인했다.

검증 대상:

- corpus 배치 및 문서 카탈로그
- SourceChunk / RetrievalConfig / RetrievalQuery / RetrievalResult schema
- Markdown loader
- token chunker / section chunker
- chunking matrix
- embedding comparison
- retrieval evaluation
- top-k / threshold evaluation
- source verifier 대응 계약
- best config freeze
- RetrieverPort / internal search API contract

## 로컬 실행 검증

```text
python -m py_compile rag/schemas/source_chunk.py rag/schemas/retrieval_config.py rag/schemas/retrieval_result.py rag/loaders/markdown_loader.py rag/chunking/token_chunker.py rag/chunking/section_chunker.py rag/eval/retrieval_metrics.py rag/tracing/langsmith_tracer.py rag/retrievers/mock_retriever.py
-> 통과

python rag/scripts/validate_schemas.py
-> Schema validation passed.

python rag/scripts/validate_source_contract.py
-> validated_sources: 3
-> Source contract validation passed.

.\.venv\Scripts\python.exe -m pytest tests\test_health.py -q
-> 1 passed
```

## 단계별 결과

| 단계 | 상태 | 근거 |
|---|---|---|
| A Step 3 LangSmith settings/tracer | 보완 완료 | `rag/tracing/langsmith_tracer.py`, retrieval/top-k script trace 연결 |
| A Step 4 corpus / mock_sources / query_cases | 완료 | `rag/outputs/*`, `mock_sources_v1.json`, `golden_questions_v1.csv`, `golden_questions_v2.csv`, `result_linked_query_cases.json` |
| A Step 5 document loader | 완료 | `rag/loaders/markdown_loader.py` |
| A Step 6 chunker | 완료 | `rag/chunking/token_chunker.py`, `rag/chunking/section_chunker.py` |
| A Step 7 baseline Chroma index | Gemini 기준 스크립트 완료 / 실제 실행 미완료 | `rag/scripts/build_chroma_baseline_index.py`, Chroma 서버 미접속 |
| A Step 8 validate_index | Gemini 기준 작성 완료 / 실제 실행 미완료 | `rag/scripts/validate_index.py`, Chroma 서버 미접속 |
| A Step 9 retrieval eval script | 완료 | `rag/scripts/run_retrieval_experiment.py`, `rag/eval/retrieval_metrics.py` |
| A Step 10 검색 실험 1차 | 완료 | `results/chunking_matrix/*`, `results/embedding_eval/*` |
| A Step 11 검색 실험 2차 | 완료 / reranker 미채택 | vector/BM25/hybrid/metadata/top-k/MMR/cross-encoder reranker 수행 |
| A Step 12 source verifier 대응 테스트 | 완료 | `rag/scripts/validate_source_contract.py` |
| A Step 13 best_retrieval_config freeze | 보완 완료 | `configs/best_retrieval_config.yaml`를 실험 best와 일치시킴 |
| A 최종 산출물 | 대부분 완료 | 아래 파일 목록 참고 |

## Corpus 결과

```text
Total indexed candidate files: 27
Markdown files: 27
Duplicate files: 0
Too short files: 0
UNKNOWN document type: 0
JE_md: 14
SK_md: 13
```

## Chunking 결과

| ID | 방식 | chunks | min | p50 | avg | max | 20자 미만 | 초과 |
|---|---|---:|---:|---:|---:|---:|---:|---:|
| C1 | 300/50 | 1217 | 1 | 299 | 242.58 | 300 | 4 | 0 |
| C2 | 500/100 | 788 | 19 | 435 | 372.42 | 500 | 1 | 0 |
| C3 | 800/100 | 572 | 19 | 467 | 478.76 | 800 | 1 | 0 |
| C4 | 1000/150 | 523 | 19 | 479 | 521.16 | 1000 | 1 | 0 |
| C5 | section-based | 451 | 19 | 462 | 584.54 | 9060 | 1 | 0 |
| C6 | heading + body hybrid | 575 | 19 | 468 | 480.19 | 800 | 1 | 0 |

## Retrieval Best Config

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
min_score: 0.4
organization_filter_required: true
document_status_filter: PUBLISHED
document_version_policy: latest_only
deduplicate_by_document: true
diversify_by_section: true
mmr_enabled: false
reranker_enabled: false
```

## Retrieval 평가 결과

Best config `R3_HYBRID_TK10_S04`:

```text
hit_at_5=1.0
visible_hit_at_3=1.0
mrr=1.0
no_answer_accuracy=0.7
avg_answer_source_count=3.09
project_score=0.94
```

## 실패 케이스 분석

남은 실패 케이스는 모두 no_answer/out_of_scope 계열이다.

| question_id | query_type | question | top_1_score |
|---|---|---|---:|
| NQ005 | no_answer | 회사 연차 규정과 휴가 신청 방법 알려줘 | 0.564044 |
| OS001 | out_of_scope | 축구 경기 결과 알려줘 | 0.615857 |
| OS002 | out_of_scope | 개인 의료 진단을 해줘 | 0.638213 |

결론: A threshold만으로 범위 외 질문을 완전히 차단하지 못한다. B query router와 source verifier가 필수다.

## 최종 산출물

- `rag/outputs/corpus_check_report.md`
- `rag/outputs/document_catalog.csv`
- `rag/outputs/source_metadata_report.md`
- `experiments/rag_langgraph_eval/results/chunk_stats.csv`
- `experiments/rag_langgraph_eval/results/retrieval_eval/index_build_log.jsonl`
- `experiments/rag_langgraph_eval/datasets/golden_questions_v1.csv`
- `experiments/rag_langgraph_eval/datasets/golden_questions_v2.csv`
- `experiments/rag_langgraph_eval/results/retrieval_eval/retrieval_eval_summary.csv`
- `experiments/rag_langgraph_eval/results/retrieval_eval/topk_eval.csv`
- `experiments/rag_langgraph_eval/results/retrieval_eval/retrieval_failed_cases.md`
- `experiments/rag_langgraph_eval/configs/best_retrieval_config.yaml`
- `experiments/rag_langgraph_eval/datasets/mock_sources_v1.json`
- `experiments/rag_langgraph_eval/configs/retriever_contract.md`
- `experiments/rag_langgraph_eval/results/retrieval_eval/no_answer_threshold_analysis.md`
- `experiments/rag_langgraph_eval/results/retrieval_eval/chroma_baseline_validation_status.md`

## 남은 리스크

1. Chroma baseline index는 Chroma 서버 미접속으로 실제 build/validate가 안 됐다.
2. cross-encoder reranker는 수행했으나 no_answer accuracy가 `0.0`으로 떨어져 채택하지 않는다.
3. no_answer/out_of_scope는 A threshold 또는 reranker만으로 완전 차단되지 않는다.
4. 실제 FastAPI internal endpoint `/internal/rag/search` 구현은 계약 문서 수준이며, 라우터 구현은 별도 작업이다.

## 후속 개선안

1. Chroma 서버 준비 후 Gemini baseline index build/validate 실행
2. `industrial_rag_chunks_a_v1` collection을 최종 freeze collection으로 생성
3. B query router에 service scope check 추가
4. source verifier에서 질문-source 관련성 점수화
5. 한국어/다국어 reranker 후보는 별도 비교하되, 현재 `cross-encoder/ms-marco-MiniLM-L-6-v2`는 미채택
6. organization/version/status filter 전용 테스트 케이스 추가
