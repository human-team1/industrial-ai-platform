package com.example.factoryguard.adapter.out.persistence.result;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageJpaRepository extends JpaRepository<ImageJpaEntity, Long> {

    List<ImageJpaEntity> findAllByResultIdOrderByCreatedAtAsc(Long resultId);
}
