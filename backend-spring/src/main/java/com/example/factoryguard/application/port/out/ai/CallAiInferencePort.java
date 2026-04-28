package com.example.factoryguard.application.port.out.ai;

import com.example.factoryguard.application.dto.inspection.AiInspectionRequest;
import com.example.factoryguard.application.dto.inspection.AiInspectionResponse;

public interface CallAiInferencePort {

    AiInspectionResponse call(AiInspectionRequest request);
}
