package com.example.factoryguard.adapter.in.web.dashboard;

import com.example.factoryguard.application.dto.dashboard.DashboardOverviewQuery;
import com.example.factoryguard.application.dto.dashboard.DashboardOverviewResult;
import com.example.factoryguard.application.port.in.dashboard.GetDashboardOverviewUseCase;
import com.example.factoryguard.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final GetDashboardOverviewUseCase getDashboardOverviewUseCase;

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<DashboardOverviewResult>> getOverview(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,
            @RequestParam(required = false) Long organizationId
    ) {
        DashboardOverviewResult result = getDashboardOverviewUseCase.execute(
                new DashboardOverviewQuery(startDate, endDate, organizationId)
        );
        return ResponseEntity.ok(ApiResponse.success(result, "대시보드 요약 정보를 조회했습니다."));
    }
}
