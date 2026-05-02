package com.example.factoryguard.application.dto.dashboard;

import java.time.LocalDate;

public record DashboardOverviewQuery(
        LocalDate startDate,
        LocalDate endDate,
        Long organizationId
) {
}
