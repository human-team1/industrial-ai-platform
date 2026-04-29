-- Local development sample organizations
-- Manual execution only.
-- Target database: industrial_ai
-- Purpose:
--   Provide ACTIVE organizations for the public signup organization selector.
--   organization_public view exposes only ACTIVE organizations.

INSERT INTO organization (organization_id, organization_name, status)
VALUES
  (9001, 'Sample Factory', 'ACTIVE'),
  (1001, 'Sample Org A', 'ACTIVE'),
  (1002, 'Sample Org B', 'ACTIVE'),
  (1003, 'Sample Org C', 'ACTIVE')
ON DUPLICATE KEY UPDATE
  organization_name = VALUES(organization_name),
  status = VALUES(status);
