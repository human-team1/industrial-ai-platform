package com.example.factoryguard.adapter.in.web.inspection;

import com.example.factoryguard.application.dto.inspection.AvailableInspectionModelItem;
import com.example.factoryguard.application.dto.inspection.AvailableRealtimeCameraItem;
import com.example.factoryguard.application.port.in.inspection.GetAvailableInspectionModelsUseCase;
import com.example.factoryguard.application.port.in.inspection.GetAvailableRealtimeCamerasUseCase;
import com.example.factoryguard.common.response.ApiResponse;
import com.example.factoryguard.config.security.AuthenticatedPrincipal;
import com.example.factoryguard.config.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class InspectionModelController {

    private final GetAvailableInspectionModelsUseCase getAvailableInspectionModelsUseCase;
    private final GetAvailableRealtimeCamerasUseCase getAvailableRealtimeCamerasUseCase;
    private final SecurityUtils securityUtils;

    @GetMapping("/api/v1/inspection-models/available")
    public ApiResponse<Map<String, List<AvailableInspectionModelItem>>> availableModels(
            @RequestParam(required = false) Long targetId,
            @RequestParam(required = false) String inspectionType,
            @RequestParam(required = false) String modelCategory
    ) {
        AuthenticatedPrincipal principal = securityUtils.getCurrentPrincipal();
        List<AvailableInspectionModelItem> items = getAvailableInspectionModelsUseCase.execute(
                principal.organizationId(),
                targetId,
                inspectionType,
                modelCategory
        );
        return ApiResponse.success(
                Map.of("items", items),
                "사용 가능한 검사 모델 목록을 조회했습니다."
        );
    }

    @GetMapping("/api/v1/realtime/cameras/available")
    public ApiResponse<Map<String, List<AvailableRealtimeCameraItem>>> availableCameras(
            @RequestParam(required = false) Long targetId
    ) {
        AuthenticatedPrincipal principal = securityUtils.getCurrentPrincipal();
        return ApiResponse.success(
                Map.of("items", getAvailableRealtimeCamerasUseCase.execute(principal.organizationId(), targetId)),
                "사용 가능한 카메라 목록을 조회했습니다."
        );
    }
}
