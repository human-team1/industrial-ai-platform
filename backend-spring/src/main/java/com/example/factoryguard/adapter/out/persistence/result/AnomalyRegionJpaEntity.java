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
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ANOMALY_REGION")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnomalyRegionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "region_id")
    private Long regionId;

    @Column(name = "image_id", nullable = false)
    private Long imageId;

    @Column(name = "label_code")
    private String labelCode;

    @Column(name = "bbox_x")
    private BigDecimal bboxX;

    @Column(name = "bbox_y")
    private BigDecimal bboxY;

    @Column(name = "bbox_w")
    private BigDecimal bboxW;

    @Column(name = "bbox_h")
    private BigDecimal bboxH;

    @Column(name = "score")
    private BigDecimal score;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
