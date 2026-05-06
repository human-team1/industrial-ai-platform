package com.example.factoryguard.application.port.out.model;

import com.example.factoryguard.adapter.out.persistence.model.ModelArtifactJpaEntity;
import com.example.factoryguard.adapter.out.persistence.model.ModelDeploymentJpaEntity;
import com.example.factoryguard.adapter.out.persistence.model.ModelJpaEntity;
import com.example.factoryguard.adapter.out.persistence.model.ModelVersionJpaEntity;
import com.example.factoryguard.application.dto.model.ListModelDeploymentsQuery;
import com.example.factoryguard.application.dto.model.ListModelVersionsQuery;
import com.example.factoryguard.application.dto.model.ListModelsQuery;
import com.example.factoryguard.application.dto.inspection.AvailableInspectionModelItem;
import com.example.factoryguard.application.dto.model.ModelArtifactResponse;
import com.example.factoryguard.application.dto.model.ModelDeploymentResponse;
import com.example.factoryguard.application.dto.model.ModelPageResponse;
import com.example.factoryguard.application.dto.model.ModelSummaryResponse;
import com.example.factoryguard.application.dto.model.ModelVersionSummaryResponse;
import com.example.factoryguard.domain.model.vo.DeploymentScope;
import com.example.factoryguard.domain.model.vo.ModelArtifactType;

import java.util.List;
import java.util.Optional;

public interface ModelManagementPort {

    boolean existsModelByNameAndType(String modelName, String modelType);

    ModelJpaEntity saveModel(ModelJpaEntity entity);

    Optional<ModelJpaEntity> findModelById(Long modelId);

    ModelPageResponse<ModelSummaryResponse> findModels(ListModelsQuery query);

    boolean existsVersionByModelIdAndVersionName(Long modelId, String versionName);

    ModelVersionJpaEntity saveModelVersion(ModelVersionJpaEntity entity);

    Optional<ModelVersionJpaEntity> findModelVersionById(Long versionId);

    ModelPageResponse<ModelVersionSummaryResponse> findModelVersions(ListModelVersionsQuery query);

    List<ModelArtifactJpaEntity> saveArtifacts(List<ModelArtifactJpaEntity> entities);

    List<ModelArtifactJpaEntity> findArtifactsByVersionId(Long versionId);

    List<ModelArtifactResponse> findArtifactResponsesByVersionId(Long versionId);

    boolean hasArtifact(Long versionId, ModelArtifactType artifactType);

    ModelDeploymentJpaEntity saveDeployment(ModelDeploymentJpaEntity entity);

    Optional<ModelDeploymentJpaEntity> findDeploymentById(Long deploymentId);

    ModelPageResponse<ModelDeploymentResponse> findDeployments(ListModelDeploymentsQuery query);

    List<AvailableInspectionModelItem> findAvailableInspectionModels(Long organizationId, Long targetId, String modelCategory);

    List<ModelDeploymentJpaEntity> findActiveDeployments(Long organizationId, Long targetId, DeploymentScope scope);

    List<ModelDeploymentJpaEntity> findActiveDeploymentsByVersionId(Long versionId);
}
