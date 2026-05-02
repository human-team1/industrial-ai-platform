package com.example.factoryguard.adapter.out.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSettingJpaRepository extends JpaRepository<UserSettingJpaEntity, Long> {

    Optional<UserSettingJpaEntity> findByUserId(Long userId);
}
