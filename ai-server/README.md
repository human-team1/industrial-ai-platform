# AI Server

FastAPI 기반 AI/RAG 내부 서비스입니다. 비전 이상 탐지, 문서 인덱싱, RAG 검색, LLM 응답 정리, 모델 아티팩트 로딩을 담당합니다.

## 현재 스택

- Python 3.10.6
- FastAPI 0.115.5 / Pydantic 2.9.2 / Uvicorn 0.32.0
- PyTorch 2.6.0 / Torchvision 0.21.0 / Anomalib 2.4.0
- LangChain 0.3.x / LangGraph 0.2.x
- ChromaDB 0.5.15
- Redis 5.2.0
- MinIO 7.2.10
- Pillow, OpenCV, scikit-image, scikit-learn
- pytest, ruff, black

## 구조

```text
api/             FastAPI router, 요청/응답 스키마
application/     usecase, 처리 흐름
domain/          모델/포트/규칙
infrastructure/  MinIO, ChromaDB, Redis, 모델/RAG 구현체
container/       DI 조립
config/          환경 설정
scripts/         seed/smoke/helper 스크립트
tests/           테스트
```

Router에서 구현체를 직접 생성하지 않고 `container`를 통해 연결합니다.

## 환경 변수

```powershell
Copy-Item .env.example .env
```

| 변수 | 설명 |
| --- | --- |
| `APP_PORT`, `AI_SERVER_PORT` | FastAPI 포트, 기본 `8001` |
| `CHROMA_HOST`, `CHROMA_PORT` | ChromaDB 접속 정보 |
| `MINIO_ENDPOINT`, `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY` | MinIO 접속 정보 |
| `REDIS_HOST`, `REDIS_PORT` | Redis 접속 정보 |
| `MODEL_NAME` | 기본 비전 모델 이름 |
| `RAG_*` | 검색/청킹/출처 정책 |
| `EMBEDDING_MODEL_NAME` | 임베딩 모델 이름 |
| `LLM_PROVIDER`, `LLM_MODEL_NAME` | LLM 공급자/모델 설정 |

## 설치

```powershell
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
```

NVIDIA CUDA 환경은 가상환경 안에서 아래처럼 전환합니다.

```powershell
pip uninstall -y torch torchvision torchaudio
pip install -r requirements-cuda.txt
```

## 실행

```powershell
uvicorn main:app --reload --host 0.0.0.0 --port 8001
```

시연/운영 계열에서는 reload 없이 worker 1개 기준으로 실행합니다.

```powershell
uvicorn main:app --host 0.0.0.0 --port 8001 --workers 1
```

확인:

- `http://localhost:8001/ai/v1/health`
- `http://localhost:8001/docs`

## 주요 API

- `GET /ai/v1/health`
- `GET /ai/v1/health/infra`
- `POST /ai/v1/inference/anomaly`
- `POST /ai/v1/internal/vision/infer-image`
- `POST /ai/v1/internal/documents/index`
- `POST /ai/v1/internal/rag/query`
- `POST /ai/v1/rag/query`
- `POST /ai/v1/internal/models/memory-bank/from-normal-images`

Spring Backend가 외부 클라이언트의 단일 진입점이며, `internal/*` API는 Spring 내부 연동용입니다.

## 검증

```powershell
pytest
ruff check .
black --check .
```

CUDA 확인:

```powershell
python -c "import torch; print(torch.cuda.is_available()); print(torch.version.cuda); print(torch.cuda.get_device_name(0) if torch.cuda.is_available() else 'CPU only')"
```

## 커밋 제외

`.env`, `.venv/`, `__pycache__/`, `.pytest_cache/`, `.ruff_cache/`, 모델 대용량 파일, 실험 결과물은 커밋하지 않습니다.
