package com.example.factoryguard.adapter.out.persistence.result;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResultArtifactJpaRepository extends JpaRepository<ResultArtifactJpaEntity, Long> {

    List<ResultArtifactJpaEntity> findAllByResultIdOrderByCreatedAtAsc(Long resultId);
}
