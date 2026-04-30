package com.example.factoryguard.adapter.out.persistence.organization.mapper;

import com.example.factoryguard.adapter.out.persistence.organization.OrganizationJpaEntity;
import com.example.factoryguard.domain.organization.model.Organization;
import org.springframework.stereotype.Component;

@Component
public class OrganizationPersistenceMapper {

    public Organization toDomain(OrganizationJpaEntity entity) {
        return Organization.builder()
                .organizationId(entity.getOrganizationId())
                .organizationName(entity.getOrganizationName())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
