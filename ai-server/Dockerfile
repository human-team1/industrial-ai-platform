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

ENV PYTHONPATH=/app
EXPOSE 8001

# curl: Compose healthcheck 가 `GET /ai/v1/health` 검사 시 사용함.
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8001"]
