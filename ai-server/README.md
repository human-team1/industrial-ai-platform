# AI Server

FastAPI 기반 AI 서버입니다. 이상 탐지 추론, 문서 업로드 및 인덱싱, RAG 질의응답의 최소 실행 골격을 제공합니다.

## 기술 스택

- Python 3.10.6
- FastAPI 0.115.5
- Pydantic 2.9.2
- LangChain 0.3.7
- LangChain Community 0.3.5
- ChromaDB 0.5.15
- Redis 5.2.0
- MinIO 7.2.10
- pypdf 5.1.0
- python-docx 1.1.2
- numpy 1.26.4
- pillow 11.0.0
- opencv-python-headless 4.10.0.84

`sentence-transformers`, `langchain-openai`, `openai`, `torch`, `transformers`, `ultralytics` 같은 모델/공급자 패키지는 모델 확정 후 추가합니다.

## 환경변수

PowerShell:

```powershell
Copy-Item .env.example .env
```

실제 비밀값은 커밋하지 않습니다.

## 설치

```powershell
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
```

PowerShell 실행 정책 때문에 activate가 실패하면 다음 명령을 실행한 뒤 PowerShell을 새로 엽니다.

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

## API

- `GET /ai/v1/health`
- `POST /ai/v1/inference/anomaly`
- `POST /ai/v1/documents/index`
- `POST /ai/v1/rag/query`

## 테스트

```powershell
pytest
```
