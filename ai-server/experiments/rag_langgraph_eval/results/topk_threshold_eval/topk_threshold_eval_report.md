# Top-k / Threshold Retrieval Evaluation Report

## Experiment Setup

- documents: `27`
- chunks: `572`
- questions: `45`
- embedding_model: `BAAI/bge-m3`
- chunk_config: `chunk_size=800`, `chunk_overlap=100`
- search_mode: `vector`
- internal_top_k candidates: `3 / 5 / 10`
- min_score candidates: `0.2 / 0.3 / 0.4`
- source diversity: `deduplicate_by_document=true`, `diversify_by_section=true`
- MMR: `false / true`

## Best Config

- config_id: `TK10_S04_MMR0`
- internal_top_k: `10`
- answer_top_k: `5`
- visible_source_limit: `3`
- min_score: `0.4`
- mmr_enabled: `False`
- hit_at_5: `1.0`
- visible_hit_at_3: `1.0`
- mrr: `1.0`
- no_answer_accuracy: `0.2`
- avg_answer_source_count: `3.51`
- failed_cases: `8`

## Summary Table

| Config | internal_top_k | min_score | MMR | hit@1 | hit@3 | hit@5 | visible_hit@3 | MRR | no_answer_acc | avg_sources | dup_doc_ratio | elapsed_ms | project_score |
|---|---:|---:|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| TK10_S02_MMR0 | 10 | 0.2 | False | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.0 | 3.91 | 0.3674 | 15.73 | 0.8 |
| TK10_S02_MMR1 | 10 | 0.2 | True | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.0 | 3.91 | 0.3363 | 34.02 | 0.8 |
| TK10_S03_MMR0 | 10 | 0.3 | False | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.0 | 3.91 | 0.3674 | 13.69 | 0.8 |
| TK10_S03_MMR1 | 10 | 0.3 | True | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.0 | 3.91 | 0.3363 | 36.0 | 0.8 |
| TK10_S04_MMR0 | 10 | 0.4 | False | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.2 | 3.51 | 0.3556 | 15.66 | 0.84 |
| TK10_S04_MMR1 | 10 | 0.4 | True | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.2 | 3.51 | 0.3378 | 35.1 | 0.84 |
| TK3_S02_MMR0 | 3 | 0.2 | False | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.0 | 2.47 | 0.3741 | 14.64 | 0.8 |
| TK3_S02_MMR1 | 3 | 0.2 | True | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.0 | 2.47 | 0.3741 | 16.92 | 0.8 |
| TK3_S03_MMR0 | 3 | 0.3 | False | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.0 | 2.47 | 0.3741 | 14.15 | 0.8 |
| TK3_S03_MMR1 | 3 | 0.3 | True | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.0 | 2.47 | 0.3741 | 13.95 | 0.8 |
| TK3_S04_MMR0 | 3 | 0.4 | False | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.2 | 2.31 | 0.3556 | 14.84 | 0.84 |
| TK3_S04_MMR1 | 3 | 0.4 | True | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.2 | 2.31 | 0.3556 | 17.4 | 0.84 |
| TK5_S02_MMR0 | 5 | 0.2 | False | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.0 | 3.18 | 0.3989 | 14.54 | 0.8 |
| TK5_S02_MMR1 | 5 | 0.2 | True | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.0 | 3.18 | 0.3989 | 21.16 | 0.8 |
| TK5_S03_MMR0 | 5 | 0.3 | False | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.0 | 3.18 | 0.3989 | 12.9 | 0.8 |
| TK5_S03_MMR1 | 5 | 0.3 | True | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.0 | 3.18 | 0.3989 | 21.6 | 0.8 |
| TK5_S04_MMR0 | 5 | 0.4 | False | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.2 | 2.84 | 0.3837 | 14.2 | 0.84 |
| TK5_S04_MMR1 | 5 | 0.4 | True | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.2 | 2.84 | 0.3837 | 19.61 | 0.84 |

## Conclusion

본 실험은 BAAI/bge-m3 embedding과 C3 chunk 설정을 고정하고 internal_top_k, min_score, MMR 적용 여부에 따른 검색 품질을 비교했다.
최종 설정은 hit@5, visible_hit@3, MRR, no_answer_accuracy를 함께 고려해 선정했다.
관련 문서가 없는 질문에서 source를 비워야 하는 정책 때문에 no_answer_accuracy를 선택 기준에 포함했다.
