param(
    [string]$SeedDir = ""
)

$ErrorActionPreference = "Stop"
$infraRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $infraRoot
. (Join-Path $PSScriptRoot "db-common.ps1")

Assert-MariaDbContainerRunning

if (-not $SeedDir) {
    $SeedDir = Join-Path $infraRoot "mariadb\seed"
}

$seedOrder = @(
    "seed-sample-organizations.sql",
    "seed-sample-users.sql",
    "seed-sample-documents.sql",
    "seed-sample-inspections.sql"
)

foreach ($seed in $seedOrder) {
    $path = Join-Path $SeedDir $seed
    if (-not (Test-Path $path)) {
        Write-Warning "[db-seed] 파일 없음, 건너뜀: $seed"
        continue
    }
    Write-Host "[db-seed] APPLY $seed"
    Invoke-MariaDbFile -SqlFilePath $path
}

Write-Host "[db-seed] 완료"
