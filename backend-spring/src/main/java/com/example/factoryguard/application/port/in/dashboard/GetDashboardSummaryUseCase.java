package com.example.factoryguard.application.port.in.dashboard;

import com.example.factoryguard.application.dto.dashboard.DashboardSummaryResult;

public interface GetDashboardSummaryUseCase {

    DashboardSummaryResult execute(Long organizationId);
}
