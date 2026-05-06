package com.example.factoryguard.application.service.model;

import com.example.factoryguard.adapter.out.persistence.model.ModelJpaEntity;
import com.example.factoryguard.adapter.out.persistence.model.ModelVersionJpaEntity;
import com.example.factoryguard.adapter.out.storage.minio.MinioProperties;
import com.example.factoryguard.adapter.out.storage.minio.MinioStorageAdapter;
import com.example.factoryguard.application.dto.ai.GenerateMemoryBankCommand;
import com.example.factoryguard.application.dto.ai.GenerateMemoryBankResult;
import com.example.factoryguard.application.dto.model.GenerateModelVersionFromNormalImagesCommand;
import com.example.factoryguard.application.dto.model.ModelVersionStatusCommand;
import com.example.factoryguard.application.dto.model.UploadModelVersionCommand;
import com.example.factoryguard.application.port.in.operation.RecordAdminActionLogUseCase;
import com.example.factoryguard.application.port.out.ai.GenerateMemoryBankPort;
import com.example.factoryguard.application.port.out.file.LoadFilePort;
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
import com.example.factoryguard.domain.model.vo.DeploymentScope;
import com.example.factoryguard.domain.model.vo.ModelArtifactType;
import com.example.factoryguard.domain.model.vo.ModelCategory;
import com.example.factoryguard.domain.model.vo.ModelProfile;
import com.example.factoryguard.domain.organization.model.Organization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ModelManagementServiceTest {

    @Mock private ModelManagementPort modelManagementPort;
    @Mock private GenerateMemoryBankPort generateMemoryBankPort;
    @Mock private PersistUploadedFilePort persistUploadedFilePort;
    @Mock private LoadFilePort loadFilePort;
    @Mock private MinioStorageAdapter minioStorageAdapter;
    @Mock private MinioProperties minioProperties;
    @Mock private ModelMemoryBankProperties modelMemoryBankProperties;
    @Mock private SecurityUtils securityUtils;
    @Mock private FindOrganizationByIdPort findOrganizationByIdPort;
    @Mock private LoadAnalysisTargetPort loadAnalysisTargetPort;
    @Mock private RecordAdminActionLogUseCase recordAdminActionLogUseCase;
    @Mock private TransactionTemplate transactionTemplate;

    @InjectMocks
    private ModelManagementService service;

    @BeforeEach
    void setUp() {
        lenient().when(minioProperties.getBucketModels()).thenReturn("models");
        ModelMemoryBankProperties.MemoryBank memoryBank = new ModelMemoryBankProperties.MemoryBank();
        memoryBank.setMinNormalImageCount(10);
        lenient().when(modelMemoryBankProperties.getMemoryBank()).thenReturn(memoryBank);
        lenient().when(modelMemoryBankProperties.getFixedProfiles()).thenReturn(fixedProfiles());
        lenient().when(minioStorageAdapter.objectExists(eq("models"), any())).thenReturn(true);
        lenient().when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });
    }

    @Test
    @DisplayName("No.39 모델 버전 업로드 - ckptFile 누락 시 INVALID_REQUEST")
    void uploadVersionRejectsMissingCkpt() {
        when(modelManagementPort.findModelById(1L)).thenReturn(Optional.of(modelEntity()));
        UploadModelVersionCommand command = UploadModelVersionCommand.builder()
                .modelId(1L)
                .versionName("v1.0.0")
                .modelCategory(ModelCategory.TEXTURE)
                .modelProfile(ModelProfile.PERFORMANCE)
                .ckptFile(null)
                .configFile(file("config.json", "application/json"))
                .memoryBankFile(file("memory_bank.npy", "application/octet-stream"))
                .build();

        assertThatThrownBy(() -> service.uploadModelVersion(command))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    @Test
    @DisplayName("No.39 모델 버전 업로드 - thresholdDefault 범위 외 차단(MODEL_VALIDATION_FAILED)")
    void uploadVersionRejectsOutOfRangeThreshold() {
        when(modelManagementPort.findModelById(1L)).thenReturn(Optional.of(modelEntity()));
        UploadModelVersionCommand command = UploadModelVersionCommand.builder()
                .modelId(1L)
                .versionName("v1.0.0")
                .modelCategory(ModelCategory.TEXTURE)
                .modelProfile(ModelProfile.PERFORMANCE)
                .ckptFile(file("model.ckpt", "application/octet-stream"))
                .configFile(file("config.json", "application/json"))
                .memoryBankFile(file("memory_bank.npy", "application/octet-stream"))
                .thresholdDefault(new BigDecimal("1.5"))
                .build();

        assertThatThrownBy(() -> service.uploadModelVersion(command))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.MODEL_VALIDATION_FAILED);
    }

    @Test
    @DisplayName("No.40 모델 활성화 - MEMORY_BANK 산출물 누락 시 MODEL_VALIDATION_FAILED")
    void activateRejectsMissingMemoryBank() {
        long versionId = 50L;
        ModelVersionJpaEntity version = ModelVersionJpaEntity.builder().modelVersionId(versionId).build();
        when(modelManagementPort.findModelVersionById(versionId)).thenReturn(Optional.of(version));
        when(modelManagementPort.hasArtifact(eq(versionId), eq(ModelArtifactType.CKPT))).thenReturn(true);
        when(modelManagementPort.hasArtifact(eq(versionId), eq(ModelArtifactType.CONFIG))).thenReturn(true);
        when(modelManagementPort.hasArtifact(eq(versionId), eq(ModelArtifactType.MEMORY_BANK))).thenReturn(false);

        ModelVersionStatusCommand command = ModelVersionStatusCommand.builder()
                .versionId(versionId).reason("activate").build();

        assertThatThrownBy(() -> service.activateModelVersion(command))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.MODEL_VALIDATION_FAILED);
    }

    @Test
    @DisplayName("No.40 모델 활성화 - CKPT가 없으면 실패")
    void activateRejectsMissingCkpt() {
        long versionId = 51L;
        ModelVersionJpaEntity version = ModelVersionJpaEntity.builder()
                .modelVersionId(versionId)
                .modelId(1L)
                .versionName("v-auto")
                .modelCategory(ModelCategory.OBJECT)
                .modelProfile(ModelProfile.SPEED)
                .framework("PYTORCH")
                .inputSize("224x224")
                .deployStatus(com.example.factoryguard.domain.model.vo.ModelDeployStatus.REGISTERED)
                .isActive(false)
                .build();
        when(modelManagementPort.findModelVersionById(versionId)).thenReturn(Optional.of(version));
        when(modelManagementPort.findModelById(1L)).thenReturn(Optional.of(modelEntity()));
        when(modelManagementPort.hasArtifact(eq(versionId), eq(ModelArtifactType.CONFIG))).thenReturn(true);
        when(modelManagementPort.hasArtifact(eq(versionId), eq(ModelArtifactType.MEMORY_BANK))).thenReturn(true);
        when(modelManagementPort.hasArtifact(eq(versionId), eq(ModelArtifactType.CKPT))).thenReturn(false);
        ModelVersionStatusCommand command = ModelVersionStatusCommand.builder()
                .versionId(versionId).build();

        assertThatThrownBy(() -> service.activateModelVersion(command))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.MODEL_VALIDATION_FAILED);
    }

    @Test
    @DisplayName("No.41 메모리뱅크 생성 요청 DTO - UploadModelVersionCommand에 memoryBankFile 포함하면 정상 구성")
    void memoryBankFileIsCarriedInUploadCommand() {
        UploadModelVersionCommand command = UploadModelVersionCommand.builder()
                .modelId(1L)
                .versionName("v1.0.0")
                .modelCategory(ModelCategory.TEXTURE)
                .modelProfile(ModelProfile.PERFORMANCE)
                .ckptFile(file("model.ckpt", "application/octet-stream"))
                .configFile(file("config.json", "application/json"))
                .memoryBankFile(file("memory_bank.npy", "application/octet-stream"))
                .thresholdDefault(new BigDecimal("0.75"))
                .build();

        org.assertj.core.api.Assertions.assertThat(command.getMemoryBankFile()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(command.getMemoryBankFile().getOriginalFilename()).isEqualTo("memory_bank.npy");
        org.assertj.core.api.Assertions.assertThat(command.getCkptFile().getOriginalFilename()).isEqualTo("model.ckpt");
        org.assertj.core.api.Assertions.assertThat(command.getConfigFile().getOriginalFilename()).isEqualTo("config.json");
    }

    @Test
    @DisplayName("정상 이미지 9장 - 모델 생성 차단")
    void generateFromNormalImagesRejectsNineImages() {
        when(modelManagementPort.findModelById(1L)).thenReturn(Optional.of(modelEntity()));
        when(findOrganizationByIdPort.findById(1001L)).thenReturn(Optional.of(Organization.builder().organizationId(1001L).build()));

        assertThatThrownBy(() -> service.generateFromNormalImages(generateCommand(null, 9)))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.MODEL_VALIDATION_FAILED);

        verifyNoInteractions(generateMemoryBankPort);
    }

    @Test
    @DisplayName("정상 이미지 10장 + profile 미전송 - SPEED/PERFORMANCE 모두 생성 시도")
    void generateFromNormalImagesCreatesBothProfilesWhenProfileMissing() throws Exception {
        stubSuccessfulGeneration();

        service.generateFromNormalImages(generateCommand(null, 10));

        verify(generateMemoryBankPort, times(2)).generateMemoryBank(any());
    }

    @Test
    @DisplayName("정상 이미지 50장 + profile 전송 - 해당 profile만 생성 시도")
    void generateFromNormalImagesCreatesRequestedProfileForFiftyImages() throws Exception {
        stubSuccessfulGeneration();

        service.generateFromNormalImages(generateCommand(ModelProfile.SPEED, 50));

        verify(generateMemoryBankPort, times(1)).generateMemoryBank(any());
    }

    @Test
    @DisplayName("TARGET 배포인데 targetId 누락 - 422")
    void targetDeploymentRequiresTargetId() {
        when(modelManagementPort.findModelById(1L)).thenReturn(Optional.of(modelEntity()));
        when(findOrganizationByIdPort.findById(1001L)).thenReturn(Optional.of(Organization.builder().organizationId(1001L).build()));

        GenerateModelVersionFromNormalImagesCommand command = GenerateModelVersionFromNormalImagesCommand.builder()
                .modelId(1L)
                .organizationId(1001L)
                .deploymentScope(DeploymentScope.TARGET)
                .modelCategory(ModelCategory.OBJECT)
                .normalImages(images(10))
                .build();

        assertThatThrownBy(() -> service.generateFromNormalImages(command))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.MODEL_VALIDATION_FAILED);
    }

    @Test
    @DisplayName("자동 생성은 base ckpt/config MinIO 존재 검사를 하지 않고 FastAPI 호출")
    void generateFromNormalImagesChecksBaseArtifactsAndPassesKeys() throws Exception {
        stubSuccessfulGeneration();

        service.generateFromNormalImages(generateCommand(ModelProfile.SPEED, 10));

        verify(minioStorageAdapter).objectExists("models", "models/base/speed-object/model.ckpt");
        verify(minioStorageAdapter).objectExists("models", "models/base/speed-object/config.json");
        verify(generateMemoryBankPort).generateMemoryBank(argThat(command ->
                command.getModelCategory() == ModelCategory.OBJECT
                        && command.getModelProfile() == ModelProfile.SPEED
                        && command.getCkptFileKey().equals("models/base/speed-object/model.ckpt")
                        && command.getConfigFileKey().equals("models/base/speed-object/config.json")
                        && command.getNormalImageFileKeys().size() == 10
                        && command.getOutputPrefix() != null
        ));
    }

    @Test
    @DisplayName("base ckpt/config가 MinIO에 없으면 BASE_MODEL_PROFILE_NOT_FOUND")
    void generateFromNormalImagesRejectsMissingBaseArtifact() throws Exception {
        stubSuccessfulGeneration();
        when(minioStorageAdapter.objectExists("models", "models/base/speed-object/model.ckpt")).thenReturn(false);

        assertThatThrownBy(() -> service.generateFromNormalImages(generateCommand(ModelProfile.SPEED, 10)))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.BASE_MODEL_PROFILE_NOT_FOUND);

        verify(generateMemoryBankPort, never()).generateMemoryBank(any());
    }

    @Test
    @DisplayName("FastAPI 실패 - upstream 정보 보존")
    void fastApiFailureKeepsUpstreamDetails() throws Exception {
        stubSuccessfulGeneration();
        doThrow(new com.example.factoryguard.application.exception.ai.AiServerException(
                        404,
                        "MODEL_CONFIG_NOT_FOUND: missing",
                        "MODEL_CONFIG_NOT_FOUND",
                        "MinIO object not found: config.json",
                        "req-upstream"
                ))
                .when(generateMemoryBankPort).generateMemoryBank(any());

        assertThatThrownBy(() -> service.generateFromNormalImages(generateCommand(ModelProfile.SPEED, 10)))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException business = (BusinessException) ex;
                    org.assertj.core.api.Assertions.assertThat(business.getErrorCode()).isEqualTo(ErrorCode.AI_SERVER_ERROR);
                    org.assertj.core.api.Assertions.assertThat(business.getDetails()).containsEntry("upstreamErrorCode", "MODEL_CONFIG_NOT_FOUND");
                    org.assertj.core.api.Assertions.assertThat(business.getDetails()).containsEntry("upstreamStatus", 404);
                });

        verify(modelManagementPort, never()).saveModelVersion(any());
        verify(modelManagementPort, never()).saveDeployment(any());
    }

    private MockMultipartFile file(String name, String contentType) {
        return new MockMultipartFile("file", name, contentType, new byte[]{1, 2, 3});
    }

    private List<MultipartFile> images(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(index -> new MockMultipartFile("normalImages", "normal-" + index + ".png", "image/png", png()))
                .map(file -> (MultipartFile) file)
                .toList();
    }

    private byte[] png() {
        return java.util.Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAIAAACQd1PeAAAADElEQVR4nGP4//8/AAX+Av5B4l6bAAAAAElFTkSuQmCC");
    }

    private Map<String, ModelMemoryBankProperties.FixedProfile> fixedProfiles() {
        Map<String, ModelMemoryBankProperties.FixedProfile> profiles = new HashMap<>();
        profiles.put("speed-object", fixedProfile("models/base/speed-object/model.ckpt", "models/base/speed-object/config.json"));
        profiles.put("performance-object", fixedProfile("models/base/performance-object/model.ckpt", "models/base/performance-object/config.json"));
        profiles.put("speed-texture", fixedProfile("models/base/speed-texture/model.ckpt", "models/base/speed-texture/config.json"));
        profiles.put("performance-texture", fixedProfile("models/base/performance-texture/model.ckpt", "models/base/performance-texture/config.json"));
        return profiles;
    }

    private ModelMemoryBankProperties.FixedProfile fixedProfile(String ckptKey, String configKey) {
        ModelMemoryBankProperties.FixedProfile profile = new ModelMemoryBankProperties.FixedProfile();
        profile.setCkptFileKey(ckptKey);
        profile.setConfigFileKey(configKey);
        profile.setFramework("PYTORCH");
        profile.setInputSize("224x224");
        return profile;
    }

    private GenerateModelVersionFromNormalImagesCommand generateCommand(ModelProfile profile, int imageCount) {
        return GenerateModelVersionFromNormalImagesCommand.builder()
                .modelId(1L)
                .organizationId(1001L)
                .deploymentScope(DeploymentScope.ORGANIZATION)
                .modelCategory(ModelCategory.OBJECT)
                .modelProfile(profile)
                .normalImages(images(imageCount))
                .requestId("req-test")
                .build();
    }

    private void stubSuccessfulGeneration() throws Exception {
        when(modelManagementPort.findModelById(1L)).thenReturn(Optional.of(modelEntity()));
        when(findOrganizationByIdPort.findById(1001L)).thenReturn(Optional.of(Organization.builder().organizationId(1001L).build()));
        when(persistUploadedFilePort.save(any())).thenAnswer(invocation -> storedFile(300L, ((StoredFile) invocation.getArgument(0)).getObjectKey()));
        when(generateMemoryBankPort.generateMemoryBank(any())).thenAnswer(invocation -> {
            GenerateMemoryBankCommand command = invocation.getArgument(0);
            return GenerateMemoryBankResult.builder()
                    .memoryBankFileKey(command.getOutputPrefix() + "/memory_bank.pt")
                    .configFileKey(command.getOutputPrefix() + "/config.json")
                    .ckptFileKey(command.getCkptFileKey())
                    .normalImageCount(command.getNormalImageFileKeys().size())
                    .modelCategory(command.getModelCategory())
                    .modelProfile(command.getModelProfile())
                    .framework("PYTORCH")
                    .inputSize("224x224")
                    .createdAt(LocalDateTime.now())
                    .build();
        });
        when(modelManagementPort.existsVersionByModelIdAndVersionName(any(), any())).thenReturn(false);
        when(modelManagementPort.saveModelVersion(any())).thenAnswer(invocation -> {
            ModelVersionJpaEntity entity = invocation.getArgument(0);
            return ModelVersionJpaEntity.builder()
                    .modelVersionId(500L)
                    .modelId(entity.getModelId())
                    .fileId(entity.getFileId())
                    .versionName(entity.getVersionName())
                    .modelCategory(entity.getModelCategory())
                    .modelProfile(entity.getModelProfile())
                    .framework(entity.getFramework())
                    .inputSize(entity.getInputSize())
                    .thresholdDefault(entity.getThresholdDefault())
                    .deployStatus(entity.getDeployStatus())
                    .isActive(entity.getIsActive())
                    .validatedAt(entity.getValidatedAt())
                    .validatedBy(entity.getValidatedBy())
                    .build();
        });
        when(modelManagementPort.saveArtifacts(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(modelManagementPort.findActiveDeployments(any(), any(), any())).thenReturn(List.of());
        when(modelManagementPort.saveDeployment(any())).thenAnswer(invocation -> {
            com.example.factoryguard.adapter.out.persistence.model.ModelDeploymentJpaEntity entity = invocation.getArgument(0);
            return com.example.factoryguard.adapter.out.persistence.model.ModelDeploymentJpaEntity.builder()
                    .deploymentId(700L)
                    .organizationId(entity.getOrganizationId())
                    .targetId(entity.getTargetId())
                    .modelVersionId(entity.getModelVersionId())
                    .deploymentScope(entity.getDeploymentScope())
                    .deployStatus(entity.getDeployStatus())
                    .isActive(entity.getIsActive())
                    .deployedAt(entity.getDeployedAt())
                    .deployedBy(entity.getDeployedBy())
                    .reason(entity.getReason())
                    .rollbackFlag(false)
                    .build();
        });
        lenient().when(securityUtils.getCurrentUserId()).thenReturn(9001L);
    }

    private StoredFile storedFile(Long fileId, String objectKey) {
        return StoredFile.builder()
                .fileId(fileId)
                .storageType(StorageType.MINIO)
                .bucketName("models")
                .objectKey(objectKey)
                .fileName(objectKey.substring(objectKey.lastIndexOf('/') + 1))
                .fileSize(1L)
                .createdAt(LocalDateTime.now())
                .createdBy(9001L)
                .build();
    }

    private ModelJpaEntity modelEntity() {
        return ModelJpaEntity.builder()
                .modelName("anomaly-baseline")
                .modelType("PATCHCORE")
                .description("테스트")
                .build();
    }
}
