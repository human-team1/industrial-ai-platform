# 탐지 MVP 범위 조정 메모

## 기능 정의

- 업로드 탐지는 이미지 파일만 지원한다.
- 영상 업로드와 자동 스트리밍 검사는 이번 MVP 프론트 범위에서 제외한다.
- 실시간 탐지 화면은 브라우저 카메라 프리뷰와 버튼 클릭 단건 캡처 검사 방식으로 동작한다.
- 결과 목록과 상세에서는 입력 출처를 `이미지 업로드` 또는 `카메라 캡처`로 구분해 표시한다.

## ERD 해석 메모

- 물리 ERD 변경은 없다.
- 브라우저 카메라 단건 캡처도 업로드 검사 이력으로 저장한다.
- 권장 저장 기준:
  - `inspection_run.run_type = UPLOAD`
  - `inspection_run.input_type = IMAGE`
  - `inspection_run.source_type = BROWSER_CAMERA`
  - `inspection_input.source_type = BROWSER_CAMERA`
  - `inspection_input.file_id = 캡처 이미지 파일 ID`
  - `inspection_input.camera_id = null`
  - `inspection_input.frame_count = 1`
  - `inspection_input.quality_gate_enabled = true`
