# 02. Embedding Evaluation

## 목적

한국어 질의, 영문 장비명, 혼합 질의 기준으로 embedding 모델별 retrieval 품질을 비교했다.

## 실행 코드

```powershell
cd C:\Users\jecho\OneDrive\Desktop\CJE\industrial-ai-platform\ai-server
.\.venv\Scripts\python.exe -m rag.scripts.run_embedding_experiment
```

## 실제 스크립트

- `rag/scripts/run_embedding_experiment.py`

## 입력

- chunk 입력: `experiments/rag_langgraph_eval/results/chunking_matrix/C3_chunk_records.jsonl`
- 질문셋: `experiments/rag_langgraph_eval/datasets/golden_questions_v1.csv`

## 비교 후보

| 후보 | 상태 |
|---|---|
| BAAI/bge-m3 | 완료 |
| Gemini embedding | 완료 |
| OpenAI embedding | 결제/쿼터 문제로 제외 |
| KoSimCSE | 로컬 모델 캐시 문제로 제외 |
| e5-multilingual | 로컬 모델 캐시 문제로 제외 |

## 출력

- `experiments/rag_langgraph_eval/results/embedding_eval/embedding_eval_summary.csv`
- `experiments/rag_langgraph_eval/results/embedding_eval/embedding_eval_detail.csv`
- `experiments/rag_langgraph_eval/results/embedding_eval/embedding_eval_report.md`
- `experiments/rag_langgraph_eval/results/embedding_eval/bge-m3_embedding_eval_summary.csv`
- `experiments/rag_langgraph_eval/results/embedding_eval/bge-m3_embedding_eval_detail.csv`
- `experiments/rag_langgraph_eval/results/embedding_eval/bge-m3_embedding_eval_report.md`

## 현재 판단

A 검색 실험의 고정 embedding 모델은 아래로 결정했다.

```text
embedding_model=BAAI/bge-m3
```

이유:

- 로컬 재현 가능
- API 비용 없음
- 현재 corpus/golden question 기준 검색 품질 충분
