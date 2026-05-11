Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

if (-not $script:DbComposeFile) {
    $script:DbComposeFile = "docker-compose.yml"
}

if (-not $script:DbEnvFile) {
    $script:DbEnvFile = ".env"
}

function Get-InfraRoot {
    return (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
}

function Get-EnvMap {
    $infraRoot = Get-InfraRoot
    $envPath = Join-Path $infraRoot $script:DbEnvFile
    if (-not (Test-Path $envPath)) {
        throw "infra/.env 파일이 없습니다. 먼저 infra/.env.example을 복사하세요."
    }

    $map = @{}
    Get-Content $envPath | ForEach-Object {
        $line = $_.Trim()
        if (-not $line -or $line.StartsWith("#")) { return }
        $idx = $line.IndexOf("=")
        if ($idx -lt 1) { return }
        $key = $line.Substring(0, $idx).Trim()
        $value = $line.Substring($idx + 1).Trim()
        $map[$key] = $value
    }
    return $map
}

function Invoke-MariaDbSql {
    param(
        [Parameter(Mandatory = $true)][string]$Sql,
        [switch]$UseRoot
    )
    $envMap = Get-EnvMap
    $db = $envMap["MARIADB_DATABASE"]
    $user = if ($UseRoot) { "root" } else { $envMap["MARIADB_USER"] }
    $password = if ($UseRoot) { $envMap["MARIADB_ROOT_PASSWORD"] } else { $envMap["MARIADB_PASSWORD"] }
    if (-not $db -or -not $user -or -not $password) {
        throw "DB 접속 환경변수가 부족합니다. MARIADB_DATABASE / USER / PASSWORD / ROOT_PASSWORD 확인이 필요합니다."
    }

    $Sql |
        docker compose -f $script:DbComposeFile --env-file $script:DbEnvFile exec -T mariadb sh -lc "mariadb --default-character-set=utf8mb4 -u${user} -p${password} ${db}"
}

function Invoke-MariaDbFile {
    param(
        [Parameter(Mandatory = $true)][string]$SqlFilePath,
        [switch]$UseRoot
    )
    if (-not (Test-Path $SqlFilePath)) {
        throw "SQL 파일을 찾을 수 없습니다: $SqlFilePath"
    }

    $envMap = Get-EnvMap
    $db = $envMap["MARIADB_DATABASE"]
    $user = if ($UseRoot) { "root" } else { $envMap["MARIADB_USER"] }
    $password = if ($UseRoot) { $envMap["MARIADB_ROOT_PASSWORD"] } else { $envMap["MARIADB_PASSWORD"] }
    $tempFileName = "codex-" + [System.Guid]::NewGuid().ToString("N") + ".sql"
    $containerSqlPath = "/tmp/$tempFileName"

    try {
        docker compose -f $script:DbComposeFile --env-file $script:DbEnvFile cp $SqlFilePath "mariadb:${containerSqlPath}" | Out-Null
        docker compose -f $script:DbComposeFile --env-file $script:DbEnvFile exec -T mariadb sh -lc "mariadb --default-character-set=utf8mb4 -u${user} -p${password} ${db} < ${containerSqlPath}"
    } finally {
        docker compose -f $script:DbComposeFile --env-file $script:DbEnvFile exec -T mariadb sh -lc "rm -f ${containerSqlPath}" | Out-Null
    }
}

function Assert-MariaDbContainerRunning {
    $name = docker compose -f $script:DbComposeFile --env-file $script:DbEnvFile ps -q mariadb
    if (-not $name) {
        throw "mariadb 컨테이너가 실행 중이 아닙니다. infra에서 docker compose up -d mariadb를 먼저 실행하세요."
    }
}
