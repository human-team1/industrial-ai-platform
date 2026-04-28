package com.example.factoryguard.application.port.in.inspection;

import com.example.factoryguard.application.dto.inspection.CancelInspectionRunCommand;

public interface CancelInspectionRunUseCase {

    void execute(CancelInspectionRunCommand command);
}
