package com.example.factoryguard.adapter.out.persistence.model;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ModelJpaRepository extends JpaRepository<ModelJpaEntity, Long> {

    boolean existsByModelNameAndModelType(String modelName, String modelType);
}
