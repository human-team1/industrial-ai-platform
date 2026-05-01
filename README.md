# Industrial AI Platform

AI 기반 설비 점검 보조 시스템 프로젝트입니다.
이 문서는 **프로젝트 입구(문서 지도 + 실행 진입점)** 역할만 담당합니다.

## 빠른 시작

1. 환경 변수 파일 준비: 각 시스템 README 참고
2. 인프라 실행: `infra/README.md`
3. 서비스 실행:
   - Backend: `backend-spring/README.md`
   - Frontend: `frontend/README.md`
   - AI Server: `ai-server/README.md`

## 시스템별 실행 문서

- 인프라: [`infra/README.md`](infra/README.md)
- Backend(Spring): [`backend-spring/README.md`](backend-spring/README.md)
- Frontend: [`frontend/README.md`](frontend/README.md)
- AI Server(FastAPI): [`ai-server/README.md`](ai-server/README.md)

## 문서 지도

문서의 기준 경로와 역할은 [`docs/README.md`](docs/README.md)에서 먼저 확인합니다.

- 프로젝트 정책/요구/설계: `docs/project/`
- 권한/역할 기준(SSOT): `docs/auth/api-authority-matrix.md`
- DB 초기화/마이그레이션/시드: `docs/db/`

## 권한 기준 (요약)

권한 관련 단일 기준 문서는 다음입니다.

- [`docs/auth/api-authority-matrix.md`](docs/auth/api-authority-matrix.md)

다른 문서에서는 권한 상세를 중복 설명하지 않고 위 문서를 참조합니다.
