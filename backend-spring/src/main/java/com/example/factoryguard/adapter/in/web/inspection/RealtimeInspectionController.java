package com.example.factoryguard.adapter.in.web.inspection;

import com.example.factoryguard.adapter.in.web.inspection.dto.RealtimeInspectionResponse;
import com.example.factoryguard.adapter.in.web.inspection.dto.SubmitRealtimeInspectionRequest;
import com.example.factoryguard.application.dto.inspection.SubmitInspectionResult;
import com.example.factoryguard.application.dto.inspection.SubmitRealtimeInspectionCommand;
import com.example.factoryguard.application.port.in.inspection.SubmitRealtimeInspectionUseCase;
import com.example.factoryguard.common.response.ApiResponse;
import com.example.factoryguard.config.security.AuthenticatedPrincipal;
import com.example.factoryguard.config.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/inspections")
@RequiredArgsConstructor
public class RealtimeInspectionController {

    private final SubmitRealtimeInspectionUseCase submitRealtimeInspectionUseCase;
    private final SecurityUtils securityUtils;

    @PostMapping("/realtime")
    public ApiResponse<RealtimeInspectionResponse> realtime(@RequestBody SubmitRealtimeInspectionRequest request) {
        AuthenticatedPrincipal principal = securityUtils.getCurrentPrincipal();
        LocalDateTime requestedAt = LocalDateTime.now();
        SubmitInspectionResult result = submitRealtimeInspectionUseCase.execute(
                new SubmitRealtimeInspectionCommand(
                        principal.userId(),
                        principal.sessionId(),
                        request.targetId(),
                        request.cameraId(),
                        request.deploymentId(),
                        request.thresholdId()
                )
        );

        return ApiResponse.success(
                new RealtimeInspectionResponse(
                        result.getInspectionId(),
                        result.getRunStatus(),
                        request.cameraId(),
                        requestedAt
                ),
                "실시간 탐지가 시작되었습니다."
        );
    }
}
