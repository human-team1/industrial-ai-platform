package com.example.factoryguard.adapter.in.web.inspection;

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

@RestController
@RequestMapping("/api/v1/inspections")
@RequiredArgsConstructor
public class RealtimeInspectionController {

    private final SubmitRealtimeInspectionUseCase submitRealtimeInspectionUseCase;
    private final SecurityUtils securityUtils;

    @PostMapping("/realtime")
    public ApiResponse<SubmitInspectionResult> realtime(@RequestBody SubmitRealtimeInspectionRequest request) {
        AuthenticatedPrincipal p = securityUtils.getCurrentPrincipal();
        return ApiResponse.success(submitRealtimeInspectionUseCase.execute(new SubmitRealtimeInspectionCommand(
                p.userId(),
                p.sessionId(),
                request.targetId(),
                request.cameraId(),
                request.thresholdId()
        )), "실시간 검사 요청이 처리되었습니다.");
    }
}
