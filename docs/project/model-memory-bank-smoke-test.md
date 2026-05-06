# Model Memory Bank Smoke Test

## 현재 상태

- 정상 이미지 기반 모델 생성은 base `CKPT`/`CONFIG`를 MinIO에서 읽고, FastAPI가 `memory_bank.pt`와 config snapshot을 생성한다.
- base artifact는 Git LFS로 받은 `ai-server/config/model` 파일을 seed 스크립트로 MinIO에 업로드한다.
- `memory_bank.pt`는 고객사/검사대상 정상 이미지셋으로 매번 새로 생성되는 산출물이므로 Git LFS에 포함하지 않는다.
- 실제 화면 E2E 성공 전까지 기능 완료로 표시하지 않는다.

## 고정 기준

- 정상 이미지 최소 기준은 10장이다.
- 9장 이하는 422.
- 10장 이상은 개수 검증 통과.
- `modelProfile` 미전송 시 Spring은 `SPEED`, `PERFORMANCE` 두 버전을 모두 생성 요청한다.
- Spring은 `AI_SERVER_BASE_URL=http://localhost:8001` 기준으로 FastAPI를 호출한다. 8002 우회 검증은 금지한다.

## Git LFS

```powershell
git lfs install
git lfs pull
cd ai-server
dir config\model
```

필수 파일:

- `obj_speed.ckpt`
- `object_speed_wrn50_layer2_224_full_mb10000.json`
- `obj_perf.ckpt`
- `object_perf_dinov2_base_3layers_336_50shot_mb2000.json`
- `texture_speed.ckpt`
- `texture_speed_wrn50_layer2_256_full_mb5000.json`
- `texture_perf.ckpt`
- `texture_perf_dinov2_base_3layers_448_50shot_mb2000.json`

## Base Artifact Seed

```powershell
cd ai-server
.\.venv\Scripts\python.exe .\scripts\seed_base_model_artifacts.py
```

성공 시 MinIO `models` bucket에 아래 object가 있어야 한다.

- `models/base/speed-object/model.ckpt`
- `models/base/speed-object/config.json`
- `models/base/performance-object/model.ckpt`
- `models/base/performance-object/config.json`
- `models/base/speed-texture/model.ckpt`
- `models/base/speed-texture/config.json`
- `models/base/performance-texture/model.ckpt`
- `models/base/performance-texture/config.json`

## FastAPI Runtime Smoke

전제: FastAPI가 `localhost:8001`에서 실행 중이어야 한다.

```powershell
cd ai-server
.\.venv\Scripts\python.exe .\scripts\smoke_memory_bank_runtime.py --category TEXTURE --profile SPEED
.\.venv\Scripts\python.exe .\scripts\smoke_memory_bank_runtime.py --category TEXTURE --profile PERFORMANCE
```

확인 단계:

- MinIO health
- bucket 존재 확인
- base ckpt/config object 확인
- tiny normal images 10장 업로드
- FastAPI memory-bank 호출
- `memory_bank.pt` 업로드 확인
- config snapshot 업로드 확인

## 화면 E2E Smoke

1. Spring 8080, FastAPI 8001, Frontend 5173, MinIO를 실행한다.
2. 관리자 계정으로 로그인한다.
3. 모델 생성 화면에서 `TEXTURE`를 선택한다.
4. 우선 `SPEED` 단일 생성으로 정상 이미지 10장 이상 업로드 후 생성한다.
5. 이후 `PERFORMANCE` 단일 생성, 가능하면 전체 생성을 검증한다.
6. 성공 시 Spring DB에 `FILE`, `MODEL_VERSION`, `MODEL_ARTIFACT(CKPT, CONFIG, MEMORY_BANK)`, `MODEL_DEPLOYMENT` 저장을 확인한다.

실패 시 브라우저 Response, Spring 로그, FastAPI 로그에서 같은 `requestId`로 `errorCode`와 `detail`을 추적한다.
