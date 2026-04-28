package com.example.factoryguard.application.port.out.inspection;

import com.example.factoryguard.application.dto.inspection.AiInspectionRequest;
import com.example.factoryguard.application.dto.inspection.AiInspectionResponse;
import com.example.factoryguard.application.dto.inspection.AiRealtimeInspectionRequest;

import java.util.concurrent.TimeoutException;

public interface CallAiInspectionPort {

    AiInspectionResponse call(AiInspectionRequest request) throws TimeoutException;

    AiInspectionResponse callRealtime(AiRealtimeInspectionRequest request) throws TimeoutException;
}
