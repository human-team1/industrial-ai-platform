package com.example.factoryguard.application.port.in.inspection;

import com.example.factoryguard.application.dto.inspection.SubmitInspectionResult;
import com.example.factoryguard.application.dto.inspection.SubmitRealtimeInspectionCommand;

public interface SubmitRealtimeInspectionUseCase {
    SubmitInspectionResult execute(SubmitRealtimeInspectionCommand command);
}
