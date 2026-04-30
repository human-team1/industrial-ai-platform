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
Get-Content -Raw -Encoding UTF8 .\mariadb\init\industrial-ai-platform.sql |
  docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -p<root_password> <database_name>
```

`<root_password>`와 `<database_name>`은 `infra/.env`의 `MARIADB_ROOT_PASSWORD`, `MARIADB_DATABASE` 값으로 바꿉니다.

## 3. 샘플 조직 시드

가입 페이지의 조직 선택 목록은 `organization_public` view를 사용합니다. 로컬에서 바로 확인하려면 샘플 조직을 넣습니다.

```powershell
cd infra
Get-Content -Raw -Encoding UTF8 .\mariadb\seed\seed-sample-organizations.sql |
  docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -p<root_password> <database_name>
```

## 4. 관리자 승격

회원가입/로그인 후 `PENDING` 상태인 계정을 로컬 검증용 관리자 계정으로 승격합니다.

```powershell
docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -p<root_password> <database_name> -e "UPDATE users SET status='ACTIVE', role='ADMIN', deleted_at=NULL, updated_at=CURRENT_TIMESTAMP WHERE email='본인@gmail.com';"
```

확인:

```powershell
docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -p<root_password> <database_name> -e "SELECT user_id, organization_id, email, name, status, role, deleted_at FROM users WHERE email='본인@gmail.com';"
```

## 5. 확인

```powershell
docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -p<root_password> <database_name> -e "SHOW FULL TABLES;"
docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -p<root_password> <database_name> -e "SELECT organization_id, organization_name, status FROM organization ORDER BY organization_id;"
docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -p<root_password> <database_name> -e "SELECT id, name FROM organization_public ORDER BY id;"
```

## 6. 최신 스키마 확인

```powershell
docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -p<root_password> <database_name> -e "SHOW COLUMNS FROM inspection_input LIKE 'source_type';"
docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -p<root_password> <database_name> -e "SHOW COLUMNS FROM chat_message; SHOW COLUMNS FROM chat_source;"
```

## 7. 재초기화

로컬 DB를 완전히 지워도 되는 경우에만 실행합니다.

```powershell
cd infra
docker compose down -v
docker compose --env-file .env up -d
```

그 뒤 스키마와 seed를 다시 적용합니다.

## 8. 현재 스키마 주의사항

- 가입 신청은 `signup_request`에 저장됩니다.
- 로컬 관리자 승격은 `users.status='ACTIVE'`, `users.role='ADMIN'` 기준입니다.
- 문서 메타데이터는 `document`, `document_tag`, `document_version`에 저장됩니다.
- 문서 인덱싱 상태는 `document_version.indexing_status`에 저장됩니다.
- 청크/벡터 참조는 `chunk`, `vector_index`에 저장됩니다.
- 챗봇 대화/메시지/출처는 `chat_conversation`, `chat_message`, `chat_source`에 저장됩니다.
- 파일 실물은 MinIO에 있고, MariaDB `file` 테이블은 참조/메타데이터만 보관합니다.
