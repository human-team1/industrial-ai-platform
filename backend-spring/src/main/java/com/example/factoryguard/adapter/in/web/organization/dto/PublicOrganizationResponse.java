package com.example.factoryguard.adapter.in.web.organization.dto;

import com.example.factoryguard.application.dto.organization.PublicOrganizationResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PublicOrganizationResponse {

    private final Long id;
    private final String name;

    public static PublicOrganizationResponse from(PublicOrganizationResult result) {
        return new PublicOrganizationResponse(result.getId(), result.getName());
    }
}
