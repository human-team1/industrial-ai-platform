package com.example.factoryguard.adapter.out.persistence.inspection;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnalysisTargetJpaRepository extends JpaRepository<AnalysisTargetJpaEntity, Long> {

    List<AnalysisTargetJpaEntity> findByOrganizationIdOrderByTargetIdAsc(Long organizationId);
}
