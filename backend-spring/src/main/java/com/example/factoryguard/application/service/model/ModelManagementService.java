package com.example.factoryguard.application.service.model;

import com.example.factoryguard.adapter.out.persistence.model.ModelArtifactJpaEntity;
import com.example.factoryguard.adapter.out.persistence.model.ModelDeploymentJpaEntity;
import com.example.factoryguard.adapter.out.persistence.model.ModelJpaEntity;
import com.example.factoryguard.adapter.out.persistence.model.ModelVersionJpaEntity;
import com.example.factoryguard.adapter.out.storage.minio.MinioProperties;
import com.example.factoryguard.adapter.out.storage.minio.MinioStorageAdapter;
import com.example.factoryguard.application.dto.ai.GenerateMemoryBankCommand;
import com.example.factoryguard.application.dto.ai.GenerateMemoryBankResult;
import com.example.factoryguard.application.dto.model.CreateModelCommand;
import com.example.factoryguard.application.dto.model.CreatedModelVersionResponse;
import com.example.factoryguard.application.dto.model.ActivateModelDeploymentCommand;
import com.example.factoryguard.application.dto.model.DeactivateModelDeploymentCommand;
import com.example.factoryguard.application.dto.model.DeployModelVersionCommand;
import com.example.factoryguard.application.dto.model.GenerateModelVersionFromNormalImagesCommand;
import com.example.factoryguard.application.dto.model.GenerateModelVersionsFromNormalImagesResponse;
import com.example.factoryguard.application.dto.model.ListModelDeploymentsQuery;
import com.example.factoryguard.application.dto.model.ListModelVersionsQuery;
import com.example.factoryguard.application.dto.model.ListModelsQuery;
import com.example.factoryguard.application.dto.model.ModelArtifactResponse;
import com.example.factoryguard.application.dto.model.ModelDeploymentResponse;
import com.example.factoryguard.application.dto.model.ModelDetailResponse;
import com.example.factoryguard.application.dto.model.ModelPageResponse;
import com.example.factoryguard.application.dto.model.ModelSummaryResponse;
import com.example.factoryguard.application.dto.model.ModelVersionDetailResponse;
import com.example.factoryguard.application.dto.model.ModelVersionStatusCommand;
import com.example.factoryguard.application.dto.model.ModelVersionSummaryResponse;
import com.example.factoryguard.application.dto.model.RollbackModelDeploymentCommand;
import com.example.factoryguard.application.dto.model.UploadModelVersionCommand;
import com.example.factoryguard.application.exception.ai.AiInvalidRequestException;
import com.example.factoryguard.application.exception.ai.AiServerException;
import com.example.factoryguard.application.port.in.model.ActivateModelVersionUseCase;
import com.example.factoryguard.application.dto.operation.RecordAdminActionLogCommand;
import com.example.factoryguard.application.port.in.model.CreateModelUseCase;
import com.example.factoryguard.application.port.in.model.ActivateModelDeploymentUseCase;
import com.example.factoryguard.application.port.in.model.DeactivateModelDeploymentUseCase;
import com.example.factoryguard.application.port.in.model.DeleteModelDeploymentUseCase;
import com.example.factoryguard.application.port.in.model.DeleteModelVersionUseCase;
import com.example.factoryguard.application.port.in.model.DeprecateModelVersionUseCase;
import com.example.factoryguard.application.port.in.model.DeployModelVersionUseCase;
import com.example.factoryguard.application.port.in.model.GenerateModelVersionFromNormalImagesUseCase;
import com.example.factoryguard.application.port.in.model.GetModelUseCase;
import com.example.factoryguard.application.port.in.model.GetModelVersionUseCase;
import com.example.factoryguard.application.port.in.model.ListModelArtifactsUseCase;
import com.example.factoryguard.application.port.in.model.ListModelDeploymentsUseCase;
import com.example.factoryguard.application.port.in.model.ListModelVersionsUseCase;
import com.example.factoryguard.application.port.in.model.ListModelsUseCase;
import com.example.factoryguard.application.port.in.model.RollbackModelDeploymentUseCase;
import com.example.factoryguard.application.port.in.model.UploadModelVersionUseCase;
import com.example.factoryguard.application.port.in.operation.RecordAdminActionLogUseCase;
import com.example.factoryguard.application.port.out.ai.GenerateMemoryBankPort;
import com.example.factoryguard.application.port.out.file.PersistUploadedFilePort;
import com.example.factoryguard.application.port.out.inspection.LoadAnalysisTargetPort;
import com.example.factoryguard.application.port.out.model.ModelManagementPort;
import com.example.factoryguard.application.port.out.organization.FindOrganizationByIdPort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.config.model.ModelMemoryBankProperties;
import com.example.factoryguard.config.security.SecurityUtils;
import com.example.factoryguard.domain.file.model.StoredFile;
import com.example.factoryguard.domain.file.vo.StorageType;
import com.example.factoryguard.domain.inspection.model.AnalysisTarget;
import com.example.factoryguard.domain.model.vo.DeploymentScope;
import com.example.factoryguard.domain.model.vo.DeploymentStatus;
import com.example.factoryguard.domain.model.vo.ModelArtifactType;
import com.example.factoryguard.domain.model.vo.ModelCategory;
import com.example.factoryguard.domain.model.vo.ModelDeployStatus;
import com.example.factoryguard.domain.model.vo.ModelProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

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
        ActivateModelDeploymentUseCase,
        DeactivateModelDeploymentUseCase,
        DeleteModelDeploymentUseCase,
        RollbackModelDeploymentUseCase,
        DeleteModelVersionUseCase,
        GenerateModelVersionFromNormalImagesUseCase {

    private static final Set<String> NORMAL_IMAGE_MIME_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final ModelManagementPort modelManagementPort;
    private final GenerateMemoryBankPort generateMemoryBankPort;
    private final PersistUploadedFilePort persistUploadedFilePort;
    private final MinioStorageAdapter minioStorageAdapter;
    private final MinioProperties minioProperties;
    private final ModelMemoryBankProperties modelMemoryBankProperties;
    private final SecurityUtils securityUtils;
    private final FindOrganizationByIdPort findOrganizationByIdPort;
    private final LoadAnalysisTargetPort loadAnalysisTargetPort;
    private final RecordAdminActionLogUseCase recordAdminActionLogUseCase;
    private final TransactionTemplate transactionTemplate;

    @Override
    public GenerateModelVersionsFromNormalImagesResponse generateFromNormalImages(GenerateModelVersionFromNormalImagesCommand command) {
        String failedStep = "CONTROLLER_ENTRY";
        ModelJpaEntity model = loadModel(command.getModelId());
        failedStep = "REQUEST_VALIDATION";
        validateGenerateCommand(command);

        String requestId = blankToNull(command.getRequestId());
        if (requestId == null) {
            requestId = blankToNull(MDC.get("requestId"));
        }
        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
        }

        List<GeneratedMemoryBank> generatedMemoryBanks = new ArrayList<>();
        failedStep = "NORMAL_IMAGES_RECEIVED";
        List<StoredFile> normalImageFiles = uploadNormalImages(command, requestId);
        List<ModelProfile> profiles = resolveProfiles(command.getModelProfile());
        log.info("model_generation_started requestId={} modelId={} organizationId={} targetId={} category={} profiles={}",
                requestId, command.getModelId(), command.getOrganizationId(), command.getTargetId(), command.getModelCategory(), profiles);

        try {
            for (ModelProfile profile : profiles) {
                failedStep = "POLICY_RESOLUTION";
                FixedProfile fixedProfile = resolveFixedProfile(command.getModelCategory(), profile);
                VisionModelProfilePolicy.Spec policy = resolvePolicy(command.getModelCategory(), profile);
                log.info("model_generation_policy_resolved requestId={} profile={} inputSize={} imageThreshold={} pixelThreshold={} targetMemoryBankSize={} shotPolicy={}",
                        requestId, profile, policy.inputSize(), policy.imageThreshold(), policy.pixelThreshold(), policy.targetMemoryBankSize(), policy.shotPolicy());
                String outputPrefix = buildOutputPrefix(command.getOrganizationId(), command.getTargetId(), command.getDeploymentScope(), profile, command.getModelCategory());
                failedStep = "MEMORY_BANK_GENERATION";
                GenerateMemoryBankResult memoryBankResult = callMemoryBank(command, normalImageFiles, outputPrefix, profile, fixedProfile, requestId);
                log.info("memory_bank_response_received requestId={} profile={} memoryBankFileKey={} configFileKey={}",
                        requestId, profile, memoryBankResult.getMemoryBankFileKey(), memoryBankResult.getConfigFileKey());
                generatedMemoryBanks.add(new GeneratedMemoryBank(profile, fixedProfile, memoryBankResult));
            }

            final String finalRequestId = requestId;
            failedStep = "PERSIST_MODEL_ENTITIES";
            List<CreatedModelVersionResponse> createdVersions = transactionTemplate.execute(status -> {
                List<CreatedModelVersionResponse> responses = new ArrayList<>();
                for (GeneratedMemoryBank generated : generatedMemoryBanks) {
                    responses.add(saveGeneratedModelVersion(model, command, generated, normalImageFiles, finalRequestId, profiles.size()));
                }
                return responses;
            });

            log.info("model_generation_completed requestId={} modelId={} createdVersionCount={}",
                    requestId, model.getModelId(), createdVersions == null ? 0 : createdVersions.size());

            return GenerateModelVersionsFromNormalImagesResponse.builder()
                    .modelId(model.getModelId())
                    .modelCategory(command.getModelCategory().name())
                    .normalImageCount(normalImageFiles.size())
                    .createdVersions(createdVersions == null ? List.of() : createdVersions)
                    .build();
        } catch (Exception exception) {
            cleanupGeneratedObjects(normalImageFiles, generatedMemoryBanks, requestId);
            log.error("model_generation_failed requestId={} modelId={} failedStep={} error={}",
                    requestId, command.getModelId(), failedStep, exception.getMessage(), exception);
            if (exception instanceof BusinessException businessException) {
                if (businessException.getErrorCode() == ErrorCode.MODEL_GENERATION_FAILED) {
                    throw businessException;
                }
                Map<String, Object> details = new LinkedHashMap<>();
                if (businessException.getDetails() != null) {
                    details.putAll(businessException.getDetails());
                }
                details.put("failedStep", failedStep);
                details.put("requestId", requestId);
                throw new BusinessException(ErrorCode.MODEL_GENERATION_FAILED, businessException.getMessage(), details);
            }
            Map<String, Object> details = new LinkedHashMap<>();
            details.put("failedStep", failedStep);
            details.put("requestId", requestId);
            throw new BusinessException(ErrorCode.MODEL_GENERATION_FAILED, "모델 생성 중 실패했습니다: " + failedStep, details);
        }
    }

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
            throw new BusinessException(ErrorCode.MODEL_CONFLICT, "媛숈? modelName + modelType 議고빀???대? 議댁옱?⑸땲??");
        }
        ModelJpaEntity saved = modelManagementPort.saveModel(
                ModelJpaEntity.builder()
                        .modelName(modelName)
                        .modelType(modelType)
                        .description(blankToNull(command.getDescription()))
                        .build()
        );
        recordAction("MODEL_CREATED", saved.getModelId(), "MODEL", "紐⑤뜽 ?깅줉");
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
            throw new BusinessException(ErrorCode.MODEL_VERSION_CONFLICT, "媛숈? 紐⑤뜽???숈씪??versionName???대? 議댁옱?⑸땲??");
        }

        ModelVersionJpaEntity version = modelManagementPort.saveModelVersion(
                ModelVersionJpaEntity.builder()
                        .modelId(model.getModelId())
                        .versionName(command.getVersionName().trim())
                        .modelCategory(command.getModelCategory())
                        .modelProfile(command.getModelProfile())
                        .framework(blankToNull(command.getFramework()))
                        .inputSize(resolveInputSize(command))
                        .thresholdDefault(resolveThresholdDefault(command.getModelCategory(), command.getModelProfile(), command.getThresholdDefault()))
                        .accuracy(command.getAccuracy())
                        .precisionScore(command.getPrecisionScore())
                        .recallScore(command.getRecallScore())
                        .f1Score(command.getF1Score())
                        .aurocScore(command.getAurocScore())
                        .deployStatus(ModelDeployStatus.REGISTERED)
                        .isActive(false)
                        .build()
        );

        List<ModelArtifactJpaEntity> artifacts = new ArrayList<>();
        artifacts.add(uploadArtifact(model.getModelId(), version.getModelVersionId(), command.getCkptFile(), "model.ckpt", ModelArtifactType.CKPT));
        artifacts.add(uploadArtifact(model.getModelId(), version.getModelVersionId(), command.getConfigFile(), "config.json", ModelArtifactType.CONFIG));
        artifacts.add(uploadArtifact(
                model.getModelId(),
                version.getModelVersionId(),
                command.getMemoryBankFile(),
                buildMemoryBankFileName(command.getMemoryBankFile()),
                ModelArtifactType.MEMORY_BANK
        ));
        if (command.getLabelsFile() != null && !command.getLabelsFile().isEmpty()) {
            String labelsExt = extension(command.getLabelsFile().getOriginalFilename());
            String labelsName = labelsExt == null ? "labels" : "labels." + labelsExt;
            artifacts.add(uploadArtifact(model.getModelId(), version.getModelVersionId(), command.getLabelsFile(), labelsName, ModelArtifactType.LABELS));
        }
        modelManagementPort.saveArtifacts(artifacts);
        recordAction("MODEL_VERSION_UPLOADED", version.getModelVersionId(), "MODEL_VERSION", "model version uploaded");
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
                || !modelManagementPort.hasArtifact(version.getModelVersionId(), ModelArtifactType.CONFIG)
                || !modelManagementPort.hasArtifact(version.getModelVersionId(), ModelArtifactType.MEMORY_BANK)) {
            throw new BusinessException(ErrorCode.MODEL_VALIDATION_FAILED, "모델 버전 활성화에는 CKPT, CONFIG, MEMORY_BANK 산출물이 필요합니다.");
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
            throw new BusinessException(ErrorCode.MODEL_DEPLOYMENT_CONFLICT, "?쒖꽦 諛고룷媛 ?⑥븘 ?덈뒗 紐⑤뜽 踰꾩쟾? ?ъ슜 以묐떒?????놁뒿?덈떎.");
        }
        version.deprecate();
        ModelVersionJpaEntity saved = modelManagementPort.saveModelVersion(version);
        recordAction("MODEL_VERSION_DEPRECATED", saved.getModelVersionId(), "MODEL_VERSION", blankToNull(command.getReason()));
        return buildVersionDetail(saved, loadModel(saved.getModelId()).getModelName());
    }

    @Override
    @Transactional
    public ModelVersionDetailResponse deleteModelVersion(ModelVersionStatusCommand command) {
        ModelVersionJpaEntity version = loadVersion(command.getVersionId());
        boolean hasHistory = modelManagementPort.existsInspectionResultByModelVersionId(version.getModelVersionId());
        List<ModelDeploymentJpaEntity> deployments = modelManagementPort.findDeploymentsByVersionId(version.getModelVersionId());
        String deleteReason = blankToNull(command.getReason());
        Long actorUserId = securityUtils.getCurrentUserId();
        for (ModelDeploymentJpaEntity deployment : deployments) {
            deployment.softDelete(actorUserId, deleteReason);
            modelManagementPort.saveDeployment(deployment);
        }
        version.softDelete(actorUserId, deleteReason);
        ModelVersionJpaEntity saved = modelManagementPort.saveModelVersion(version);
        recordAction(
                hasHistory ? "MODEL_VERSION_SOFT_DELETED_WITH_HISTORY" : "MODEL_VERSION_SOFT_DELETED",
                saved.getModelVersionId(),
                "MODEL_VERSION",
                deleteReason
        );
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
            throw new BusinessException(ErrorCode.MODEL_DEPLOYMENT_CONFLICT, "?쒖꽦?붾릺吏 ?딆븯嫄곕굹 ?ъ슜 以묐떒??紐⑤뜽 踰꾩쟾? 諛고룷?????놁뒿?덈떎.");
        }
        validateDeployment(command.getOrganizationId(), command.getTargetId(), command.getDeploymentScope());
        ModelDeploymentJpaEntity deployment = modelManagementPort.saveDeployment(
                ModelDeploymentJpaEntity.builder()
                        .organizationId(command.getOrganizationId())
                        .targetId(command.getDeploymentScope() == DeploymentScope.TARGET ? command.getTargetId() : null)
                        .modelVersionId(version.getModelVersionId())
                        .deploymentScope(command.getDeploymentScope())
                        .deployStatus(DeploymentStatus.DEACTIVATED)
                        .isActive(false)
                        .deployedAt(LocalDateTime.now())
                        .deployedBy(securityUtils.getCurrentUserId())
                        .reason(blankToNull(command.getReason()))
                        .rollbackFlag(false)
                        .build()
        );
        deactivateActiveDeploymentsInSameSlotExcluding(
                deployment,
                version.getModelCategory(),
                version.getModelProfile()
        );
        deployment.activate(securityUtils.getCurrentUserId(), blankToNull(command.getReason()));
        deployment = modelManagementPort.saveDeployment(deployment);
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
    public ModelDeploymentResponse activateModelDeployment(ActivateModelDeploymentCommand command) {
        ModelDeploymentJpaEntity deployment = loadDeployment(command.getDeploymentId());
        if (deployment.getDeletedAt() != null) {
            throw new BusinessException(
                    ErrorCode.MODEL_DEPLOYMENT_DELETED,
                    "삭제 처리된 모델 배포는 다시 활성화할 수 없습니다.",
                    Map.of("failedStep", "ACTIVATE_DELETED_DEPLOYMENT")
            );
        }
        ModelVersionJpaEntity version = loadVersion(deployment.getModelVersionId());
        if (version.getDeployStatus() == ModelDeployStatus.DEPRECATED || version.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.MODEL_DEPLOYMENT_CONFLICT, "사용 중단된 모델 버전 배포는 활성화할 수 없습니다.");
        }
        deactivateActiveDeploymentsInSameSlotExcluding(
                deployment,
                version.getModelCategory(),
                version.getModelProfile()
        );
        deployment.activate(securityUtils.getCurrentUserId(), blankToNull(command.getReason()));
        ModelDeploymentJpaEntity saved = modelManagementPort.saveDeployment(deployment);
        recordAction("MODEL_DEPLOYMENT_ACTIVATED", saved.getDeploymentId(), "MODEL_DEPLOYMENT", blankToNull(command.getReason()));
        return toDeploymentResponse(saved);
    }

    @Override
    @Transactional
    public ModelDeploymentResponse deleteModelDeployment(DeactivateModelDeploymentCommand command) {
        ModelDeploymentJpaEntity deployment = loadDeployment(command.getDeploymentId());
        deployment.softDelete(securityUtils.getCurrentUserId(), blankToNull(command.getReason()));
        ModelDeploymentJpaEntity saved = modelManagementPort.saveDeployment(deployment);
        recordAction("MODEL_DEPLOYMENT_DELETED", saved.getDeploymentId(), "MODEL_DEPLOYMENT", blankToNull(command.getReason()));
        return toDeploymentResponse(saved);
    }

    @Override
    @Transactional
    public ModelDeploymentResponse rollbackModelDeployment(RollbackModelDeploymentCommand command) {
        ModelDeploymentJpaEntity current = loadDeployment(command.getDeploymentId());
        ModelDeploymentJpaEntity rollbackTarget = loadDeployment(command.getRollbackToDeploymentId());
        if (!sameScope(current, rollbackTarget)) {
            throw new BusinessException(ErrorCode.MODEL_VALIDATION_FAILED, "濡ㅻ갚 ??곸? 媛숈? organization/target/scope?ъ빞 ?⑸땲??");
        }
        ModelVersionJpaEntity rollbackVersion = loadVersion(rollbackTarget.getModelVersionId());
        if (rollbackVersion.getDeployStatus() == ModelDeployStatus.DEPRECATED) {
            throw new BusinessException(ErrorCode.MODEL_DEPLOYMENT_CONFLICT, "?ъ슜 以묐떒??踰꾩쟾?쇰줈??濡ㅻ갚?????놁뒿?덈떎.");
        }
        ModelDeploymentJpaEntity restored = modelManagementPort.saveDeployment(
                ModelDeploymentJpaEntity.builder()
                        .organizationId(current.getOrganizationId())
                        .targetId(current.getTargetId())
                        .modelVersionId(rollbackTarget.getModelVersionId())
                        .deploymentScope(current.getDeploymentScope())
                        .deployStatus(DeploymentStatus.DEACTIVATED)
                        .isActive(false)
                        .deployedAt(LocalDateTime.now())
                        .deployedBy(securityUtils.getCurrentUserId())
                        .rollbackFromDeploymentId(current.getDeploymentId())
                        .reason(blankToNull(command.getReason()))
                        .rollbackFlag(true)
                        .build()
        );
        deactivateActiveDeploymentsInSameSlotExcluding(
                restored,
                rollbackVersion.getModelCategory(),
                rollbackVersion.getModelProfile()
        );
        restored.activate(securityUtils.getCurrentUserId(), blankToNull(command.getReason()));
        restored = modelManagementPort.saveDeployment(restored);
        recordAction("MODEL_ROLLED_BACK", restored.getDeploymentId(), "MODEL_DEPLOYMENT", blankToNull(command.getReason()));
        return toDeploymentResponse(restored);
    }

    private void validateGenerateCommand(GenerateModelVersionFromNormalImagesCommand command) {
        if (command.getModelCategory() == null) {
            throw new BusinessException(ErrorCode.MODEL_VALIDATION_FAILED, "modelCategory???꾩닔?낅땲??");
        }
        if (command.getDeploymentScope() == null) {
            throw new BusinessException(ErrorCode.MODEL_VALIDATION_FAILED, "deploymentScope???꾩닔?낅땲??");
        }
        validateDeployment(command.getOrganizationId(), command.getTargetId(), command.getDeploymentScope());
        validateThreshold(command.getThresholdDefault());
        List<MultipartFile> normalImages = command.getNormalImages();
        if (normalImages == null || normalImages.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "normalImages???꾩닔?낅땲??");
        }
        int minCount = modelMemoryBankProperties.getMemoryBank().getMinNormalImageCount();
        if (normalImages.size() < minCount) {
            throw new BusinessException(ErrorCode.MODEL_VALIDATION_FAILED, "?뺤긽 ?대?吏 媛쒖닔媛 理쒖냼 湲곗?蹂대떎 ?곸뒿?덈떎.");
        }
        for (MultipartFile image : normalImages) {
            if (image == null || image.isEmpty()) {
                throw new BusinessException(ErrorCode.INVALID_FILE_EMPTY);
            }
            String contentType = image.getContentType();
            if (contentType == null || !NORMAL_IMAGE_MIME_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
                throw new BusinessException(ErrorCode.INVALID_FILE_MIME);
            }
            validateReadableImage(image);
        }
    }

    private void validateReadableImage(MultipartFile image) {
        try (InputStream inputStream = image.getInputStream()) {
            BufferedImage decoded = ImageIO.read(inputStream);
            if (decoded == null || decoded.getWidth() <= 0 || decoded.getHeight() <= 0) {
                throw new BusinessException(ErrorCode.MODEL_VALIDATION_FAILED, "?먯긽?섏뿀嫄곕굹 ?쎌쓣 ???녿뒗 ?뺤긽 ?대?吏媛 ?ы븿?섏뼱 ?덉뒿?덈떎.");
            }
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.MODEL_VALIDATION_FAILED, "?먯긽?섏뿀嫄곕굹 ?쎌쓣 ???녿뒗 ?뺤긽 ?대?吏媛 ?ы븿?섏뼱 ?덉뒿?덈떎.");
        }
    }

    private List<StoredFile> uploadNormalImages(GenerateModelVersionFromNormalImagesCommand command, String requestId) {
        List<StoredFile> uploaded = new ArrayList<>();
        for (MultipartFile image : command.getNormalImages()) {
            String fileName = buildUniqueFileName(image.getOriginalFilename(), "normal");
            String objectKey = buildNormalImageObjectKey(command.getOrganizationId(), command.getTargetId(), command.getDeploymentScope(), requestId, fileName);
            String checksum = calculateChecksum(image);
            try (InputStream inputStream = image.getInputStream()) {
                minioStorageAdapter.upload(
                        minioProperties.getBucketModels(),
                        objectKey,
                        inputStream,
                        image.getSize(),
                        fileName,
                        image.getContentType(),
                        checksum
                );
            } catch (IOException | RuntimeException exception) {
                log.error("Failed to upload normal image, organizationId={}, targetId={}, requestId={}",
                        command.getOrganizationId(), command.getTargetId(), requestId, exception);
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "?뺤긽 ?대?吏 ???以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎.");
            }
            StoredFile storedFile = StoredFile.builder()
                    .storageType(StorageType.MINIO)
                    .bucketName(minioProperties.getBucketModels())
                    .objectKey(objectKey)
                    .filePath(null)
                    .fileName(fileName)
                    .fileExt(extension(fileName))
                    .mimeType(image.getContentType())
                    .fileSize(image.getSize())
                    .checksum(checksum)
                    .createdAt(LocalDateTime.now())
                    .createdBy(securityUtils.getCurrentUserId())
                    .build();
            uploaded.add(transactionTemplate.execute(status -> persistUploadedFilePort.save(storedFile)));
        }
        return uploaded;
    }

    private List<ModelProfile> resolveProfiles(ModelProfile requestedProfile) {
        if (requestedProfile != null) {
            return List.of(requestedProfile);
        }
        return List.of(ModelProfile.SPEED, ModelProfile.PERFORMANCE);
    }

    private FixedProfile resolveFixedProfile(ModelCategory category, ModelProfile profile) {
        String profileKey = profile.name().toLowerCase(Locale.ROOT) + "-" + category.name().toLowerCase(Locale.ROOT);
        ModelMemoryBankProperties.FixedProfile configured = modelMemoryBankProperties.getFixedProfiles().get(profileKey);
        if (configured == null) {
            throw new BusinessException(ErrorCode.BASE_MODEL_PROFILE_NOT_FOUND,
                    "Base model profile mapping is missing: " + profileKey);
        }
        String ckptFileKey = blankToNull(configured.getCkptFileKey());
        String configFileKey = blankToNull(configured.getConfigFileKey());
        if (ckptFileKey == null || configFileKey == null) {
            throw new BusinessException(ErrorCode.BASE_MODEL_PROFILE_NOT_FOUND,
                    "Base model profile mapping is incomplete: " + profileKey);
        }
        assertBaseArtifactExists(ckptFileKey);
        assertBaseArtifactExists(configFileKey);
        return new FixedProfile(ckptFileKey, configFileKey);
    }

    private void assertBaseArtifactExists(String objectKey) {
        if (!minioStorageAdapter.objectExists(minioProperties.getBucketModels(), objectKey)) {
            throw new BusinessException(ErrorCode.BASE_MODEL_PROFILE_NOT_FOUND,
                    "Base model artifact is missing in MinIO: " + objectKey
                            + ". Run ai-server/scripts/seed_base_model_artifacts.py after git lfs pull.");
        }
    }

    private GenerateMemoryBankResult callMemoryBank(GenerateModelVersionFromNormalImagesCommand command,
                                                    List<StoredFile> normalImageFiles,
                                                    String outputPrefix,
                                                    ModelProfile profile,
                                                    FixedProfile fixedProfile,
                                                    String requestId) {
        VisionModelProfilePolicy.Spec policy = resolvePolicy(command.getModelCategory(), profile);
        try {
            log.info("Requesting memory_bank generation, requestId={}, modelId={}, organizationId={}, targetId={}, modelCategory={}, modelProfile={}, normalImageCount={}, ckptFileKey={}, configFileKey={}, outputPrefix={}",
                    requestId, command.getModelId(), command.getOrganizationId(), command.getTargetId(),
                    command.getModelCategory(), profile, normalImageFiles.size(), fixedProfile.ckptFileKey(), fixedProfile.configFileKey(), outputPrefix);
            return generateMemoryBankPort.generateMemoryBank(GenerateMemoryBankCommand.builder()
                    .requestId(requestId)
                    .modelId(command.getModelId())
                    .organizationId(command.getOrganizationId())
                    .targetId(command.getTargetId())
                    .deploymentScope(command.getDeploymentScope().name())
                    .modelCategory(command.getModelCategory())
                    .modelProfile(profile)
                    .ckptFileKey(fixedProfile.ckptFileKey())
                    .configFileKey(fixedProfile.configFileKey())
                    .normalImageFileKeys(normalImageFiles.stream().map(StoredFile::getObjectKey).toList())
                    .outputPrefix(outputPrefix)
                    .inputSize(policy.inputSize())
                    .targetMemoryBankSize(policy.targetMemoryBankSize())
                    .shotPolicy(policy.shotPolicy())
                    .imageThreshold(policy.imageThreshold())
                    .pixelThreshold(policy.pixelThreshold())
                    .build());
        } catch (TimeoutException exception) {
            log.error("memory_bank generation timed out, requestId={}, modelId={}, modelProfile={}",
                    requestId, command.getModelId(), profile, exception);
            throw new BusinessException(ErrorCode.AI_TIMEOUT, "AI 서버 memory_bank 생성 시간이 초과되었습니다.");
        } catch (AiInvalidRequestException exception) {
            Map<String, Object> upstream = upstreamDetails(exception.getStatus(), exception.getUpstreamErrorCode(), exception.getUpstreamDetail(), exception.getRequestId());
            log.error("memory_bank generation rejected by FastAPI, requestId={}, modelId={}, modelProfile={}, upstreamStatus={}, upstreamErrorCode={}, upstreamDetail={}, upstreamRequestId={}",
                    requestId, command.getModelId(), profile, exception.getStatus(), exception.getUpstreamErrorCode(), exception.getUpstreamDetail(), exception.getRequestId(), exception);
            throw new BusinessException(ErrorCode.AI_REQUEST_INVALID, "FastAPI memory_bank request failed.", upstream);
        } catch (AiServerException exception) {
            Map<String, Object> upstream = upstreamDetails(exception.getStatus(), exception.getUpstreamErrorCode(), exception.getUpstreamDetail(), exception.getRequestId());
            log.error("memory_bank generation failed in FastAPI, requestId={}, modelId={}, modelProfile={}, upstreamStatus={}, upstreamErrorCode={}, upstreamDetail={}, upstreamRequestId={}",
                    requestId, command.getModelId(), profile, exception.getStatus(), exception.getUpstreamErrorCode(), exception.getUpstreamDetail(), exception.getRequestId(), exception);
            throw new BusinessException(ErrorCode.AI_SERVER_ERROR, "FastAPI memory_bank generation failed.", upstream);
        } catch (RuntimeException exception) {
            log.error("memory_bank generation failed, requestId={}, modelId={}, modelProfile={}",
                    requestId, command.getModelId(), profile, exception);
            throw new BusinessException(ErrorCode.AI_SERVER_ERROR, "AI ?쒕쾭 memory_bank ?앹꽦???ㅽ뙣?덉뒿?덈떎.");
        }
    }

    private Map<String, Object> upstreamDetails(int upstreamStatus, String upstreamErrorCode, String upstreamDetail, String upstreamRequestId) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("upstreamStatus", upstreamStatus);
        details.put("upstreamErrorCode", upstreamErrorCode);
        details.put("upstreamDetail", upstreamDetail);
        details.put("upstreamRequestId", upstreamRequestId);
        return details;
    }

    private CreatedModelVersionResponse saveGeneratedModelVersion(ModelJpaEntity model,
                                                                  GenerateModelVersionFromNormalImagesCommand command,
                                                                  GeneratedMemoryBank generated,
                                                                  List<StoredFile> normalImageFiles,
                                                                  String requestId,
                                                                  int profileCount) {
        String versionName = resolveVersionName(command, generated.profile(), profileCount);
        if (modelManagementPort.existsVersionByModelIdAndVersionName(model.getModelId(), versionName)) {
            throw new BusinessException(ErrorCode.MODEL_VERSION_CONFLICT, "?숈씪??modelId/versionName 議고빀???대? 議댁옱?⑸땲??");
        }
        StoredFile memoryBankFile = persistUploadedFilePort.save(StoredFile.builder()
                .storageType(StorageType.MINIO)
                .bucketName(minioProperties.getBucketModels())
                .objectKey(generated.result().getMemoryBankFileKey())
                .filePath(null)
                .fileName(fileNameFromObjectKey(generated.result().getMemoryBankFileKey()))
                .fileExt(extension(generated.result().getMemoryBankFileKey()))
                .mimeType("application/octet-stream")
                .fileSize(0L)
                .checksum(null)
                .createdAt(LocalDateTime.now())
                .createdBy(securityUtils.getCurrentUserId())
                .build());
        StoredFile configFile = persistUploadedFilePort.save(StoredFile.builder()
                .storageType(StorageType.MINIO)
                .bucketName(minioProperties.getBucketModels())
                .objectKey(generated.result().getConfigFileKey())
                .filePath(null)
                .fileName(fileNameFromObjectKey(generated.result().getConfigFileKey()))
                .fileExt(extension(generated.result().getConfigFileKey()))
                .mimeType("application/json")
                .fileSize(0L)
                .checksum(null)
                .createdAt(LocalDateTime.now())
                .createdBy(securityUtils.getCurrentUserId())
                .build());
        StoredFile ckptFile = persistUploadedFilePort.save(StoredFile.builder()
                .storageType(StorageType.MINIO)
                .bucketName(minioProperties.getBucketModels())
                .objectKey(generated.result().getCkptFileKey() != null ? generated.result().getCkptFileKey() : generated.fixedProfile().ckptFileKey())
                .filePath(null)
                .fileName(fileNameFromObjectKey(generated.result().getCkptFileKey() != null ? generated.result().getCkptFileKey() : generated.fixedProfile().ckptFileKey()))
                .fileExt(extension(generated.result().getCkptFileKey() != null ? generated.result().getCkptFileKey() : generated.fixedProfile().ckptFileKey()))
                .mimeType("application/octet-stream")
                .fileSize(0L)
                .checksum(null)
                .createdAt(LocalDateTime.now())
                .createdBy(securityUtils.getCurrentUserId())
                .build());

        LocalDateTime now = LocalDateTime.now();
        VisionModelProfilePolicy.Spec policy = resolvePolicy(command.getModelCategory(), generated.profile());
        ModelVersionJpaEntity version = modelManagementPort.saveModelVersion(ModelVersionJpaEntity.builder()
                .modelId(model.getModelId())
                .fileId(memoryBankFile.getFileId())
                .versionName(versionName)
                .modelCategory(command.getModelCategory())
                .modelProfile(generated.profile())
                .framework(blankToNull(generated.result().getFramework()))
                .inputSize(policy.inputSize())
                .thresholdDefault(policy.imageThreshold())
                .deployStatus(ModelDeployStatus.DEPLOYED)
                .isActive(true)
                .validatedAt(now)
                .validatedBy(securityUtils.getCurrentUserId())
                .build());

        modelManagementPort.saveArtifacts(List.of(
                artifact(version.getModelVersionId(), ckptFile, ModelArtifactType.CKPT),
                artifact(version.getModelVersionId(), configFile, ModelArtifactType.CONFIG),
                artifact(version.getModelVersionId(), memoryBankFile, ModelArtifactType.MEMORY_BANK)
        ));

        ModelDeploymentJpaEntity deployment = modelManagementPort.saveDeployment(ModelDeploymentJpaEntity.builder()
                .organizationId(command.getOrganizationId())
                .targetId(command.getDeploymentScope() == DeploymentScope.TARGET ? command.getTargetId() : null)
                .modelVersionId(version.getModelVersionId())
                .deploymentScope(command.getDeploymentScope())
                .deployStatus(DeploymentStatus.DEACTIVATED)
                .isActive(false)
                .deployedAt(now)
                .deployedBy(securityUtils.getCurrentUserId())
                .reason(blankToNull(command.getReason()))
                .rollbackFlag(false)
                .build());
        deactivateActiveDeploymentsInSameSlotExcluding(
                deployment,
                version.getModelCategory(),
                version.getModelProfile()
        );
        deployment.activate(securityUtils.getCurrentUserId(), blankToNull(command.getReason()));
        deployment = modelManagementPort.saveDeployment(deployment);

        recordAction("MODEL_MEMORY_BANK_GENERATE", version.getModelVersionId(), "MODEL_VERSION", blankToNull(command.getReason()));
        recordAction("MODEL_DEPLOY", deployment.getDeploymentId(), "MODEL_DEPLOYMENT", blankToNull(command.getReason()));
        log.info("Created model version from normal images, requestId={}, modelVersionId={}, deploymentId={}, memoryBankFileId={}, normalImageCount={}",
                requestId, version.getModelVersionId(), deployment.getDeploymentId(), memoryBankFile.getFileId(), normalImageFiles.size());

        return CreatedModelVersionResponse.builder()
                .modelProfile(generated.profile().name())
                .modelVersionId(version.getModelVersionId())
                .versionName(version.getVersionName())
                .deployStatus(version.getDeployStatus().name())
                .isActive(version.getIsActive())
                .memoryBankFileId(memoryBankFile.getFileId())
                .deploymentId(deployment.getDeploymentId())
                .build();
    }

    private ModelArtifactJpaEntity artifact(Long versionId, StoredFile file, ModelArtifactType artifactType) {
        return ModelArtifactJpaEntity.builder()
                .modelVersionId(versionId)
                .fileId(file.getFileId())
                .artifactType(artifactType)
                .checksum(file.getChecksum())
                .build();
    }

    private void cleanupGeneratedObjects(List<StoredFile> normalImageFiles, List<GeneratedMemoryBank> generatedMemoryBanks, String requestId) {
        for (StoredFile file : normalImageFiles) {
            cleanupObject(file.getObjectKey(), requestId);
        }
        for (GeneratedMemoryBank generated : generatedMemoryBanks) {
            cleanupObject(generated.result().getMemoryBankFileKey(), requestId);
            cleanupObject(generated.result().getConfigFileKey(), requestId);
        }
    }

    private void cleanupObject(String objectKey, String requestId) {
        if (objectKey == null || objectKey.isBlank()) {
            return;
        }
        try {
            minioStorageAdapter.delete(minioProperties.getBucketModels(), objectKey);
        } catch (RuntimeException cleanupException) {
            log.warn("Failed to cleanup model generation object, requestId={}, objectKey={}", requestId, objectKey, cleanupException);
        }
    }

    private void validateUploadCommand(UploadModelVersionCommand command) {
        required(command.getVersionName(), "versionName");
        if (command.getModelCategory() == null) {
            throw new BusinessException(ErrorCode.MODEL_VALIDATION_FAILED, "modelCategory???꾩닔?낅땲??");
        }
        if (command.getModelProfile() == null) {
            throw new BusinessException(ErrorCode.MODEL_VALIDATION_FAILED, "modelProfile? ?꾩닔?낅땲??");
        }
        if (command.getCkptFile() == null || command.getCkptFile().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "ckptFile? ?꾩닔?낅땲??");
        }
        if (command.getConfigFile() == null || command.getConfigFile().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "configFile? ?꾩닔?낅땲??");
        }
        if (command.getMemoryBankFile() == null || command.getMemoryBankFile().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "memoryBankFile? ?꾩닔?낅땲??");
        }
        validateThreshold(command.getThresholdDefault());
    }

    private void validateThreshold(BigDecimal thresholdDefault) {
        if (thresholdDefault == null) {
            return;
        }
        if (thresholdDefault.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ErrorCode.MODEL_VALIDATION_FAILED, "thresholdDefault는 0 이상이어야 합니다.");
        }
    }

    private VisionModelProfilePolicy.Spec resolvePolicy(ModelCategory category, ModelProfile profile) {
        try {
            return VisionModelProfilePolicy.resolve(category, profile);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.MODEL_VALIDATION_FAILED, "지원하지 않는 modelCategory/modelProfile 조합입니다.");
        }
    }

    private BigDecimal resolveThresholdDefault(ModelCategory category, ModelProfile profile, BigDecimal requestedThreshold) {
        if (requestedThreshold != null) {
            return requestedThreshold;
        }
        return resolvePolicy(category, profile).imageThreshold();
    }

    private String resolveInputSize(UploadModelVersionCommand command) {
        if (command.getInputSize() != null && !command.getInputSize().isBlank()) {
            return command.getInputSize().trim();
        }
        return resolvePolicy(command.getModelCategory(), command.getModelProfile()).inputSize();
    }

    private void validateDeployment(Long organizationId, Long targetId, DeploymentScope scope) {
        if (organizationId == null) {
            throw new BusinessException(ErrorCode.MODEL_VALIDATION_FAILED, "organizationId???꾩닔?낅땲??");
        }
        findOrganizationByIdPort.findById(organizationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORGANIZATION_NOT_FOUND));
        if (scope == DeploymentScope.TARGET) {
            if (targetId == null) {
                throw new BusinessException(ErrorCode.MODEL_VALIDATION_FAILED, "TARGET scope?먯꽌??targetId媛 ?꾩닔?낅땲??");
            }
            AnalysisTarget target = loadAnalysisTargetPort.findById(targetId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.TARGET_NOT_FOUND));
            if (!organizationId.equals(target.getOrganizationId())) {
                throw new BusinessException(ErrorCode.MODEL_VALIDATION_FAILED, "targetId媛 organization ?뚯냽怨??쇱튂?섏? ?딆뒿?덈떎.");
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
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "紐⑤뜽 artifact ???以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎.");
        }
        StoredFile storedFile = persistUploadedFilePort.save(
                StoredFile.builder()
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
                        .build()
        );
        return ModelArtifactJpaEntity.builder()
                .modelVersionId(versionId)
                .fileId(storedFile.getFileId())
                .artifactType(artifactType)
                .checksum(checksum)
                .build();
    }

    private String buildMemoryBankFileName(MultipartFile file) {
        String ext = extension(file.getOriginalFilename());
        return ext == null ? "memory_bank" : "memory_bank." + ext;
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
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "?뚯씪 泥댄겕??怨꾩궛???ㅽ뙣?덉뒿?덈떎.");
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
                .version(
                        ModelVersionSummaryResponse.builder()
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
                                .build()
                )
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
                .deletedAt(deployment.getDeletedAt())
                .deletedBy(deployment.getDeletedBy())
                .deleteReason(deployment.getDeleteReason())
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

    private void deactivateActiveDeploymentsInSameSlotExcluding(
            ModelDeploymentJpaEntity deployment,
            ModelCategory modelCategory,
            ModelProfile modelProfile
    ) {
        modelManagementPort.deactivateActiveDeploymentsInSameSlotExcludingDeployment(
                deployment.getOrganizationId(),
                deployment.getDeploymentScope() == DeploymentScope.TARGET ? deployment.getTargetId() : null,
                deployment.getDeploymentScope(),
                modelCategory,
                modelProfile,
                deployment.getDeploymentId()
        );
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
            recordAdminActionLogUseCase.recordAdminActionLog(
                    RecordAdminActionLogCommand.builder()
                            .actorUserId(securityUtils.getCurrentUserId())
                            .actionType(actionType)
                            .targetType(targetType)
                            .targetId(targetId)
                            .reason(reason)
                            .build()
            );
        } catch (Exception exception) {
            log.warn("Failed to record model admin action, actionType={}, targetId={}", actionType, targetId, exception);
        }
    }

    private String required(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.MODEL_VALIDATION_FAILED, fieldName + "? ?꾩닔?낅땲??");
        }
        return value.trim();
    }

    private String blankToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private String resolveVersionName(GenerateModelVersionFromNormalImagesCommand command, ModelProfile profile, int profileCount) {
        String requested = blankToNull(command.getVersionName());
        if (requested != null) {
            return profileCount > 1 ? requested + "-" + profile.name().toLowerCase(Locale.ROOT) : requested;
        }
        String scopeToken = command.getDeploymentScope() == DeploymentScope.TARGET
                ? "target" + command.getTargetId()
                : "org";
        return "v" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "-" + command.getModelCategory().name().toLowerCase(Locale.ROOT)
                + "-" + profile.name().toLowerCase(Locale.ROOT)
                + "-org" + command.getOrganizationId()
                + "-" + scopeToken;
    }

    private String buildOutputPrefix(Long organizationId, Long targetId, DeploymentScope scope, ModelProfile profile, ModelCategory category) {
        String targetSegment = scope == DeploymentScope.TARGET ? "target-" + targetId : "organization";
        return "models/generated/org-" + organizationId + "/" + targetSegment + "/"
                + profile.name().toLowerCase(Locale.ROOT) + "-" + category.name().toLowerCase(Locale.ROOT)
                + "/job-" + UUID.randomUUID();
    }

    private String buildNormalImageObjectKey(Long organizationId, Long targetId, DeploymentScope scope, String requestId, String fileName) {
        String targetSegment = scope == DeploymentScope.TARGET ? "target-" + targetId : "organization";
        return "models/tmp/normal/org-" + organizationId + "/" + targetSegment + "/job-" + requestId + "/" + fileName;
    }

    private String buildUniqueFileName(String originalFilename, String defaultBaseName) {
        String safeName = fileNameFromObjectKey(blankToNull(originalFilename) == null ? defaultBaseName : originalFilename);
        return UUID.randomUUID() + "-" + safeName.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private String fileNameFromObjectKey(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return "file";
        }
        int index = objectKey.lastIndexOf('/');
        return index >= 0 ? objectKey.substring(index + 1) : objectKey;
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


    private record FixedProfile(String ckptFileKey, String configFileKey) {
    }

    private record GeneratedMemoryBank(ModelProfile profile, FixedProfile fixedProfile, GenerateMemoryBankResult result) {
    }
}
