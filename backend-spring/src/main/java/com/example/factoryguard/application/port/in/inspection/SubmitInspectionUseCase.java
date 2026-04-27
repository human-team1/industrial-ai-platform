package com.example.factoryguard.application.port.in.inspection;

import com.example.factoryguard.application.dto.inspection.SubmitInspectionCommand;
import com.example.factoryguard.application.dto.inspection.SubmitInspectionResult;

public interface SubmitInspectionUseCase {

    SubmitInspectionResult execute(SubmitInspectionCommand command);
}