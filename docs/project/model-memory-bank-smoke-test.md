# Model Memory Bank Smoke Test

## 운영 페이지

1. `ROLE_SITE_ADMIN` 계정으로 관리자 설정 또는 모델 관리 화면에 접속한다.
2. 모델을 선택하거나 새 모델을 등록한다.
3. 정상 이미지 100장 이상을 업로드한다.
4. `modelCategory`는 `OBJECT` 또는 `TEXTURE` 중 하나를 선택한다.
5. `modelProfile` 미선택 시 `SPEED`, `PERFORMANCE` 두 버전이 모두 생성되는지 확인한다.
6. `modelProfile=SPEED` 또는 `modelProfile=PERFORMANCE` 선택 시 해당 프로필 1개만 생성되는지 확인한다.

## curl

`ROLE_SITE_ADMIN` 토큰과 정상 이미지 100장 이상을 준비한 뒤 아래 형식으로 확인한다.

```powershell
curl -X POST "http://localhost:8080/api/v1/models/1/versions/from-normal-images" `
  -H "Authorization: Bearer ${ACCESS_TOKEN}" `
  -F "normalImages=@./sample/normal_001.jpg" `
  -F "normalImages=@./sample/normal_002.jpg" `
  # ... normalImages를 총 100장 이상 반복 첨부
  -F "modelCategory=TEXTURE" `
  -F "organizationId=1001" `
  -F "targetId=10" `
  -F "deploymentScope=TARGET" `
  -F "thresholdDefault=0.7500" `
  -F "reason=normal image memory_bank smoke test"
```

## 현재 검증 기준

- 정상 이미지는 최소 100장 필요하다.
- DINOv2는 `PERFORMANCE`, WideResNet50은 `SPEED` 프로필로 고정한다.
- `ckpt/config`는 고정 산출물을 재사용하고, 고객사/검사대상별 `memory_bank`만 새로 생성한다.
- 생성된 `memory_bank`는 `FILE`, `MODEL_VERSION`, `MODEL_ARTIFACT`, `MODEL_DEPLOYMENT`에 연결된다.
