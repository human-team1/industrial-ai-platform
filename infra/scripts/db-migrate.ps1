param(
    [string]$MigrationsDir = ""
)

$ErrorActionPreference = "Stop"
$infraRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $infraRoot
. (Join-Path $PSScriptRoot "db-common.ps1")

Assert-MariaDbContainerRunning

if (-not $MigrationsDir) {
    $MigrationsDir = Join-Path $infraRoot "mariadb\migrations"
}

if (-not (Test-Path $MigrationsDir)) {
    throw "migration 디렉터리가 없습니다: $MigrationsDir"
}

Invoke-MariaDbSql -Sql @"
CREATE TABLE IF NOT EXISTS schema_migration (
  migration_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  filename VARCHAR(255) NOT NULL UNIQUE,
  applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
"@

$files = @(Get-ChildItem -Path $MigrationsDir -Filter "*.sql" | Sort-Object Name)
if ($files.Count -eq 0) {
    Write-Host "[db-migrate] 적용할 migration SQL이 없습니다."
    exit 0
}

foreach ($file in $files) {
    $filename = $file.Name.Replace("'", "''")
    $countRaw = (Invoke-MariaDbSql -Sql "SELECT COUNT(*) AS cnt FROM schema_migration WHERE filename='${filename}';" | Out-String)
    if ($countRaw -match "(\d+)\s*$") {
        $count = [int]$Matches[1]
    } else {
        $count = 0
    }
    if ($count -gt 0) {
        Write-Host "[db-migrate] SKIP $($file.Name)"
        continue
    }

    Write-Host "[db-migrate] APPLY $($file.Name)"
    try {
        Invoke-MariaDbFile -SqlFilePath $file.FullName
        Invoke-MariaDbSql -Sql "INSERT INTO schema_migration (filename) VALUES ('${filename}');"
    } catch {
        Write-Error "[db-migrate] FAILED $($file.Name): $($_.Exception.Message)"
        throw
    }
}

Write-Host "[db-migrate] 완료"
