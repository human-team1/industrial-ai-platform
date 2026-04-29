package com.example.factoryguard.adapter.in.web.signuprequest.dto;

import com.example.factoryguard.application.dto.organization.PublicOrganizationResult;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PublicOrganizationResponse {

    private final Long id;
    private final String name;

    public static PublicOrganizationResponse from(PublicOrganizationResult result) {
        return new PublicOrganizationResponse(result.getId(), result.getName());
    }
}
