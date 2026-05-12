param(
    [string]$ComposeFile = "docker-compose.yml",
    [string]$EnvFile = ".env"
)

$ErrorActionPreference = "Stop"
$infraRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $infraRoot
$script:DbComposeFile = $ComposeFile
$script:DbEnvFile = $EnvFile
. (Join-Path $PSScriptRoot "db-common.ps1")

Assert-MariaDbContainerRunning

Write-Host "== DB 접속 확인 =="
Invoke-MariaDbSql -Sql "SELECT NOW() AS server_time;"

Write-Host "`n== 주요 테이블 존재 여부 =="
Invoke-MariaDbSql -Sql @"
SELECT table_name
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN ('organization','users','document','document_version','inspection_run','inspection_result','chat_conversation','chat_message','schema_migration')
ORDER BY table_name;
"@

Write-Host "`n== migration 적용 목록 =="
try {
    Invoke-MariaDbSql -Sql "SELECT filename, applied_at FROM schema_migration ORDER BY applied_at, filename;"
} catch {
    Write-Host "schema_migration 테이블이 아직 없습니다."
}

Write-Host "`n== 샘플 데이터 건수 =="
Invoke-MariaDbSql -Sql @"
SELECT
  (SELECT COUNT(*) FROM organization) AS organizations,
  (SELECT COUNT(*) FROM users) AS users,
  (SELECT COUNT(*) FROM document) AS documents,
  (SELECT COUNT(*) FROM inspection_result) AS results,
  (SELECT COUNT(*) FROM chat_conversation) AS chat_conversations;
"@

Write-Host "`n== role 분포 =="
Invoke-MariaDbSql -Sql "SELECT role, COUNT(*) AS cnt FROM users GROUP BY role ORDER BY role;"

Write-Host "`n== 주요 컬럼 존재 여부 =="
Invoke-MariaDbSql -Sql @"
SELECT table_name, column_name
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND (
       (table_name='inspection_run' AND column_name IN ('payload_fingerprint','idempotency_key'))
    OR (table_name='document_version' AND column_name IN ('indexing_status','index_error_message'))
    OR (table_name='chat_message' AND column_name IN ('message_status','answer_status'))
  )
ORDER BY table_name, column_name;
"@
