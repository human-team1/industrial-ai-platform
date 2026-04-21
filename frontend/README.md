# Frontend

React + Vite + TypeScript 기반 클라이언트입니다.

## 기술 스택

- React 18.3.1
- Vite 5.4.10
- TypeScript 5.6.3
- React Router 6.28.0
- Axios 1.7.7
- Bootstrap 5.3.3
- Zustand 5.0.1
- ESLint 8.57.1
- Prettier 3.3.3

## 환경변수

PowerShell:

```powershell
Copy-Item .env.example .env
```

| 변수 | 설명 |
| --- | --- |
| `VITE_APP_NAME` | 화면에 표시할 앱 이름 |
| `VITE_API_BASE_URL` | Spring API 서버 base URL |
| `VITE_AI_API_BASE_URL` | FastAPI AI 서버 base URL |

## 설치

```powershell
npm install
```

## 실행

```powershell
npm run dev
```

기본 개발 서버는 `http://localhost:5173`입니다.

## 검증

```powershell
npm run build
npm run lint
```

`npm audit` 경고가 나올 수 있지만, 초기 단계에서는 강제 업그레이드보다 프로젝트 고정 버전과 호환성을 우선합니다.

## 폴더 구조

```text
src/
  app/        # providers, router
  pages/      # route 단위 페이지
  widgets/    # 페이지 조합 UI
  features/   # inspection, result, chatbot, auth 기능 경계
  entities/   # 도메인 entity UI/model
  shared/     # API client, 공통 UI, hooks, lib, types
```
