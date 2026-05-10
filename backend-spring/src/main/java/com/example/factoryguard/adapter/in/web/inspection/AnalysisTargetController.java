package com.example.factoryguard.adapter.in.web.inspection;

import com.example.factoryguard.application.dto.inspection.AvailableAnalysisTargetItem;
import com.example.factoryguard.application.port.in.inspection.GetAvailableAnalysisTargetsUseCase;
import com.example.factoryguard.common.response.ApiResponse;
import com.example.factoryguard.config.security.AuthenticatedPrincipal;
import com.example.factoryguard.config.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AnalysisTargetController {

    private final GetAvailableAnalysisTargetsUseCase getAvailableAnalysisTargetsUseCase;
    private final SecurityUtils securityUtils;

    @GetMapping("/api/v1/analysis-targets")
    public ApiResponse<Map<String, List<AvailableAnalysisTargetItem>>> availableTargets() {
        AuthenticatedPrincipal principal = securityUtils.getCurrentPrincipal();
        return ApiResponse.success(
                Map.of("items", getAvailableAnalysisTargetsUseCase.execute(principal.organizationId())),
                "사용 가능한 검사 대상 목록을 조회했습니다."
        );
    }
}
