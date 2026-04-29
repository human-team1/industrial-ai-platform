package com.example.factoryguard.application.service.organization;

import com.example.factoryguard.application.dto.organization.PublicOrganizationResult;
import com.example.factoryguard.application.port.in.organization.GetPublicOrganizationsUseCase;
import com.example.factoryguard.application.port.out.organization.LoadActiveOrganizationsPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetPublicOrganizationsService implements GetPublicOrganizationsUseCase {

    private final LoadActiveOrganizationsPort loadActiveOrganizationsPort;

    @Override
    public List<PublicOrganizationResult> execute() {
        return loadActiveOrganizationsPort.loadActive().stream()
                .map(org -> PublicOrganizationResult.builder()
                        .id(org.getOrganizationId())
                        .name(org.getOrganizationName())
                        .build())
                .collect(Collectors.toList());
    }
}
