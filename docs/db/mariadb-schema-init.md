# MariaDB 스키마 초기화 및 ERD 확인 가이드

로컬 개발환경에서 MariaDB 10.6.21을 Docker Compose로 실행하고, `infra/mariadb/init/industrial-ai-platform.sql` 기준으로 `industrial_ai` 스키마를 생성한 뒤 seed 데이터와 관리자 권한을 준비하는 절차입니다.

## 1. 기준 정보

| 항목 | 값 |
| --- | --- |
| DBMS | MariaDB 10.6.21 |
| Database | `industrial_ai` |
| Host | `localhost` |
| Port | `3307` |
| Docker service | `mariadb` |
| Docker container | `industrial-mariadb` |
| Docker volume | `infra_mariadb_data` |
| Init SQL | `infra/mariadb/init/industrial-ai-platform.sql` |
| Seed SQL | `infra/mariadb/seed/seed-sample-organizations.sql` |

비밀번호는 `infra/.env`의 값을 사용합니다. 실제 비밀번호는 문서나 Git에 기록하지 않습니다.

## 2. 사전 준비

```powershell
Copy-Item infra\.env.example infra\.env
```

`infra/.env`에서 MariaDB 값을 확인합니다.

```text
MARIADB_ROOT_PASSWORD=...
MARIADB_DATABASE=industrial_ai
MARIADB_USER=industrial_user
MARIADB_PASSWORD=...
```

## 3. 로컬 DB 전체 재초기화

로컬 DB/스토리지 데이터를 모두 지워도 되는 경우에만 실행합니다.

```powershell
cd infra
docker compose down -v
docker compose --env-file .env up -d
```

주의:

- `docker compose down -v`는 MariaDB, Redis, MinIO, ChromaDB의 로컬 데이터를 모두 삭제합니다.
- 기존 데이터를 유지해야 한다면 `docs/db/mariadb-schema-delta-from-develop.md`의 ALTER SQL을 검토합니다.

## 4. Init SQL 수동 실행

현재 `infra/docker-compose.yml`은 init SQL을 자동 마운트하지 않습니다. `SHOW TABLES;` 결과가 비어 있으면 아래 명령으로 스키마를 수동 적용합니다.

```powershell
cd infra
Get-Content -Raw -Encoding UTF8 .\mariadb\init\industrial-ai-platform.sql |
  docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -p<root_password> <database_name>
```

`<root_password>`와 `<database_name>`은 `infra/.env` 값으로 바꿉니다.

## 5. 테이블 생성 확인

```powershell
docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -p<root_password> <database_name> -e "SHOW TABLES;"
```

주요 테이블:

- `users`
- `organization`
- `inspection_input`
- `inspection_run`
- `chat_conversation`
- `chat_message`
- `chat_source`
- `document`
- `chunk`
- `vector_index`

## 6. Seed 데이터 적용

가입 화면의 공개 조직 목록은 `organization_public` view를 사용합니다. 로컬에서 가입 흐름을 확인하려면 샘플 조직을 넣습니다.

```powershell
cd infra
Get-Content -Raw -Encoding UTF8 .\mariadb\seed\seed-sample-organizations.sql |
  docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -p<root_password> <database_name>
```

확인:

```powershell
docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -p<root_password> <database_name> -e "SELECT organization_id, organization_name, status FROM organization ORDER BY organization_id;"

docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -p<root_password> <database_name> -e "SELECT id, name FROM organization_public ORDER BY id;"
```

## 7. 로컬 관리자 계정 승격

회원가입/로그인 후 계정이 `PENDING` 상태라면 로컬 검증을 위해 본인 계정을 관리자 계정으로 승격할 수 있습니다.

```powershell
docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -p<root_password> <database_name> -e "UPDATE users SET status='ACTIVE', role='ROLE_SITE_ADMIN', deleted_at=NULL, updated_at=CURRENT_TIMESTAMP WHERE email='본인@gmail.com';"
```

확인:

```powershell
docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -p<root_password> <database_name> -e "SELECT user_id, organization_id, email, name, status, role, deleted_at FROM users WHERE email='본인@gmail.com';"
```

주의:

- 실제 Gmail 주소는 문서에 기록하지 않습니다.
- 프로젝트의 현재 로컬 승격 값은 `role='ROLE_SITE_ADMIN'`, `status='ACTIVE'` 기준입니다.
- `ADMIN`처럼 enum에 없는 값을 신규 데이터에 넣지 않습니다. (`UserRole`: `ROLE_SITE_ADMIN`, `ROLE_COMPANY_ADMIN`, `ROLE_COMPANY_WORKER`)

## 8. 최신 스키마 검증 쿼리

Spring local profile은 JPA schema validation을 수행합니다. 아래 컬럼이 없으면 `bootRun` 중 validation 오류가 발생할 수 있습니다.

### 검사 도메인

```powershell
docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -p<root_password> <database_name> -e "SHOW COLUMNS FROM inspection_input LIKE 'source_type';"

docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -p<root_password> <database_name> -e "SHOW COLUMNS FROM inspection_run LIKE 'payload_fingerprint';"

docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -p<root_password> <database_name> -e "SHOW COLUMNS FROM user_threshold_history LIKE 'version';"
```

### 챗봇 도메인

```powershell
docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -p<root_password> <database_name> -e "SHOW COLUMNS FROM chat_message; SHOW COLUMNS FROM chat_source;"
```

확인 대상:

`chat_message`

- `message_status`
- `answer_status`
- `error_code`
- `model_name`
- `updated_at`

`chat_source`

- `document_id`
- `document_title`
- `document_type`
- `page_no`
- `section`
- `score`
- `created_at`

## 9. DBeaver 연결

| 항목 | 값 |
| --- | --- |
| Server Host | `localhost` |
| Port | `3307` |
| Database | `industrial_ai` |
| Username | `industrial_user` |
| Password | `infra/.env`의 `MARIADB_PASSWORD` |

테이블 목록이 보이지 않으면 연결을 Refresh하고 `SHOW TABLES;`를 직접 실행합니다.

## 10. 자주 발생하는 문제

### Spring 실행 시 `missing column [source_type] in table [inspection_input]`

로컬 DB가 최신 init SQL과 맞지 않는 상태입니다. 로컬 데이터를 지워도 되면 아래 순서로 재초기화합니다.

```powershell
cd infra
docker compose down -v
docker compose --env-file .env up -d

Get-Content -Raw -Encoding UTF8 .\mariadb\init\industrial-ai-platform.sql |
  docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -p<root_password> <database_name>
```

### `SHOW TABLES;` 결과가 비어 있음

init SQL이 아직 적용되지 않은 상태입니다. `infra` 폴더에서 Init SQL을 수동 실행합니다.

### 가입 페이지 조직 목록이 비어 있음

샘플 조직 seed를 적용하지 않은 상태입니다. `infra/mariadb/seed/seed-sample-organizations.sql`을 실행합니다.

### Table already exists 오류

기존 테이블이 남아 있는 상태에서 init SQL을 다시 실행한 경우입니다. 로컬 데이터를 삭제해도 되면 `docker compose down -v` 후 다시 진행합니다.

### Docker daemon 연결 실패

Docker Desktop이 실행 중인지 확인합니다.

```powershell
docker --version
docker compose version
docker compose ps
```

## 11. 검증 체크리스트

- [ ] `docker compose --env-file .env up -d` 실행 가능
- [ ] `SHOW TABLES;` 실행 시 테이블 목록 확인
- [ ] `inspection_input.source_type` 확인
- [ ] `chat_message.message_status`, `chat_source.document_title` 확인
- [ ] 샘플 조직 조회 가능
- [ ] 본인 계정 `ACTIVE` + `ROLE_SITE_ADMIN` 승격 가능
- [ ] Spring `bootRun` 시 JPA schema validation 통과
- [ ] DBeaver `localhost:3307` 연결 가능
