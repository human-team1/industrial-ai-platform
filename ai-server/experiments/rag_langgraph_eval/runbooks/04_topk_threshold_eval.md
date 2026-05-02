# 04. Top-k / Threshold / Diversity Evaluation

## 목적

Embedding과 chunk 설정은 고정하고, 검색 후보 수, threshold, source diversity, MMR 적용 여부를 비교했다.

## 실행 코드

```powershell
cd C:\Users\jecho\OneDrive\Desktop\CJE\industrial-ai-platform\ai-server
.\.venv\Scripts\python.exe -m rag.scripts.run_topk_threshold_experiment
```

## 실제 스크립트

- `rag/scripts/run_topk_threshold_experiment.py`

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
search_mode=vector
```

변수:

```text
internal_top_k=3 / 5 / 10
min_score=0.2 / 0.3 / 0.4
mmr_enabled=false / true
deduplicate_by_document=true
diversify_by_section=true
```

총 18개 조합:

```text
3 top-k * 3 threshold * 2 MMR = 18
```

## 출력

- `experiments/rag_langgraph_eval/results/topk_threshold_eval/topk_threshold_eval_summary.csv`
- `experiments/rag_langgraph_eval/results/topk_threshold_eval/topk_threshold_eval_detail.csv`
- `experiments/rag_langgraph_eval/results/topk_threshold_eval/topk_threshold_by_query_type.csv`
- `experiments/rag_langgraph_eval/results/topk_threshold_eval/topk_threshold_failed_cases.csv`
- `experiments/rag_langgraph_eval/results/topk_threshold_eval/topk_threshold_eval_report.md`
- `experiments/rag_langgraph_eval/results/topk_threshold_eval/topk_eval.csv`

## 실행 결과

```text
documents=27
chunks=572
questions=45
configs=18
```

Best vector-only top-k/threshold config:

```text
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
no_answer_accuracy=0.2
```

판단:

- vector-only 기준으로도 정상 질문 검색은 잘 맞는다.
- 다만 no_answer/out_of_scope 억제는 hybrid 실험이 더 나았다.
- 최종 freeze는 retrieval v2 결과에 따라 `hybrid + min_score=0.4`로 결정했다.
