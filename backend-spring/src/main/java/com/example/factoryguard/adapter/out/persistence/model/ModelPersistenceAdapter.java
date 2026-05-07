package com.example.factoryguard.adapter.out.persistence.model;

import com.example.factoryguard.application.dto.inspection.AvailableInspectionModelItem;
import com.example.factoryguard.application.dto.model.ListModelDeploymentsQuery;
import com.example.factoryguard.application.dto.model.ListModelVersionsQuery;
import com.example.factoryguard.application.dto.model.ListModelsQuery;
import com.example.factoryguard.application.dto.model.ModelArtifactResponse;
import com.example.factoryguard.application.dto.model.ModelDeploymentResponse;
import com.example.factoryguard.application.dto.model.ModelPageResponse;
import com.example.factoryguard.application.dto.model.ModelSummaryResponse;
import com.example.factoryguard.application.dto.model.ModelVersionSummaryResponse;
import com.example.factoryguard.application.port.out.model.ModelManagementPort;
import com.example.factoryguard.domain.model.vo.DeploymentScope;
import com.example.factoryguard.domain.model.vo.ModelArtifactType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ModelPersistenceAdapter implements ModelManagementPort {

    private final ModelJpaRepository modelJpaRepository;
    private final ModelVersionJpaRepository modelVersionJpaRepository;
    private final ModelArtifactJpaRepository modelArtifactJpaRepository;
    private final ModelDeploymentJpaRepository modelDeploymentJpaRepository;
    private final ModelManagementQueryRepository queryRepository;

    @Override
    public boolean existsModelByNameAndType(String modelName, String modelType) {
        return modelJpaRepository.existsByModelNameAndModelType(modelName, modelType);
    }

    @Override
    public ModelJpaEntity saveModel(ModelJpaEntity entity) {
        return modelJpaRepository.save(entity);
    }

    @Override
    public Optional<ModelJpaEntity> findModelById(Long modelId) {
        return modelJpaRepository.findById(modelId);
    }

    @Override
    public ModelPageResponse<ModelSummaryResponse> findModels(ListModelsQuery query) {
        return queryRepository.findModels(query);
    }

    @Override
    public boolean existsVersionByModelIdAndVersionName(Long modelId, String versionName) {
        return modelVersionJpaRepository.existsByModelIdAndVersionName(modelId, versionName);
    }

    @Override
    public ModelVersionJpaEntity saveModelVersion(ModelVersionJpaEntity entity) {
        return modelVersionJpaRepository.save(entity);
    }

    @Override
    public Optional<ModelVersionJpaEntity> findModelVersionById(Long versionId) {
        return modelVersionJpaRepository.findById(versionId);
    }

    @Override
    public ModelPageResponse<ModelVersionSummaryResponse> findModelVersions(ListModelVersionsQuery query) {
        return queryRepository.findModelVersions(query);
    }

    @Override
    public List<ModelArtifactJpaEntity> saveArtifacts(List<ModelArtifactJpaEntity> entities) {
        return modelArtifactJpaRepository.saveAll(entities);
    }

    @Override
    public List<ModelArtifactJpaEntity> findArtifactsByVersionId(Long versionId) {
        return modelArtifactJpaRepository.findByModelVersionIdOrderByCreatedAtAsc(versionId);
    }

    @Override
    public List<ModelArtifactResponse> findArtifactResponsesByVersionId(Long versionId) {
        return queryRepository.findArtifactsByVersionId(versionId);
    }

    @Override
    public boolean hasArtifact(Long versionId, ModelArtifactType artifactType) {
        return modelArtifactJpaRepository.existsByModelVersionIdAndArtifactType(versionId, artifactType);
    }

    @Override
    public ModelDeploymentJpaEntity saveDeployment(ModelDeploymentJpaEntity entity) {
        return modelDeploymentJpaRepository.save(entity);
    }

    @Override
    public Optional<ModelDeploymentJpaEntity> findDeploymentById(Long deploymentId) {
        return modelDeploymentJpaRepository.findById(deploymentId);
    }

    @Override
    public ModelPageResponse<ModelDeploymentResponse> findDeployments(ListModelDeploymentsQuery query) {
        return queryRepository.findDeployments(query);
    }

    @Override
    public List<AvailableInspectionModelItem> findAvailableInspectionModels(Long organizationId, Long targetId, String modelCategory) {
        return queryRepository.findAvailableInspectionModels(organizationId, targetId, modelCategory);
    }

    @Override
    public List<ModelDeploymentJpaEntity> findActiveDeployments(Long organizationId, Long targetId, DeploymentScope scope) {
        if (scope == DeploymentScope.TARGET) {
            return modelDeploymentJpaRepository.findByOrganizationIdAndTargetIdAndDeploymentScopeAndIsActiveTrue(organizationId, targetId, scope);
        }
        return modelDeploymentJpaRepository.findByOrganizationIdAndDeploymentScopeAndIsActiveTrue(organizationId, scope);
    }

    @Override
    public List<ModelDeploymentJpaEntity> findActiveDeploymentsByVersionId(Long versionId) {
        return modelDeploymentJpaRepository.findByModelVersionIdAndIsActiveTrue(versionId);
    }
}
