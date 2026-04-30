package com.example.factoryguard.application.port.in.inspection;

public interface StopInspectionUseCase {
    void execute(Long userId, Long organizationId, Long inspectionId);
}
