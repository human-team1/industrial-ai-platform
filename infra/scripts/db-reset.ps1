param(
    [switch]$Force
)

$ErrorActionPreference = "Stop"
$infraRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $infraRoot
. (Join-Path $PSScriptRoot "db-common.ps1")

if (-not $Force) {
    Write-Host "[db-reset] WARNING: destructive 작업입니다. DB를 초기화합니다."
    Write-Host "[db-reset] 실행하려면 --Force 옵션을 사용하세요."
    exit 1
}

Assert-MariaDbContainerRunning

$envMap = Get-EnvMap
$db = $envMap["MARIADB_DATABASE"]
if (-not $db) {
    throw "MARIADB_DATABASE 값이 없습니다."
}

Write-Host "[db-reset] database 초기화 시작: $db"
Invoke-MariaDbSql -UseRoot -Sql "DROP DATABASE IF EXISTS ${db}; CREATE DATABASE ${db} DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

$initSql = Join-Path $infraRoot "mariadb\init\industrial-ai-platform.sql"
Write-Host "[db-reset] init SQL 적용"
Invoke-MariaDbFile -UseRoot -SqlFilePath $initSql

Write-Host "[db-reset] migration 적용"
& (Join-Path $PSScriptRoot "db-migrate.ps1")

Write-Host "[db-reset] seed 적용"
& (Join-Path $PSScriptRoot "db-seed.ps1")

Write-Host "[db-reset] 상태 확인"
& (Join-Path $PSScriptRoot "db-status.ps1")
