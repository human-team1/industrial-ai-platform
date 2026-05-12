# Project Docs Index

프로젝트 요구사항, 정책, 화면, API, ERD, 실행 기준 문서 모음입니다. 루트 README는 빠른 진입점이고, 상세 기준은 이 디렉터리의 문서를 따릅니다.

## 권장 읽기 순서

1. [프로젝트개요.md](프로젝트개요.md)
2. [기능정의.md](기능정의.md)
3. [정책정의.md](정책정의.md)
4. [페이지목록.md](페이지목록.md)
5. [API.md](API.md)
6. [ERD.md](ERD.md)
7. [디렉터리구조.md](디렉터리구조.md)
8. [개발환경.md](개발환경.md)
9. [컨벤션.md](컨벤션.md)

## 실행/검증 문서

- [테스트실행가이드.md](테스트실행가이드.md): 로컬 개발 실행, fullstack compose, smoke checklist
- [docker-compose-prod.md](docker-compose-prod.md): 운영용 Docker Compose 구성
- [포트정리.md](포트정리.md): 로컬/컨테이너 포트 기준

## 참조 원칙

- 권한/Role 상세는 [권한 매트릭스](../auth/api-authority-matrix.md)를 기준으로 합니다.
- DB 초기화/마이그레이션/시드는 [DB 문서](../db/README.md)를 기준으로 합니다.
- 상세 API는 중복 작성하지 않고 [API.md](API.md)에 모읍니다.
