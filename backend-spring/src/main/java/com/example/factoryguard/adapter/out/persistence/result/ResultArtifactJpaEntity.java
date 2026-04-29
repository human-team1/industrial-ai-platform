package com.example.factoryguard.adapter.out.persistence.result;

import com.example.factoryguard.domain.result.vo.ArtifactType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "result_artifact")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResultArtifactJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "artifact_id")
    private Long artifactId;

    @Column(name = "result_id", nullable = false)
    private Long resultId;

    @Enumerated(EnumType.STRING)
    @Column(name = "artifact_type")
    private ArtifactType artifactType;

    @Column(name = "file_id")
    private Long fileId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public ResultArtifactJpaEntity(Long resultId, ArtifactType artifactType, Long fileId) {
        this.resultId = resultId;
        this.artifactType = artifactType;
        this.fileId = fileId;
    }
}
