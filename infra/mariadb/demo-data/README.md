# demo-data

특정 환경 전용 데이터 스크립트 모음입니다.

## 주의사항

이 폴더의 SQL은 **신규 로컬 환경에서 자동 실행되지 않습니다.**
`db-reset` / `db-migrate` / `db-seed` 스크립트 대상에서 제외됩니다.

### 실행 전 조건

- 참조 중인 `file_id`, `model_id`, `organization_id` 가 해당 환경 DB에 실제로 존재해야 합니다.
- 참조 파일의 MinIO 오브젝트(`bucket/object_key`)가 존재해야 합니다.

### 수동 실행 방법

```powershell
cd infra
Get-Content -Raw -Encoding UTF8 .\mariadb\demo-data\<파일명>.sql |
  docker compose exec -T mariadb mariadb --default-character-set=utf8mb4 -uroot -p<MARIADB_ROOT_PASSWORD> <MARIADB_DATABASE>
```

## 파일 목록

| 파일 | 설명 | 의존 ID |
|---|---|---|
| `V20260508_002__add_texture_model_versions_with_thresholds.sql` | TEXTURE 모델 버전/배포 데이터 삽입 | `file_id` 92987~92992, `model_id` 93004, `organization_id` 9001 |
