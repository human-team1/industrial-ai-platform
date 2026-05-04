# Model Memory Bank Smoke Test

## 운영 페이지

1. `ROLE_SITE_ADMIN` 계정으로 관리자 설정 페이지에 접속한다.
2. 모델 운영 관리에서 모델을 선택하거나 새 모델을 등록한다.
3. 정상 이미지셋을 업로드하고 `OBJECT` 또는 `TEXTURE`만 선택한다.
4. 배포 범위를 `ORGANIZATION` 또는 `TARGET`으로 선택한다.
5. 제출하면 프론트는 `modelProfile`을 보내지 않는다.

`modelProfile` 미전송 시 Spring이 `SPEED`, `PERFORMANCE` 두 모델 버전을 모두 생성한다. ckpt, config, backbone, layer 설정은 운영자가 입력하지 않고 Spring 고정 프로필 설정을 사용한다.

## curl

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

## 프로필 사용 규칙

- 업로드 검사와 브라우저 카메라 단건 캡처는 `PERFORMANCE` 프로필 활성 배포를 사용한다.
- 재검토 큐는 `PERFORMANCE` 프로필 기준 모델 정보를 표시한다.
- 실시간 검사는 `SPEED` 프로필 활성 배포를 사용한다.
- 대상 범위 활성 배포가 없으면 조직 범위 활성 배포로 fallback한다.

정상 이미지 최소 개수는 `MODEL_MEMORY_BANK_MIN_NORMAL_IMAGE_COUNT` 또는 `factoryguard.model.memory-bank.min-normal-image-count`로 조정한다.
