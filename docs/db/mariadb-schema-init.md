# MariaDB 스키마 초기화 및 ERD 확인 가이드

로컬 개발환경에서 MariaDB 10.6.21을 Docker Compose로 실행하고, `infra/mariadb/init/industrial-ai-platform.sql` 기준으로 `industrial_ai` 스키마를 생성한 뒤 DBeaver에서 ERD를 확인하는 절차입니다.

## 1. 기준 정보

| 항목 | 값 |
| --- | --- |
| DBMS | MariaDB 10.6.21 |
| Database | `industrial_ai` |
| Host | `localhost` |
| Port | `3307` |
| User | `industrial_user` |
| Docker container | `industrial-mariadb` |
| Docker volume | `infra_mariadb_data` |
| Init SQL | `infra/mariadb/init/industrial-ai-platform.sql` |

비밀번호는 `infra/.env`의 `MARIADB_PASSWORD` 값을 사용합니다. 실제 비밀번호는 문서나 Git에 기록하지 않습니다.

## 2. 사전 준비

프로젝트 루트에서 `infra/.env` 파일이 없다면 예시 파일을 복사합니다.

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

## 3. MariaDB 실행

프로젝트 루트에서 `infra` 폴더로 이동한 뒤 MariaDB만 실행합니다.

```powershell
cd infra
docker compose --env-file .env up -d mariadb
```

컨테이너 상태를 확인합니다.

```powershell
docker ps
```

`industrial-mariadb` 컨테이너의 `STATUS`가 `Up`이면 정상입니다.

로그가 필요하면 다음 명령을 사용합니다.

```powershell
docker logs industrial-mariadb
```

## 4. industrial_ai DB 접속

컨테이너 안의 MariaDB 클라이언트로 접속합니다.

```powershell
docker exec -it industrial-mariadb mariadb -uindustrial_user -p industrial_ai
```

비밀번호 입력 프롬프트가 나오면 `infra/.env`의 `MARIADB_PASSWORD` 값을 입력합니다.

접속 후 현재 DB를 확인합니다.

```sql
SELECT DATABASE();
```

결과가 `industrial_ai`이면 정상입니다.

종료는 다음 명령을 입력합니다.

```sql
exit
```

## 5. Init SQL 수동 실행

현재 `infra/docker-compose.yml`은 MariaDB 데이터 디렉터리 볼륨만 연결합니다. 따라서 `infra/mariadb/init/industrial-ai-platform.sql` 파일은 컨테이너 최초 실행 시 자동 실행되지 않습니다. 스키마를 생성하려면 아래 방식으로 SQL을 수동 실행합니다.

`infra` 폴더에서 실행합니다.

```powershell
Get-Content .\mariadb\init\industrial-ai-platform.sql -Raw | docker exec -i industrial-mariadb sh -c 'mariadb -uindustrial_user -p"$MARIADB_PASSWORD" industrial_ai'
```

또는 MariaDB에 접속한 뒤 SQL 파일 내용을 복사해서 실행할 수 있습니다.

```powershell
docker exec -it industrial-mariadb mariadb -uindustrial_user -p industrial_ai
```

## 6. 테이블 생성 확인

MariaDB에 접속한 뒤 테이블 목록을 확인합니다.

```sql
SHOW TABLES;
```

테이블 개수까지 확인하려면 다음 쿼리를 실행합니다.

```sql
SELECT COUNT(*) AS table_count
FROM information_schema.tables
WHERE table_schema = 'industrial_ai';
```

특정 주요 테이블이 있는지 확인할 수도 있습니다.

```sql
SHOW TABLES LIKE 'USERS';
SHOW TABLES LIKE 'INSPECTION_RUN';
SHOW TABLES LIKE 'DOCUMENT';
```

## 7. 스키마 변경 후 재초기화

MariaDB 공식 이미지는 `/var/lib/mysql` 데이터 디렉터리가 이미 초기화되어 있으면 최초 생성용 SQL을 다시 실행하지 않습니다. 현재 프로젝트에서는 SQL을 수동 실행하더라도 기존 테이블이 남아 있으면 `CREATE TABLE` 충돌이 날 수 있습니다.

스키마를 처음 상태부터 다시 만들려면 MariaDB 컨테이너를 내리고 MariaDB 볼륨을 삭제한 뒤 다시 실행합니다.

```powershell
cd infra
docker compose down
docker volume rm infra_mariadb_data
docker compose --env-file .env up -d mariadb
```

그 다음 Init SQL을 다시 실행합니다.

```powershell
Get-Content .\mariadb\init\industrial-ai-platform.sql -Raw | docker exec -i industrial-mariadb sh -c 'mariadb -uindustrial_user -p"$MARIADB_PASSWORD" industrial_ai'
```

주의:
- `docker volume rm infra_mariadb_data`는 로컬 MariaDB 데이터를 모두 삭제합니다.
- Redis, MinIO, Chroma 데이터까지 함께 지우려는 목적이 아니라면 `docker compose down -v` 대신 MariaDB 볼륨만 삭제합니다.
- 중요한 로컬 데이터가 있으면 삭제 전에 dump를 먼저 생성합니다.

## 8. DBeaver 연결

DBeaver에서 새 연결을 생성합니다.

1. `Database` > `New Database Connection`을 선택합니다.
2. `MariaDB`를 선택합니다.
3. 연결 정보를 입력합니다.

| 항목 | 값 |
| --- | --- |
| Server Host | `localhost` |
| Port | `3307` |
| Database | `industrial_ai` |
| Username | `industrial_user` |
| Password | `infra/.env`의 `MARIADB_PASSWORD` |

4. `Test Connection`을 클릭합니다.
5. 드라이버 다운로드 안내가 나오면 다운로드합니다.
6. 연결 성공을 확인한 뒤 `Finish`를 클릭합니다.

연결 후 좌측 Database Navigator에서 다음 경로를 펼칩니다.

```text
industrial_ai
  Databases
    industrial_ai
      Tables
```

테이블 목록이 보이면 연결과 스키마 생성이 정상입니다.

## 9. DBeaver ERD 확인

DBeaver에서 ERD를 확인하는 방법입니다.

1. Database Navigator에서 `industrial_ai` 데이터베이스 또는 `Tables`를 선택합니다.
2. 우클릭 후 `View Diagram`을 선택합니다.
3. 다이어그램이 열리면 테이블과 FK 관계를 확인합니다.
4. 일부 테이블만 보고 싶으면 원하는 테이블들을 선택한 뒤 우클릭해서 `View Diagram`을 실행합니다.

FK 관계가 보이지 않으면 다음을 확인합니다.
- Init SQL 실행이 완료되었는지 확인합니다.
- `SHOW TABLES;`로 테이블이 생성되었는지 확인합니다.
- DBeaver에서 연결을 우클릭한 뒤 `Refresh`를 실행합니다.
- 테이블이 MyISAM이 아니라 InnoDB로 생성되었는지 확인합니다.

```sql
SELECT table_name, engine
FROM information_schema.tables
WHERE table_schema = 'industrial_ai'
ORDER BY table_name;
```

## 10. 자주 발생하는 문제

### docker compose 명령이 실패함

Docker Desktop이 실행 중인지 확인합니다.

```powershell
docker --version
docker compose version
```

`Cannot connect to the Docker daemon` 메시지가 나오면 Docker Desktop을 실행하고 초기화가 끝난 뒤 다시 시도합니다.

### industrial-mariadb 컨테이너가 보이지 않음

`infra` 폴더에서 MariaDB 서비스를 다시 실행합니다.

```powershell
cd infra
docker compose --env-file .env up -d mariadb
docker ps
```

### 3307 포트 충돌

이미 다른 프로세스가 `3307` 포트를 사용 중인지 확인합니다.

```powershell
netstat -ano | findstr :3307
```

충돌 프로세스를 종료하거나 `infra/docker-compose.yml`의 포트 매핑을 조정합니다.

### Access denied 오류

사용자, 비밀번호, DB 이름을 다시 확인합니다.

```text
User: industrial_user
Database: industrial_ai
Password: infra/.env의 MARIADB_PASSWORD
```

이미 생성된 MariaDB 볼륨에는 최초 실행 당시의 계정 정보가 남아 있습니다. `.env`를 바꿨는데 접속 정보가 바뀌지 않았다면 `infra_mariadb_data` 볼륨을 재초기화해야 합니다.

### SHOW TABLES 결과가 비어 있음

`industrial_ai` DB에는 접속했지만 Init SQL이 아직 실행되지 않은 상태입니다. `infra` 폴더에서 SQL을 수동 실행합니다.

```powershell
Get-Content .\mariadb\init\industrial-ai-platform.sql -Raw | docker exec -i industrial-mariadb sh -c 'mariadb -uindustrial_user -p"$MARIADB_PASSWORD" industrial_ai'
```

### Table already exists 오류

기존 스키마가 남아 있는 상태에서 Init SQL을 다시 실행한 경우입니다. 로컬 데이터를 삭제해도 되는 상황이면 MariaDB 볼륨을 재초기화한 뒤 SQL을 다시 실행합니다.

```powershell
cd infra
docker compose down
docker volume rm infra_mariadb_data
docker compose --env-file .env up -d mariadb
```

### DBeaver 연결은 되지만 테이블이 안 보임

DBeaver에서 다음을 확인합니다.
- 연결 정보의 Database가 `industrial_ai`인지 확인합니다.
- Database Navigator에서 연결을 우클릭하고 `Refresh`를 실행합니다.
- `SHOW TABLES;`를 직접 실행해 실제 테이블 생성 여부를 확인합니다.

## 11. 검증 체크리스트

- [ ] `docker compose --env-file .env up -d mariadb` 실행 가능
- [ ] `docker ps`에서 `industrial-mariadb` Up 상태 확인
- [ ] `docker exec -it industrial-mariadb mariadb -uindustrial_user -p industrial_ai` 접속 가능
- [ ] `SHOW TABLES;` 실행 시 테이블 목록 확인
- [ ] DBeaver `localhost:3307` 연결 가능
- [ ] DBeaver ERD 확인 가능
