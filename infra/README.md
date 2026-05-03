# Infra

로컬 개발용 인프라 실행과 DB 초기화 절차를 정리한 문서입니다.

## 구성 서비스

- MariaDB: `localhost:3307`
- Redis: `localhost:6379`
- MinIO: `localhost:9000`, 콘솔 `localhost:9001`
- ChromaDB: `localhost:8000`

MariaDB는 컨테이너에서 `utf8mb4` / `utf8mb4_unicode_ci` 기본값으로 실행됩니다.

## 시작 전 준비

```powershell
cd infra
Copy-Item .env.example .env
```

실제 비밀번호는 `.env`에만 작성하고 커밋하지 않습니다.

## 신규 클론 기준 DB 초기화 순서

1. 컨테이너 기동

```powershell
cd infra
docker compose --env-file .env up -d
docker compose ps
```

2. 초기 스키마 적용

```powershell
.\scripts\db-reset.ps1 -Force
```

`db-reset.ps1 -Force`는 아래를 한 번에 수행합니다.

- 데이터베이스 재생성
- `mariadb/init/industrial-ai-platform.sql` 적용
- 후속 migration 적용
- seed 데이터 적용
- 상태 점검

이미 컨테이너 볼륨까지 완전히 비우고 새로 시작하려면 아래 순서로 실행합니다.

```powershell
docker compose down
docker compose --env-file .env up -d
.\scripts\db-reset.ps1 -Force
```

주의: 데이터 볼륨 보호를 위해 로컬 재기동 시 `docker compose down -v`는 사용하지 않습니다.

3. 개별 단계만 다시 실행하고 싶을 때

```powershell
.\scripts\db-migrate.ps1
.\scripts\db-seed.ps1
.\scripts\db-status.ps1
```

## 한글 샘플 데이터 주의사항

- 모든 SQL 파일은 UTF-8(무 BOM) 기준으로 관리합니다.
- PowerShell 스크립트는 SQL 파일을 컨테이너로 그대로 복사한 뒤 MariaDB에서 실행합니다.
- MariaDB client 호출은 항상 `--default-character-set=utf8mb4`를 사용합니다.
- 이미 깨진 값이 저장된 DB는 `UPDATE`보다 `db-reset.ps1 -Force`로 재생성하는 방식을 우선합니다.

## 검증 명령

```powershell
.\scripts\db-status.ps1
```

```powershell
docker compose --env-file .env exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -pchange_me_root_password industrial_ai -e "SELECT target_id, target_name, equipment_name, product_name, location_name FROM analysis_target LIMIT 10;"
```

```powershell
docker compose --env-file .env exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -pchange_me_root_password industrial_ai -e "SELECT notification_id, title, message FROM notification LIMIT 10;"
```
