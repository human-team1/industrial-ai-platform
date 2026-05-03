package com.example.factoryguard.adapter.out.persistence.model;

import com.example.factoryguard.domain.model.vo.ModelArtifactType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "model_artifact")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ModelArtifactJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "model_artifact_id")
    private Long modelArtifactId;

    @Column(name = "model_version_id", nullable = false)
    private Long modelVersionId;

    @Column(name = "file_id", nullable = false)
    private Long fileId;

    @Enumerated(EnumType.STRING)
    @Column(name = "artifact_type", nullable = false)
    private ModelArtifactType artifactType;

    @Column(name = "checksum")
    private String checksum;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public ModelArtifactJpaEntity(Long modelArtifactId, Long modelVersionId, Long fileId, ModelArtifactType artifactType, String checksum) {
        this.modelArtifactId = modelArtifactId;
        this.modelVersionId = modelVersionId;
        this.fileId = fileId;
        this.artifactType = artifactType;
        this.checksum = checksum;
    }
}
