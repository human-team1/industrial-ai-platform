# Chroma Baseline Validation Status

## 현재 상태

Chroma baseline index 생성 스크립트와 검증 스크립트는 Gemini embedding 기준으로 작성되어 있다.

- `rag/scripts/build_chroma_baseline_index.py`
- `rag/scripts/validate_index.py`

다만 이 워크스페이스에서는 실제 baseline Chroma index build와 validate를 아직 실행하지 못했다.

## 미실행 사유

검증 시점 환경 확인 결과:

```text
GEMINI_API_KEY_SET=True
CHROMA_HOST=localhost
CHROMA_PORT=8003
CHROMA_TCP_REACHABLE=False
```

`build_chroma_baseline_index.py`는 Gemini `gemini-embedding-001` 임베딩을 호출하고, `localhost:8003` Chroma HTTP 서버에 upsert한다. 현재 Gemini key는 설정되어 있으나 Chroma 서버가 열려 있지 않아 실제 실행 조건이 충족되지 않았다.

## 작성된 baseline 설정

스크립트 기준 baseline:

```text
retrieval_config_id=R0_GEMINI_BASELINE
collection_name=industrial_rag_chunks_a_gemini_baseline
embedding_provider=gemini
embedding_model=gemini-embedding-001
chunk_size=800
chunk_overlap=100
search_mode=vector
top_k=5
document_status_filter=PUBLISHED
```

## 최종 freeze 설정과의 관계

최종 실험 best config는 다음과 같다.

```text
retrieval_config_id=R3_HYBRID_TK10_S04
collection_name=industrial_rag_chunks_a_v1
embedding_model=BAAI/bge-m3
search_mode=hybrid
internal_top_k=10
answer_top_k=5
visible_source_limit=3
min_score=0.4
```

즉, `R0_BASELINE`은 Day 1 baseline용이고, B 통합용 freeze는 `R3_HYBRID_TK10_S04`이다.

## 실행 절차

Chroma와 OpenAI baseline을 실제 검증하려면 아래 순서로 실행한다.

```powershell
cd C:\Users\jecho\OneDrive\Desktop\CJE\industrial-ai-platform\ai-server

# 1. .env에 GEMINI_API_KEY 또는 GOOGLE_API_KEY 설정
# 2. Chroma HTTP server를 localhost:8003에서 실행

.\.venv\Scripts\python.exe rag\scripts\build_chroma_baseline_index.py
.\.venv\Scripts\python.exe rag\scripts\validate_index.py
```

성공 시 `experiments/rag_langgraph_eval/results/retrieval_eval/index_build_log.jsonl`에 `chroma_baseline_index_built` 이벤트가 추가되어야 한다.

## 검증 쿼리

`validate_index.py`는 아래 3개 샘플을 확인한다.

| case | query | 기대 |
|---|---|---|
| Q1 | FlexLink X65 컨베이어 정렬 문제 조치 방법 | 관련 문서 검색 |
| Q2 | 조명 반사 때문에 heatmap이 넓게 나올 때 확인할 항목 | 관련 문서 검색 |
| Q3 | 커피머신 물탱크 청소 방법 | 낮은 score 또는 빈 결과 |

## 남은 작업

- Chroma 서버 실행 후 실제 upsert 로그 확보
- `industrial_rag_chunks_a_v1` 통합 collection 생성 여부 확인
- bge-m3 기반 hybrid config를 실제 서비스 retriever로 구현할지, Gemini Chroma vector-only와 BM25 local index를 조합할지 결정
