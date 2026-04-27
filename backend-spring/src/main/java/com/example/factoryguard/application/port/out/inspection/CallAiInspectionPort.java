package com.example.factoryguard.application.port.out.inspection;

import com.example.factoryguard.application.dto.inspection.AiInspectionRequest;
import com.example.factoryguard.application.dto.inspection.AiInspectionResponse;

public interface CallAiInspectionPort {

    AiInspectionResponse call(AiInspectionRequest request);
}