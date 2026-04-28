package com.example.factoryguard.application.port.in.dashboard;

import com.example.factoryguard.application.dto.dashboard.TopDefectTypeResult;

import java.util.List;

public interface GetTopDefectTypesUseCase {

    List<TopDefectTypeResult> execute(Long organizationId);
}
