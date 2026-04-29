# MariaDB 로컬 초기화/시드

로컬 MariaDB는 Docker Compose로 실행하고, 스키마는 `mariadb/init/industrial-ai-platform.sql`을 수동 적용합니다.

## 기준

- DBMS: MariaDB 10.6.21
- Database: `industrial_ai`
- Container: `industrial-mariadb`
- Port: `localhost:3307`
- 공식 테이블명: lowercase snake_case
- Spring local profile: `ddl-auto=validate`

## 1. MariaDB 실행

```powershell
cd infra
docker compose --env-file .env up -d mariadb
docker ps
```

## 2. 스키마 적용

```powershell
cd infra
Get-Content .\mariadb\init\industrial-ai-platform.sql -Raw |
  docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -pchange_me_root_password industrial_ai
```

`change_me_root_password`는 `infra/.env`의 `MARIADB_ROOT_PASSWORD` 값으로 바꿉니다.

## 3. 샘플 조직 시드

가입 페이지의 조직 선택 목록은 `organization_public` view를 사용합니다. 로컬에서 바로 확인하려면 샘플 조직을 넣습니다.

```powershell
cd infra
Get-Content .\mariadb\seed\seed-sample-organizations.sql -Raw |
  docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -pchange_me_root_password industrial_ai
```

## 4. 확인

```powershell
docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -pchange_me_root_password industrial_ai -e "SHOW FULL TABLES;"
docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -pchange_me_root_password industrial_ai -e "SELECT organization_id, organization_name, status FROM organization ORDER BY organization_id;"
docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -pchange_me_root_password industrial_ai -e "SELECT id, name FROM organization_public ORDER BY id;"
```

## 5. 재초기화

로컬 DB를 완전히 지워도 되는 경우에만 실행합니다.

```powershell
cd infra
docker compose down
docker volume rm infra_mariadb_data
docker compose --env-file .env up -d mariadb
```

그 뒤 스키마와 seed를 다시 적용합니다.

## 6. 현재 스키마 주의사항

- `users.role` 기본값은 `ROLE_COMPANY_WORKER`입니다.
- 가입 신청은 `signup_request`에 저장됩니다.
- 문서 메타데이터는 `document`, `document_tag`, `document_version`에 저장됩니다.
- 문서 인덱싱 상태는 `document_version.indexing_status`에 저장됩니다.
- 청크/벡터 참조는 `chunk`, `vector_index`에 저장됩니다.
- 파일 실물은 MinIO에 있고, MariaDB `file` 테이블은 참조/메타데이터만 보관합니다.
