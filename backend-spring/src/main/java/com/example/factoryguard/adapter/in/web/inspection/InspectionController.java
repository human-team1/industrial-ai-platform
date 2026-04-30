package com.example.factoryguard.adapter.in.web.inspection;

import com.example.factoryguard.adapter.in.web.inspection.dto.InspectionEventLogResponse;
import com.example.factoryguard.adapter.in.web.inspection.dto.InspectionRunResponse;
import com.example.factoryguard.adapter.in.web.inspection.mapper.InspectionWebMapper;
import com.example.factoryguard.adapter.out.storage.minio.MinioProperties;
import com.example.factoryguard.adapter.out.storage.minio.MinioStorageAdapter;
import com.example.factoryguard.application.dto.inspection.InspectionStatusResponse;
import com.example.factoryguard.application.dto.inspection.SubmitInspectionCommand;
import com.example.factoryguard.application.dto.inspection.SubmitInspectionResult;
import com.example.factoryguard.application.port.in.inspection.GetInspectionEventsUseCase;
import com.example.factoryguard.application.port.in.inspection.GetInspectionStatusUseCase;
import com.example.factoryguard.application.port.in.inspection.GetInspectionsUseCase;
import com.example.factoryguard.application.port.in.inspection.StopInspectionUseCase;
import com.example.factoryguard.application.port.in.inspection.SubmitInspectionUseCase;
import com.example.factoryguard.common.response.ApiResponse;
import com.example.factoryguard.common.validation.FileValidator;
import com.example.factoryguard.config.security.AuthenticatedPrincipal;
import com.example.factoryguard.config.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inspections")
@RequiredArgsConstructor
public class InspectionController {

    private final GetInspectionStatusUseCase getInspectionStatusUseCase;
    private final SubmitInspectionUseCase submitInspectionUseCase;
    private final StopInspectionUseCase stopInspectionUseCase;
    private final GetInspectionsUseCase getInspectionsUseCase;
    private final GetInspectionEventsUseCase getInspectionEventsUseCase;
    private final MinioStorageAdapter minioStorageAdapter;
    private final MinioProperties minioProperties;
    private final SecurityUtils securityUtils;
    private final FileValidator fileValidator;
    private final InspectionWebMapper webMapper;

    @GetMapping("/status")
    public ApiResponse<InspectionStatusResponse> getStatus() {
        return ApiResponse.success(InspectionStatusResponse.from(getInspectionStatusUseCase.getStatus()));
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<SubmitInspectionResult>> upload(
            @RequestPart("file") MultipartFile file,
            @RequestPart("targetId") String targetId,
            @RequestPart(value = "thresholdId", required = false) String thresholdId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) throws Exception {

        fileValidator.validate(file);

        AuthenticatedPrincipal p = securityUtils.getCurrentPrincipal();

        String objectKey = "inspections/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        String bucket = minioProperties.getBucketInspectionArtifacts();

        minioStorageAdapter.upload(
                bucket, objectKey,
                file.getInputStream(), file.getSize(),
                file.getOriginalFilename(), file.getContentType(), ""
        );

        String fileUrl = minioStorageAdapter.createPresignedGetUrl(bucket, objectKey, Duration.ofHours(1));

        SubmitInspectionResult result = submitInspectionUseCase.execute(new SubmitInspectionCommand(
                p.userId(),
                p.sessionId(),
                Long.parseLong(targetId),
                thresholdId != null ? Long.parseLong(thresholdId) : null,
                fileUrl,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                idempotencyKey
        ));

        String message = switch (result.getRunStatus()) {
            case PENDING, PROCESSING -> "검사가 진행 중입니다.";
            case COMPLETED -> "검사 요청이 완료되었습니다.";
            default -> "검사 요청이 처리되었습니다.";
        };

        return ResponseEntity.ok()
                .header("Idempotent-Replay", String.valueOf(result.isReplay()))
                .body(ApiResponse.success(result, message));
    }

    @PatchMapping("/{inspectionId}/stop")
    public ApiResponse<Void> stop(@PathVariable Long inspectionId) {
        AuthenticatedPrincipal p = securityUtils.getCurrentPrincipal();
        stopInspectionUseCase.execute(p.userId(), p.organizationId(), inspectionId);
        return ApiResponse.success(null, "검사가 중단되었습니다.");
    }

    @GetMapping
    public ApiResponse<List<InspectionRunResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        AuthenticatedPrincipal p = securityUtils.getCurrentPrincipal();
        return ApiResponse.success(getInspectionsUseCase.list(p.organizationId(), page, size).stream()
                .map(webMapper::toResponse)
                .toList());
    }

    @GetMapping("/{inspectionId}")
    public ApiResponse<InspectionRunResponse> detail(@PathVariable Long inspectionId) {
        AuthenticatedPrincipal p = securityUtils.getCurrentPrincipal();
        return ApiResponse.success(webMapper.toResponse(
                getInspectionsUseCase.detail(p.organizationId(), inspectionId)));
    }

    @GetMapping("/{inspectionId}/events")
    public ApiResponse<List<InspectionEventLogResponse>> events(@PathVariable Long inspectionId) {
        AuthenticatedPrincipal p = securityUtils.getCurrentPrincipal();
        return ApiResponse.success(getInspectionEventsUseCase.execute(p.organizationId(), inspectionId).stream()
                .map(webMapper::toResponse)
                .toList());
    }
}
