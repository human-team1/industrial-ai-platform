package com.example.factoryguard.adapter.out.persistence.organization;

import com.example.factoryguard.application.port.out.organization.FindOrganizationByIdPort;
import com.example.factoryguard.domain.organization.model.Organization;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrganizationPersistenceAdapter implements FindOrganizationByIdPort {

    private final OrganizationJpaRepository organizationJpaRepository;

    @Override
    public Optional<Organization> findById(Long organizationId) {
        return organizationJpaRepository.findById(organizationId)
                .map(this::toDomain);
    }

    private Organization toDomain(OrganizationJpaEntity entity) {
        return Organization.builder()
                .organizationId(entity.getOrganizationId())
                .organizationName(entity.getOrganizationName())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
