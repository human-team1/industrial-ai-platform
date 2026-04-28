package com.example.factoryguard.application.port.in.result;

import com.example.factoryguard.application.dto.result.AnomalyRegionResult;

import java.util.List;

public interface GetAnomalyRegionsUseCase {

    List<AnomalyRegionResult> execute(Long imageId);
}
