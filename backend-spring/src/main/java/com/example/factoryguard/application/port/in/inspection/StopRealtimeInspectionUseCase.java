package com.example.factoryguard.application.port.in.inspection;

import com.example.factoryguard.application.dto.inspection.StopRealtimeInspectionCommand;

public interface StopRealtimeInspectionUseCase {

    void execute(StopRealtimeInspectionCommand command);
}
