# Frontend

React + Vite + TypeScript 기반 웹 클라이언트입니다. 사용자는 검사 요청, 결과 조회, 문서/RAG, 챗봇, 대시보드를 사용하고 관리자는 가입 승인과 운영 화면을 사용합니다.

## 현재 범위

- 이미지 업로드 검사를 지원합니다.
- 브라우저 카메라 화면은 현재 프레임 1장을 캡처해 업로드 검사 API를 재사용합니다.
- 영상 전체 분석과 지속 스트리밍 업로드는 확장 예정입니다.
- FastAPI는 프론트에서 직접 호출하지 않고 Spring API를 통해서만 연동합니다.

## 기술 스택

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
| `VITE_GOOGLE_CLIENT_ID` | Google OAuth Web Client ID |

운영/시연 환경에서는 가능한 한 `/api/v1` 상대경로를 사용합니다. 도메인이 바뀌어도 Nginx가 같은 origin에서 Spring으로 프록시할 수 있습니다.

## 실행

```powershell
npm install
npm run dev
```

기본 주소는 `http://localhost:5173`입니다.

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

상위 계층은 하위 계층만 참조합니다. API 호출은 `shared/api` 또는 각 feature의 `api` 계층에 둡니다.

## 주요 라우트

- `/dashboard`
- `/inspections`
- `/results`
- `/documents`
- `/documents/new`
- `/documents/:documentId/edit`
- `/chatbot-history`
- 관리자/운영 화면은 라우터와 권한 설정을 기준으로 확인합니다.

## 커밋 제외

`node_modules/`, `dist/`, `.env`, `*.log`는 커밋하지 않습니다.
