# Infra

industrial-ai-platform.sql — USER_THRESHOLD_HISTORY.version INT 컬럼 추가 (init 일관성)

로컬 개발 인프라 전용 Docker Compose입니다. 전체 개발환경 설치 절차는 루트 [README.md](../README.md)를 참고하세요.

## 서비스

| 서비스 | 포트 | 역할 |
| --- | --- | --- |
| MariaDB 10.6.21 | `3307:3306` | 서비스 데이터, 메타데이터 |
| Redis 7.x | `6379:6379` | 캐시, 세션, 작업 상태 |
| MinIO | `9000:9000`, `9001:9001` | 원본 파일, 산출물 저장 |
| Chroma | `8000:8000` | 벡터 인덱스, 유사도 검색 |

## 실행

PowerShell:

```powershell
Copy-Item .env.example .env
docker compose --env-file .env up -d
docker ps
```

## 중지

```powershell
docker compose down
```

볼륨까지 삭제해야 할 때만 사용:

```powershell
docker compose down -v
```

Docker Desktop이 실행 중이어야 합니다. 실제 비밀값은 `.env`에만 작성하고 커밋하지 않습니다.
