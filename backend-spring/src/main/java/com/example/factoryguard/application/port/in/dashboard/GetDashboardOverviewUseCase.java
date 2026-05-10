package com.example.factoryguard.application.port.in.dashboard;

import com.example.factoryguard.application.dto.dashboard.DashboardOverviewQuery;
import com.example.factoryguard.application.dto.dashboard.DashboardOverviewResult;

public interface GetDashboardOverviewUseCase {

    DashboardOverviewResult execute(DashboardOverviewQuery query);
}
