# AI Server

FastAPI 기반 AI/RAG 서버입니다. 비전 이상 탐지, 문서 인덱싱, RAG 검색, LLM 응답 정리 기능을 담당하는 내부 서비스입니다.

## 현재 스택

- Python 3.10.6
- FastAPI 0.115.5
- Pydantic 2.9.2
- Uvicorn 0.32.0
- ChromaDB 0.5.15
- LangChain 0.3.7
- Redis 5.2.0
- MinIO 7.2.10
- pypdf 5.1.0
- python-docx 1.1.2
- numpy 1.26.4
- pillow 11.0.0
- opencv-python-headless 4.10.0.84

## 구조 원칙

```text
api/             FastAPI router, 요청/응답
application/     usecase, 처리 흐름
domain/          모델/포트/규칙
infrastructure/  MinIO, ChromaDB, Redis, 모델/RAG 구현체
container/       DI 조립
config/          환경 설정
rag/             RAG loader/chunker/schema/indexing/Chroma code
experiments/     최종 설정, 계약 문서, 요약 보고서
```

Router에서 구현체를 직접 생성하지 않고 container를 통해 연결합니다.

## RAG 구조

```text
experiments/rag_langgraph_eval/
├── corpus/              # 실험용 문서 배치 영역, 현재 원본은 rag/corpus 사용
├── configs/             # 최종 채택 retrieval 설정
├── prompts/             # B 역할 prompt 초안
├── graph/               # B 역할 LangGraph 초안
├── scripts/             # notebook/실험 래퍼용 보조 스크립트
├── results/
│   ├── chunking_matrix/     # 요약 MD만 유지
│   ├── embedding_eval/      # 요약 MD만 유지
│   ├── retrieval_eval/      # 최종 검증 MD/YAML만 유지
│   └── topk_threshold_eval/ # 요약 MD만 유지
└── README.md
```

## 환경 변수

```powershell
Copy-Item .env.example .env
```

주요 값:

| 변수 | 설명 |
| --- | --- |
| `APP_PORT` | FastAPI 포트, 기본 `8001` |
| `CHROMA_HOST`, `CHROMA_PORT` | ChromaDB 접속 정보 |
| `MINIO_ENDPOINT`, `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY` | MinIO 접속 정보 |
| `REDIS_HOST`, `REDIS_PORT` | Redis 접속 정보 |
| `MODEL_NAME` | 비전 모델 이름 |
| `EMBEDDING_MODEL_NAME` | 임베딩 모델 이름 |
| `LLM_MODEL_NAME` | LLM 모델 이름 |

## 설치

```powershell
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
```

PowerShell 실행 정책 때문에 activate가 실패하면:

```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

## 실행

```powershell
uvicorn main:app --reload --host 0.0.0.0 --port 8001
```

확인:

- `http://localhost:8001/docs`
- `http://localhost:8001/ai/v1/health`

## 주요 API

- `GET /ai/v1/health`
- `POST /ai/v1/inference/anomaly`
- `POST /ai/v1/documents/index`
- `POST /ai/v1/rag/query`

## RAG 인덱싱/검증

```powershell
.\.venv\Scripts\python.exe -m rag.scripts.build_chroma_final_index
.\.venv\Scripts\python.exe -m rag.scripts.validate_index
.\.venv\Scripts\python.exe -m rag.scripts.validate_schemas
.\.venv\Scripts\python.exe -m rag.scripts.validate_source_contract
```

현재 확정 retrieval 설정:

- `embedding_model`: `BAAI/bge-m3`
- `chunk_size`: `800`
- `chunk_overlap`: `100`
- `search_mode`: `hybrid`
- `internal_top_k`: `10`
- `answer_top_k`: `5`
- `visible_source_limit`: `3`
- `min_score`: `0.4`

## 검증

```powershell
pytest
ruff check .
black --check .
```

## 커밋 제외

- `.env`, `.venv/`, `__pycache__/`, `.pytest_cache/`, `.ruff_cache/`는 커밋하지 않습니다.
- `experiments/**/*.csv`, `experiments/**/*.json`, `experiments/**/*.jsonl`, `cache/`, 모델 파일, 대용량 결과 파일은 Git 대상에서 제외합니다.
- 실험 결과 공유는 요약 MD 또는 외부 문서 링크로 제한합니다. 전체 detail 결과와 임시 산출물은 커밋하지 않습니다.
