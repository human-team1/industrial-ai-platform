# Project Docs Index

프로젝트 요구/정책/화면/API/ERD 기준 문서 모음입니다.

## 읽는 순서(권장)

1. [`프로젝트개요.md`](프로젝트개요.md)
2. [`기능정의.md`](기능정의.md)
3. [`정책정의.md`](정책정의.md)
4. [`페이지목록.md`](페이지목록.md)
5. [`API.md`](API.md)
6. [`ERD.md`](ERD.md)
7. [`디렉터리구조.md`](디렉터리구조.md)
8. [`컨벤션.md`](컨벤션.md)
9. [`개발환경.md`](개발환경.md)

## 참조 원칙

- 권한/Role 상세 규칙은 여기서 중복 관리하지 않고
  [`../auth/api-authority-matrix.md`](../auth/api-authority-matrix.md)를 참조합니다.
- DB 작업(초기화/마이그레이션/시드)은 여기서 중복 관리하지 않고
  [`../db/README.md`](../db/README.md)를 참조합니다.

## 최신 반영

- 대시보드 API는 `API.md`의 `GET /dashboard/overview`를 기준으로 한다.
- `ANALYSIS_TARGET.location_name`은 `ERD.md`, init SQL, migration, seed에 반영되어 있다.
- 운영 관리 API는 `API.md`의 Operation/Admin 섹션을 기준으로 하며 `/api/v1/admin/**`는 SITE_ADMIN 전용이다.
- 운영 관리 스키마 확장은 `V20260502_002__extend_operation_admin_schema.sql`, 시연 seed는 `seed-sample-operation-admin.sql`을 적용한다.
