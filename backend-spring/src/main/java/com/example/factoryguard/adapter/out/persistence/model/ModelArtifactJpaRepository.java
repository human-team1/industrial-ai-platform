package com.example.factoryguard.adapter.out.persistence.model;

import com.example.factoryguard.domain.model.vo.ModelArtifactType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModelArtifactJpaRepository extends JpaRepository<ModelArtifactJpaEntity, Long> {

    List<ModelArtifactJpaEntity> findByModelVersionIdOrderByCreatedAtAsc(Long modelVersionId);

    boolean existsByModelVersionIdAndArtifactType(Long modelVersionId, ModelArtifactType artifactType);
}
