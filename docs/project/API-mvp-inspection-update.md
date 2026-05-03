# 탐지 MVP API 보정 메모

## 업로드 탐지

- MVP 프론트는 `POST /inspections/upload`에 이미지 파일만 전송한다.
- 허용 형식은 `jpg`, `jpeg`, `png`, `webp` 및 해당 이미지 MIME 타입이다.
- `sourceType`은 optional 필드이며 기본값은 `IMAGE`다.
- 카메라 캡처 요청은 `sourceType=BROWSER_CAMERA`를 사용한다.
- 프론트 업로드 요청 기본값:
  - `inputMode=IMAGE`
  - `sourceType=IMAGE`
  - `roiMode=FULL_FRAME`
  - `qualityGateEnabled=true`

## 카메라 단건 검사

- MVP 프론트는 지속 스트리밍 업로드를 사용하지 않는다.
- 브라우저 카메라 프리뷰는 유지한다.
- 사용자가 `[현재 화면 검사]` 버튼을 누르면 현재 프레임 1장을 캡처해 기존 `POST /inspections/upload` API로 전송한다.
- 카메라 캡처 요청 기본값:
  - `file=captured-frame-{timestamp}.jpg`
  - `inputMode=IMAGE`
  - `sourceType=BROWSER_CAMERA`
  - `roiMode=FULL_FRAME`
  - `qualityGateEnabled=true`
  - `idempotencyKey=camera-capture-{timestamp}-{random}`

## 실시간 세션 API 유지

아래 API는 삭제하지 않고 백엔드 확장 계약으로 유지한다.

- `POST /inspections/realtime`
- `POST /inspections/{inspectionId}/frames`
- `PATCH /inspections/{inspectionId}/stop`

단, 현재 MVP 프론트에서는 호출하지 않는다.
