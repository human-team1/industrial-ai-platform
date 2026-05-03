package com.example.factoryguard.adapter.out.persistence.model;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ModelVersionJpaRepository extends JpaRepository<ModelVersionJpaEntity, Long> {

    boolean existsByModelIdAndVersionName(Long modelId, String versionName);
}
