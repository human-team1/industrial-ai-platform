package com.example.factoryguard.application.port.out.inspection;

import com.example.factoryguard.application.dto.inspection.AiInspectionRequest;
import com.example.factoryguard.application.dto.inspection.AiInspectionResponse;
import com.example.factoryguard.application.dto.inspection.AiRealtimeInspectionRequest;

public interface CallAiInspectionPort {

    AiInspectionResponse call(AiInspectionRequest request);

    AiInspectionResponse callRealtime(AiRealtimeInspectionRequest request);
}