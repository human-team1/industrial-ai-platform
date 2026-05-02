# AI Server

FastAPI 기반 AI/RAG 서버입니다. 비전 이상탐지, 문서 인덱싱, RAG 검색, LangGraph 기반 질의응답 실험을 담당합니다.

## 기본 구조

```text
ai-server/
├── api/                 # FastAPI router
├── application/         # usecase/service
├── config/              # settings
├── container/           # DI assembly
├── domain/              # schema/domain model
├── infrastructure/      # Chroma/MinIO/Redis/model adapters
├── rag/                 # RAG loader/chunker/schema/experiment scripts
├── experiments/         # 실험 데이터셋, 설정, 결과
├── tests/
├── main.py
├── requirements.txt
└── .env.example
```

## 실험 구조

```text
experiments/rag_langgraph_eval/
├── corpus/              # 실험용 문서 배치 영역, 현재 원본은 rag/corpus 사용
├── datasets/            # golden questions, mock sources
├── configs/             # best_retrieval_config.yaml
├── prompts/             # B 역할 prompt 초안
├── graph/               # B 역할 LangGraph 초안
├── scripts/             # notebook/실험 래퍼용 보조 스크립트
├── results/
│   ├── chunking_matrix/
│   ├── embedding_eval/
│   ├── retrieval_eval/
│   └── topk_threshold_eval/
└── README.md
```

## 설치

```powershell
cd ai-server
python -m venv .venv
.\.venv\Scripts\Activate.ps1
python -m pip install -r requirements.txt
```

필수 버전:

```powershell
python -c "import openai, chromadb; print(openai.__version__); print(chromadb.__version__)"
```

기준:

```text
openai==1.54.4
chromadb==0.5.15
```

## 실행

```powershell
uvicorn main:app --reload --host 0.0.0.0 --port 8005
```

확인:

- `http://localhost:8005/docs`
- `http://localhost:8005/ai/v1/health`

## A 역할 실험 명령

```powershell
.\.venv\Scripts\python.exe -m rag.scripts.run_chunking_matrix
.\.venv\Scripts\python.exe -m rag.scripts.run_embedding_experiment
.\.venv\Scripts\python.exe -m rag.scripts.run_retrieval_experiment
.\.venv\Scripts\python.exe -m rag.scripts.run_topk_threshold_experiment
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

## 테스트

```powershell
pytest
```

## 주의

- `.env`와 API key는 커밋하지 않습니다.
- `experiments/**/cache/`, 모델 파일, 대용량 결과 파일은 Git 대상에서 제외합니다.
- 실서비스 코드는 `api/application/domain/infrastructure/container` 계층을 우선합니다.
- 실험 스크립트는 `rag/scripts`에 두고, 결과는 `experiments/rag_langgraph_eval/results`에 저장합니다.
