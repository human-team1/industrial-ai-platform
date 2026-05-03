package com.example.factoryguard.adapter.out.persistence.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "model")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ModelJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "model_id")
    private Long modelId;

    @Column(name = "model_name", nullable = false)
    private String modelName;

    @Column(name = "model_type", nullable = false)
    private String modelType;

    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public ModelJpaEntity(Long modelId, String modelName, String modelType, String description) {
        this.modelId = modelId;
        this.modelName = modelName;
        this.modelType = modelType;
        this.description = description;
    }
}
