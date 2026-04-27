package com.example.factoryguard.application.port.out.inspection;

import com.example.factoryguard.domain.inspection.model.AnalysisTarget;

import java.util.Optional;

public interface LoadAnalysisTargetPort {

    Optional<AnalysisTarget> findById(Long targetId);
}