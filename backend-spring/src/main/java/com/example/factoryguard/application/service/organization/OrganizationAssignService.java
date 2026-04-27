package com.example.factoryguard.application.service.organization;

import com.example.factoryguard.application.port.in.organization.OrganizationAssignUseCase;
import org.springframework.stereotype.Service;

@Service
public class OrganizationAssignService implements OrganizationAssignUseCase {

    // TODO: implement in Organizations domain
    @Override
    public void assignUser(Long userId, Long organizationId) {
    }
}