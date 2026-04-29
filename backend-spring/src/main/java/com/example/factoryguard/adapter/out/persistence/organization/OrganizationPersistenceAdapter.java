package com.example.factoryguard.adapter.out.persistence.organization;

import com.example.factoryguard.application.port.out.organization.LoadActiveOrganizationsPort;
import com.example.factoryguard.application.port.out.organization.LoadOrganizationByIdPort;
import com.example.factoryguard.domain.organization.model.Organization;
import com.example.factoryguard.domain.organization.vo.OrganizationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrganizationPersistenceAdapter
        implements LoadActiveOrganizationsPort, LoadOrganizationByIdPort {

    private final OrganizationJpaRepository organizationJpaRepository;

    @Override
    public List<Organization> loadActive() {
        return organizationJpaRepository.findAllByStatus(OrganizationStatus.ACTIVE).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

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
