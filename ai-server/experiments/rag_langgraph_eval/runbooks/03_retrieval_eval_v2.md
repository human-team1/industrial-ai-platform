# 03. Retrieval Evaluation v2

## 목적

Golden Question v2 45개 기준으로 R1~R4 검색 방식을 비교하고 B 통합용 best retrieval config를 freeze했다.

## 실행 코드

```powershell
cd C:\Users\jecho\OneDrive\Desktop\CJE\industrial-ai-platform\ai-server
.\.venv\Scripts\python.exe -m rag.scripts.run_retrieval_experiment
```

## 실제 스크립트

- `rag/scripts/run_retrieval_experiment.py`

## 입력

- chunk 입력: `experiments/rag_langgraph_eval/results/chunking_matrix/C3_chunk_records.jsonl`
- 질문셋: `experiments/rag_langgraph_eval/datasets/golden_questions_v2.csv`
- embedding: `BAAI/bge-m3`

## 실험 조건

고정값:

```text
embedding_model=BAAI/bge-m3
chunk_size=800
chunk_overlap=100
questions=45
```

검색 방식:

| ID | 방식 |
|---|---|
| R1 | vector |
| R2 | bm25 |
| R3 | hybrid |
| R4 | vector_metadata_filter |

변수:

```text
internal_top_k=3 / 5 / 10
min_score=0.2 / 0.3 / 0.4
```

총 36개 조합:

```text
4 search modes * 3 top-k * 3 threshold = 36
```

## 출력

- `experiments/rag_langgraph_eval/results/retrieval_eval/retrieval_eval_summary.csv`
- `experiments/rag_langgraph_eval/results/retrieval_eval/retrieval_eval_detail.csv`
- `experiments/rag_langgraph_eval/results/retrieval_eval/retrieval_eval_by_query_type.csv`
- `experiments/rag_langgraph_eval/results/retrieval_eval/retrieval_failed_cases.csv`
- `experiments/rag_langgraph_eval/results/retrieval_eval/retrieval_failed_cases.md`
- `experiments/rag_langgraph_eval/results/retrieval_eval/retrieval_eval_report.md`
- `experiments/rag_langgraph_eval/results/retrieval_eval/index_build_log.jsonl`
- `experiments/rag_langgraph_eval/configs/best_retrieval_config.yaml`

## 실행 결과

```text
documents=27
chunks=572
questions=45
configs=36
best_config=R3_HYBRID_TK10_S04
```

Best config:

```text
search_mode=hybrid
internal_top_k=10
answer_top_k=5
visible_source_limit=3
min_score=0.4
deduplicate_by_document=true
diversify_by_section=true
mmr_enabled=false
```

주요 지표:

```text
hit_at_5=1.0
visible_hit_at_3=1.0
mrr=1.0
no_answer_accuracy=0.7
avg_answer_source_count=3.09
```

실패 케이스:

```text
failed_count=3
NQ005: 회사 연차/휴가 질문
OS001: 축구 경기 결과 질문
OS002: 개인 의료 진단 질문
```

판단:

- 검색 품질은 `hybrid`가 가장 안정적이다.
- 범위 외 질문 3건은 A 검색기 단독보다 B의 `service_scope_check`에서 차단하는 것이 맞다.
