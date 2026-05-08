# 초기 셋업 (해야 할 일만)

클론 후 로컬에서 할 작업만 순서대로 적었습니다.

---

## 목차

1. [설치](#1-설치)
2. [`.env` 만들기](#2-env-만들기)
3. [비밀번호 맞추기](#3-비밀번호-맞추기)
4. [`.env`에서 꼭 손볼 값](#4-env에서-꼭-손볼-값)
5. [npm / pip](#5-npm--pip)
6. [Spring Boot와 `backend-spring/.env` (필수)](#6-spring-boot와-backend-springenv-필수)
7. [Docker + DB](#7-docker--db)
8. [MinIO 버킷](#8-minio-버킷)
9. [모델: LFS → MinIO](#9-모델-lfs--minio)
10. [Ollama](#10-ollama)
11. [Embedding (선택)](#11-embedding-선택)
12. [GPU (선택)](#12-gpu-선택)
13. [Google 로그인](#13-google-로그인)
14. [LangSmith 끄기 (선택)](#14-langsmith-끄기-선택)
15. [관리자 승인](#15-관리자-승인)
16. [기동 순서](#16-기동-순서)
17. [헬스 URL](#17-헬스-url)
18. [`.gitignore`와 이 문서](#18-gitignore와-이-문서)
19. [문제 나오면](#19-문제-나오면)

---

## 1. 설치

- Docker Desktop
- Node.js 18+
- Python **3.10.6**
- JDK **17** (`JAVA_HOME` = JDK 17)

activate 막히면 (Windows):

```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

---

## 2. `.env` 만들기

프로젝트 루트에서:

```powershell
Copy-Item .\infra\.env.example              .\infra\.env
Copy-Item .\backend-spring\.env.example     .\backend-spring\.env
Copy-Item .\frontend\.env.example           .\frontend\.env
Copy-Item .\ai-server\.env.example          .\ai-server\.env
```

---

## 3. 비밀번호 맞추기

동일 문자열로 통일:

| 용도 | 파일·변수 |
|---|---|
| DB 사용자 비번 | `infra` → `MARIADB_PASSWORD` |
| DB 사용자 비번 | `backend-spring` → `DB_PASSWORD` |
| MinIO | `infra` → `MINIO_ROOT_PASSWORD` = `MINIO_SECRET_KEY` |
| MinIO | `backend-spring`, `ai-server` → `MINIO_SECRET_KEY` (= 위 MinIO 루트 비번) |

---

## 4. `.env`에서 꼭 손볼 값

| 파일 | 변수 | 할 일 |
|---|---|---|
| `infra/.env` | `MARIADB_ROOT_PASSWORD`, `MARIADB_PASSWORD`, `MINIO_ROOT_PASSWORD`, `MINIO_SECRET_KEY` | 기본값 바꾸기 |
| `backend-spring/.env` | `DB_PASSWORD`, `MINIO_SECRET_KEY`, `JWT_SECRET` | DB·MinIO 위와 맞추기 / JWT는 32자 이상 임의값 |
| `backend-spring/.env` | `DOCUMENT_UPLOAD_ALLOWED_MIME_TYPES` | **예시 그대로 두거나** 없으면 `.env.example` 57행 복사 |
| `backend-spring/.env` | `GOOGLE_CLIENT_SECRET` | Google 로그인 쓰면 입력 |
| `ai-server/.env` | `MINIO_SECRET_KEY` | MinIO와 동일 |
| `infra/.env` | `REDIS_PASSWORD` | 쓰면 `backend-spring`·`ai-server` 의 `REDIS_PASSWORD` 와 동일하게 |

---

## 5. npm / pip

```powershell
cd frontend
npm install
```

```powershell
cd ai-server
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
```

---

## 6. Spring Boot와 `backend-spring/.env` (필수)

- **Spring Boot는 `backend-spring/.env` 파일을 읽지 않습니다.**  
  `application.yml`의 `${변수:기본값}` 은 **OS 환경 변수**만 사용합니다.
- FastAPI는 `ai-server`에서 `pydantic`으로 `.env` 자동 로드, Vite는 `frontend/.env` 자동 로드.

**`bootRun` 전에** `backend-spring/.env` 내용을 프로세스 환경에 넣고 실행 (PowerShell 예):

```powershell
cd backend-spring
foreach ($line in Get-Content .\.env) {
  if ($line -match '^\s*([^#][^=]*)=(.*)$') {
    [Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim(), 'Process')
  }
}
.\gradlew.bat bootRun --args="--spring.profiles.active=local"
```

또는 IDE에서 Env 파일 플러그인으로 동일 변수 주입.

---

## 7. Docker + DB

```powershell
cd infra
docker compose --env-file .env up -d
```

최초 1회 (DB 전부):

```powershell
.\scripts\db-reset.ps1 -Force
```

이후 마이그레이션만 추가됐을 때:

```powershell
.\scripts\db-migrate.ps1
```

---

## 8. MinIO 버킷

콘솔 `http://localhost:9001` → 버킷 존재 확인: `documents`, `inspection-artifacts`, `reports`, `models`  
(`MINIO_AUTO_CREATE_BUCKETS=true`면 Spring 첫 기동 때 만들 수 있음 — MinIO가 먼저 떠 있어야 함)

---

## 9. 모델: LFS → MinIO

**LFS로 받은 파일 ≠ MinIO 자동 반영.** 둘 다 해야 함.

1. 레포 루트에서:

```powershell
git lfs install
git lfs pull
```

2. `.gitattributes`: `ai-server/config/model/*.ckpt`, `*.pth` 만 LFS.  
   `ai-server/config/model/` 아래 ckpt가 **큰 파일**이면 성공. 텍스트 몇 줄이면 `git lfs pull` 다시.

3. MinIO 버킷 **`models`** 에, `backend-spring/.env` 의 `MODEL_*_CKPT_KEY` / `MODEL_*_CONFIG_KEY` 와 **같은 경로**로 업로드. 기본 예:

```
models/base/speed-object/model.ckpt
models/base/speed-object/config.json
models/base/speed-texture/model.ckpt
models/base/speed-texture/config.json
models/base/performance-object/model.ckpt
models/base/performance-object/config.json
models/base/performance-texture/model.ckpt
models/base/performance-texture/config.json
```

4. 콘솔 `http://localhost:9001` 에서 폴더 맞춰 업로드하거나:

```powershell
mc alias set local http://localhost:9000 minioadmin <MINIO_ROOT_PASSWORD>
mc cp .\로컬파일.ckpt local/models/base/speed-object/model.ckpt
```

레포에 ckpt 없으면 담당자·공유폴더에서 받아서 **같은 MinIO 키**로만 올리면 됨.

---

## 10. Ollama

- 설치: https://ollama.com/download  
- `ai-server/.env` 의 `LLM_MODEL_NAME` 에 맞춰:

```powershell
ollama pull qwen2.5:1.5b
```

---

## 11. Embedding (선택)

기본은 첫 FastAPI 실행 시 HuggingFace 자동 다운로드 → `ai-server/**/.cache/` 등 생성 (용량 큼, git 무시).  
오프라인이면 `ai-server/.env` 에서 `EMBEDDING_LOCAL_FILES_ONLY=true` 등 팀 정책 따름.

---

## 12. GPU (선택)

venv 안에서 CPU:

```powershell
pip install -r requirements.txt
```

NVIDIA CUDA:

```powershell
pip uninstall -y torch torchvision torchaudio
pip install -r requirements-cuda.txt
```

---

## 13. Google 로그인

`backend-spring/.env` → `GOOGLE_CLIENT_SECRET` 채우기 (팀에서 받은 값).

---

## 14. LangSmith 끄기 (선택)

`ai-server/.env`:

```env
LANGSMITH_TRACING=false
```

---

## 15. 관리자 승인

```powershell
cd infra
.\scripts\promote-admin.ps1
```

또는 DB에서 해당 이메일 `ACTIVE` + `ROLE_SITE_ADMIN` 처리.

---

## 16. 기동 순서

1. `infra` → `docker compose --env-file .env up -d`
2. (최초) `db-reset.ps1 -Force`
3. `backend-spring` → **6절**처럼 env 로드 후 `bootRun` 또는 IDE 주입
4. `ai-server` venv → **`ai-server` 디렉터리에서** `uvicorn main:app --reload --host 0.0.0.0 --port 8001` (`.env` 로드됨)
5. `frontend` → `npm run dev`

---

## 17. 헬스 URL

| 대상 | URL |
|---|---|
| 프론트 | http://localhost:5173 |
| Spring | http://localhost:8080/api/v1/health |
| FastAPI | http://localhost:8001/ai/v1/health |
| Swagger | http://localhost:8001/docs |
| MinIO | http://localhost:9001 |
| Nginx | http://localhost |

---

## 18. `.gitignore`와 이 문서

| gitignore 대상 | 로컬에서 할 일 |
|---|---|
| **`.env`** | 2·3·4절 + Spring은 **6절** |
| **`node_modules/`, `dist/`** | 5절 `npm install` |
| **`.venv/`, `__pycache__/`** | 5절 venv + pip |
| **`.gradle/`, `build/`** | 첫 `gradlew` 때 자동 생성 (커밋 안 함) |
| **`*.pt`, `*.ckpt`, MinIO 대용량** | 9절 LFS → MinIO (레포·gitignore와 별개) |
| **`**/memory_bank*`, `generated/`** | 추론·메모리뱅크 생성 시 로컬/.minio에 생김, 커밋 안 함 |
| **`ai-server/**/.cache/`, HuggingFace** | 11절 첫 실행 시 자동 |
| **실험 `experiments/**/results` 등** | RAG 실험 시만, 초기 셋업 필수 아님 |
| **`.vscode/`, `.idea/`** | IDE 설정은 레포에 없음 — 팀 공유 있으면 따로 적용 |
| **로그 `*.log`** | 별도 설치 없음 |

→ **문서만으로 부족했던 부분**: Spring은 `.env` 파일을 안 읽음 → **6절 추가함.**

---

## 19. 문제 나오면

| 증상 | 할 일 |
|---|---|
| 문서 업로드 422 MIME | `backend-spring/.env`에 `DOCUMENT_UPLOAD_ALLOWED_MIME_TYPES=` 줄이 있는지 확인 → **6절로 env 반영 후** Spring 재시작 |
| `.env` 고쳤는데 Spring만 안 바뀜 | **6절** (환경 변수 미주입) |
| venv activate 실패 | 1절 `Set-ExecutionPolicy` |
| DB 연결 안 됨 | `DB_PASSWORD` = `MARIADB_PASSWORD` |
| MinIO 연결 안 됨 | 세 `.env`의 MinIO 비번 통일 |
| db-reset 실패 | 먼저 `docker compose up -d` 후 재실행 |

```powershell
cd infra
docker compose --env-file .env up -d
.\scripts\db-reset.ps1 -Force
```
