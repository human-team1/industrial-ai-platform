-- Sample users for local verification
INSERT INTO users (
  user_id, organization_id, email, name, status, role, created_at, updated_at
)
VALUES
  (91001, 9001, 'site-admin@sample.local', '사이트 관리자 샘플', 'ACTIVE', 'ROLE_SITE_ADMIN', NOW(), NOW()),
  (91002, 9001, 'company-admin@sample.local', '회사 관리자 샘플', 'ACTIVE', 'ROLE_COMPANY_ADMIN', NOW(), NOW()),
  (91003, 9001, 'worker@sample.local', '작업자 샘플', 'ACTIVE', 'ROLE_COMPANY_WORKER', NOW(), NOW())
ON DUPLICATE KEY UPDATE
  organization_id = VALUES(organization_id),
  name = VALUES(name),
  status = VALUES(status),
  role = VALUES(role),
  updated_at = NOW();
