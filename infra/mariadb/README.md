## MariaDB 로컬 초기화/시드

- 공식 테이블명은 `lowercase snake_case` 기준입니다.
- 로컬 DB는 `mariadb/init/industrial-ai-platform.sql`로 수동 생성합니다.
- Spring local 프로필은 `ddl-auto=validate`를 사용하며 Hibernate 자동 생성/수정을 사용하지 않습니다.

```powershell
cd infra
docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -pchange_me_root_password -e "DROP DATABASE IF EXISTS industrial_ai; CREATE DATABASE industrial_ai DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
Get-Content .\mariadb\init\industrial-ai-platform.sql -Raw | docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -pchange_me_root_password industrial_ai
```

샘플 조직 시드(수동):

```powershell
cd infra
Get-Content .\mariadb\seed\seed-sample-organizations.sql -Raw | docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -pchange_me_root_password industrial_ai
```

반영 확인:

```powershell
docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -pchange_me_root_password industrial_ai -e "SHOW FULL TABLES;"
docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -pchange_me_root_password industrial_ai -e "SELECT organization_id, organization_name, status FROM organization WHERE organization_id IN (9001,1001,1002,1003);"
docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -pchange_me_root_password industrial_ai -e "SELECT id, name FROM organization_public ORDER BY id;"
```
