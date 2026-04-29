package com.example.factoryguard.application.port.in.organization;

import com.example.factoryguard.application.dto.organization.PublicOrganizationResult;

import java.util.List;

public interface GetPublicOrganizationsUseCase {

    List<PublicOrganizationResult> execute();
}
