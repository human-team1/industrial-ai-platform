# A Retrieval Experiment Runbooks

지금까지 A 역할에서 실행한 실험과 검증 코드를 재현하기 위한 runbook 모음입니다.

## 실행 순서

```powershell
cd C:\Users\jecho\OneDrive\Desktop\CJE\industrial-ai-platform\ai-server
.\.venv\Scripts\Activate.ps1
```

1. `01_chunking_matrix.md`
2. `02_embedding_eval.md`
3. `03_retrieval_eval_v2.md`
4. `04_topk_threshold_eval.md`
5. `05_schema_contract_trace.md`

## 현재 Freeze Config

정식 B 통합용 검색 설정은 아래 파일입니다.

- `experiments/rag_langgraph_eval/configs/best_retrieval_config.yaml`

현재 값:

```text
retrieval_config_id=R3_HYBRID_TK10_S04
embedding_model=BAAI/bge-m3
search_mode=hybrid
chunk_size=800
chunk_overlap=100
internal_top_k=10
answer_top_k=5
visible_source_limit=3
min_score=0.4
```
