package com.example.factoryguard.adapter.out.persistence.result;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
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

    @Column(name = "artifact_type")
    private String artifactType;

    @Column(name = "file_id")
    private Long fileId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
