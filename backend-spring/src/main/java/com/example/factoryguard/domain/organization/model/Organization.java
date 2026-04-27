package com.example.factoryguard.domain.organization.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class Organization {

    private final Long organizationId;
    private final String organizationName;
    private final OrganizationStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
