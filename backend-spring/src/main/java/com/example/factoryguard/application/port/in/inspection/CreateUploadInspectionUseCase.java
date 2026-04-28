package com.example.factoryguard.application.port.in.inspection;

import com.example.factoryguard.application.dto.inspection.CreateUploadInspectionCommand;
import com.example.factoryguard.application.dto.inspection.CreateUploadInspectionResult;

public interface CreateUploadInspectionUseCase {

    CreateUploadInspectionResult execute(CreateUploadInspectionCommand command);
}
