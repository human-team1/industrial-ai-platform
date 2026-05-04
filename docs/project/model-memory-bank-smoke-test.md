# Model Memory Bank Smoke Test

`ROLE_SITE_ADMIN` 토큰과 정상 이미지 1장 이상을 준비한 뒤 아래 형식으로 확인한다.

```powershell
curl -X POST "http://localhost:8080/api/v1/models/1/versions/from-normal-images" `
  -H "Authorization: Bearer ${ACCESS_TOKEN}" `
  -F "normalImages=@./sample/normal_001.jpg" `
  -F "modelCategory=TEXTURE" `
  -F "organizationId=1001" `
  -F "targetId=10" `
  -F "deploymentScope=TARGET" `
  -F "thresholdDefault=0.7500" `
  -F "reason=normal image memory_bank smoke test"
```

`modelProfile`을 생략하면 `SPEED`, `PERFORMANCE` 두 버전이 생성된다. 단일 프로필만 생성하려면 `-F "modelProfile=PERFORMANCE"`처럼 전달한다.

정상 이미지 최소 개수는 `MODEL_MEMORY_BANK_MIN_NORMAL_IMAGE_COUNT` 또는 `factoryguard.model.memory-bank.min-normal-image-count`로 조정한다.
