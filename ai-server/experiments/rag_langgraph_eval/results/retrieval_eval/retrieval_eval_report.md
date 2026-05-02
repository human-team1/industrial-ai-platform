# Retrieval Evaluation Report

## Experiment Setup

- documents: `27`
- chunks: `572`
- questions: `45`
- embedding_model: `BAAI/bge-m3`
- chunk_config: `chunk_size=800`, `chunk_overlap=100`
- golden_questions: `golden_questions_v2.csv`
- search_modes: `vector`, `bm25`, `hybrid`, `vector_metadata_filter`
- internal_top_k candidates: `3 / 5 / 10`
- min_score candidates: `0.2 / 0.3 / 0.4`
- source diversity: `deduplicate_by_document=true`, `diversify_by_section=true`

## Best Config

- config_id: `R3_HYBRID_TK10_S04`
- search_mode: `hybrid`
- internal_top_k: `10`
- answer_top_k: `5`
- visible_source_limit: `3`
- min_score: `0.4`
- hit_at_5: `1.0`
- visible_hit_at_3: `1.0`
- mrr: `1.0`
- no_answer_accuracy: `0.7`
- avg_answer_source_count: `3.09`
- project_score: `0.94`

## Summary Table

| Config | Search | top_k | min_score | hit@1 | hit@3 | hit@5 | visible_hit@3 | MRR | no_answer_acc | avg_sources | elapsed_ms | project_score |
|---|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| R1_VECTOR_TK10_S02 | vector | 10 | 0.2 | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.0 | 3.96 | 27.85 | 0.8 |
| R1_VECTOR_TK10_S03 | vector | 10 | 0.3 | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.0 | 3.96 | 25.65 | 0.8 |
| R1_VECTOR_TK10_S04 | vector | 10 | 0.4 | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.2 | 3.56 | 28.22 | 0.84 |
| R1_VECTOR_TK3_S02 | vector | 3 | 0.2 | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.0 | 2.47 | 25.65 | 0.8 |
| R1_VECTOR_TK3_S03 | vector | 3 | 0.3 | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.0 | 2.47 | 17.53 | 0.8 |
| R1_VECTOR_TK3_S04 | vector | 3 | 0.4 | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.2 | 2.31 | 19.99 | 0.84 |
| R1_VECTOR_TK5_S02 | vector | 5 | 0.2 | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.0 | 3.22 | 17.7 | 0.8 |
| R1_VECTOR_TK5_S03 | vector | 5 | 0.3 | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.0 | 3.22 | 23.88 | 0.8 |
| R1_VECTOR_TK5_S04 | vector | 5 | 0.4 | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.2 | 2.89 | 20.58 | 0.84 |
| R2_BM25_TK10_S02 | bm25 | 10 | 0.2 | 0.9429 | 1.0 | 1.0 | 1.0 | 0.9714 | 0.7 | 3.36 | 59.64 | 0.9343 |
| R2_BM25_TK10_S03 | bm25 | 10 | 0.3 | 0.9429 | 1.0 | 1.0 | 1.0 | 0.9714 | 0.7 | 3.36 | 46.02 | 0.9343 |
| R2_BM25_TK10_S04 | bm25 | 10 | 0.4 | 0.9429 | 1.0 | 1.0 | 1.0 | 0.9714 | 0.7 | 3.31 | 64.82 | 0.9343 |
| R2_BM25_TK3_S02 | bm25 | 3 | 0.2 | 0.9429 | 1.0 | 1.0 | 1.0 | 0.9714 | 0.7 | 2.2 | 59.89 | 0.9343 |
| R2_BM25_TK3_S03 | bm25 | 3 | 0.3 | 0.9429 | 1.0 | 1.0 | 1.0 | 0.9714 | 0.7 | 2.2 | 55.44 | 0.9343 |
| R2_BM25_TK3_S04 | bm25 | 3 | 0.4 | 0.9429 | 1.0 | 1.0 | 1.0 | 0.9714 | 0.7 | 2.2 | 55.48 | 0.9343 |
| R2_BM25_TK5_S02 | bm25 | 5 | 0.2 | 0.9429 | 1.0 | 1.0 | 1.0 | 0.9714 | 0.7 | 2.93 | 51.44 | 0.9343 |
| R2_BM25_TK5_S03 | bm25 | 5 | 0.3 | 0.9429 | 1.0 | 1.0 | 1.0 | 0.9714 | 0.7 | 2.93 | 58.04 | 0.9343 |
| R2_BM25_TK5_S04 | bm25 | 5 | 0.4 | 0.9429 | 1.0 | 1.0 | 1.0 | 0.9714 | 0.7 | 2.91 | 55.57 | 0.9343 |
| R3_HYBRID_TK10_S02 | hybrid | 10 | 0.2 | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.0 | 3.84 | 79.83 | 0.8 |
| R3_HYBRID_TK10_S03 | hybrid | 10 | 0.3 | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.5 | 3.29 | 87.03 | 0.9 |
| R3_HYBRID_TK10_S04 | hybrid | 10 | 0.4 | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.7 | 3.09 | 79.53 | 0.94 |
| R3_HYBRID_TK3_S02 | hybrid | 3 | 0.2 | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.0 | 2.47 | 84.18 | 0.8 |
| R3_HYBRID_TK3_S03 | hybrid | 3 | 0.3 | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.5 | 2.16 | 78.28 | 0.9 |
| R3_HYBRID_TK3_S04 | hybrid | 3 | 0.4 | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.7 | 2.07 | 73.59 | 0.94 |
| R3_HYBRID_TK5_S02 | hybrid | 5 | 0.2 | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.0 | 3.29 | 67.49 | 0.8 |
| R3_HYBRID_TK5_S03 | hybrid | 5 | 0.3 | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.5 | 2.8 | 71.9 | 0.9 |
| R3_HYBRID_TK5_S04 | hybrid | 5 | 0.4 | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.7 | 2.67 | 66.39 | 0.94 |
| R4_VECTOR_METADATA_FILTER_TK10_S02 | vector_metadata_filter | 10 | 0.2 | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.0 | 3.8 | 44.59 | 0.8 |
| R4_VECTOR_METADATA_FILTER_TK10_S03 | vector_metadata_filter | 10 | 0.3 | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.0 | 3.8 | 50.29 | 0.8 |
| R4_VECTOR_METADATA_FILTER_TK10_S04 | vector_metadata_filter | 10 | 0.4 | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.2 | 3.4 | 81.05 | 0.84 |
| R4_VECTOR_METADATA_FILTER_TK3_S02 | vector_metadata_filter | 3 | 0.2 | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.0 | 2.42 | 42.06 | 0.8 |
| R4_VECTOR_METADATA_FILTER_TK3_S03 | vector_metadata_filter | 3 | 0.3 | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.0 | 2.42 | 38.12 | 0.8 |
| R4_VECTOR_METADATA_FILTER_TK3_S04 | vector_metadata_filter | 3 | 0.4 | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.2 | 2.27 | 34.7 | 0.84 |
| R4_VECTOR_METADATA_FILTER_TK5_S02 | vector_metadata_filter | 5 | 0.2 | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.0 | 3.2 | 40.16 | 0.8 |
| R4_VECTOR_METADATA_FILTER_TK5_S03 | vector_metadata_filter | 5 | 0.3 | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.0 | 3.2 | 38.42 | 0.8 |
| R4_VECTOR_METADATA_FILTER_TK5_S04 | vector_metadata_filter | 5 | 0.4 | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 0.2 | 2.87 | 35.37 | 0.84 |

## Failed Cases

| Question ID | Type | Question | Expected | Top 1 Title | Top 1 Score |
|---|---|---|---|---|---:|
| NQ005 | no_answer | 회사 연차 규정과 휴가 신청 방법 알려줘 | NO_ANSWER | Moxa EDS-205A/EDS-208A 산업용 이더넷 스위치 운영 가이드 | 0.564044 |
| OS001 | out_of_scope | 축구 경기 결과 알려줘 | OUT_OF_SCOPE | 작업자 교대 시 인수인계 체크리스트 | 0.615857 |
| OS002 | out_of_scope | 개인 의료 진단을 해줘 | OUT_OF_SCOPE | SICK InspectorP62x 2D 비전 검사 운영 가이드 | 0.638213 |
