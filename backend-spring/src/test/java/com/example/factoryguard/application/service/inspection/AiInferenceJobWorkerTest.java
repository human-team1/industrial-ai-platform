package com.example.factoryguard.application.service.inspection;

import com.example.factoryguard.adapter.out.persistence.model.ModelArtifactJpaEntity;
import com.example.factoryguard.adapter.out.persistence.model.ModelDeploymentJpaEntity;
import com.example.factoryguard.adapter.out.persistence.model.ModelVersionJpaEntity;
import com.example.factoryguard.adapter.out.storage.minio.MinioProperties;
import com.example.factoryguard.application.dto.inspection.AiInspectionResult;
import com.example.factoryguard.application.dto.inspection.ResolvedThreshold;
import com.example.factoryguard.application.dto.model.ResolvedModelDeployment;
import com.example.factoryguard.application.port.in.operation.RecordOperationLogUseCase;
import com.example.factoryguard.application.port.out.file.LoadFilePort;
import com.example.factoryguard.application.port.out.file.PersistUploadedFilePort;
import com.example.factoryguard.application.port.out.inspection.CallAiInspectionPort;
import com.example.factoryguard.application.port.out.inspection.LoadInspectionInputPort;
import com.example.factoryguard.application.port.out.inspection.LoadInspectionRunPort;
import com.example.factoryguard.application.port.out.inspection.SaveInspectionResultPort;
import com.example.factoryguard.application.port.out.inspection.SaveInspectionRunPort;
import com.example.factoryguard.application.port.out.model.ModelManagementPort;
import com.example.factoryguard.application.port.out.operation.ClaimAsyncJobPort;
import com.example.factoryguard.application.port.out.operation.SaveAsyncJobPort;
import com.example.factoryguard.application.port.out.result.SaveResultArtifactPort;
import com.example.factoryguard.application.port.out.result.SaveResultImagePort;
import com.example.factoryguard.application.port.out.notification.SaveNotificationPort;
import com.example.factoryguard.application.port.out.review.SaveReviewQueuePort;
import com.example.factoryguard.application.service.model.ActiveModelDeploymentResolver;
import com.example.factoryguard.config.inspection.AiJobWorkerProperties;
import com.example.factoryguard.domain.file.model.StoredFile;
import com.example.factoryguard.domain.file.vo.StorageType;
import com.example.factoryguard.domain.inspection.model.InspectionInput;
import com.example.factoryguard.domain.inspection.model.InspectionResult;
import com.example.factoryguard.domain.inspection.model.InspectionRun;
import com.example.factoryguard.domain.inspection.model.RunStatus;
import com.example.factoryguard.domain.inspection.model.RunType;
import com.example.factoryguard.domain.inspection.vo.RoiMode;
import com.example.factoryguard.domain.notification.model.Notification;
import com.example.factoryguard.domain.notification.vo.NotificationSeverity;
import com.example.factoryguard.domain.notification.vo.NotificationType;
import com.example.factoryguard.domain.model.vo.DeploymentScope;
import com.example.factoryguard.domain.model.vo.ModelArtifactType;
import com.example.factoryguard.domain.model.vo.ModelCategory;
import com.example.factoryguard.domain.model.vo.ModelProfile;
import com.example.factoryguard.domain.operation.model.AsyncJob;
import com.example.factoryguard.domain.operation.vo.AsyncJobStatus;
import com.example.factoryguard.domain.operation.vo.AsyncJobType;
import com.example.factoryguard.domain.result.model.Image;
import com.example.factoryguard.domain.result.vo.ImageRole;
import com.example.factoryguard.domain.review.model.ReviewQueue;
import com.example.factoryguard.domain.review.vo.ReviewQueuedReason;
import com.example.factoryguard.domain.user.model.ThresholdSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiInferenceJobWorkerTest {

    @Mock AiJobWorkerProperties properties;
    @Mock ClaimAsyncJobPort claimAsyncJobPort;
    @Mock SaveAsyncJobPort saveAsyncJobPort;
    @Mock LoadInspectionRunPort loadInspectionRunPort;
    @Mock SaveInspectionRunPort saveInspectionRunPort;
    @Mock LoadInspectionInputPort loadInspectionInputPort;
    @Mock LoadFilePort loadFilePort;
    @Mock ModelManagementPort modelManagementPort;
    @Mock CallAiInspectionPort callAiInspectionPort;
    @Mock SaveInspectionResultPort saveInspectionResultPort;
    @Mock SaveResultArtifactPort saveResultArtifactPort;
    @Mock SaveResultImagePort saveResultImagePort;
    @Mock SaveReviewQueuePort saveReviewQueuePort;
    @Mock SaveNotificationPort saveNotificationPort;
    @Mock PersistUploadedFilePort persistUploadedFilePort;
    @Mock InspectionEventLogger inspectionEventLogger;
    @Mock RecordOperationLogUseCase recordOperationLogUseCase;
    @Mock MinioProperties minioProperties;
    @Mock ResolveInspectionThresholdService resolveInspectionThresholdService;
    @Mock ActiveModelDeploymentResolver activeModelDeploymentResolver;
    @Mock InferenceModelArtifactResolver inferenceModelArtifactResolver;

    private AiInferenceJobWorker worker;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws Exception {
        DecisionProperties decisionProperties = new DecisionProperties();
        decisionProperties.setBoundaryMargin(0.05);
        InspectionDecisionEvaluator evaluator = new InspectionDecisionEvaluator(decisionProperties);

        worker = new AiInferenceJobWorker(
                properties, claimAsyncJobPort, saveAsyncJobPort,
                loadInspectionRunPort, saveInspectionRunPort,
                loadInspectionInputPort, loadFilePort, modelManagementPort,
                activeModelDeploymentResolver,
                inferenceModelArtifactResolver,
                callAiInspectionPort, saveInspectionResultPort,
                saveResultArtifactPort, saveResultImagePort, saveReviewQueuePort,
                saveNotificationPort,
                persistUploadedFilePort, inspectionEventLogger, recordOperationLogUseCase,
                minioProperties, resolveInspectionThresholdService, evaluator
        );

        lenient().when(properties.isEnabled()).thenReturn(true);
        lenient().when(claimAsyncJobPort.claimNextPending(AsyncJobType.AI_IMAGE_INFERENCE))
                .thenReturn(Optional.of(asyncJob()));
        lenient().when(loadInspectionRunPort.findRunById(100L)).thenReturn(Optional.of(run()));
        lenient().when(loadInspectionInputPort.findByInspectionId(100L)).thenReturn(List.of(fullFrameInput()));
        lenient().when(loadFilePort.findById(7L)).thenReturn(Optional.of(storedFile("inputs/sample.jpg")));
        lenient().when(loadFilePort.findById(11L)).thenReturn(Optional.of(storedFile("models/ckpt.pt")));
        lenient().when(loadFilePort.findById(12L)).thenReturn(Optional.of(storedFile("models/config.yaml")));
        lenient().when(loadFilePort.findById(13L)).thenReturn(Optional.of(storedFile("models/memory_bank.npz")));
        lenient().when(modelManagementPort.findActiveDeployments(org.mockito.ArgumentMatchers.eq(1L), any(), any()))
                .thenReturn(List.of(deployment()));
        lenient().when(modelManagementPort.findModelVersionById(50L)).thenReturn(Optional.of(version()));
        lenient().when(activeModelDeploymentResolver.resolve(any(), any(), any()))
                .thenReturn(ResolvedModelDeployment.builder()
                        .deployment(deployment())
                        .version(version())
                        .build());
        lenient().when(modelManagementPort.findArtifactsByVersionId(50L)).thenReturn(List.of(
                artifact(ModelArtifactType.CKPT, 11L),
                artifact(ModelArtifactType.CONFIG, 12L),
                artifact(ModelArtifactType.MEMORY_BANK, 13L)
        ));
        lenient().when(resolveInspectionThresholdService.resolve(7L, null))
                .thenReturn(new ResolvedThreshold(0.75, 0.55, ThresholdSource.SYSTEM_DEFAULT, null, null));
        lenient().when(saveInspectionResultPort.save(any())).thenAnswer(inv -> {
            InspectionResult r = inv.getArgument(0);
            return InspectionResult.builder()
                    .resultId(3001L)
                    .inspectionId(r.getInspectionId())
                    .decisionCode(r.getDecisionCode())
                    .resultStatus(r.getResultStatus())
                    .build();
        });
    }

    @Test
    @DisplayName("No.22 NORMAL - SaveReviewQueuePort 미호출")
    void normalDoesNotEnqueueReview() throws TimeoutException {
        when(callAiInspectionPort.call(any())).thenReturn(aiResult(0.30, 0.90, "PASSED"));

        worker.poll();

        verify(saveReviewQueuePort, never()).save(any());
        verify(saveResultImagePort).save(argThat((Image img) -> img.getImageRole() == ImageRole.ORIGINAL
                && Objects.equals(img.getFileId(), 7L)
                && Objects.equals(img.getResultId(), 3001L)));
    }

    @Test
    @DisplayName("No.22 DEFECT - SaveReviewQueuePort 미호출")
    void defectDoesNotEnqueueReview() throws TimeoutException {
        when(callAiInspectionPort.call(any())).thenReturn(aiResult(0.95, 0.90, "PASSED"));

        worker.poll();

        verify(saveReviewQueuePort, never()).save(any());
    }

    @Test
    @DisplayName("No.22 RECHECK + LOW_CONFIDENCE - SaveReviewQueuePort 호출 1회, 사유=LOW_CONFIDENCE")
    void lowConfidenceEnqueuesReview() throws TimeoutException {
        when(callAiInspectionPort.call(any())).thenReturn(aiResult(0.30, 0.30, "PASSED"));

        worker.poll();

        ArgumentCaptor<ReviewQueue> captor = ArgumentCaptor.forClass(ReviewQueue.class);
        verify(saveReviewQueuePort).save(captor.capture());
        assertThat(captor.getValue().getResultId()).isEqualTo(3001L);
        assertThat(captor.getValue().getQueuedReason()).isEqualTo(ReviewQueuedReason.LOW_CONFIDENCE);
    }

    @Test
    @DisplayName("No.22 RECHECK + BOUNDARY_SCORE - SaveReviewQueuePort 호출 1회, 사유=BOUNDARY_SCORE")
    void boundaryScoreEnqueuesReview() throws TimeoutException {
        when(callAiInspectionPort.call(any())).thenReturn(aiResult(0.78, 0.90, "PASSED"));

        worker.poll();

        ArgumentCaptor<ReviewQueue> captor = ArgumentCaptor.forClass(ReviewQueue.class);
        verify(saveReviewQueuePort).save(captor.capture());
        assertThat(captor.getValue().getQueuedReason()).isEqualTo(ReviewQueuedReason.BOUNDARY_SCORE);
    }

    @Test
    @DisplayName("No.22 RECHECK + QUALITY_FAILED - SaveReviewQueuePort 호출 1회, 사유=QUALITY_FAILED")
    void qualityFailedEnqueuesReview() throws TimeoutException {
        when(callAiInspectionPort.call(any())).thenReturn(aiResult(0.95, 0.90, "FAILED"));

        worker.poll();

        ArgumentCaptor<ReviewQueue> captor = ArgumentCaptor.forClass(ReviewQueue.class);
        verify(saveReviewQueuePort).save(captor.capture());
        assertThat(captor.getValue().getQueuedReason()).isEqualTo(ReviewQueuedReason.QUALITY_FAILED);
    }

    @Test
    @DisplayName("No.37 NORMAL - SaveNotificationPort 미호출 (이상/재검사 외에는 알림 미생성)")
    void normalDoesNotCreateNotification() throws TimeoutException {
        when(callAiInspectionPort.call(any())).thenReturn(aiResult(0.30, 0.90, "PASSED"));

        worker.poll();

        verify(saveNotificationPort, never()).save(any());
    }

    @Test
    @DisplayName("No.37 DEFECT (UPLOAD) - DEFECT_DETECTED + CRITICAL 알림 1건 생성 + targetUrl=/results/{id}")
    void defectCreatesCriticalNotification() throws TimeoutException {
        when(callAiInspectionPort.call(any())).thenReturn(aiResult(0.95, 0.90, "PASSED"));

        worker.poll();

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(saveNotificationPort).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(7L);
        assertThat(saved.getNotificationType()).isEqualTo(NotificationType.DEFECT_DETECTED);
        assertThat(saved.getSeverity()).isEqualTo(NotificationSeverity.CRITICAL);
        assertThat(saved.getRelatedType()).isEqualTo("RESULT");
        assertThat(saved.getRelatedId()).isEqualTo(3001L);
        assertThat(saved.getTargetUrl()).isEqualTo("/results/3001");
        assertThat(saved.getDedupKey()).isEqualTo("inspection-result:3001");
        assertThat(saved.getIsRead()).isFalse();
    }

    @Test
    @DisplayName("NOTI-001-03 DEFECT (REALTIME) - 알림은 생성하되 targetUrl=null (이동 페이지 없음)")
    void realtimeDefectCreatesNotificationWithoutTargetUrl() throws TimeoutException {
        InspectionRun realtimeRun = InspectionRun.builder()
                .inspectionId(100L)
                .organizationId(1L)
                .userId(7L)
                .targetId(20L)
                .runType(RunType.REALTIME)
                .runStatus(RunStatus.PROCESSING)
                .build();
        when(loadInspectionRunPort.findRunById(100L)).thenReturn(Optional.of(realtimeRun));
        when(callAiInspectionPort.call(any())).thenReturn(aiResult(0.95, 0.90, "PASSED"));

        worker.poll();

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(saveNotificationPort).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getNotificationType()).isEqualTo(NotificationType.DEFECT_DETECTED);
        assertThat(saved.getRelatedType()).isEqualTo("RESULT");
        assertThat(saved.getRelatedId()).isEqualTo(3001L);
        assertThat(saved.getTargetUrl()).isNull();
        assertThat(saved.getDedupKey()).isEqualTo("inspection-result:3001");
    }

    @Test
    @DisplayName("No.37 RECHECK - REINSPECTION_REQUIRED + WARNING 알림 1건 생성")
    void recheckCreatesWarningNotification() throws TimeoutException {
        when(callAiInspectionPort.call(any())).thenReturn(aiResult(0.30, 0.30, "PASSED"));

        worker.poll();

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(saveNotificationPort).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getNotificationType()).isEqualTo(NotificationType.REINSPECTION_REQUIRED);
        assertThat(saved.getSeverity()).isEqualTo(NotificationSeverity.WARNING);
        assertThat(saved.getDedupKey()).isEqualTo("inspection-result:3001");
    }

    @Test
    @DisplayName("No.37 RECHECK + QUALITY_FAILED - 알림은 RECHECK 기준 1건만 생성")
    void qualityFailedAlsoCreatesRecheckNotification() throws TimeoutException {
        when(callAiInspectionPort.call(any())).thenReturn(aiResult(0.95, 0.90, "FAILED"));

        worker.poll();

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(saveNotificationPort).save(captor.capture());
        assertThat(captor.getValue().getNotificationType()).isEqualTo(NotificationType.REINSPECTION_REQUIRED);
        assertThat(captor.getValue().getSeverity()).isEqualTo(NotificationSeverity.WARNING);
    }

    @Test
    @DisplayName("No.15 FastAPI decisionCode 무시 - FastAPI=NORMAL이라도 Spring 자체 BOUNDARY 판정으로 RECHECK 등록")
    void ignoresFastApiDecisionCodeAndUsesSpringDecision() throws TimeoutException {
        AiInspectionResult fastApiSaysNormal = AiInspectionResult.builder()
                .score(0.78).confidence(0.90).modelVersionId(50L)
                .decisionCode("NORMAL")
                .quality(AiInspectionResult.Quality.builder().status("PASSED").build())
                .build();
        when(callAiInspectionPort.call(any())).thenReturn(fastApiSaysNormal);

        worker.poll();

        ArgumentCaptor<ReviewQueue> captor = ArgumentCaptor.forClass(ReviewQueue.class);
        verify(saveReviewQueuePort).save(captor.capture());
        assertThat(captor.getValue().getQueuedReason()).isEqualTo(ReviewQueuedReason.BOUNDARY_SCORE);
    }

    private AiInspectionResult aiResult(double score, double confidence, String qualityStatus) {
        return AiInspectionResult.builder()
                .score(score).confidence(confidence).modelVersionId(50L)
                .decisionCode("OVERRIDE_NORMAL")
                .quality(AiInspectionResult.Quality.builder().status(qualityStatus).build())
                .build();
    }

    private AsyncJob asyncJob() {
        return AsyncJob.builder()
                .jobId(900L)
                .jobType(AsyncJobType.AI_IMAGE_INFERENCE)
                .jobStatus(AsyncJobStatus.PENDING)
                .targetType("INSPECTION")
                .targetId(100L)
                .build();
    }

    private InspectionRun run() {
        return InspectionRun.builder()
                .inspectionId(100L)
                .organizationId(1L)
                .userId(7L)
                .targetId(20L)
                .runType(RunType.UPLOAD)
                .runStatus(RunStatus.PROCESSING)
                .build();
    }

    private InspectionInput fullFrameInput() {
        return InspectionInput.builder()
                .inspectionInputId(200L)
                .inspectionId(100L)
                .fileId(7L)
                .roiMode(RoiMode.FULL_FRAME)
                .qualityGateEnabled(true)
                .build();
    }

    private StoredFile storedFile(String objectKey) {
        return StoredFile.builder()
                .fileId(System.identityHashCode(objectKey) + 1000L)
                .storageType(StorageType.MINIO)
                .bucketName("inspection-artifacts")
                .objectKey(objectKey)
                .build();
    }

    private ModelDeploymentJpaEntity deployment() {
        try {
            var ctor = ModelDeploymentJpaEntity.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            ModelDeploymentJpaEntity entity = ctor.newInstance();
            setField(entity, "deploymentId", 700L);
            setField(entity, "organizationId", 1L);
            setField(entity, "targetId", 20L);
            setField(entity, "modelVersionId", 50L);
            return entity;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private ModelVersionJpaEntity version() {
        try {
            var ctor = ModelVersionJpaEntity.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            ModelVersionJpaEntity entity = ctor.newInstance();
            setField(entity, "modelVersionId", 50L);
            setField(entity, "modelCategory", ModelCategory.OBJECT);
            setField(entity, "modelProfile", ModelProfile.PERFORMANCE);
            setField(entity, "framework", "PYTORCH");
            setField(entity, "inputSize", "224x224");
            return entity;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private ModelArtifactJpaEntity artifact(ModelArtifactType type, Long fileId) {
        try {
            var ctor = ModelArtifactJpaEntity.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            ModelArtifactJpaEntity entity = ctor.newInstance();
            setField(entity, "artifactType", type);
            setField(entity, "fileId", fileId);
            return entity;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void setField(Object target, String name, Object value) throws IllegalAccessException, NoSuchFieldException {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
