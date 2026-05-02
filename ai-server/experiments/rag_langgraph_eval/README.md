# RAG LangGraph Evaluation

3일 실험용 작업 영역입니다. A는 검색 품질 검증, B는 LangGraph/프롬프트/API 응답 검증을 담당합니다.

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
├── datasets/    # golden questions, mock source/context
├── configs/     # best_retrieval_config.yaml
├── prompts/     # B prompt 파일
├── graph/       # B graph_v0.py 등
├── scripts/     # notebook 보조 또는 래퍼 스크립트
└── results/     # 실험 산출물
```

## A 실험 명령

루트는 `ai-server`입니다.

```powershell
.\.venv\Scripts\python.exe -m rag.scripts.run_chunking_matrix
.\.venv\Scripts\python.exe -m rag.scripts.run_embedding_experiment
.\.venv\Scripts\python.exe -m rag.scripts.run_retrieval_experiment
.\.venv\Scripts\python.exe -m rag.scripts.run_topk_threshold_experiment
```

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

- `results/chunking_matrix/`
- `results/embedding_eval/`
- `results/retrieval_eval/`
- `results/topk_threshold_eval/`

`cache/`, 모델 파일, 대용량 바이너리는 Git 커밋 대상이 아닙니다.
