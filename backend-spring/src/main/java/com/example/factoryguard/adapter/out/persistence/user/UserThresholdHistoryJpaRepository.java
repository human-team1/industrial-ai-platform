package com.example.factoryguard.adapter.out.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserThresholdHistoryJpaRepository extends JpaRepository<UserThresholdHistoryJpaEntity, Long> {

    List<UserThresholdHistoryJpaEntity> findAllByThresholdIdOrderByChangedAtAsc(Long thresholdId);

    Optional<UserThresholdHistoryJpaEntity> findTopByThresholdIdAndVersionIsNotNullOrderByVersionDesc(Long thresholdId);
}
