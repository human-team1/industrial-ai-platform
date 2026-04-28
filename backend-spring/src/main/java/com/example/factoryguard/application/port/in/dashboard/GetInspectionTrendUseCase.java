package com.example.factoryguard.application.port.in.dashboard;

import com.example.factoryguard.application.dto.dashboard.InspectionTrendResult;

import java.util.List;

public interface GetInspectionTrendUseCase {

    List<InspectionTrendResult> execute(Long organizationId);
}
