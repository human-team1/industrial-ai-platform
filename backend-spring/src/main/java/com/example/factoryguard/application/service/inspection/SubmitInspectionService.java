package com.example.factoryguard.application.service.inspection;

import com.example.factoryguard.adapter.out.storage.minio.MinioProperties;
import com.example.factoryguard.adapter.out.storage.minio.MinioStorageAdapter;
import com.example.factoryguard.application.dto.inspection.ResolvedThreshold;
import com.example.factoryguard.application.dto.inspection.SubmitInspectionCommand;
import com.example.factoryguard.application.dto.inspection.SubmitInspectionResult;
import com.example.factoryguard.application.dto.operation.RecordOperationLogCommand;
import com.example.factoryguard.application.port.in.inspection.SubmitInspectionUseCase;
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
import com.example.factoryguard.domain.inspection.model.InputSourceType;
import com.example.factoryguard.domain.inspection.model.InspectionInput;
import com.example.factoryguard.domain.inspection.model.InspectionResult;
import com.example.factoryguard.domain.inspection.model.InspectionRun;
import com.example.factoryguard.domain.inspection.model.RunStatus;
import com.example.factoryguard.domain.inspection.model.RunType;
import com.example.factoryguard.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmitInspectionService implements SubmitInspectionUseCase {

    private static final int IDEMPOTENCY_KEY_MAX_LENGTH = 255;

    private final TokenStorePort tokenStorePort;
    private final FindUserByIdPort findUserByIdPort;
    private final LoadAnalysisTargetPort loadAnalysisTargetPort;
    private final LoadInspectionResultPort loadInspectionResultPort;
    private final LoadReviewQueuePort loadReviewQueuePort;
    private final LoadInspectionRunPort loadInspectionRunPort;
    private final ResolveInspectionThresholdService resolveInspectionThresholdService;
    private final InspectionUploadTransactionService inspectionUploadTransactionService;
    private final FileValidator fileValidator;
    private final MinioStorageAdapter minioStorageAdapter;
    private final MinioProperties minioProperties;
    private final RecordOperationLogUseCase recordOperationLogUseCase;
    private final InspectionIdempotencyCachePort inspectionIdempotencyCachePort;
    private final SessionValidationService sessionValidationService;

    @Override
    public SubmitInspectionResult execute(SubmitInspectionCommand command) {
        sessionValidationService.validate(command.getUserId(), command.getSessionId());
        User user = validateUserStatus(command.getUserId());
        fileValidator.validate(command.getFile());

        ResolvedThreshold resolved = resolveInspectionThresholdService.resolve(
                command.getUserId(), command.getThresholdId());
        validateTargetAccess(command.getTargetId(), user.getOrganizationId());

        Long orgId = user.getOrganizationId();
        Long userId = command.getUserId();
        MultipartFile file = command.getFile();
        String inputMode = resolveInputMode(command, file);
        String sourceType = resolveSourceType(command);
        String fingerprint = PayloadFingerprintCalculator.compute(
                orgId, userId, command.getTargetId(), command.getThresholdId(),
                null, file.getOriginalFilename(), file.getContentType(), file.getSize()
        );

        String idempotencyKey = resolveIdempotencyKey(command.getIdempotencyKey());
        boolean clientProvidedKey = command.getIdempotencyKey() != null && !command.getIdempotencyKey().isBlank();

        if (clientProvidedKey) {
            findCachedFingerprint(orgId, userId, idempotencyKey)
                    .filter(existingFingerprint -> !existingFingerprint.equals(fingerprint))
                    .ifPresent(existingFingerprint -> {
                        throw new BusinessException(ErrorCode.IDEMPOTENCY_CONFLICT);
                    });
            Optional<InspectionRun> existingOpt = loadInspectionRunPort
                    .findByOrganizationIdAndUserIdAndIdempotencyKey(orgId, userId, idempotencyKey);
            if (existingOpt.isPresent()) {
                return buildReplay(existingOpt.get(), fingerprint);
            }
            reserveIdempotencyKey(orgId, userId, idempotencyKey, fingerprint);
        }

        InspectionRun run;
        try {
            run = createRun(command, resolved, orgId, userId, idempotencyKey, fingerprint, file);
        } catch (DataIntegrityViolationException race) {
            log.info("Idempotency race detected, key={}", idempotencyKey);
            InspectionRun existing = loadInspectionRunPort
                    .findByOrganizationIdAndUserIdAndIdempotencyKey(orgId, userId, idempotencyKey)
                    .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_ERROR));
            return buildReplay(existing, fingerprint);
        }

        Long runId = run.getInspectionId();
        String bucket = minioProperties.getBucketInspectionArtifacts();
        String objectKey = buildObjectKey(runId, file.getOriginalFilename());
        boolean uploaded = false;

        try {
            StoredFile uploadedFile = uploadToMinio(file, userId, bucket, objectKey);
            uploaded = true;
            inspectionUploadTransactionService.persistFileInputAndMarkProcessing(
                    runId,
                    uploadedFile,
                    InspectionInput.builder()
                            .inspectionId(runId)
                            .sourceType(resolveInputSourceType(sourceType))
                            .sourceName(file.getOriginalFilename())
                            .mimeType(file.getContentType())
                            .frameCount(isBrowserCamera(sourceType) ? 1 : null)
                            .build(),
                    file.getOriginalFilename()
            );
        } catch (BusinessException exception) {
            markUploadFailed(runId, userId, exception, bucket, objectKey, uploaded);
            throw exception;
        } catch (Exception exception) {
            markUploadFailed(runId, userId, exception, bucket, objectKey, uploaded);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "검사 파일 저장 중 오류가 발생했습니다.");
        }

        // FastAPI inference is intentionally not called here. The run remains queued/processing.
        return SubmitInspectionResult.accepted(run.toBuilder().runStatus(RunStatus.PROCESSING).build(), false);
    }

    private InspectionRun createRun(SubmitInspectionCommand command, ResolvedThreshold resolved, Long orgId,
                                    Long userId, String idempotencyKey, String fingerprint, MultipartFile file) {
        String inputMode = resolveInputMode(command, file);
        String sourceType = resolveSourceType(command);
        return inspectionUploadTransactionService.createPendingRun(InspectionRun.builder()
                .organizationId(orgId)
                .userId(userId)
                .targetId(command.getTargetId())
                .runType(RunType.UPLOAD)
                .inputType(inputMode)
                .sourceType(sourceType)
                .sourceId(file.getOriginalFilename())
                .runStatus(RunStatus.PENDING)
                .appliedThreshold(BigDecimal.valueOf(resolved.getAnomalyThreshold()))
                .idempotencyKey(idempotencyKey)
                .payloadFingerprint(fingerprint)
                .startedAt(LocalDateTime.now())
                .build());
    }

    private StoredFile uploadToMinio(MultipartFile file, Long userId, String bucket, String objectKey) {
        String checksum = calculateSha256(file);
        try {
            minioStorageAdapter.upload(
                    bucket,
                    objectKey,
                    file.getInputStream(),
                    file.getSize(),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    checksum
            );
        } catch (Exception exception) {
            recordOperationLogUseCase.recordOperationLog(RecordOperationLogCommand.builder()
                    .eventType("FILE_UPLOAD_FAILED")
                    .eventStatus("FAILED")
                    .logLevel("ERROR")
                    .sourceComponent("SPRING_API")
                    .actorUserId(userId)
                    .detailMessage("inspection file upload failed")
                    .relatedPath("/api/v1/inspections")
                    .build());
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "검사 파일 업로드에 실패했습니다.");
        }

        return StoredFile.builder()
                .storageType(StorageType.MINIO)
                .bucketName(bucket)
                .objectKey(objectKey)
                .filePath(null)
                .fileName(file.getOriginalFilename())
                .fileExt(fileExt(file.getOriginalFilename()))
                .mimeType(file.getContentType())
                .fileSize(file.getSize())
                .checksum(checksum)
                .createdAt(LocalDateTime.now())
                .createdBy(userId)
                .build();
    }

    private SubmitInspectionResult buildReplay(InspectionRun existing, String fingerprint) {
        if (existing.getPayloadFingerprint() != null
                && !existing.getPayloadFingerprint().equals(fingerprint)) {
            throw new BusinessException(ErrorCode.IDEMPOTENCY_CONFLICT);
        }

        RunStatus status = existing.getRunStatus();
        if (status == RunStatus.PENDING || status == RunStatus.PROCESSING) {
            return SubmitInspectionResult.accepted(existing, true);
        }
        if (status == RunStatus.FAILED) {
            throw new BusinessException(mapFailedErrorCode(existing.getErrorCode()));
        }
        List<InspectionResult> results = loadInspectionResultPort
                .findAllByInspectionId(existing.getInspectionId());
        InspectionResult result = results.isEmpty() ? null : results.get(0);
        boolean reviewQueued = result != null
                && loadReviewQueuePort.findByResultId(result.getResultId()).isPresent();
        return SubmitInspectionResult.ofReplay(existing, result, reviewQueued);
    }

    private void validateTargetAccess(Long targetId, Long organizationId) {
        if (targetId == null) {
            return;
        }
        AnalysisTarget target = loadAnalysisTargetPort.findById(targetId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TARGET_NOT_FOUND));
        if (!target.getOrganizationId().equals(organizationId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private String resolveIdempotencyKey(String rawKey) {
        if (rawKey == null || rawKey.isBlank()) {
            return UUID.randomUUID().toString();
        }
        String idempotencyKey = rawKey.trim();
        if (idempotencyKey.length() > IDEMPOTENCY_KEY_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_IDEMPOTENCY_KEY);
        }
        return idempotencyKey;
    }

    private String resolveInputMode(SubmitInspectionCommand command, MultipartFile file) {
        String rawMode = command.getInputMode();
        if (rawMode != null && !rawMode.isBlank()) {
            String normalized = rawMode.trim().toUpperCase(Locale.ROOT);
            if ("IMAGE".equals(normalized) || "VIDEO".equals(normalized)) {
                return normalized;
            }
        }

        String contentType = file.getContentType();
        if (contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith("video/")) {
            return "VIDEO";
        }
        return "IMAGE";
    }

    private String resolveSourceType(SubmitInspectionCommand command) {
        String rawSourceType = command.getSourceType();
        if (rawSourceType == null || rawSourceType.isBlank()) {
            return "IMAGE";
        }

        String normalized = rawSourceType.trim().toUpperCase(Locale.ROOT);
        if ("BROWSER_CAMERA".equals(normalized)) {
            return "BROWSER_CAMERA";
        }
        return "IMAGE";
    }

    private InputSourceType resolveInputSourceType(String sourceType) {
        if (isBrowserCamera(sourceType)) {
            return InputSourceType.BROWSER_CAMERA;
        }
        return InputSourceType.FILE;
    }

    private boolean isBrowserCamera(String sourceType) {
        return "BROWSER_CAMERA".equalsIgnoreCase(sourceType);
    }

    private ErrorCode mapFailedErrorCode(String errorCode) {
        if (ErrorCode.AI_TIMEOUT.name().equals(errorCode)) return ErrorCode.AI_TIMEOUT;
        if (ErrorCode.AI_SERVER_ERROR.name().equals(errorCode)) return ErrorCode.AI_SERVER_ERROR;
        if (ErrorCode.AI_REQUEST_INVALID.name().equals(errorCode)) return ErrorCode.AI_REQUEST_INVALID;
        return ErrorCode.INSPECTION_FAILED;
    }

    private String calculateSha256(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(file.getBytes()));
        } catch (NoSuchAlgorithmException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "SHA-256 algorithm is not available.");
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "검사 파일 체크섬 계산에 실패했습니다.");
        }
    }

    private String buildObjectKey(Long inspectionId, String originalFilename) {
        String ext = fileExt(originalFilename);
        if (ext.isBlank()) {
            return "inspections/" + inspectionId + "/inputs/" + UUID.randomUUID();
        }
        return "inspections/" + inspectionId + "/inputs/" + UUID.randomUUID() + "." + ext;
    }

    private String fileExt(String filename) {
        if (filename == null) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private User validateUserStatus(Long userId) {
        User user = findUserByIdPort.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        switch (user.getStatus()) {
            case ACTIVE -> { return user; }
            case PENDING -> throw new BusinessException(ErrorCode.PENDING_APPROVAL);
            case REJECTED -> throw new BusinessException(ErrorCode.ACCOUNT_REJECTED);
            default -> throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }

    private Optional<String> findCachedFingerprint(Long orgId, Long userId, String idempotencyKey) {
        try {
            return inspectionIdempotencyCachePort.findFingerprint(orgId, userId, idempotencyKey);
        } catch (Exception exception) {
            log.warn("Redis idempotency lookup failed, organizationId={}, userId={}, idempotencyKey={}, reason={}",
                    orgId, userId, idempotencyKey, exception.getMessage());
            return Optional.empty();
        }
    }

    private void reserveIdempotencyKey(Long orgId, Long userId, String idempotencyKey, String fingerprint) {
        try {
            boolean reserved = inspectionIdempotencyCachePort.reserve(orgId, userId, idempotencyKey, fingerprint);
            if (!reserved) {
                log.info("Redis idempotency reservation already exists, organizationId={}, userId={}, idempotencyKey={}",
                        orgId, userId, idempotencyKey);
            }
        } catch (Exception exception) {
            log.warn("Redis idempotency reservation failed, organizationId={}, userId={}, idempotencyKey={}, reason={}",
                    orgId, userId, idempotencyKey, exception.getMessage());
        }
    }

    private void markUploadFailed(Long inspectionId, Long userId, Exception exception,
                                  String bucket, String objectKey, boolean uploaded) {
        log.warn("Inspection upload persistence failed, inspectionId={}, userId={}, reason={}",
                inspectionId, userId, exception.getMessage());
        if (uploaded) {
            compensateUploadedObject(inspectionId, userId, bucket, objectKey);
        }
        inspectionUploadTransactionService.markFailedRequiresNew(
                inspectionId,
                ErrorCode.INTERNAL_ERROR.name(),
                "inspection upload failed: " + exception.getMessage()
        );
    }

    private void compensateUploadedObject(Long inspectionId, Long userId, String bucket, String objectKey) {
        try {
            boolean deleted = minioStorageAdapter.delete(bucket, objectKey);
            log.warn("Compensating MinIO delete result, inspectionId={}, userId={}, deleted={}",
                    inspectionId, userId, deleted);
        } catch (Exception deleteException) {
            log.warn("Compensating MinIO delete failed, inspectionId={}, userId={}, reason={}",
                    inspectionId, userId, deleteException.getMessage());
            recordOperationLogUseCase.recordOperationLog(RecordOperationLogCommand.builder()
                    .eventType("MINIO_COMPENSATION_DELETE_FAILED")
                    .eventStatus("FAILED")
                    .logLevel("WARN")
                    .sourceComponent("MINIO")
                    .actorUserId(userId)
                    .detailMessage("inspection upload compensation delete failed")
                    .relatedPath("/api/v1/inspections/upload")
                    .build());
        }
    }
}
