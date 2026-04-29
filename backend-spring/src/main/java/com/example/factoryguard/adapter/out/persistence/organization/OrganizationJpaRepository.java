package com.example.factoryguard.adapter.out.persistence.organization;

import com.example.factoryguard.domain.organization.vo.OrganizationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrganizationJpaRepository extends JpaRepository<OrganizationJpaEntity, Long> {

    List<OrganizationJpaEntity> findAllByStatus(OrganizationStatus status);
}
