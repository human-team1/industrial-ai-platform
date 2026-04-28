package com.example.factoryguard.application.port.in.dashboard;

import com.example.factoryguard.application.dto.dashboard.RecentInspectionResult;

import java.util.List;

public interface ListRecentInspectionResultsUseCase {

    List<RecentInspectionResult> execute(Long organizationId, int limit);
}
