package com.example.factoryguard.adapter.out.persistence.result;

import com.example.factoryguard.domain.result.vo.AnomalyRegionLabel;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
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

    @Column(name = "bbox_x", precision = 10, scale = 4)
    private BigDecimal bboxX;

    @Column(name = "bbox_y", precision = 10, scale = 4)
    private BigDecimal bboxY;

    @Column(name = "bbox_w", precision = 10, scale = 4)
    private BigDecimal bboxW;

    @Column(name = "bbox_h", precision = 10, scale = 4)
    private BigDecimal bboxH;

    @Column(name = "score", precision = 6, scale = 4)
    private BigDecimal score;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public AnomalyRegionJpaEntity(Long imageId, AnomalyRegionLabel labelCode,
                                  BigDecimal bboxX, BigDecimal bboxY,
                                  BigDecimal bboxW, BigDecimal bboxH,
                                  BigDecimal score) {
        this.imageId = imageId;
        this.labelCode = labelCode;
        this.bboxX = bboxX;
        this.bboxY = bboxY;
        this.bboxW = bboxW;
        this.bboxH = bboxH;
        this.score = score;
    }
}
