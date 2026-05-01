# Infra

로컬 개발 인프라 Docker Compose입니다. 전체 실행 순서는 루트 [README.md](../README.md)를 기준으로 합니다.

## Recent Schema Changes

- `industrial-ai-platform.sql` — `user_threshold_history.version INT` 컬럼 추가 (init 일관성)
- `inspection_run`의 단독 `idempotency_key UNIQUE`를 제거하고 `(organization_id, user_id, idempotency_key)` 복합 UNIQUE `uk_inspection_run_org_user_idempotency`로 변경. 사용자/조직 간 Idempotency-Key 충돌 방지
- `inspection_run.payload_fingerprint VARCHAR(64)` 컬럼 추가
- `inspection_input.source_type VARCHAR(20) NOT NULL` 컬럼 추가 (검사 입력 출처 구분: 파일/카메라/스트림)
- `inspection_result.threshold_version` 타입 `VARCHAR(50)` → `INT` 정정 (`user_threshold_history.version INT` 참조 정수 버전과 정합)

## 서비스

| 서비스 | 이미지 | 포트 | 역할 |
| --- | --- | --- | --- |
| MariaDB | `mariadb:10.6.21` | `3307:3306` | 서비스 메타데이터, 이력, 사용자/권한, 문서 메타데이터 |
| Redis | `redis:7` | `6379:6379` | 캐시, 세션, 작업 상태 |
| MinIO | `minio/minio:latest` | `9000`, `9001` | 원본 파일, 시각화 산출물, 보고서, 모델 파일 |
| ChromaDB | `chromadb/chroma:0.5.15` | `8000:8000` | 문서 벡터 인덱스 |

## 환경 변수

```powershell
Copy-Item .env.example .env
```

실제 비밀번호는 `.env`에만 작성하고 커밋하지 않습니다.

## 실행

```powershell
docker compose --env-file .env up -d
docker ps
```

특정 서비스만 실행:

```powershell
docker compose --env-file .env up -d mariadb
```

## 중지

```powershell
docker compose down
```

볼륨까지 삭제:

```powershell
docker compose down -v
```

주의: `down -v`는 MariaDB, Redis, MinIO, ChromaDB의 로컬 데이터를 모두 삭제합니다.

## 헬스 확인

```powershell
docker compose ps
docker logs industrial-mariadb
docker logs industrial-redis
docker logs industrial-minio
docker logs industrial-chroma
```

MinIO 콘솔:

- URL: `http://localhost:9001`
- ID: `MINIO_ROOT_USER`
- Password: `MINIO_ROOT_PASSWORD`

## MariaDB 초기화

Compose는 init SQL을 자동 마운트하지 않습니다. 수동 실행 또는 아래 스크립트를 사용합니다.

```powershell
.\scripts\db-migrate.ps1
.\scripts\db-seed.ps1
```

샘플 조직:

```powershell
.\scripts\db-status.ps1
```

전체 초기화가 필요하면 아래 명령을 사용합니다.

```powershell
.\scripts\db-reset.ps1 -Force
```

자세한 절차는 [mariadb/README.md](mariadb/README.md)와 [docs/db/mariadb-schema-init.md](../docs/db/mariadb-schema-init.md)를 참고하세요.
