package com.example.factoryguard.adapter.out.persistence.inspection;

import com.example.factoryguard.domain.inspection.model.RunStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface InspectionRunJpaRepository extends JpaRepository<InspectionRunJpaEntity, Long> {
    Page<InspectionRunJpaEntity> findByOrganizationId(Long organizationId, Pageable pageable);
    List<InspectionRunJpaEntity> findByRunStatusAndStartedAtBefore(RunStatus runStatus, LocalDateTime threshold);
}
