package com.example.factoryguard.application.port.out.organization;

import com.example.factoryguard.domain.organization.model.Organization;

import java.util.List;

public interface LoadActiveOrganizationsPort {

    List<Organization> loadActive();
}
