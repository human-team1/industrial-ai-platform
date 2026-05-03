# ERD 메모: MODEL_ARTIFACT MEMORY_BANK

테이블 구조 변경은 없다.

## MODEL_ARTIFACT.artifact_type
허용 설명:
- `CKPT`
- `CONFIG`
- `MEMORY_BANK`
- `LABELS`
- `EXTRA`

## MODEL_VERSION 비고
PatchCore 계열 모델 버전은 `CKPT`, `CONFIG`, `MEMORY_BANK` 산출물이 모두 존재해야 배포 가능 상태로 활성화할 수 있다.
