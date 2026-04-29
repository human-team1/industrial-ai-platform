package com.example.factoryguard.adapter.out.persistence.result;

import com.example.factoryguard.adapter.out.persistence.inspection.InspectionResultJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResultJpaRepository extends JpaRepository<InspectionResultJpaEntity, Long> {
}
