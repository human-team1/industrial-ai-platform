# 05. Schema / Contract / Trace Smoke

## 목적

B가 LangGraph에서 사용할 A-side retrieval contract를 고정했다.

포함:

- `SourceChunk`
- `RetrievalQuery`
- `RetrievalResult`
- `RetrievalConfig`
- `mock_sources_v1.json`
- `retriever_contract.md`
- LangSmith retrieval tracer wrapper

## 실행 코드

```powershell
cd C:\Users\jecho\OneDrive\Desktop\CJE\industrial-ai-platform\ai-server
.\.venv\Scripts\python.exe -m rag.scripts.validate_schemas
```

Mock source 검증:

```powershell
.\.venv\Scripts\python.exe -c "import json; from pathlib import Path; from rag.schemas import SourceChunk; rows=json.loads(Path('experiments/rag_langgraph_eval/datasets/mock_sources_v1.json').read_text(encoding='utf-8')); [SourceChunk(**r) for r in rows]; print('mock_sources_valid', len(rows))"
```

Tracer/chunker import 검증:

```powershell
.\.venv\Scripts\python.exe -c "from rag.tracing import RetrievalTracer; from rag.chunking import TokenChunker, SectionChunker; t=RetrievalTracer(enabled=False); print(t.trace.__name__, TokenChunker.__name__, SectionChunker.__name__)"
```

Health test:

```powershell
.\.venv\Scripts\python.exe -m pytest tests\test_health.py -q
```

## 실제 파일

Schema:

- `rag/schemas/source_chunk.py`
- `rag/schemas/retrieval_result.py`
- `rag/schemas/retrieval_config.py`
- `rag/schemas/__init__.py`

Trace:

- `rag/tracing/langsmith_tracer.py`
- `rag/tracing/__init__.py`

Chunker:

- `rag/chunking/section_chunker.py`
- `rag/chunking/token_chunker.py`

Contract / dataset:

- `experiments/rag_langgraph_eval/configs/retriever_contract.md`
- `experiments/rag_langgraph_eval/datasets/mock_sources_v1.json`
- `experiments/rag_langgraph_eval/datasets/result_linked_query_cases.json`

## 검증 결과

```text
Schema validation passed.
mock_sources_valid 4
trace TokenChunker SectionChunker
1 passed
```

## A/B 책임 경계

A:

```text
RetrieverPort.search()
또는 POST /internal/rag/search
-> SourceChunk[] 반환
```

B:

```text
POST /api/v1/rag/query
-> LangGraph + Prompt + LLM + Source Verifier + 최종 RagQueryResponse
```
