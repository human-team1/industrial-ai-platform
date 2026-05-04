package com.example.factoryguard.application.service.inspection;

import com.example.factoryguard.adapter.out.storage.minio.MinioProperties;
import com.example.factoryguard.application.dto.inspection.AiInspectionCommand;
import com.example.factoryguard.application.dto.inspection.AiInspectionResult;
import com.example.factoryguard.application.dto.inspection.ResolvedThreshold;
import com.example.factoryguard.application.dto.operation.RecordOperationLogCommand;
import com.example.factoryguard.application.exception.ai.AiInvalidRequestException;
import com.example.factoryguard.application.exception.ai.AiServerException;
import com.example.factoryguard.application.port.in.operation.RecordOperationLogUseCase;
import com.example.factoryguard.application.port.out.file.LoadFilePort;
import com.example.factoryguard.application.port.out.file.PersistUploadedFilePort;
import com.example.factoryguard.application.port.out.inspection.CallAiInspectionPort;
import com.example.factoryguard.application.port.out.inspection.LoadInspectionInputPort;
import com.example.factoryguard.application.port.out.inspection.LoadInspectionRunPort;
import com.example.factoryguard.application.port.out.inspection.SaveInspectionResultPort;
import com.example.factoryguard.application.port.out.inspection.SaveInspectionRunPort;
import com.example.factoryguard.application.port.out.model.ModelManagementPort;
import com.example.factoryguard.application.port.out.notification.SaveNotificationPort;
import com.example.factoryguard.application.port.out.operation.ClaimAsyncJobPort;
import com.example.factoryguard.application.port.out.operation.SaveAsyncJobPort;
import com.example.factoryguard.application.port.out.result.SaveResultArtifactPort;
import com.example.factoryguard.application.port.out.result.SaveResultImagePort;
import com.example.factoryguard.application.port.out.review.SaveReviewQueuePort;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.config.inspection.AiJobWorkerProperties;
import com.example.factoryguard.config.web.RequestIdFilter;
import com.example.factoryguard.domain.file.model.StoredFile;
import com.example.factoryguard.domain.file.vo.StorageType;
import com.example.factoryguard.domain.inspection.model.DecisionCode;
import com.example.factoryguard.domain.inspection.model.InspectionEventType;
import com.example.factoryguard.domain.inspection.model.InspectionInput;
import com.example.factoryguard.domain.inspection.model.InspectionResult;
import com.example.factoryguard.domain.inspection.model.InspectionRun;
import com.example.factoryguard.domain.inspection.model.RunStatus;
import com.example.factoryguard.domain.inspection.vo.RoiMode;
import com.example.factoryguard.domain.model.vo.DeploymentScope;
import com.example.factoryguard.domain.model.vo.ModelArtifactType;
import com.example.factoryguard.domain.notification.model.Notification;
import com.example.factoryguard.domain.notification.vo.NotificationSeverity;
import com.example.factoryguard.domain.notification.vo.NotificationType;
import com.example.factoryguard.domain.operation.model.AsyncJob;
import com.example.factoryguard.domain.operation.vo.AsyncJobStatus;
import com.example.factoryguard.domain.operation.vo.AsyncJobType;
import com.example.factoryguard.domain.result.model.Image;
import com.example.factoryguard.domain.result.model.ResultArtifact;
import com.example.factoryguard.domain.result.vo.ArtifactType;
import com.example.factoryguard.domain.result.vo.ImageRole;
import com.example.factoryguard.domain.review.model.ReviewQueue;
import com.example.factoryguard.domain.review.vo.ReviewQueueStatus;
import com.example.factoryguard.domain.review.vo.ReviewQueuedReason;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiInferenceJobWorker {

    private static final String RESULT_STATUS_SUCCESS = "SUCCESS";
    private static final String RESULT_STATUS_REVIEW_REQUIRED = "REVIEW_REQUIRED";
    private static final String RESULT_STATUS_FAILED = "FAILED";

    private final AiJobWorkerProperties properties;
    private final ClaimAsyncJobPort claimAsyncJobPort;
    private final SaveAsyncJobPort saveAsyncJobPort;
    private final LoadInspectionRunPort loadInspectionRunPort;
    private final SaveInspectionRunPort saveInspectionRunPort;
    private final LoadInspectionInputPort loadInspectionInputPort;
    private final LoadFilePort loadFilePort;
    private final ModelManagementPort modelManagementPort;
    private final CallAiInspectionPort callAiInspectionPort;
    private final SaveInspectionResultPort saveInspectionResultPort;
    private final SaveResultArtifactPort saveResultArtifactPort;
    private final SaveResultImagePort saveResultImagePort;
    private final SaveReviewQueuePort saveReviewQueuePort;
    private final SaveNotificationPort saveNotificationPort;
    private final PersistUploadedFilePort persistUploadedFilePort;
    private final InspectionEventLogger inspectionEventLogger;
    private final RecordOperationLogUseCase recordOperationLogUseCase;
    private final MinioProperties minioProperties;
    private final ResolveInspectionThresholdService resolveInspectionThresholdService;
    private final InspectionDecisionEvaluator inspectionDecisionEvaluator;

    private final AtomicBoolean running = new AtomicBoolean(false);

    @Scheduled(fixedDelayString = "${inspection.ai-job-worker.poll-interval-ms:1000}")
    public void poll() {
        if (!properties.isEnabled()) {
            return;
        }
        if (!running.compareAndSet(false, true)) {
            return;
        }

        MDC.put(RequestIdFilter.MDC_KEY, UUID.randomUUID().toString());
        try {
            Optional<AsyncJob> claimed = claimAsyncJobPort.claimNextPending(AsyncJobType.AI_IMAGE_INFERENCE);
            claimed.ifPresent(this::processJob);
        } finally {
            MDC.remove(RequestIdFilter.MDC_KEY);
            running.set(false);
        }
    }

    private void processJob(AsyncJob job) {
        Long inspectionId = job.getTargetId();
        if (inspectionId == null) {
            failWithoutRun(job, "INVALID_JOB_TARGET", "inspection target is missing");
            return;
        }

        InspectionRun run = loadInspectionRunPort.findRunById(inspectionId).orElse(null);
        if (run == null) {
            failWithoutRun(job, ErrorCode.INSPECTION_NOT_FOUND.name(), "inspection run not found");
            return;
        }

        long startedAt = System.currentTimeMillis();
        log.info("[AI_JOB_PICKED] requestId={} inspectionId={} jobId={}", requestId(), inspectionId, job.getJobId());
        recordOperationLogUseCase.recordOperationLog(RecordOperationLogCommand.builder()
                .eventType("AI_JOB_PICKED")
                .eventStatus("PROCESSING")
                .logLevel("INFO")
                .sourceComponent("SPRING_API")
                .actorUserId(run.getUserId())
                .detailMessage("inspectionId=" + inspectionId + ", jobId=" + job.getJobId())
                .relatedPath("/api/v1/inspections/" + inspectionId)
                .build());

        try {
            inspectionEventLogger.log(inspectionId, InspectionEventType.AI_CALLED, "worker started ai inference");
            InspectionInput input = loadInspectionInputPort.findByInspectionId(run.getInspectionId()).stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("inspection input not found"));
            // TODO: MVP 단계에서는 사용자 활성/시스템 기본 임계값만 사용한다.
            //       후속 PR에서 사용자 설정 -> 회사/설비(target/organization) 기본값으로의 fallback 체인을 추가해야 한다.
            ResolvedThreshold threshold = resolveInspectionThresholdService.resolve(
                    run.getUserId(), null);
            AiInspectionResult result = callAiInspectionPort.call(buildAiCommand(run, input, threshold));
            persistSuccessfulResult(run, job, result, input, threshold);

            long latencyMs = System.currentTimeMillis() - startedAt;
            log.info("[AI_JOB_COMPLETED] requestId={} inspectionId={} jobId={} latencyMs={}",
                    requestId(), inspectionId, job.getJobId(), latencyMs);
            recordOperationLogUseCase.recordOperationLog(RecordOperationLogCommand.builder()
                    .eventType("AI_JOB_COMPLETED")
                    .eventStatus("SUCCESS")
                    .logLevel("INFO")
                    .sourceComponent("SPRING_API")
                    .actorUserId(run.getUserId())
                    .detailMessage("inspectionId=" + inspectionId + ", jobId=" + job.getJobId() + ", latencyMs=" + latencyMs)
                    .relatedPath("/api/v1/inspections/" + inspectionId)
                    .build());
        } catch (TimeoutException exception) {
            markJobFailed(job, run, ErrorCode.AI_TIMEOUT.name(), "AI 서버 응답 시간이 초과되었습니다.");
        } catch (AiInvalidRequestException exception) {
            String errorCode = exception.getStatus() == 429 ? "INFERENCE_QUEUE_FULL" : ErrorCode.AI_REQUEST_INVALID.name();
            markJobFailed(job, run, errorCode, safeMessage(exception));
        } catch (AiServerException exception) {
            String errorCode = exception.getStatus() == 429 ? "INFERENCE_QUEUE_FULL" : ErrorCode.AI_SERVER_ERROR.name();
            markJobFailed(job, run, errorCode, safeMessage(exception));
        } catch (IllegalStateException exception) {
            String errorCode = "INPUT_FILE_NOT_FOUND".equals(exception.getMessage())
                    ? "INPUT_FILE_NOT_FOUND"
                    : "MODEL_ARTIFACT_MISSING";
            markJobFailed(job, run, errorCode, safeMessage(exception));
        } catch (Exception exception) {
            log.error("[AI_JOB_FAILED] requestId={} inspectionId={} jobId={} errorCode={}",
                    requestId(), inspectionId, job.getJobId(), ErrorCode.INSPECTION_FAILED.name(), exception);
            markJobFailed(job, run, ErrorCode.INSPECTION_FAILED.name(), safeMessage(exception));
        }
    }

    private AiInspectionCommand buildAiCommand(InspectionRun run, InspectionInput input, ResolvedThreshold threshold) {
        if (input.getFileId() == null) {
            throw new IllegalStateException("INPUT_FILE_NOT_FOUND");
        }
        StoredFile originalFile = loadFilePort.findById(input.getFileId())
                .orElseThrow(() -> new IllegalStateException("INPUT_FILE_NOT_FOUND"));

        var deployment = modelManagementPort.findActiveDeployments(run.getOrganizationId(), run.getTargetId(), DeploymentScope.TARGET)
                .stream()
                .findFirst()
                .or(() -> modelManagementPort.findActiveDeployments(run.getOrganizationId(), null, DeploymentScope.ORGANIZATION)
                        .stream()
                        .findFirst())
                .orElseThrow(() -> new IllegalStateException("active model deployment not found"));

        var version = modelManagementPort.findModelVersionById(deployment.getModelVersionId())
                .orElseThrow(() -> new IllegalStateException("model version not found"));

        Map<ModelArtifactType, StoredFile> artifactFiles = loadArtifactFiles(version.getModelVersionId());
        StoredFile ckpt = requiredArtifact(artifactFiles, ModelArtifactType.CKPT);
        StoredFile config = requiredArtifact(artifactFiles, ModelArtifactType.CONFIG);
        StoredFile memoryBank = requiredArtifact(artifactFiles, ModelArtifactType.MEMORY_BANK);
        StoredFile labels = artifactFiles.get(ModelArtifactType.LABELS);

        RoiMode roiMode = input.getRoiMode() == null ? RoiMode.FULL_FRAME : input.getRoiMode();
        AiInspectionCommand.Roi.RoiBuilder roiBuilder = AiInspectionCommand.Roi.builder()
                .roiMode(roiMode.name())
                .roiCoordinateType(input.getRoiCoordinateType() == null ? "NORMALIZED" : input.getRoiCoordinateType());
        if (roiMode == RoiMode.FIXED) {
            roiBuilder
                    .roiX(toDouble(input.getRoiX()))
                    .roiY(toDouble(input.getRoiY()))
                    .roiWidth(toDouble(input.getRoiWidth()))
                    .roiHeight(toDouble(input.getRoiHeight()));
        }
        boolean qualityGateEnabled = input.getQualityGateEnabled() == null
                ? true
                : input.getQualityGateEnabled();

        return AiInspectionCommand.builder()
                .inspectionId(run.getInspectionId())
                .fileKey(originalFile.getObjectKey())
                .targetId(run.getTargetId())
                .model(AiInspectionCommand.Model.builder()
                        .modelVersionId(version.getModelVersionId())
                        .modelCategory(version.getModelCategory().name())
                        .modelProfile(version.getModelProfile().name())
                        .framework(version.getFramework())
                        .inputSize(version.getInputSize())
                        .ckptFileKey(ckpt.getObjectKey())
                        .configFileKey(config.getObjectKey())
                        .memoryBankFileKey(memoryBank.getObjectKey())
                        .labelsFileKey(labels == null ? null : labels.getObjectKey())
                        .build())
                .roi(roiBuilder.build())
                .qualityGateEnabled(qualityGateEnabled)
                .threshold(AiInspectionCommand.Threshold.builder()
                        .anomalyThreshold(threshold.getAnomalyThreshold())
                        .lowConfidenceThreshold(threshold.getLowConfidenceThreshold())
                        .build())
                .build();
    }

    private Double toDouble(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }

    private Notification buildInspectionNotification(InspectionRun run, Long resultId, DecisionCode decisionCode) {
        boolean defect = decisionCode == DecisionCode.DEFECT;
        return Notification.builder()
                .userId(run.getUserId())
                .notificationType(defect ? NotificationType.DEFECT_DETECTED : NotificationType.REINSPECTION_REQUIRED)
                .severity(defect ? NotificationSeverity.CRITICAL : NotificationSeverity.WARNING)
                .title(defect ? "이상이 감지되었습니다." : "재검사가 필요합니다.")
                .message("inspectionId=" + run.getInspectionId() + ", resultId=" + resultId)
                .relatedType("INSPECTION_RESULT")
                .relatedId(resultId)
                .targetUrl("/inspections/" + run.getInspectionId())
                .dedupKey("inspection-result:" + resultId)
                .isRead(false)
                .build();
    }

    private Map<ModelArtifactType, StoredFile> loadArtifactFiles(Long versionId) {
        Map<ModelArtifactType, StoredFile> files = new EnumMap<>(ModelArtifactType.class);
        modelManagementPort.findArtifactsByVersionId(versionId).forEach(artifact ->
                loadFilePort.findById(artifact.getFileId()).ifPresent(file -> files.put(artifact.getArtifactType(), file)));
        return files;
    }

    private StoredFile requiredArtifact(Map<ModelArtifactType, StoredFile> files, ModelArtifactType type) {
        StoredFile file = files.get(type);
        if (file == null || file.getObjectKey() == null || file.getObjectKey().isBlank()) {
            throw new IllegalStateException("required model artifact missing: " + type.name());
        }
        return file;
    }

    private void persistSuccessfulResult(InspectionRun run, AsyncJob job, AiInspectionResult result,
                                         InspectionInput input, ResolvedThreshold threshold) {
        InspectionDecisionEvaluator.Outcome outcome = inspectionDecisionEvaluator.evaluate(result, input, threshold);
        DecisionCode decisionCode = outcome.decisionCode();
        ReviewQueuedReason queuedReason = outcome.queuedReason();
        String resultStatus = decisionCode == DecisionCode.RECHECK ? RESULT_STATUS_REVIEW_REQUIRED : RESULT_STATUS_SUCCESS;
        InspectionResult saved = saveInspectionResultPort.save(InspectionResult.builder()
                .inspectionId(run.getInspectionId())
                .score(toBigDecimal(result.getScore()))
                .confidence(toBigDecimal(result.getConfidence()))
                .decisionCode(decisionCode)
                .finalDecisionCode(decisionCode)
                .resultStatus(resultStatus)
                .thresholdSource(threshold.getSource() == null ? "SYSTEM_DEFAULT" : threshold.getSource().name())
                .thresholdId(threshold.getThresholdId())
                .thresholdVersion(threshold.getThresholdVersion())
                .modelVersionId(result.getModelVersionId())
                .failureReason(null)
                .build());

        persistArtifacts(saved.getResultId(), result.getArtifacts());

        if (decisionCode == DecisionCode.RECHECK) {
            saveReviewQueuePort.save(ReviewQueue.builder()
                    .resultId(saved.getResultId())
                    .queueStatus(ReviewQueueStatus.WAITING)
                    .queuedReason(queuedReason)
                    .build());
        }

        if (decisionCode == DecisionCode.DEFECT || decisionCode == DecisionCode.RECHECK) {
            saveNotificationPort.save(buildInspectionNotification(run, saved.getResultId(), decisionCode));
        }

        saveInspectionRunPort.save(run.toBuilder()
                .runStatus(RunStatus.COMPLETED)
                .errorCode(null)
                .build());
        saveAsyncJobPort.save(AsyncJob.builder()
                .jobId(job.getJobId())
                .jobType(job.getJobType())
                .jobStatus(AsyncJobStatus.COMPLETED)
                .targetType(job.getTargetType())
                .targetId(job.getTargetId())
                .errorMessage(null)
                .createdAt(job.getCreatedAt())
                .completedAt(LocalDateTime.now())
                .build());
        inspectionEventLogger.log(run.getInspectionId(), InspectionEventType.RESULT_SAVED, "inspection result saved");
        inspectionEventLogger.log(run.getInspectionId(), InspectionEventType.COMPLETED, "inspection completed");
    }

    private void persistArtifacts(Long resultId, List<AiInspectionResult.Artifact> artifacts) {
        if (artifacts == null) {
            return;
        }
        for (AiInspectionResult.Artifact artifact : artifacts) {
            StoredFile storedFile = persistUploadedFilePort.save(StoredFile.builder()
                    .storageType(StorageType.MINIO)
                    .bucketName(minioProperties.getBucketInspectionArtifacts())
                    .objectKey(artifact.getFileKey())
                    .filePath(null)
                    .fileName(extractFileName(artifact.getFileKey()))
                    .fileExt(extractFileExt(artifact.getFileKey()))
                    .mimeType(guessMimeType(artifact.getFileKey()))
                    .fileSize(null)
                    .checksum(null)
                    .createdAt(LocalDateTime.now())
                    .createdBy(null)
                    .build());

            ArtifactType artifactType = mapArtifactType(artifact.getArtifactType());
            saveResultArtifactPort.save(ResultArtifact.builder()
                    .resultId(resultId)
                    .artifactType(artifactType)
                    .fileId(storedFile.getFileId())
                    .build());
            if (artifactType == ArtifactType.HEATMAP) {
                saveResultImagePort.save(Image.builder()
                        .resultId(resultId)
                        .fileId(storedFile.getFileId())
                        .imageRole(ImageRole.VISUALIZED)
                        .build());
            }
        }
    }

    private ArtifactType mapArtifactType(String artifactType) {
        if ("HEATMAP".equalsIgnoreCase(artifactType)) {
            return ArtifactType.HEATMAP;
        }
        if ("ANOMALY_MAP".equalsIgnoreCase(artifactType)) {
            return ArtifactType.ANOMALY_MAP;
        }
        if ("BOUNDING_BOX_IMAGE".equalsIgnoreCase(artifactType)) {
            return ArtifactType.BOUNDING_BOX_IMAGE;
        }
        return ArtifactType.REPORT;
    }

    private void markJobFailed(AsyncJob job, InspectionRun run, String errorCode, String failureReason) {
        saveInspectionResultPort.save(InspectionResult.builder()
                .inspectionId(run.getInspectionId())
                .score(null)
                .confidence(null)
                .decisionCode(null)
                .finalDecisionCode(null)
                .resultStatus(RESULT_STATUS_FAILED)
                .thresholdSource("SYSTEM_DEFAULT")
                .thresholdId(null)
                .thresholdVersion(null)
                .modelVersionId(null)
                .failureReason(failureReason)
                .build());
        saveInspectionRunPort.save(run.toBuilder()
                .runStatus(RunStatus.FAILED)
                .errorCode(errorCode)
                .build());
        saveAsyncJobPort.save(AsyncJob.builder()
                .jobId(job.getJobId())
                .jobType(job.getJobType())
                .jobStatus(AsyncJobStatus.FAILED)
                .targetType(job.getTargetType())
                .targetId(job.getTargetId())
                .errorMessage(failureReason)
                .createdAt(job.getCreatedAt())
                .completedAt(LocalDateTime.now())
                .build());
        inspectionEventLogger.logFailure(run.getInspectionId(), InspectionEventType.AI_FAILED, failureReason);
        inspectionEventLogger.logFailure(run.getInspectionId(), InspectionEventType.FAILED, failureReason);
        log.warn("[AI_JOB_FAILED] requestId={} inspectionId={} jobId={} errorCode={} detail={}",
                requestId(), run.getInspectionId(), job.getJobId(), errorCode, failureReason);
        recordOperationLogUseCase.recordOperationLog(RecordOperationLogCommand.builder()
                .eventType("AI_JOB_FAILED")
                .eventStatus("FAILED")
                .logLevel("WARN")
                .sourceComponent("SPRING_API")
                .actorUserId(run.getUserId())
                .detailMessage("inspectionId=" + run.getInspectionId() + ", jobId=" + job.getJobId() + ", errorCode=" + errorCode)
                .relatedPath("/api/v1/inspections/" + run.getInspectionId())
                .build());
    }

    private void failWithoutRun(AsyncJob job, String errorCode, String failureReason) {
        saveAsyncJobPort.save(AsyncJob.builder()
                .jobId(job.getJobId())
                .jobType(job.getJobType())
                .jobStatus(AsyncJobStatus.FAILED)
                .targetType(job.getTargetType())
                .targetId(job.getTargetId())
                .errorMessage(failureReason)
                .createdAt(job.getCreatedAt())
                .completedAt(LocalDateTime.now())
                .build());
        log.warn("[AI_JOB_FAILED] requestId={} inspectionId={} jobId={} errorCode={} detail={}",
                requestId(), job.getTargetId(), job.getJobId(), errorCode, failureReason);
    }

    private String requestId() {
        return MDC.get(RequestIdFilter.MDC_KEY);
    }

    private String safeMessage(Throwable throwable) {
        String message = throwable.getMessage();
        if (message == null || message.isBlank()) {
            return throwable.getClass().getSimpleName();
        }
        return message.length() > 500 ? message.substring(0, 500) : message;
    }

    private String extractFileName(String objectKey) {
        int index = objectKey.lastIndexOf('/');
        return index >= 0 ? objectKey.substring(index + 1) : objectKey;
    }

    private String extractFileExt(String objectKey) {
        String fileName = extractFileName(objectKey);
        int index = fileName.lastIndexOf('.');
        return index >= 0 && index < fileName.length() - 1 ? fileName.substring(index + 1) : null;
    }

    private String guessMimeType(String objectKey) {
        String ext = extractFileExt(objectKey);
        if ("png".equalsIgnoreCase(ext)) {
            return "image/png";
        }
        if ("jpg".equalsIgnoreCase(ext) || "jpeg".equalsIgnoreCase(ext)) {
            return "image/jpeg";
        }
        if ("webp".equalsIgnoreCase(ext)) {
            return "image/webp";
        }
        return "application/octet-stream";
    }

    private BigDecimal toBigDecimal(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }
}
