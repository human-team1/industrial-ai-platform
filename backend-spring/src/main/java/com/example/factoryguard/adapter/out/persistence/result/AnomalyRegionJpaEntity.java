package com.example.factoryguard.adapter.out.persistence.result;

import com.example.factoryguard.domain.result.vo.AnomalyRegionLabel;
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
@Table(name = "anomaly_region")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnomalyRegionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "region_id")
    private Long regionId;

    @Column(name = "image_id", nullable = false)
    private Long imageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "label_code")
    private AnomalyRegionLabel labelCode;

    @Column(name = "bbox_x")
    private Double bboxX;

    @Column(name = "bbox_y")
    private Double bboxY;

    @Column(name = "bbox_w")
    private Double bboxW;

    @Column(name = "bbox_h")
    private Double bboxH;

    @Column(name = "score")
    private Double score;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public AnomalyRegionJpaEntity(
            Long imageId,
            AnomalyRegionLabel labelCode,
            Double bboxX,
            Double bboxY,
            Double bboxW,
            Double bboxH,
            Double score
    ) {
        this.imageId = imageId;
        this.labelCode = labelCode;
        this.bboxX = bboxX;
        this.bboxY = bboxY;
        this.bboxW = bboxW;
        this.bboxH = bboxH;
        this.score = score;
    }
}
