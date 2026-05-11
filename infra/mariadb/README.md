# MariaDB 로컬 초기화/시드

## 기준

- DBMS: MariaDB 10.6.21
- Database: `industrial_ai`
- Container: `industrial-mariadb`
- Port: `localhost:3307`
- 공식 테이블명: lowercase snake_case
- Spring local profile: `ddl-auto=validate`

## 폴더 구조

```
mariadb/
├── init/         # 베이스라인 스키마 (전체 테이블 정의, 최신 상태 유지)
├── migrations/   # 스키마 변경 이력 (schema_migration 테이블로 추적, 幂等성 보장)
├── seed/         # 샘플 데이터 (ON DUPLICATE KEY UPDATE, 반복 적용 가능)
└── demo-data/    # 특정 환경 전용 데이터 (MinIO 파일 참조 포함, 해당 환경에서만 실행)
```

### `demo-data/` 주의사항

이 폴더의 SQL은 특정 MinIO 파일(`file_id`, `model_id` 등)을 하드코딩으로 참조합니다.
**신규 로컬 환경에서는 자동 실행되지 않으며, 해당 MinIO 오브젝트가 존재하는 환경에서만 수동 적용합니다.**

---

## 신규 팀원 환경 설정 (표준 절차)

> **사전 조건**: `infra/.env` 준비 완료, Docker 실행 중

```powershell
# 1. MariaDB 컨테이너 실행
cd infra
docker compose --env-file .env up -d mariadb

# 2. 전체 초기화 (스키마 + 마이그레이션 + 샘플 데이터 한 번에)
.\scripts\db-reset.ps1 -Force

# 3. 구글 로그인 후 관리자 승격
docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -p<MARIADB_ROOT_PASSWORD> <MARIADB_DATABASE> -e "UPDATE users SET status='ACTIVE', role='ROLE_SITE_ADMIN', deleted_at=NULL, updated_at=CURRENT_TIMESTAMP WHERE email='본인@gmail.com';"
```

`<MARIADB_ROOT_PASSWORD>`, `<MARIADB_DATABASE>` 는 `infra/.env` 값으로 교체합니다.

---

## 스크립트 모드 선택

| 스크립트 | 동작 | 파괴적 |
|---|---|---|
| `db-reset -Force` | DB 초기화 + init + migration + seed | **예** |
| `db-migrate` | `migrations/` SQL을 미적용분만 순차 적용 | 아니오 |
| `db-seed` | `seed/` 샘플 데이터 재삽입 | 아니오 |
| `db-status` | 테이블/migration/샘플 건수/role 분포 조회 | 아니오 |

PowerShell:

```powershell
cd infra
.\scripts\db-status.ps1
.\scripts\db-migrate.ps1
.\scripts\db-seed.ps1
.\scripts\db-reset.ps1 -Force
```

운영 compose를 대상으로 실행할 때는 `-ComposeFile`, `-EnvFile` 인자를 사용합니다.

```powershell
cd infra
.\scripts\db-migrate.ps1 -ComposeFile docker-compose.prod.yml -EnvFile .env.prod
.\scripts\db-status.ps1 -ComposeFile docker-compose.prod.yml -EnvFile .env.prod
```

Bash:

```bash
cd infra
./scripts/db-status.sh
./scripts/db-migrate.sh
./scripts/db-seed.sh
./scripts/db-reset.sh --force
```

---

## 마이그레이션 이력 관리 방식

- `schema_migration` 테이블로 적용 여부를 추적합니다.
- 이미 적용된 파일은 파일명 기준으로 건너뜁니다.
- `migrations/` 의 모든 SQL은 `IF NOT EXISTS` / `ON DUPLICATE KEY` 등 **幂等성 패턴**을 준수합니다.
- 신규 스키마 변경은 반드시 `migrations/` 에 버전 파일(`V날짜_순번__설명.sql`)로 추가합니다.
- `init/industrial-ai-platform.sql` 은 **현재 시점 전체 스키마 베이스라인**으로 유지합니다.
  마이그레이션을 추가할 때마다 init 파일도 동기화합니다.

---

## 관리자 승격

구글 로그인 후 `PENDING` 상태인 계정을 관리자로 승격합니다.

```powershell
docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -p<MARIADB_ROOT_PASSWORD> <MARIADB_DATABASE> -e "UPDATE users SET status='ACTIVE', role='ROLE_SITE_ADMIN', deleted_at=NULL, updated_at=CURRENT_TIMESTAMP WHERE email='본인@gmail.com';"
```

확인:

```powershell
docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -p<MARIADB_ROOT_PASSWORD> <MARIADB_DATABASE> -e "SELECT user_id, organization_id, email, name, status, role, deleted_at FROM users WHERE email='본인@gmail.com';"
```

---

## DB 상태 확인

```powershell
docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -p<MARIADB_ROOT_PASSWORD> <MARIADB_DATABASE> -e "SHOW FULL TABLES;"
docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -p<MARIADB_ROOT_PASSWORD> <MARIADB_DATABASE> -e "SELECT organization_id, organization_name, status FROM organization ORDER BY organization_id;"
docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -p<MARIADB_ROOT_PASSWORD> <MARIADB_DATABASE> -e "SELECT id, name FROM organization_public ORDER BY id;"
```

---

## 재초기화

로컬 DB를 완전히 지워도 되는 경우에만 실행합니다.

```powershell
cd infra
docker compose down -v
docker compose --env-file .env up -d mariadb
.\scripts\db-reset.ps1 -Force
```

---

## 현재 스키마 주의사항

- 가입 신청은 `signup_request`에 저장됩니다.
- 로컬 관리자 승격은 `users.status='ACTIVE'`, `users.role='ROLE_SITE_ADMIN'` 기준입니다.
- 문서 메타데이터는 `document`, `document_tag`, `document_version`에 저장됩니다.
- 문서 인덱싱 상태는 `document_version.indexing_status`에 저장됩니다.
- 청크/벡터 참조는 `chunk`, `vector_index`에 저장됩니다.
- 챗봇 대화/메시지/출처는 `chat_conversation`, `chat_message`, `chat_source`에 저장됩니다.
- 파일 실물은 MinIO에 있고, MariaDB `file` 테이블은 참조/메타데이터만 보관합니다.
