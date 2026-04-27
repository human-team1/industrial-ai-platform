package com.example.factoryguard.adapter.out.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserThresholdJpaRepository extends JpaRepository<UserThresholdJpaEntity, Long> {

    Optional<UserThresholdJpaEntity> findTopByUserIdAndIsActiveTrueOrderByCreatedAtDesc(Long userId);
}