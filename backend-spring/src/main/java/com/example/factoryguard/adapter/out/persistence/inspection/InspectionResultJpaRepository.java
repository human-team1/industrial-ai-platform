package com.example.factoryguard.adapter.out.persistence.inspection;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InspectionResultJpaRepository extends JpaRepository<InspectionResultJpaEntity, Long> {

    List<InspectionResultJpaEntity> findAllByInspectionIdOrderByCreatedAtAsc(Long inspectionId);
}
