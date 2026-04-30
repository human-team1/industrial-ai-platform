package com.example.factoryguard.application.service.inspection;

import com.example.factoryguard.adapter.out.storage.minio.MinioProperties;
import com.example.factoryguard.adapter.out.storage.minio.MinioStorageAdapter;
import com.example.factoryguard.application.dto.inspection.ResolvedThreshold;
import com.example.factoryguard.application.dto.inspection.SubmitInspectionCommand;
import com.example.factoryguard.application.dto.inspection.SubmitInspectionResult;
import com.example.factoryguard.application.port.out.auth.TokenStorePort;
import com.example.factoryguard.application.port.out.file.PersistUploadedFilePort;
import com.example.factoryguard.application.port.out.inspection.LoadAnalysisTargetPort;
import com.example.factoryguard.application.port.out.inspection.LoadInspectionRunPort;
import com.example.factoryguard.application.port.out.result.LoadInspectionResultPort;
import com.example.factoryguard.application.port.out.review.LoadReviewQueuePort;
import com.example.factoryguard.application.port.out.user.FindUserByIdPort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.common.validation.FileValidator;
import com.example.factoryguard.domain.file.model.StoredFile;
import com.example.factoryguard.domain.file.vo.StorageType;
import com.example.factoryguard.domain.inspection.model.AnalysisTarget;
import com.example.factoryguard.domain.inspection.model.InspectionInput;
import com.example.factoryguard.domain.inspection.model.InspectionRun;
import com.example.factoryguard.domain.inspection.model.RunStatus;
import com.example.factoryguard.domain.inspection.model.RunType;
import com.example.factoryguard.domain.user.model.ThresholdSource;
import com.example.factoryguard.domain.user.model.User;
import com.example.factoryguard.domain.user.model.UserRole;
import com.example.factoryguard.domain.user.model.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmitInspectionServiceTest {

    @Mock TokenStorePort tokenStorePort;
    @Mock FindUserByIdPort findUserByIdPort;
    @Mock LoadAnalysisTargetPort loadAnalysisTargetPort;
    @Mock LoadInspectionResultPort loadInspectionResultPort;
    @Mock LoadReviewQueuePort loadReviewQueuePort;
    @Mock LoadInspectionRunPort loadInspectionRunPort;
    @Mock ResolveInspectionThresholdService resolveInspectionThresholdService;
    @Mock InspectionRunRecorder runRecorder;
    @Mock InspectionInputRecorder inputRecorder;
    @Mock InspectionEventLogger eventLogger;
    @Mock FileValidator fileValidator;
    @Mock MinioStorageAdapter minioStorageAdapter;
    @Mock MinioProperties minioProperties;
    @Mock PersistUploadedFilePort persistUploadedFilePort;

    SubmitInspectionService service;

    @BeforeEach
    void setUp() {
        service = new SubmitInspectionService(
                tokenStorePort,
                findUserByIdPort,
                loadAnalysisTargetPort,
                loadInspectionResultPort,
                loadReviewQueuePort,
                loadInspectionRunPort,
                resolveInspectionThresholdService,
                runRecorder,
                inputRecorder,
                eventLogger,
                fileValidator,
                minioStorageAdapter,
                minioProperties,
                persistUploadedFilePort
        );
    }

    @Test
    void fileOnlyUploadReturnsProcessing() {
        MockMultipartFile file = imageFile();
        givenActiveUser();
        when(resolveInspectionThresholdService.resolve(1L, null)).thenReturn(defaultThreshold());
        when(minioProperties.getBucketInspectionArtifacts()).thenReturn("inspection-artifacts");
        when(persistUploadedFilePort.save(any())).thenReturn(storedFile());
        when(runRecorder.create(any())).thenAnswer(invocation -> {
            InspectionRun run = invocation.getArgument(0);
            return run.toBuilder().inspectionId(1001L).build();
        });

        SubmitInspectionResult result = service.execute(new SubmitInspectionCommand(
                1L, "session-1", null, null, file, null
        ));

        assertThat(result.getInspectionId()).isEqualTo(1001L);
        assertThat(result.getRunStatus()).isEqualTo(RunStatus.PROCESSING);

        ArgumentCaptor<InspectionInput> inputCaptor = ArgumentCaptor.forClass(InspectionInput.class);
        verify(inputRecorder).record(inputCaptor.capture());
        assertThat(inputCaptor.getValue().getFileId()).isEqualTo(10L);
        verify(runRecorder).transitTo(1001L, RunStatus.PROCESSING);
    }

    @Test
    void missingFileValidationErrorIsPropagated() {
        givenActiveUser();
        doThrow(new BusinessException(ErrorCode.INVALID_FILE_EMPTY)).when(fileValidator).validate(null);

        assertThatThrownBy(() -> service.execute(new SubmitInspectionCommand(
                1L, "session-1", null, null, null, null
        ))).isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.INVALID_FILE_EMPTY.getDefaultMessage());
    }

    @Test
    void inaccessibleTargetThrowsForbidden() {
        MockMultipartFile file = imageFile();
        givenActiveUser();
        when(resolveInspectionThresholdService.resolve(1L, null)).thenReturn(defaultThreshold());
        when(loadAnalysisTargetPort.findById(99L)).thenReturn(Optional.of(AnalysisTarget.builder()
                .targetId(99L)
                .organizationId(2L)
                .targetName("other")
                .build()));

        assertThatThrownBy(() -> service.execute(new SubmitInspectionCommand(
                1L, "session-1", 99L, null, file, null
        ))).isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.FORBIDDEN.getDefaultMessage());
    }

    private void givenActiveUser() {
        when(tokenStorePort.getSessionId(1L)).thenReturn(Optional.of("session-1"));
        when(findUserByIdPort.findById(1L)).thenReturn(Optional.of(User.builder()
                .userId(1L)
                .organizationId(1L)
                .email("user@example.com")
                .name("user")
                .role(UserRole.ROLE_COMPANY_WORKER)
                .status(UserStatus.ACTIVE)
                .build()));
    }

    private MockMultipartFile imageFile() {
        return new MockMultipartFile("file", "sample.png", "image/png", "image".getBytes());
    }

    private ResolvedThreshold defaultThreshold() {
        return new ResolvedThreshold(0.75, 0.55, ThresholdSource.SYSTEM_DEFAULT, null, null);
    }

    private StoredFile storedFile() {
        return StoredFile.builder()
                .fileId(10L)
                .storageType(StorageType.MINIO)
                .bucketName("inspection-artifacts")
                .objectKey("inspections/sample.png")
                .fileName("sample.png")
                .fileExt("png")
                .mimeType("image/png")
                .fileSize(5L)
                .checksum("checksum")
                .createdAt(LocalDateTime.now())
                .createdBy(1L)
                .build();
    }
}
