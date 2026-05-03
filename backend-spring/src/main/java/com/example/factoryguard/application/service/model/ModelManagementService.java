package com.example.factoryguard.application.service.model;

import com.example.factoryguard.adapter.out.persistence.model.ModelArtifactJpaEntity;
import com.example.factoryguard.adapter.out.persistence.model.ModelDeploymentJpaEntity;
import com.example.factoryguard.adapter.out.persistence.model.ModelJpaEntity;
import com.example.factoryguard.adapter.out.persistence.model.ModelVersionJpaEntity;
import com.example.factoryguard.adapter.out.storage.minio.MinioProperties;
import com.example.factoryguard.adapter.out.storage.minio.MinioStorageAdapter;
import com.example.factoryguard.application.dto.model.*;
import com.example.factoryguard.application.dto.operation.RecordAdminActionLogCommand;
import com.example.factoryguard.application.port.in.model.*;
import com.example.factoryguard.application.port.in.operation.RecordAdminActionLogUseCase;
import com.example.factoryguard.application.port.out.file.PersistUploadedFilePort;
import com.example.factoryguard.application.port.out.inspection.LoadAnalysisTargetPort;
import com.example.factoryguard.application.port.out.model.ModelManagementPort;
import com.example.factoryguard.application.port.out.organization.FindOrganizationByIdPort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.config.security.SecurityUtils;
import com.example.factoryguard.domain.file.model.StoredFile;
import com.example.factoryguard.domain.file.vo.StorageType;
import com.example.factoryguard.domain.inspection.model.AnalysisTarget;
import com.example.factoryguard.domain.model.vo.DeploymentScope;
import com.example.factoryguard.domain.model.vo.DeploymentStatus;
import com.example.factoryguard.domain.model.vo.ModelArtifactType;
import com.example.factoryguard.domain.model.vo.ModelDeployStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModelManagementService implements
        ListModelsUseCase,
        CreateModelUseCase,
        GetModelUseCase,
        ListModelVersionsUseCase,
        UploadModelVersionUseCase,
        GetModelVersionUseCase,
        ListModelArtifactsUseCase,
        ActivateModelVersionUseCase,
        DeprecateModelVersionUseCase,
        ListModelDeploymentsUseCase,
        DeployModelVersionUseCase,
        DeactivateModelDeploymentUseCase,
        RollbackModelDeploymentUseCase {

    private final ModelManagementPort modelManagementPort;
    private final PersistUploadedFilePort persistUploadedFilePort;
    private final MinioStorageAdapter minioStorageAdapter;
    private final MinioProperties minioProperties;
    private final SecurityUtils securityUtils;
    private final FindOrganizationByIdPort findOrganizationByIdPort;
    private final LoadAnalysisTargetPort loadAnalysisTargetPort;
    private final RecordAdminActionLogUseCase recordAdminActionLogUseCase;

    @Override
    @Transactional(readOnly = true)
    public ModelPageResponse<ModelSummaryResponse> listModels(ListModelsQuery query) {
        return modelManagementPort.findModels(query);
    }

    @Override
    @Transactional
    public ModelDetailResponse createModel(CreateModelCommand command) {
        String modelName = required(command.getModelName(), "modelName");
        String modelType = required(command.getModelType(), "modelType");
        if (modelManagementPort.existsModelByNameAndType(modelName, modelType)) {
            throw new BusinessException(ErrorCode.MODEL_CONFLICT, "같은 modelName + modelType 조합이 이미 존재합니다.");
        }
        ModelJpaEntity saved = modelManagementPort.saveModel(ModelJpaEntity.builder()
                .modelName(modelName)
                .modelType(modelType)
                .description(blankToNull(command.getDescription()))
                .build());
        recordAction("MODEL_CREATED", saved.getModelId(), "MODEL", "모델 등록");
        return toModelDetail(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ModelDetailResponse getModel(Long modelId) {
        return toModelDetail(loadModel(modelId));
    }

    @Override
    @Transactional(readOnly = true)
    public ModelPageResponse<ModelVersionSummaryResponse> listModelVersions(ListModelVersionsQuery query) {
        loadModel(query.getModelId());
        return modelManagementPort.findModelVersions(query);
    }

    @Override
    @Transactional
    public ModelVersionDetailResponse uploadModelVersion(UploadModelVersionCommand command) {
        ModelJpaEntity model = loadModel(command.getModelId());
        validateUploadCommand(command);
        if (modelManagementPort.existsVersionByModelIdAndVersionName(model.getModelId(), command.getVersionName().trim())) {
            throw new BusinessException(ErrorCode.MODEL_VERSION_CONFLICT, "같은 모델에 동일한 versionName이 이미 존재합니다.");
        }

        ModelVersionJpaEntity version = modelManagementPort.saveModelVersion(ModelVersionJpaEntity.builder()
                .modelId(model.getModelId())
                .versionName(command.getVersionName().trim())
                .modelCategory(command.getModelCategory())
                .modelProfile(command.getModelProfile())
                .framework(blankToNull(command.getFramework()))
                .inputSize(blankToNull(command.getInputSize()))
                .thresholdDefault(command.getThresholdDefault())
                .accuracy(command.getAccuracy())
                .precisionScore(command.getPrecisionScore())
                .recallScore(command.getRecallScore())
                .f1Score(command.getF1Score())
                .aurocScore(command.getAurocScore())
                .deployStatus(ModelDeployStatus.REGISTERED)
                .isActive(false)
                .build());

        List<ModelArtifactJpaEntity> artifacts = new ArrayList<>();
        artifacts.add(uploadArtifact(model.getModelId(), version.getModelVersionId(), command.getCkptFile(), "model.ckpt", ModelArtifactType.CKPT));
        artifacts.add(uploadArtifact(model.getModelId(), version.getModelVersionId(), command.getConfigFile(), "config.json", ModelArtifactType.CONFIG));
        if (command.getLabelsFile() != null && !command.getLabelsFile().isEmpty()) {
            String labelsExt = extension(command.getLabelsFile().getOriginalFilename());
            String labelsName = labelsExt == null ? "labels" : "labels." + labelsExt;
            artifacts.add(uploadArtifact(model.getModelId(), version.getModelVersionId(), command.getLabelsFile(), labelsName, ModelArtifactType.LABELS));
        }
        modelManagementPort.saveArtifacts(artifacts);
        recordAction("MODEL_VERSION_UPLOADED", version.getModelVersionId(), "MODEL_VERSION", "모델 버전 업로드");
        return buildVersionDetail(version, model.getModelName());
    }

    @Override
    @Transactional(readOnly = true)
    public ModelVersionDetailResponse getModelVersion(Long versionId) {
        ModelVersionJpaEntity version = loadVersion(versionId);
        return buildVersionDetail(version, loadModel(version.getModelId()).getModelName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModelArtifactResponse> listModelArtifacts(Long versionId) {
        loadVersion(versionId);
        return modelManagementPort.findArtifactResponsesByVersionId(versionId);
    }

    @Override
    @Transactional
    public ModelVersionDetailResponse activateModelVersion(ModelVersionStatusCommand command) {
        ModelVersionJpaEntity version = loadVersion(command.getVersionId());
        if (!modelManagementPort.hasArtifact(version.getModelVersionId(), ModelArtifactType.CKPT)
                || !modelManagementPort.hasArtifact(version.getModelVersionId(), ModelArtifactType.CONFIG)) {
            throw new BusinessException(ErrorCode.MODEL_VALIDATION_FAILED, "CKPT와 CONFIG artifact가 모두 있어야 활성화할 수 있습니다.");
        }
        version.activate(LocalDateTime.now(), securityUtils.getCurrentUserId());
        ModelVersionJpaEntity saved = modelManagementPort.saveModelVersion(version);
        recordAction("MODEL_VERSION_ACTIVATED", saved.getModelVersionId(), "MODEL_VERSION", blankToNull(command.getReason()));
        return buildVersionDetail(saved, loadModel(saved.getModelId()).getModelName());
    }

    @Override
    @Transactional
    public ModelVersionDetailResponse deprecateModelVersion(ModelVersionStatusCommand command) {
        ModelVersionJpaEntity version = loadVersion(command.getVersionId());
        if (!modelManagementPort.findActiveDeploymentsByVersionId(version.getModelVersionId()).isEmpty()) {
            throw new BusinessException(ErrorCode.MODEL_DEPLOYMENT_CONFLICT, "활성 배포가 남아 있는 모델 버전은 사용 중단할 수 없습니다.");
        }
        version.deprecate();
        ModelVersionJpaEntity saved = modelManagementPort.saveModelVersion(version);
        recordAction("MODEL_VERSION_DEPRECATED", saved.getModelVersionId(), "MODEL_VERSION", blankToNull(command.getReason()));
        return buildVersionDetail(saved, loadModel(saved.getModelId()).getModelName());
    }

    @Override
    @Transactional(readOnly = true)
    public ModelPageResponse<ModelDeploymentResponse> listModelDeployments(ListModelDeploymentsQuery query) {
        return modelManagementPort.findDeployments(query);
    }

    @Override
    @Transactional
    public ModelDeploymentResponse deployModelVersion(DeployModelVersionCommand command) {
        ModelVersionJpaEntity version = loadVersion(command.getVersionId());
        if (Boolean.FALSE.equals(version.getIsActive()) || version.getDeployStatus() == ModelDeployStatus.DEPRECATED) {
            throw new BusinessException(ErrorCode.MODEL_DEPLOYMENT_CONFLICT, "활성화되지 않았거나 사용 중단된 모델 버전은 배포할 수 없습니다.");
        }
        validateDeployment(command.getOrganizationId(), command.getTargetId(), command.getDeploymentScope());
        List<ModelDeploymentJpaEntity> activeDeployments =
                modelManagementPort.findActiveDeployments(command.getOrganizationId(), command.getTargetId(), command.getDeploymentScope());
        for (ModelDeploymentJpaEntity activeDeployment : activeDeployments) {
            activeDeployment.deactivate("신규 배포로 인한 자동 비활성화");
            modelManagementPort.saveDeployment(activeDeployment);
        }
        ModelDeploymentJpaEntity deployment = modelManagementPort.saveDeployment(ModelDeploymentJpaEntity.builder()
                .organizationId(command.getOrganizationId())
                .targetId(command.getDeploymentScope() == DeploymentScope.TARGET ? command.getTargetId() : null)
                .modelVersionId(version.getModelVersionId())
                .deploymentScope(command.getDeploymentScope())
                .deployStatus(DeploymentStatus.DEPLOYED)
                .isActive(true)
                .deployedAt(LocalDateTime.now())
                .deployedBy(securityUtils.getCurrentUserId())
                .reason(blankToNull(command.getReason()))
                .rollbackFlag(false)
                .build());
        version.markDeployed();
        modelManagementPort.saveModelVersion(version);
        recordAction("MODEL_DEPLOYED", deployment.getDeploymentId(), "MODEL_DEPLOYMENT", blankToNull(command.getReason()));
        return toDeploymentResponse(deployment);
    }

    @Override
    @Transactional
    public ModelDeploymentResponse deactivateModelDeployment(DeactivateModelDeploymentCommand command) {
        ModelDeploymentJpaEntity deployment = loadDeployment(command.getDeploymentId());
        if (Boolean.TRUE.equals(deployment.getIsActive())) {
            deployment.deactivate(blankToNull(command.getReason()));
            deployment = modelManagementPort.saveDeployment(deployment);
            recordAction("MODEL_DEPLOYMENT_DEACTIVATED", deployment.getDeploymentId(), "MODEL_DEPLOYMENT", blankToNull(command.getReason()));
        }
        return toDeploymentResponse(deployment);
    }

    @Override
    @Transactional
    public ModelDeploymentResponse rollbackModelDeployment(RollbackModelDeploymentCommand command) {
        ModelDeploymentJpaEntity current = loadDeployment(command.getDeploymentId());
        ModelDeploymentJpaEntity rollbackTarget = loadDeployment(command.getRollbackToDeploymentId());
        if (!sameScope(current, rollbackTarget)) {
            throw new BusinessException(ErrorCode.MODEL_VALIDATION_FAILED, "롤백 대상은 같은 organization/target/scope여야 합니다.");
        }
        ModelVersionJpaEntity rollbackVersion = loadVersion(rollbackTarget.getModelVersionId());
        if (rollbackVersion.getDeployStatus() == ModelDeployStatus.DEPRECATED) {
            throw new BusinessException(ErrorCode.MODEL_DEPLOYMENT_CONFLICT, "사용 중단된 버전으로는 롤백할 수 없습니다.");
        }
        current.deactivate(blankToNull(command.getReason()));
        modelManagementPort.saveDeployment(current);
        ModelDeploymentJpaEntity restored = modelManagementPort.saveDeployment(ModelDeploymentJpaEntity.builder()
                .organizationId(current.getOrganizationId())
                .targetId(current.getTargetId())
                .modelVersionId(rollbackTarget.getModelVersionId())
                .deploymentScope(current.getDeploymentScope())
                .deployStatus(DeploymentStatus.DEPLOYED)
                .isActive(true)
                .deployedAt(LocalDateTime.now())
                .deployedBy(securityUtils.getCurrentUserId())
                .rollbackFromDeploymentId(current.getDeploymentId())
                .reason(blankToNull(command.getReason()))
                .rollbackFlag(true)
                .build());
        recordAction("MODEL_ROLLED_BACK", restored.getDeploymentId(), "MODEL_DEPLOYMENT", blankToNull(command.getReason()));
        return toDeploymentResponse(restored);
    }

    private void validateUploadCommand(UploadModelVersionCommand command) {
        required(command.getVersionName(), "versionName");
        if (command.getModelCategory() == null) {
            throw new BusinessException(ErrorCode.MODEL_VALIDATION_FAILED, "modelCategory는 필수입니다.");
        }
        if (command.getModelProfile() == null) {
            throw new BusinessException(ErrorCode.MODEL_VALIDATION_FAILED, "modelProfile은 필수입니다.");
        }
        if (command.getCkptFile() == null || command.getCkptFile().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "ckptFile은 필수입니다.");
        }
        if (command.getConfigFile() == null || command.getConfigFile().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "configFile은 필수입니다.");
        }
        validateThreshold(command.getThresholdDefault());
    }

    private void validateThreshold(BigDecimal thresholdDefault) {
        if (thresholdDefault == null) {
            return;
        }
        if (thresholdDefault.compareTo(BigDecimal.ZERO) < 0 || thresholdDefault.compareTo(BigDecimal.ONE) > 0) {
            throw new BusinessException(ErrorCode.MODEL_VALIDATION_FAILED, "thresholdDefault는 0~1 범위여야 합니다.");
        }
    }

    private void validateDeployment(Long organizationId, Long targetId, DeploymentScope scope) {
        if (organizationId == null) {
            throw new BusinessException(ErrorCode.MODEL_VALIDATION_FAILED, "organizationId는 필수입니다.");
        }
        findOrganizationByIdPort.findById(organizationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORGANIZATION_NOT_FOUND));
        if (scope == DeploymentScope.TARGET) {
            if (targetId == null) {
                throw new BusinessException(ErrorCode.MODEL_VALIDATION_FAILED, "TARGET scope에서는 targetId가 필수입니다.");
            }
            AnalysisTarget target = loadAnalysisTargetPort.findById(targetId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.TARGET_NOT_FOUND));
            if (!organizationId.equals(target.getOrganizationId())) {
                throw new BusinessException(ErrorCode.MODEL_VALIDATION_FAILED, "targetId가 organization 소속과 일치하지 않습니다.");
            }
        }
    }

    private ModelArtifactJpaEntity uploadArtifact(Long modelId, Long versionId, MultipartFile file, String fileName, ModelArtifactType artifactType) {
        String objectKey = "models/" + modelId + "/versions/" + versionId + "/" + fileName;
        String checksum = calculateChecksum(file);
        try (InputStream inputStream = file.getInputStream()) {
            minioStorageAdapter.upload(
                    minioProperties.getBucketModels(),
                    objectKey,
                    inputStream,
                    file.getSize(),
                    fileName,
                    file.getContentType(),
                    checksum
            );
        } catch (IOException exception) {
            log.error("Failed to upload model artifact, modelId={}, versionId={}, fileName={}", modelId, versionId, fileName, exception);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "모델 artifact 저장 중 오류가 발생했습니다.");
        }
        StoredFile storedFile = persistUploadedFilePort.save(StoredFile.builder()
                .storageType(StorageType.MINIO)
                .bucketName(minioProperties.getBucketModels())
                .objectKey(objectKey)
                .filePath(null)
                .fileName(fileName)
                .fileExt(extension(fileName))
                .mimeType(file.getContentType())
                .fileSize(file.getSize())
                .checksum(checksum)
                .createdAt(LocalDateTime.now())
                .createdBy(securityUtils.getCurrentUserId())
                .build());
        return ModelArtifactJpaEntity.builder()
                .modelVersionId(versionId)
                .fileId(storedFile.getFileId())
                .artifactType(artifactType)
                .checksum(checksum)
                .build();
    }

    private String calculateChecksum(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (IOException | NoSuchAlgorithmException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "파일 체크섬 계산에 실패했습니다.");
        }
    }

    private ModelDetailResponse toModelDetail(ModelJpaEntity entity) {
        return ModelDetailResponse.builder()
                .modelId(entity.getModelId())
                .modelName(entity.getModelName())
                .modelType(entity.getModelType())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private ModelVersionDetailResponse buildVersionDetail(ModelVersionJpaEntity version, String modelName) {
        return ModelVersionDetailResponse.builder()
                .version(ModelVersionSummaryResponse.builder()
                        .modelVersionId(version.getModelVersionId())
                        .modelId(version.getModelId())
                        .modelName(modelName)
                        .versionName(version.getVersionName())
                        .modelCategory(version.getModelCategory().name())
                        .modelProfile(version.getModelProfile().name())
                        .framework(version.getFramework())
                        .inputSize(version.getInputSize())
                        .thresholdDefault(version.getThresholdDefault())
                        .accuracy(version.getAccuracy())
                        .precisionScore(version.getPrecisionScore())
                        .recallScore(version.getRecallScore())
                        .f1Score(version.getF1Score())
                        .aurocScore(version.getAurocScore())
                        .deployStatus(version.getDeployStatus().name())
                        .isActive(version.getIsActive())
                        .validatedAt(version.getValidatedAt())
                        .validatedBy(version.getValidatedBy())
                        .createdAt(version.getCreatedAt())
                        .build())
                .artifacts(modelManagementPort.findArtifactResponsesByVersionId(version.getModelVersionId()))
                .build();
    }

    private ModelDeploymentResponse toDeploymentResponse(ModelDeploymentJpaEntity deployment) {
        ModelVersionJpaEntity version = loadVersion(deployment.getModelVersionId());
        ModelJpaEntity model = loadModel(version.getModelId());
        return ModelDeploymentResponse.builder()
                .deploymentId(deployment.getDeploymentId())
                .organizationId(deployment.getOrganizationId())
                .targetId(deployment.getTargetId())
                .modelVersionId(deployment.getModelVersionId())
                .modelId(model.getModelId())
                .modelName(model.getModelName())
                .versionName(version.getVersionName())
                .deploymentScope(deployment.getDeploymentScope().name())
                .deployStatus(deployment.getDeployStatus().name())
                .isActive(deployment.getIsActive())
                .deployedAt(deployment.getDeployedAt())
                .deployedBy(deployment.getDeployedBy())
                .rollbackFromDeploymentId(deployment.getRollbackFromDeploymentId())
                .reason(deployment.getReason())
                .build();
    }

    private boolean sameScope(ModelDeploymentJpaEntity left, ModelDeploymentJpaEntity right) {
        return left.getOrganizationId().equals(right.getOrganizationId())
                && equalsNullable(left.getTargetId(), right.getTargetId())
                && left.getDeploymentScope() == right.getDeploymentScope();
    }

    private boolean equalsNullable(Long left, Long right) {
        return left == null ? right == null : left.equals(right);
    }

    private ModelJpaEntity loadModel(Long modelId) {
        return modelManagementPort.findModelById(modelId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MODEL_NOT_FOUND));
    }

    private ModelVersionJpaEntity loadVersion(Long versionId) {
        return modelManagementPort.findModelVersionById(versionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MODEL_VERSION_NOT_FOUND));
    }

    private ModelDeploymentJpaEntity loadDeployment(Long deploymentId) {
        return modelManagementPort.findDeploymentById(deploymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MODEL_DEPLOYMENT_NOT_FOUND));
    }

    private void recordAction(String actionType, Long targetId, String targetType, String reason) {
        try {
            recordAdminActionLogUseCase.recordAdminActionLog(RecordAdminActionLogCommand.builder()
                    .actorUserId(securityUtils.getCurrentUserId())
                    .actionType(actionType)
                    .targetType(targetType)
                    .targetId(targetId)
                    .reason(reason)
                    .build());
        } catch (Exception exception) {
            log.warn("Failed to record model admin action, actionType={}, targetId={}", actionType, targetId, exception);
        }
    }

    private String required(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.MODEL_VALIDATION_FAILED, fieldName + "는 필수입니다.");
        }
        return value.trim();
    }

    private String blankToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private String extension(String fileName) {
        if (fileName == null) {
            return null;
        }
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            return null;
        }
        return fileName.substring(index + 1).toLowerCase(Locale.ROOT);
    }
}
