package com.example.factoryguard.adapter.in.web.inspection;

import com.example.factoryguard.adapter.in.web.inspection.dto.InspectionEventLogResponse;
import com.example.factoryguard.adapter.in.web.inspection.dto.InspectionRunResponse;
import com.example.factoryguard.adapter.in.web.inspection.dto.InspectionStatusResponse;
import com.example.factoryguard.adapter.in.web.inspection.dto.StopInspectionResponse;
import com.example.factoryguard.adapter.in.web.inspection.dto.UploadInspectionResponse;
import com.example.factoryguard.adapter.in.web.inspection.mapper.InspectionWebMapper;
import com.example.factoryguard.application.dto.inspection.SubmitInspectionCommand;
import com.example.factoryguard.application.dto.inspection.SubmitInspectionResult;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.application.port.in.inspection.GetInspectionEventsUseCase;
import com.example.factoryguard.application.port.in.inspection.GetInspectionStatusUseCase;
import com.example.factoryguard.application.port.in.inspection.GetInspectionsUseCase;
import com.example.factoryguard.application.port.in.inspection.StopInspectionUseCase;
import com.example.factoryguard.application.port.in.inspection.SubmitInspectionUseCase;
import com.example.factoryguard.common.response.ApiResponse;
import com.example.factoryguard.config.security.AuthenticatedPrincipal;
import com.example.factoryguard.config.security.SecurityUtils;
import com.example.factoryguard.domain.inspection.model.InspectionRun;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/inspections")
@RequiredArgsConstructor
public class InspectionController {

    private final GetInspectionStatusUseCase getInspectionStatusUseCase;
    private final SubmitInspectionUseCase submitInspectionUseCase;
    private final StopInspectionUseCase stopInspectionUseCase;
    private final GetInspectionsUseCase getInspectionsUseCase;
    private final GetInspectionEventsUseCase getInspectionEventsUseCase;
    private final SecurityUtils securityUtils;
    private final InspectionWebMapper webMapper;

    @GetMapping("/status")
    public ApiResponse<InspectionStatusResponse> getStatus() {
        return ApiResponse.success(webMapper.toResponse(getInspectionStatusUseCase.getStatus()));
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<UploadInspectionResponse>> upload(
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "targetId", required = false) String targetId,
            @RequestPart(value = "thresholdId", required = false) String thresholdId,
            @RequestPart(value = "inputMode", required = false) String inputMode,
            @RequestPart(value = "sourceType", required = false) String sourceType,
            @RequestPart(value = "roiMode", required = false) String roiMode,
            @RequestPart(value = "roiX", required = false) String roiX,
            @RequestPart(value = "roiY", required = false) String roiY,
            @RequestPart(value = "roiWidth", required = false) String roiWidth,
            @RequestPart(value = "roiHeight", required = false) String roiHeight,
            @RequestPart(value = "qualityGateEnabled", required = false) String qualityGateEnabled,
            @RequestPart(value = "idempotencyKey", required = false) String idempotencyKeyPart,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKeyHeader) {

        AuthenticatedPrincipal principal = securityUtils.getCurrentPrincipal();
        SubmitInspectionResult result = submitInspectionUseCase.execute(new SubmitInspectionCommand(
                principal.userId(),
                principal.sessionId(),
                parseOptionalLong(targetId),
                parseOptionalLong(thresholdId),
                inputMode,
                sourceType,
                roiMode,
                parseOptionalDecimal(roiX, "roiX"),
                parseOptionalDecimal(roiY, "roiY"),
                parseOptionalDecimal(roiWidth, "roiWidth"),
                parseOptionalDecimal(roiHeight, "roiHeight"),
                parseOptionalBoolean(qualityGateEnabled),
                file,
                resolveIdempotencyKey(idempotencyKeyPart, idempotencyKeyHeader)
        ));

        return ResponseEntity.ok()
                .header("Idempotent-Replay", String.valueOf(result.isReplay()))
                .body(ApiResponse.success(
                        new UploadInspectionResponse(result.getInspectionId(), result.getRunStatus()),
                        "업로드 검사가 요청되었습니다."
                ));
    }

    @PatchMapping("/{inspectionId}/stop")
    public ApiResponse<StopInspectionResponse> stop(@PathVariable Long inspectionId) {
        AuthenticatedPrincipal principal = securityUtils.getCurrentPrincipal();
        InspectionRun stopped = stopInspectionUseCase.execute(principal.userId(), principal.organizationId(), inspectionId);
        return ApiResponse.success(
                new StopInspectionResponse(
                        stopped.getInspectionId(),
                        stopped.getRunStatus(),
                        stopped.getCompletedAt()
                ),
                "실시간 탐지가 중지되었습니다."
        );
    }

    @GetMapping
    public ApiResponse<List<InspectionRunResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        AuthenticatedPrincipal principal = securityUtils.getCurrentPrincipal();
        return ApiResponse.success(getInspectionsUseCase.list(principal.organizationId(), page, size).stream()
                .map(webMapper::toResponse)
                .toList());
    }

    @GetMapping("/{inspectionId}")
    public ApiResponse<InspectionRunResponse> detail(@PathVariable Long inspectionId) {
        AuthenticatedPrincipal principal = securityUtils.getCurrentPrincipal();
        return ApiResponse.success(webMapper.toResponse(
                getInspectionsUseCase.detail(principal.organizationId(), inspectionId)));
    }

    @GetMapping("/{inspectionId}/events")
    public ApiResponse<List<InspectionEventLogResponse>> events(@PathVariable Long inspectionId) {
        AuthenticatedPrincipal principal = securityUtils.getCurrentPrincipal();
        return ApiResponse.success(getInspectionEventsUseCase.execute(principal.organizationId(), inspectionId).stream()
                .map(webMapper::toResponse)
                .toList());
    }

    private Long parseOptionalLong(String value) {
        return value == null || value.isBlank() ? null : Long.parseLong(value);
    }

    private Boolean parseOptionalBoolean(String value) {
        return value == null || value.isBlank() ? null : Boolean.parseBoolean(value);
    }

    private BigDecimal parseOptionalDecimal(String value, String field) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException ex) {
            throw new BusinessException(ErrorCode.INVALID_ROI_RANGE,
                    field + " 값을 숫자로 해석할 수 없습니다: " + value);
        }
    }

    private String resolveIdempotencyKey(String partValue, String headerValue) {
        if (partValue != null && !partValue.isBlank()) {
            return partValue;
        }
        return headerValue;
    }
}
