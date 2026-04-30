package com.example.factoryguard.adapter.out.persistence.result;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnomalyRegionJpaRepository extends JpaRepository<AnomalyRegionJpaEntity, Long> {

    List<AnomalyRegionJpaEntity> findAllByImageIdOrderByCreatedAtAsc(Long imageId);
}
