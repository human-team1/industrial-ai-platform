package com.example.factoryguard.adapter.in.web.inspection;

import com.example.factoryguard.application.dto.inspection.InspectionStatusResponse;
import com.example.factoryguard.application.port.in.inspection.GetInspectionStatusUseCase;
import com.example.factoryguard.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inspections")
public class InspectionController {

    private final GetInspectionStatusUseCase getInspectionStatusUseCase;

    public InspectionController(GetInspectionStatusUseCase getInspectionStatusUseCase) {
        this.getInspectionStatusUseCase = getInspectionStatusUseCase;
    }

    @GetMapping("/status")
    public ApiResponse<InspectionStatusResponse> getStatus() {
        return ApiResponse.success(InspectionStatusResponse.from(getInspectionStatusUseCase.getStatus()));
    }
}
