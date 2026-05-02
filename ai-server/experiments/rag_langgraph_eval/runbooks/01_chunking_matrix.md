# 01. Chunking Matrix Experiment

## 목적

문서 27개를 대상으로 chunk size / overlap / section 기반 chunking을 비교했다.

## 실행 코드

```powershell
cd C:\Users\jecho\OneDrive\Desktop\CJE\industrial-ai-platform\ai-server
.\.venv\Scripts\python.exe -m rag.scripts.run_chunking_matrix
```

## 실제 스크립트

- `rag/scripts/run_chunking_matrix.py`

## 입력

- 문서 원본: `rag/corpus/`
- Markdown loader: `rag/loaders/markdown_loader.py`
- Section chunker: `rag/chunking/section_chunker.py`

## 실험 조합

| config | chunk_size | overlap | 목적 |
|---|---:|---:|---|
| C1 | 300 | 50 | 짧은 질의 반응 |
| C2 | 500 | 100 | 균형형 baseline |
| C3 | 800 | 100 | 매뉴얼 절차 단위 보존 |
| C4 | 1000 | 150 | 긴 대응 절차 보존 |
| C5 | section-based | section-based | 섹션 단위 검색 품질 |
| C6 | heading + token hybrid | 100 | 제목 유지 + 본문 split |

## 출력

- `experiments/rag_langgraph_eval/results/chunking_matrix/chunking_experiment_summary.csv`
- `experiments/rag_langgraph_eval/results/chunking_matrix/chunking_experiment_report.md`
- `experiments/rag_langgraph_eval/results/chunking_matrix/C1_chunk_records.jsonl`
- `experiments/rag_langgraph_eval/results/chunking_matrix/C2_chunk_records.jsonl`
- `experiments/rag_langgraph_eval/results/chunking_matrix/C3_chunk_records.jsonl`
- `experiments/rag_langgraph_eval/results/chunking_matrix/C4_chunk_records.jsonl`
- `experiments/rag_langgraph_eval/results/chunking_matrix/C5_chunk_records.jsonl`
- `experiments/rag_langgraph_eval/results/chunking_matrix/C6_chunk_records.jsonl`

## 현재 판단

B 통합 및 retrieval 실험 기준 chunk config는 C3이다.

```text
chunk_size=800
chunk_overlap=100
```
