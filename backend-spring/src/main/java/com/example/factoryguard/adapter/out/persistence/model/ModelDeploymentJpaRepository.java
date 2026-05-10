package com.example.factoryguard.adapter.out.persistence.model;

import com.example.factoryguard.domain.model.vo.DeploymentScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ModelDeploymentJpaRepository extends JpaRepository<ModelDeploymentJpaEntity, Long> {

    List<ModelDeploymentJpaEntity> findByModelVersionIdAndIsActiveTrue(Long modelVersionId);

    List<ModelDeploymentJpaEntity> findByOrganizationIdAndDeploymentScopeAndIsActiveTrue(Long organizationId, DeploymentScope deploymentScope);

    List<ModelDeploymentJpaEntity> findByOrganizationIdAndTargetIdAndDeploymentScopeAndIsActiveTrue(Long organizationId, Long targetId, DeploymentScope deploymentScope);

    List<ModelDeploymentJpaEntity> findByModelVersionId(Long modelVersionId);

    @Modifying
    @Query(value = """
            UPDATE model_deployment md
            JOIN model_version mv ON mv.model_version_id = md.model_version_id
            SET md.is_active = FALSE,
                md.deploy_status = 'DEACTIVATED'
            WHERE md.organization_id = :organizationId
              AND md.deployment_scope = :deploymentScope
              AND (
                    (:deploymentScope = 'ORGANIZATION' AND md.target_id IS NULL)
                    OR
                    (:deploymentScope = 'TARGET' AND md.target_id = :targetId)
                  )
              AND mv.model_category = :modelCategory
              AND mv.model_profile = :modelProfile
              AND mv.deleted_at IS NULL
              AND md.deleted_at IS NULL
              AND md.is_active = TRUE
              AND md.deploy_status = 'DEPLOYED'
            """, nativeQuery = true)
    int deactivateActiveDeploymentsInSameSlot(
            @Param("organizationId") Long organizationId,
            @Param("targetId") Long targetId,
            @Param("deploymentScope") String deploymentScope,
            @Param("modelCategory") String modelCategory,
            @Param("modelProfile") String modelProfile
    );

    @Modifying
    @Query(value = """
            UPDATE model_deployment md
            JOIN model_version mv ON mv.model_version_id = md.model_version_id
            SET md.is_active = FALSE,
                md.deploy_status = 'DEACTIVATED'
            WHERE md.organization_id = :organizationId
              AND md.deployment_scope = :deploymentScope
              AND (
                    (:deploymentScope = 'ORGANIZATION' AND md.target_id IS NULL)
                    OR
                    (:deploymentScope = 'TARGET' AND md.target_id = :targetId)
                  )
              AND mv.model_category = :modelCategory
              AND mv.model_profile = :modelProfile
              AND mv.deleted_at IS NULL
              AND md.deleted_at IS NULL
              AND md.deployment_id <> :excludeDeploymentId
              AND md.is_active = TRUE
              AND md.deploy_status = 'DEPLOYED'
            """, nativeQuery = true)
    int deactivateActiveDeploymentsInSameSlotExcludingDeployment(
            @Param("organizationId") Long organizationId,
            @Param("targetId") Long targetId,
            @Param("deploymentScope") String deploymentScope,
            @Param("modelCategory") String modelCategory,
            @Param("modelProfile") String modelProfile,
            @Param("excludeDeploymentId") Long excludeDeploymentId
    );
}
