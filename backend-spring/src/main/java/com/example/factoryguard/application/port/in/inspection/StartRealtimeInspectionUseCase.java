package com.example.factoryguard.application.port.in.inspection;

import com.example.factoryguard.application.dto.inspection.StartRealtimeInspectionCommand;
import com.example.factoryguard.application.dto.inspection.StartRealtimeInspectionResult;

public interface StartRealtimeInspectionUseCase {

    StartRealtimeInspectionResult execute(StartRealtimeInspectionCommand command);
}
