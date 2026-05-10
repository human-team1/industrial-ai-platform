package com.example.factoryguard.adapter.out.persistence.organization;

import com.example.factoryguard.application.port.out.organization.FindOrganizationByIdPort;
import com.example.factoryguard.application.port.out.organization.LoadActiveOrganizationsPort;
import com.example.factoryguard.application.port.out.organization.LoadOrganizationByIdPort;
import com.example.factoryguard.adapter.out.persistence.organization.mapper.OrganizationPersistenceMapper;
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
        implements LoadActiveOrganizationsPort, LoadOrganizationByIdPort, FindOrganizationByIdPort {

    private final OrganizationJpaRepository organizationJpaRepository;
    private final OrganizationPersistenceMapper organizationPersistenceMapper;

    @Override
    public List<Organization> loadActive() {
        return organizationJpaRepository.findAllByStatus(OrganizationStatus.ACTIVE).stream()
                .map(organizationPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Organization> findById(Long organizationId) {
        return organizationJpaRepository.findById(organizationId)
                .map(organizationPersistenceMapper::toDomain);
    }
}