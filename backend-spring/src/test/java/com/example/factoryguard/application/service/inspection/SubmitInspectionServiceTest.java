package com.example.factoryguard.application.service.inspection;

import com.example.factoryguard.adapter.out.storage.minio.MinioProperties;
import com.example.factoryguard.adapter.out.storage.minio.MinioStorageAdapter;
import com.example.factoryguard.application.dto.inspection.ResolvedThreshold;
import com.example.factoryguard.application.dto.inspection.SubmitInspectionCommand;
import com.example.factoryguard.application.dto.inspection.SubmitInspectionResult;
import com.example.factoryguard.application.port.in.operation.RecordOperationLogUseCase;
import com.example.factoryguard.application.port.out.auth.TokenStorePort;
import com.example.factoryguard.application.port.out.inspection.InspectionIdempotencyCachePort;
import com.example.factoryguard.application.port.out.inspection.LoadAnalysisTargetPort;
import com.example.factoryguard.application.port.out.inspection.LoadInspectionRunPort;
import com.example.factoryguard.application.port.out.result.LoadInspectionResultPort;
import com.example.factoryguard.application.port.out.review.LoadReviewQueuePort;
import com.example.factoryguard.application.port.out.user.FindUserByIdPort;
import com.example.factoryguard.application.service.auth.SessionValidationService;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.common.validation.FileValidator;
import com.example.factoryguard.domain.file.model.StoredFile;
import com.example.factoryguard.domain.file.vo.StorageType;
import com.example.factoryguard.domain.inspection.model.AnalysisTarget;
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
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
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
    @Mock InspectionUploadTransactionService inspectionUploadTransactionService;
    @Mock FileValidator fileValidator;
    @Mock MinioStorageAdapter minioStorageAdapter;
    @Mock MinioProperties minioProperties;
    @Mock RecordOperationLogUseCase recordOperationLogUseCase;
    @Mock InspectionIdempotencyCachePort inspectionIdempotencyCachePort;
    @Mock SessionValidationService sessionValidationService;
    @Mock InferenceModelArtifactResolver inferenceModelArtifactResolver;

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
                inspectionUploadTransactionService,
                fileValidator,
                minioStorageAdapter,
                minioProperties,
                recordOperationLogUseCase,
                inspectionIdempotencyCachePort,
                sessionValidationService,
                new RoiInputValidator(),
                inferenceModelArtifactResolver
        );
    }

    @Test
    void fileOnlyUploadReturnsProcessing() {
        MockMultipartFile file = imageFile();
        givenActiveUser();
        when(resolveInspectionThresholdService.resolve(1L, null)).thenReturn(defaultThreshold());
        when(inferenceModelArtifactResolver.resolve(1L, null, 700L)).thenReturn(null);
        when(minioProperties.getBucketInspectionArtifacts()).thenReturn("inspection-artifacts");
        when(inspectionUploadTransactionService.createPendingRun(any())).thenAnswer(invocation -> {
            InspectionRun run = invocation.getArgument(0);
            return run.toBuilder().inspectionId(1001L).build();
        });
        when(inspectionUploadTransactionService.persistFileInputAndMarkProcessing(eq(1001L), any(), any(), eq("sample.png")))
                .thenReturn(storedFile());

        SubmitInspectionResult result = service.execute(new SubmitInspectionCommand(
                1L, "session-1", null, 700L, null, null, null, null, null, null, null, null, null, file, null
        ));

        assertThat(result.getInspectionId()).isEqualTo(1001L);
        assertThat(result.getRunStatus()).isEqualTo(RunStatus.PROCESSING);

        ArgumentCaptor<StoredFile> fileCaptor = ArgumentCaptor.forClass(StoredFile.class);
        verify(inspectionUploadTransactionService)
                .persistFileInputAndMarkProcessing(eq(1001L), fileCaptor.capture(), any(), eq("sample.png"));
        assertThat(fileCaptor.getValue().getBucketName()).isEqualTo("inspection-artifacts");
        assertThat(fileCaptor.getValue().getObjectKey()).startsWith("inspections/1001/inputs/");
        verify(minioStorageAdapter).upload(eq("inspection-artifacts"), startsWith("inspections/1001/inputs/"), any(), eq(5L),
                eq("sample.png"), eq("image/png"), any());
    }

    @Test
    void minioUploadFailureMarksRunFailed() {
        MockMultipartFile file = imageFile();
        givenActiveUser();
        when(resolveInspectionThresholdService.resolve(1L, null)).thenReturn(defaultThreshold());
        when(inferenceModelArtifactResolver.resolve(1L, null, 700L)).thenReturn(null);
        when(minioProperties.getBucketInspectionArtifacts()).thenReturn("inspection-artifacts");
        when(inspectionUploadTransactionService.createPendingRun(any())).thenReturn(pendingRun());
        doThrow(new IllegalStateException("minio down")).when(minioStorageAdapter)
                .upload(eq("inspection-artifacts"), startsWith("inspections/1001/inputs/"), any(), eq(5L),
                        eq("sample.png"), eq("image/png"), any());

        assertThatThrownBy(() -> service.execute(new SubmitInspectionCommand(
                1L, "session-1", null, 700L, null, null, null, null, null, null, null, null, null, file, null
        ))).isInstanceOf(BusinessException.class);

        verify(inspectionUploadTransactionService)
                .markFailedRequiresNew(eq(1001L), eq(ErrorCode.INTERNAL_ERROR.name()), any());
        verify(minioStorageAdapter, never()).delete(any(), any());
    }

    @Test
    void dbFailureAfterMinioUploadDeletesObjectAndMarksFailed() {
        MockMultipartFile file = imageFile();
        givenActiveUser();
        when(resolveInspectionThresholdService.resolve(1L, null)).thenReturn(defaultThreshold());
        when(inferenceModelArtifactResolver.resolve(1L, null, 700L)).thenReturn(null);
        when(minioProperties.getBucketInspectionArtifacts()).thenReturn("inspection-artifacts");
        when(inspectionUploadTransactionService.createPendingRun(any())).thenReturn(pendingRun());
        when(inspectionUploadTransactionService.persistFileInputAndMarkProcessing(eq(1001L), any(), any(), eq("sample.png")))
                .thenThrow(new RuntimeException("db failed"));
        when(minioStorageAdapter.delete(eq("inspection-artifacts"), startsWith("inspections/1001/inputs/")))
                .thenReturn(true);

        assertThatThrownBy(() -> service.execute(new SubmitInspectionCommand(
                1L, "session-1", null, 700L, null, null, null, null, null, null, null, null, null, file, null
        ))).isInstanceOf(BusinessException.class);

        verify(minioStorageAdapter).delete(eq("inspection-artifacts"), startsWith("inspections/1001/inputs/"));
        verify(inspectionUploadTransactionService)
                .markFailedRequiresNew(eq(1001L), eq(ErrorCode.INTERNAL_ERROR.name()), any());
    }

    @Test
    void redisFailureStillContinuesWithDatabaseFlow() {
        MockMultipartFile file = imageFile();
        givenActiveUser();
        when(resolveInspectionThresholdService.resolve(1L, null)).thenReturn(defaultThreshold());
        when(inferenceModelArtifactResolver.resolve(1L, null, 700L)).thenReturn(null);
        when(minioProperties.getBucketInspectionArtifacts()).thenReturn("inspection-artifacts");
        when(inspectionUploadTransactionService.createPendingRun(any())).thenAnswer(invocation -> {
            InspectionRun run = invocation.getArgument(0);
            return run.toBuilder().inspectionId(1001L).build();
        });
        when(inspectionUploadTransactionService.persistFileInputAndMarkProcessing(eq(1001L), any(), any(), eq("sample.png")))
                .thenReturn(storedFile());
        when(inspectionIdempotencyCachePort.findFingerprint(1L, 1L, "key-1"))
                .thenThrow(new RuntimeException("redis down"));
        when(inspectionIdempotencyCachePort.reserve(1L, 1L, "key-1", expectedFingerprint()))
                .thenThrow(new RuntimeException("redis down"));

        SubmitInspectionResult result = service.execute(new SubmitInspectionCommand(
                1L, "session-1", null, 700L, null, null, null, null, null, null, null, null, null, file, "key-1"
        ));

        assertThat(result.getInspectionId()).isEqualTo(1001L);
        verify(inspectionUploadTransactionService).createPendingRun(any());
    }

    @Test
    void sameIdempotencyKeyAndSamePayloadReturnsExistingRun() {
        MockMultipartFile file = imageFile();
        givenActiveUser();
        when(resolveInspectionThresholdService.resolve(1L, null)).thenReturn(defaultThreshold());
        when(inferenceModelArtifactResolver.resolve(1L, null, 700L)).thenReturn(null);
        InspectionRun existing = pendingRun().toBuilder()
                .idempotencyKey("key-1")
                .payloadFingerprint(expectedFingerprint())
                .build();
        when(loadInspectionRunPort.findByOrganizationIdAndUserIdAndIdempotencyKey(1L, 1L, "key-1"))
                .thenReturn(Optional.of(existing));

        SubmitInspectionResult result = service.execute(new SubmitInspectionCommand(
                1L, "session-1", null, 700L, null, null, null, null, null, null, null, null, null, file, "key-1"
        ));

        assertThat(result.getInspectionId()).isEqualTo(1001L);
        verify(inspectionUploadTransactionService, never()).createPendingRun(any());
    }

    @Test
    void sameIdempotencyKeyAndDifferentPayloadThrowsConflict() {
        MockMultipartFile file = imageFile();
        givenActiveUser();
        when(resolveInspectionThresholdService.resolve(1L, null)).thenReturn(defaultThreshold());
        when(inspectionIdempotencyCachePort.findFingerprint(1L, 1L, "key-1"))
                .thenReturn(Optional.of("different"));

        assertThatThrownBy(() -> service.execute(new SubmitInspectionCommand(
                1L, "session-1", null, 700L, null, null, null, null, null, null, null, null, null, file, "key-1"
        ))).isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.IDEMPOTENCY_CONFLICT.getDefaultMessage());
    }

    @Test
    void missingFileValidationErrorIsPropagated() {
        givenActiveUser();
        doThrow(new BusinessException(ErrorCode.INVALID_FILE_EMPTY)).when(fileValidator).validate(null);

        assertThatThrownBy(() -> service.execute(new SubmitInspectionCommand(
                1L, "session-1", null, 700L, null, null, null, null, null, null, null, null, null, null, null
        ))).isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.INVALID_FILE_EMPTY.getDefaultMessage());
    }

    @Test
    void inaccessibleTargetThrowsForbidden() {
        MockMultipartFile file = imageFile();
        givenActiveUser();
        when(resolveInspectionThresholdService.resolve(1L, null)).thenReturn(defaultThreshold());
        when(inferenceModelArtifactResolver.resolve(1L, 99L, 700L)).thenReturn(null);
        when(loadAnalysisTargetPort.findById(99L)).thenReturn(Optional.of(AnalysisTarget.builder()
                .targetId(99L)
                .organizationId(2L)
                .targetName("other")
                .build()));

        assertThatThrownBy(() -> service.execute(new SubmitInspectionCommand(
                1L, "session-1", 99L, 700L, null, null, null, null, null, null, null, null, null, file, null
        ))).isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.FORBIDDEN.getDefaultMessage());
    }

    private void givenActiveUser() {
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

    private InspectionRun pendingRun() {
        return InspectionRun.builder()
                .inspectionId(1001L)
                .organizationId(1L)
                .userId(1L)
                .runType(RunType.UPLOAD)
                .inputType("IMAGE")
                .sourceType("IMAGE")
                .sourceId("sample.png")
                .runStatus(RunStatus.PENDING)
                .appliedThreshold(BigDecimal.valueOf(0.75))
                .startedAt(LocalDateTime.now())
                .build();
    }

    private StoredFile storedFile() {
        return StoredFile.builder()
                .fileId(10L)
                .storageType(StorageType.MINIO)
                .bucketName("inspection-artifacts")
                .objectKey("inspections/1001/inputs/sample.png")
                .fileName("sample.png")
                .fileExt("png")
                .mimeType("image/png")
                .fileSize(5L)
                .checksum("checksum")
                .createdAt(LocalDateTime.now())
                .createdBy(1L)
                .build();
    }

    private String expectedFingerprint() {
        return PayloadFingerprintCalculator.compute(1L, 1L, null, 700L, null, null, "sample.png", "image/png", 5L);
    }
}
