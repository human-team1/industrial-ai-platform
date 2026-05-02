# RAG LangGraph Evaluation

RAG 검색 품질 검증의 최종 설정과 요약 보고서를 보관하는 작업 영역입니다. 런타임 코드는 `../../rag`를 우선합니다.

## 역할

```text
A:
문서 -> chunk -> embedding -> retrieval eval -> best config

B:
question -> LangGraph routing -> result context -> retrieval -> prompt -> LLM -> source verification
```

## 폴더

```text
rag_langgraph_eval/
├── corpus/      # 실험 문서 배치 영역, 현재 운영 원본은 ../../rag/corpus
├── configs/     # best_retrieval_config.yaml 등 최종 채택 설정
├── prompts/     # B prompt 파일
├── graph/       # B graph_v0.py 등
├── scripts/     # notebook 보조 또는 래퍼 스크립트
└── results/     # 요약 MD, 최종 검증 보고서, 최종 YAML만 유지
```

## 유지 기준

- 유지: `configs/*.yaml`, 계약/매핑 MD, 최종 검증 보고서, 실험 요약 MD
- 제외: raw CSV/JSON/JSONL, detail 결과, cache, Chroma local store, 모델 바이너리, 임시 압축 파일
- 공유: 상세 결과가 필요하면 Notion/Drive 링크로 분리하고 Git에는 요약 MD만 둡니다.

## 현재 best retrieval config

```text
search_mode=hybrid
embedding_model=BAAI/bge-m3
chunk_size=800
chunk_overlap=100
internal_top_k=10
answer_top_k=5
visible_source_limit=3
min_score=0.4
deduplicate_by_document=true
diversify_by_section=true
mmr_enabled=false
```

정식 설정 파일은 `configs/best_retrieval_config.yaml`입니다.

## 산출물

- `results/chunking_matrix/chunking_experiment_report.md`
- `results/embedding_eval/*_report.md`
- `results/retrieval_eval/rag_a_final_verification_report.md`
- `results/retrieval_eval/best_retrieval_config.yaml`
- `results/topk_threshold_eval/topk_threshold_eval_report.md`

`experiments/**/*.csv`, `experiments/**/*.json`, `experiments/**/*.jsonl`, `cache/`, 모델 파일, 대용량 바이너리는 Git 커밋 대상이 아닙니다.
