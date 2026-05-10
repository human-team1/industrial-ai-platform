package com.example.factoryguard.application.port.out.inspection;

import com.example.factoryguard.application.dto.inspection.AiInspectionCommand;
import com.example.factoryguard.application.dto.inspection.AiInspectionResult;
import com.example.factoryguard.application.dto.inspection.AiRealtimeInspectionCommand;

import java.util.concurrent.TimeoutException;

public interface CallAiInspectionPort {

    AiInspectionResult call(AiInspectionCommand command) throws TimeoutException;

    AiInspectionResult callRealtime(AiRealtimeInspectionCommand command) throws TimeoutException;
}
