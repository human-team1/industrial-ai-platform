package com.example.factoryguard.application.port.out.organization;

import com.example.factoryguard.domain.organization.model.Organization;

import java.util.Optional;

public interface FindOrganizationByIdPort {
    Optional<Organization> findById(Long organizationId);
}
