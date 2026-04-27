package com.example.factoryguard.application.port.in.organization;

public interface OrganizationAssignUseCase {

    void assignUser(Long userId, Long organizationId);
}