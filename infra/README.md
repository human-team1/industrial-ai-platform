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

## Nginx 게이트웨이

`infra/docker-compose.yml`에는 외부 공개용 `industrial-nginx` 컨테이너가 포함됩니다. 1차 작업 기준으로 Nginx는 Docker 컨테이너에서 실행되고, Frontend/Spring/FastAPI는 호스트에서 실행 중인 기본 포트(`5173`, `8080`, `8001`)를 프록시 대상으로 사용합니다.

### 라우팅 규칙

| 외부 경로 | 내부 대상 | 공개 여부 |
| --- | --- | --- |
| `/` | Frontend `host.docker.internal:5173` | 공개 |
| `/api/` | Spring Boot `host.docker.internal:8080` | 공개 |
| `/ai/` | 차단 | 비공개 |
| `/minio/` | 차단 | 비공개 |
| `/chroma/` | 차단 | 비공개 |

### 실행 순서

1. 인프라 컨테이너 실행

```powershell
cd infra
docker compose --env-file .env up -d
```

2. 호스트에서 애플리케이션 실행

```powershell
cd ..\backend-spring
.\gradlew.bat bootRun --args="--spring.profiles.active=local"
```

```powershell
cd ..\frontend
npm run dev
```

3. 게이트웨이 확인

```powershell
curl http://localhost/
curl http://localhost/api/v1/health
curl -i http://localhost/ai/v1/internal/system-status
curl -i http://localhost/minio/
curl -i http://localhost/chroma/
```

기대 결과:

- `http://localhost/`에서 프론트가 열립니다.
- `http://localhost/api/v1/health`가 Spring으로 프록시됩니다.
- `/ai`, `/minio`, `/chroma`는 모두 `403`입니다.

### Cloudflare Tunnel 중간발표

Cloudflare Tunnel은 Nginx 단일 진입점만 공개합니다.

```powershell
cd infra
docker compose --env-file .env up -d
cloudflared tunnel --url http://localhost:80
```

- Spring, FastAPI, DB, Redis, MinIO, ChromaDB를 각각 터널링하지 않습니다.
- 프론트는 `/api/v1` 상대경로를 사용하므로 trycloudflare URL이 바뀌어도 같은 코드로 동작합니다.

### 집컴 도메인/DDNS 최종발표

최종발표도 동일한 Nginx를 사용하고, 외부 공개는 공유기와 방화벽에서 `80`, 추후 `443`만 허용합니다.

```text
도메인/DDNS
  ↓
공유기 포트포워딩 80/443
  ↓
집컴 Nginx
  ├─ /      → Frontend
  └─ /api   → Spring Boot
```

체크리스트:

1. 집컴 내부 IP 고정
2. 공유기 80/443 포트포워딩
3. Windows 방화벽 80/443 허용
4. Docker Compose 실행
5. Nginx 접속 확인
6. 도메인/DDNS 연결 확인
7. LTE/5G 등 외부망에서 접속 확인

### 외부 공개 원칙

- 공유기 포트포워딩은 `80`, 추후 HTTPS 적용 시 `443`만 허용합니다.
- `8080`, `8001`, `3307`, `6379`, `9000`, `9001`, `8000`, `11434`는 외부에 직접 포워딩하지 않습니다.
- 로컬 개발을 위해 호스트 포트가 열려 있어도, 외부 공개는 반드시 Nginx를 단일 진입점으로 사용합니다.
