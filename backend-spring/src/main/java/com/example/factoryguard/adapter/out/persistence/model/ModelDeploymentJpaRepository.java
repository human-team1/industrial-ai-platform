package com.example.factoryguard.adapter.out.persistence.model;

import com.example.factoryguard.domain.model.vo.DeploymentScope;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModelDeploymentJpaRepository extends JpaRepository<ModelDeploymentJpaEntity, Long> {

    List<ModelDeploymentJpaEntity> findByModelVersionIdAndIsActiveTrue(Long modelVersionId);

    List<ModelDeploymentJpaEntity> findByOrganizationIdAndDeploymentScopeAndIsActiveTrue(Long organizationId, DeploymentScope deploymentScope);

    List<ModelDeploymentJpaEntity> findByOrganizationIdAndTargetIdAndDeploymentScopeAndIsActiveTrue(Long organizationId, Long targetId, DeploymentScope deploymentScope);
}
