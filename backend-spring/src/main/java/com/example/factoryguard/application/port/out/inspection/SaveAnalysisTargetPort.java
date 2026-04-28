package com.example.factoryguard.application.port.out.inspection;

import com.example.factoryguard.domain.inspection.model.AnalysisTarget;

public interface SaveAnalysisTargetPort {

    AnalysisTarget save(AnalysisTarget analysisTarget);
}
