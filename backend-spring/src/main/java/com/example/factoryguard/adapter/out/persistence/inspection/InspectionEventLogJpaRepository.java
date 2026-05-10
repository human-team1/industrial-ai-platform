package com.example.factoryguard.adapter.out.persistence.inspection;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InspectionEventLogJpaRepository extends JpaRepository<InspectionEventLogJpaEntity, Long> {
    List<InspectionEventLogJpaEntity> findByInspectionIdOrderByCreatedAtAsc(Long inspectionId);
}
