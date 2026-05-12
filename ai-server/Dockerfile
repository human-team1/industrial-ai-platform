# CPU 기본 이미지. GPU 추론이 필요하면 nvidia/cuda 기반 이미지로 교체해 빌드하세요.
FROM python:3.10-slim-bookworm

WORKDIR /app

RUN apt-get update \
  && apt-get install -y --no-install-recommends \
    ca-certificates \
    curl \
    libgl1 \
    libglib2.0-0 \
  && rm -rf /var/lib/apt/lists/*

COPY requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

# EmbeddingClient 기본 캐시 경로(Settings.embedding_cache_dir)에 BGE-M3를 미리 채움(local_files_only=True 기본값 정합). EMBEDDING_* 런타임 env 불필요.
RUN mkdir -p /app/experiments/rag_langgraph_eval/results/embedding_eval/cache/huggingface/hub \
 && HF_HOME=/app/experiments/rag_langgraph_eval/results/embedding_eval/cache/huggingface \
    HUGGINGFACE_HUB_CACHE=/app/experiments/rag_langgraph_eval/results/embedding_eval/cache/huggingface/hub \
    HF_HUB_DISABLE_SYMLINKS_WARNING=1 \
    python -c "from sentence_transformers import SentenceTransformer; SentenceTransformer('BAAI/bge-m3', cache_folder='/app/experiments/rag_langgraph_eval/results/embedding_eval/cache/huggingface')"

ENV PYTHONPATH=/app
EXPOSE 8001

# curl: Compose healthcheck 가 `GET /ai/v1/health` 검사 시 사용함.
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8001"]
