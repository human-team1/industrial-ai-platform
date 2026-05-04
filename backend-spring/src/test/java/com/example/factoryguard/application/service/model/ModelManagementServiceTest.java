package com.example.factoryguard.application.service.model;

import com.example.factoryguard.adapter.out.persistence.model.ModelJpaEntity;
import com.example.factoryguard.adapter.out.persistence.model.ModelVersionJpaEntity;
import com.example.factoryguard.adapter.out.storage.minio.MinioProperties;
import com.example.factoryguard.adapter.out.storage.minio.MinioStorageAdapter;
import com.example.factoryguard.application.dto.model.ModelVersionStatusCommand;
import com.example.factoryguard.application.dto.model.UploadModelVersionCommand;
import com.example.factoryguard.application.port.in.operation.RecordAdminActionLogUseCase;
import com.example.factoryguard.application.port.out.file.PersistUploadedFilePort;
import com.example.factoryguard.application.port.out.inspection.LoadAnalysisTargetPort;
import com.example.factoryguard.application.port.out.model.ModelManagementPort;
import com.example.factoryguard.application.port.out.organization.FindOrganizationByIdPort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.config.security.SecurityUtils;
import com.example.factoryguard.domain.model.vo.ModelArtifactType;
import com.example.factoryguard.domain.model.vo.ModelCategory;
import com.example.factoryguard.domain.model.vo.ModelProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModelManagementServiceTest {

    @Mock private ModelManagementPort modelManagementPort;
    @Mock private PersistUploadedFilePort persistUploadedFilePort;
    @Mock private MinioStorageAdapter minioStorageAdapter;
    @Mock private MinioProperties minioProperties;
    @Mock private SecurityUtils securityUtils;
    @Mock private FindOrganizationByIdPort findOrganizationByIdPort;
    @Mock private LoadAnalysisTargetPort loadAnalysisTargetPort;
    @Mock private RecordAdminActionLogUseCase recordAdminActionLogUseCase;

    @InjectMocks
    private ModelManagementService service;

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
    @DisplayName("No.40 모델 활성화 - CKPT 누락 시도 MODEL_VALIDATION_FAILED")
    void activateRejectsMissingCkpt() {
        long versionId = 51L;
        ModelVersionJpaEntity version = ModelVersionJpaEntity.builder().modelVersionId(versionId).build();
        when(modelManagementPort.findModelVersionById(versionId)).thenReturn(Optional.of(version));
        lenient().when(modelManagementPort.hasArtifact(any(), any())).thenReturn(true);
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

    private MockMultipartFile file(String name, String contentType) {
        return new MockMultipartFile("file", name, contentType, new byte[]{1, 2, 3});
    }

    private ModelJpaEntity modelEntity() {
        return ModelJpaEntity.builder()
                .modelName("anomaly-baseline")
                .modelType("PATCHCORE")
                .description("테스트")
                .build();
    }
}
