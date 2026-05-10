# MinIO 필수 버킷 생성. infra 디렉터리에서 실행.
# .\scripts\init-minio-buckets.ps1
# .\scripts\init-minio-buckets.ps1 -ComposeFile docker-compose.prod.yml -EnvFile .env.prod
param(
    [string] $ComposeFile = "docker-compose.yml",
    [string] $EnvFile = ".env"
)
$ErrorActionPreference = "Stop"
$infraRoot = Split-Path -Parent $PSScriptRoot
Set-Location $infraRoot
$sh = Join-Path $PSScriptRoot "init-minio-buckets.sh"
((Get-Content $sh -Raw) -replace "`r`n", "`n") | docker compose -f $ComposeFile --env-file $EnvFile exec -i -T minio sh
