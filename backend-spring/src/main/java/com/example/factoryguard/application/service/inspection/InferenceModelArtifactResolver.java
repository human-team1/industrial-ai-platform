package com.example.factoryguard.application.service.inspection;

import com.example.factoryguard.adapter.out.persistence.model.ModelDeploymentJpaEntity;
import com.example.factoryguard.adapter.out.persistence.model.ModelVersionJpaEntity;
import com.example.factoryguard.application.dto.inspection.ResolvedInspectionModelArtifacts;
import com.example.factoryguard.application.port.out.file.LoadFilePort;
import com.example.factoryguard.application.port.out.model.ModelManagementPort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.file.model.StoredFile;
import com.example.factoryguard.domain.model.vo.DeploymentScope;
import com.example.factoryguard.domain.model.vo.DeploymentStatus;
import com.example.factoryguard.domain.model.vo.ModelArtifactType;
import com.example.factoryguard.domain.model.vo.ModelDeployStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class InferenceModelArtifactResolver {

    private final ModelManagementPort modelManagementPort;
    private final LoadFilePort loadFilePort;

    @Transactional(readOnly = true)
    public ResolvedInspectionModelArtifacts resolve(Long organizationId, Long targetId, Long deploymentId) {
        if (deploymentId == null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "deploymentId는 필수입니다.");
        }

        ModelDeploymentJpaEntity deployment = modelManagementPort.findDeploymentById(deploymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MODEL_DEPLOYMENT_NOT_FOUND));
        if (!Objects.equals(organizationId, deployment.getOrganizationId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (!Boolean.TRUE.equals(deployment.getIsActive()) || deployment.getDeployStatus() != DeploymentStatus.DEPLOYED) {
            throw new BusinessException(ErrorCode.MODEL_DEPLOYMENT_CONFLICT);
        }
        if (deployment.getDeploymentScope() == DeploymentScope.TARGET) {
            if (targetId == null || !targetId.equals(deployment.getTargetId())) {
                throw new BusinessException(ErrorCode.FORBIDDEN);
            }
        }

        ModelVersionJpaEntity version = modelManagementPort.findModelVersionById(deployment.getModelVersionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MODEL_VERSION_NOT_FOUND));
        if (!Boolean.TRUE.equals(version.getIsActive()) || version.getDeployStatus() != ModelDeployStatus.DEPLOYED) {
            throw new BusinessException(ErrorCode.MODEL_DEPLOYMENT_CONFLICT);
        }

        Map<ModelArtifactType, StoredFile> files = new EnumMap<>(ModelArtifactType.class);
        try {
            modelManagementPort.findArtifactsByVersionId(version.getModelVersionId()).forEach(artifact -> {
                if (artifact.getArtifactType() == null) {
                    throw new BusinessException(ErrorCode.MODEL_DEPLOYMENT_CONFLICT,
                            "required model artifact type missing for versionId=" + version.getModelVersionId());
                }
                if (artifact.getFileId() == null) {
                    throw new BusinessException(ErrorCode.MODEL_DEPLOYMENT_CONFLICT,
                            "required model artifact fileId missing for versionId=" + version.getModelVersionId()
                                    + ", artifactType=" + artifact.getArtifactType().name());
                }
                loadFilePort.findById(artifact.getFileId())
                        .ifPresent(file -> files.put(artifact.getArtifactType(), file));
            });
        } catch (BusinessException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new BusinessException(
                    ErrorCode.MODEL_DEPLOYMENT_CONFLICT,
                    "failed to resolve model artifacts for deploymentId=" + deploymentId
                            + ", versionId=" + version.getModelVersionId()
                            + ", reason=" + exception.getClass().getSimpleName()
            );
        }

        return ResolvedInspectionModelArtifacts.builder()
                .deployment(deployment)
                .version(version)
                .ckpt(required(files, ModelArtifactType.CKPT))
                .config(required(files, ModelArtifactType.CONFIG))
                .memoryBank(required(files, ModelArtifactType.MEMORY_BANK))
                .labels(files.get(ModelArtifactType.LABELS))
                .build();
    }

    private StoredFile required(Map<ModelArtifactType, StoredFile> files, ModelArtifactType type) {
        StoredFile file = files.get(type);
        if (file == null || file.getObjectKey() == null || file.getObjectKey().isBlank()) {
            throw new BusinessException(ErrorCode.MODEL_DEPLOYMENT_CONFLICT, "required model artifact missing: " + type.name());
        }
        return file;
    }
}
