package com.example.factoryguard.application.service.inspection;

import com.example.factoryguard.adapter.out.storage.minio.MinioProperties;
import com.example.factoryguard.adapter.out.storage.minio.MinioStorageAdapter;
import com.example.factoryguard.application.dto.inspection.ResolvedThreshold;
import com.example.factoryguard.application.dto.inspection.SubmitInspectionCommand;
import com.example.factoryguard.application.dto.inspection.SubmitInspectionResult;
import com.example.factoryguard.application.port.in.inspection.SubmitInspectionUseCase;
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
import com.example.factoryguard.domain.inspection.model.InputSourceType;
import com.example.factoryguard.domain.inspection.model.InspectionEventType;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@Transactional
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
    private final InspectionRunRecorder runRecorder;
    private final InspectionInputRecorder inputRecorder;
    private final InspectionEventLogger eventLogger;
    private final FileValidator fileValidator;
    private final MinioStorageAdapter minioStorageAdapter;
    private final MinioProperties minioProperties;
    private final PersistUploadedFilePort persistUploadedFilePort;

    @Override
    public SubmitInspectionResult execute(SubmitInspectionCommand command) {
        validateSession(command.getUserId(), command.getSessionId());
        User user = validateUserStatus(command.getUserId());
        fileValidator.validate(command.getFile());

        ResolvedThreshold resolved = resolveInspectionThresholdService.resolve(
                command.getUserId(), command.getThresholdId());
        validateTargetAccess(command.getTargetId(), user.getOrganizationId());

        Long orgId = user.getOrganizationId();
        Long userId = command.getUserId();
        MultipartFile file = command.getFile();
        String fingerprint = PayloadFingerprintCalculator.compute(
                orgId, userId, command.getTargetId(), command.getThresholdId(),
                null, file.getOriginalFilename(), file.getContentType(), file.getSize()
        );

        String idempotencyKey = resolveIdempotencyKey(command.getIdempotencyKey());
        boolean clientProvidedKey = command.getIdempotencyKey() != null && !command.getIdempotencyKey().isBlank();

        if (clientProvidedKey) {
            var existingOpt = loadInspectionRunPort
                    .findByOrganizationIdAndUserIdAndIdempotencyKey(orgId, userId, idempotencyKey);
            if (existingOpt.isPresent()) {
                return buildReplay(existingOpt.get(), fingerprint);
            }
        }

        StoredFile storedFile = uploadAndPersist(file, userId);
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

        inputRecorder.record(InspectionInput.builder()
                .inspectionId(runId)
                .fileId(storedFile.getFileId())
                .sourceType(InputSourceType.FILE)
                .sourceName(file.getOriginalFilename())
                .mimeType(file.getContentType())
                .build());

        eventLogger.log(runId, InspectionEventType.UPLOAD_RECEIVED, file.getOriginalFilename());
        eventLogger.log(runId, InspectionEventType.INPUT_SAVED, "input persisted");

        runRecorder.transitTo(runId, RunStatus.PROCESSING);
        eventLogger.log(runId, InspectionEventType.PROCESS_STARTED, "processing queued");

        // TODO: define POST /ai/v1/inference/anomaly contract, call AI server, then persist result/artifacts/images/regions.
        return SubmitInspectionResult.accepted(run.toBuilder().runStatus(RunStatus.PROCESSING).build(), false);
    }

    private InspectionRun createRun(SubmitInspectionCommand command, ResolvedThreshold resolved, Long orgId,
                                    Long userId, String idempotencyKey, String fingerprint, MultipartFile file) {
        return runRecorder.create(InspectionRun.builder()
                .organizationId(orgId)
                .userId(userId)
                .targetId(command.getTargetId())
                .runType(RunType.UPLOAD)
                .inputType("FILE")
                .sourceType("UPLOAD")
                .sourceId(file.getOriginalFilename())
                .runStatus(RunStatus.PENDING)
                .appliedThreshold(BigDecimal.valueOf(resolved.getAnomalyThreshold()))
                .idempotencyKey(idempotencyKey)
                .payloadFingerprint(fingerprint)
                .startedAt(LocalDateTime.now())
                .build());
    }

    private StoredFile uploadAndPersist(MultipartFile file, Long userId) {
        String bucket = minioProperties.getBucketInspectionArtifacts();
        String objectKey = buildObjectKey(file.getOriginalFilename());
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
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "검사 파일 업로드에 실패했습니다.");
        }

        return persistUploadedFilePort.save(StoredFile.builder()
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
                .build());
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
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "SHA-256 알고리즘을 사용할 수 없습니다.");
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "검사 파일 체크섬 계산에 실패했습니다.");
        }
    }

    private String buildObjectKey(String originalFilename) {
        return "inspections/" + UUID.randomUUID() + "_" + originalFilename;
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

    private void validateSession(Long userId, String sessionId) {
        if (sessionId == null) {
            throw new BusinessException(ErrorCode.SESSION_INVALID);
        }
        String current = tokenStorePort.getSessionId(userId).orElse(null);
        if (current == null || current.isBlank() || !current.equals(sessionId)) {
            throw new BusinessException(ErrorCode.SESSION_INVALID);
        }
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
}
