-- Optimize same-slot deactivation query for model deployments.
-- Slot policy: organizationId + targetId/scope + deploymentScope + modelCategory + modelProfile
-- modelCategory/modelProfile are resolved via model_version join.

CREATE INDEX IF NOT EXISTS idx_model_deployment_slot_deactivate
  ON model_deployment (organization_id, deployment_scope, target_id, is_active, deploy_status, model_version_id);

CREATE INDEX IF NOT EXISTS idx_model_version_slot_lookup
  ON model_version (model_category, model_profile, model_version_id);
