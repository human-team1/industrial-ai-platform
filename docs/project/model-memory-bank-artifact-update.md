# 모델 산출물 구조 변경 메모

## 변경 목적
- 최종 비전 이상탐지 추론은 `ckpt + config.json + memory_bank` 조합이 모두 있어야 가능하다.
- `ckpt`에는 backbone 또는 feature extractor/layer 관련 산출물이 포함될 수 있다.
- 고객사/검사대상별 정상 기준은 `memory bank`가 달라지는 구조로 관리한다.

## ModelArtifactType
- `CKPT`
- `CONFIG`
- `MEMORY_BANK`
- `LABELS`
- `EXTRA`

## 모델 버전 업로드 API
`POST /api/v1/models/{modelId}/versions`

필수 multipart 필드:
- `ckptFile`
- `configFile`
- `memoryBankFile`

선택 multipart 필드:
- `labelsFile`

검증:
- `memoryBankFile` 누락 시 `400 Bad Request`

artifact 응답 예시:

```json
{
  "modelArtifactId": 103,
  "artifactType": "MEMORY_BANK",
  "fileId": 503
}
```

## 모델 버전 활성화 조건
- `CKPT`
- `CONFIG`
- `MEMORY_BANK`

위 3개 artifact가 모두 존재해야 `activate` 가능하다.

## FastAPI internal infer-image 계약
`POST /ai/v1/internal/vision/infer-image`

request.model 필드:

```json
{
  "modelVersionId": 10,
  "modelCategory": "TEXTURE",
  "modelProfile": "PERFORMANCE",
  "framework": "PYTORCH",
  "inputSize": "256x256",
  "ckptFileKey": "models/1/versions/10/model.ckpt",
  "configFileKey": "models/1/versions/10/config.json",
  "memoryBankFileKey": "models/1/versions/10/memory_bank.npy",
  "labelsFileKey": null
}
```

검증:
- `memoryBankFileKey` 누락 시 `400 Bad Request`
- 저장소에 해당 memory bank object가 없으면 `404 Not Found`
