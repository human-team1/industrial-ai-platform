package com.example.factoryguard.adapter.out.persistence.inspection;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CameraSourceJpaRepository extends JpaRepository<CameraSourceJpaEntity, Long> {
    List<CameraSourceJpaEntity> findByOrganizationIdOrderByCreatedAtDesc(Long organizationId);
}
