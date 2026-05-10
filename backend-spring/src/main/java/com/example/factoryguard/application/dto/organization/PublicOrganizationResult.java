package com.example.factoryguard.application.dto.organization;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PublicOrganizationResult {

    private final Long id;
    private final String name;
}
