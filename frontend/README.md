# Frontend

## 탐지 MVP 정책

- 업로드 탐지 화면은 이미지 파일만 허용합니다.
- 허용 파일 형식은 `image/jpeg`, `image/png`, `image/webp`입니다.
- `video/*` 파일은 선택 단계와 제출 단계에서 차단합니다.
- 실시간 탐지 화면은 브라우저 카메라 프리뷰를 보여주고, `[현재 화면 검사]` 버튼 클릭 시 현재 프레임 1장을 캡처해 `/api/v1/inspections/upload`로 전송합니다.
- `POST /inspections/realtime`, `POST /inspections/{inspectionId}/frames`, `PATCH /inspections/{inspectionId}/stop` API는 백엔드 확장 계약으로 유지하지만 현재 프론트에서는 호출하지 않습니다.

React + Vite + TypeScript 기반 웹 클라이언트입니다. 사용자는 검사/결과/문서/RAG/챗봇/대시보드 화면을 이용하고, 관리자는 가입 승인 등 운영 화면을 사용합니다.

## 현재 스택

- React 18.3.1
- Vite 5.4.10
- TypeScript 5.6.3
- React Router 6.28.0
- Axios 1.7.7
- Tailwind CSS 3.4.19
- Google OAuth React 0.13.5

## 환경 변수

```powershell
Copy-Item .env.example .env
```

| 변수 | 설명 |
| --- | --- |
| `VITE_APP_NAME` | 앱 이름 |
| `VITE_API_BASE_URL` | Spring API base URL, 기본값 `/api/v1` |
| `VITE_GOOGLE_CLIENT_ID` | Google OAuth Client ID |

## 설치

```powershell
npm install
```

## 실행

```powershell
npm run dev
```

기본 개발 서버는 `http://localhost:5173`입니다. 포트가 사용 중이면 Vite가 다음 포트를 자동 사용합니다.

## 검증

```powershell
npm run build
npm run lint
```

## 구조

```text
src/
  app/        providers, router
  pages/      라우트 단위 페이지
  widgets/    페이지 조립 UI
  features/   기능 단위 api/model/ui
  entities/   도메인 entity 단위 api/model/ui
  shared/     API client, 공통 UI, hooks, lib, types
```

상위 계층은 하위 계층만 참조합니다.

## 문서 관리 화면

현재 문서 관리 라우트:

- `/documents`
- `/documents/new`
- `/documents/:documentId/edit`

등록/수정은 같은 공통 페이지와 폼을 사용합니다.

- 페이지: `src/pages/documents/DocumentFormPage.tsx`
- 훅: `src/features/document-form/model/useDocumentForm` 역할의 `useDocumentForm`
- UI: `src/features/document-form/ui/DocumentForm`
- API: `src/features/document-form/api`

등록 모드:

- 파일 업로드 필수
- 문서명 필수
- 저장 후 문서 목록으로 이동

수정 모드:

- 문서 상세 조회 후 초기값 세팅
- 기존 파일 정보 표시
- 새 파일 선택 시 새 버전으로 등록
- 인덱싱 상태와 미리보기 영역 표시

## 인증 처리

- Access Token은 메모리에 보관합니다.
- Refresh Token은 HttpOnly Cookie 기반 갱신 API를 사용합니다.
- 401 응답 시 `/auth/refresh` 재시도 후 원 요청을 재실행합니다.
- 403 관리자 접근 실패 시 `/dashboard`로 이동합니다.

## 커밋 제외

`node_modules/`, `dist/`, `.env`, `*.log`는 커밋하지 않습니다.
## Nginx 게이트웨이 기준 API 경로

- `VITE_API_BASE_URL` 기본값은 `/api/v1`입니다.
- 운영/시연 환경에서는 `http://localhost:8080/api/v1` 같은 절대 주소를 사용하지 않습니다.
- Cloudflare Tunnel 주소나 최종 도메인이 바뀌어도 같은 프론트 코드가 동작하도록 상대경로를 유지합니다.
- FastAPI는 프론트에서 직접 호출하지 않고 Spring Boot 내부 연동으로만 사용합니다.
