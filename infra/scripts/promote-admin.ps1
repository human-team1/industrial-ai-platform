param(
    [Parameter(Mandatory = $true)]
    [string]$Email,

    [string]$OrganizationName = "관리자 조직"
)

$ErrorActionPreference = "Stop"

function Escape-SqlString([string]$Value) {
    return $Value.Replace("'", "''")
}

$escapedEmail = Escape-SqlString $Email
$escapedOrganizationName = Escape-SqlString $OrganizationName

$sql = @"
SET @admin_email = '$escapedEmail';
SET @organization_name = '$escapedOrganizationName';

INSERT INTO ORGANIZATION (organization_name, status)
SELECT @organization_name, 'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1
    FROM ORGANIZATION
    WHERE organization_name = @organization_name
);

SET @organization_id = (
    SELECT organization_id
    FROM ORGANIZATION
    WHERE organization_name = @organization_name
    ORDER BY organization_id
    LIMIT 1
);

UPDATE USERS
SET
    status = 'ACTIVE',
    role = 'ROLE_SITE_ADMIN',
    organization_id = @organization_id
WHERE email = @admin_email;

UPDATE users
SET
    status = 'ACTIVE',
    role = 'ROLE_SITE_ADMIN',
    organization_id = @organization_id
WHERE email = @admin_email;

UPDATE SIGNUP_REQUEST
SET
    request_status = 'APPROVED',
    organization_id = @organization_id,
    processed_by = (
        SELECT user_id
        FROM USERS
        WHERE email = @admin_email
        LIMIT 1
    ),
    processed_at = NOW()
WHERE user_id = (
    SELECT user_id
    FROM USERS
    WHERE email = @admin_email
    LIMIT 1
);

UPDATE signup_requests
SET
    request_status = 'APPROVED',
    processed_by = (
        SELECT user_id
        FROM users
        WHERE email = @admin_email
        LIMIT 1
    ),
    processed_at = NOW()
WHERE user_id = (
    SELECT user_id
    FROM users
    WHERE email = @admin_email
    LIMIT 1
);

SELECT user_id, email, status, role, organization_id
FROM users
WHERE email = @admin_email;
"@

$sql | docker exec -i industrial-mariadb sh -c 'mariadb -uindustrial_user -p"$MARIADB_PASSWORD" industrial_ai'
