package com.example.factoryguard.adapter.out.persistence.inspection;

import com.example.factoryguard.domain.inspection.model.InputSourceType;
import com.example.factoryguard.domain.inspection.vo.RoiMode;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inspection_input")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InspectionInputJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inspection_input_id")
    private Long inspectionInputId;

    @Column(name = "inspection_id", nullable = false)
    private Long inspectionId;

    @Column(name = "file_id")
    private Long fileId;

    @Column(name = "camera_id")
    private Long cameraId;

    @Column(name = "stream_url", columnDefinition = "TEXT")
    private String streamUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    private InputSourceType sourceType;

    @Column(name = "source_name", length = 255)
    private String sourceName;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "duration_sec")
    private Integer durationSec;

    @Column(name = "frame_count")
    private Integer frameCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "roi_mode", length = 20)
    private RoiMode roiMode;

    @Column(name = "roi_coordinate_type", length = 20)
    private String roiCoordinateType;

    @Column(name = "roi_x", precision = 8, scale = 6)
    private BigDecimal roiX;

    @Column(name = "roi_y", precision = 8, scale = 6)
    private BigDecimal roiY;

    @Column(name = "roi_width", precision = 8, scale = 6)
    private BigDecimal roiWidth;

    @Column(name = "roi_height", precision = 8, scale = 6)
    private BigDecimal roiHeight;

    @Column(name = "quality_gate_enabled")
    private Boolean qualityGateEnabled;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public InspectionInputJpaEntity(Long inspectionId, Long fileId, Long cameraId, String streamUrl,
                                    InputSourceType sourceType, String sourceName, String mimeType,
                                    Integer durationSec, Integer frameCount,
                                    RoiMode roiMode, String roiCoordinateType,
                                    BigDecimal roiX, BigDecimal roiY,
                                    BigDecimal roiWidth, BigDecimal roiHeight,
                                    Boolean qualityGateEnabled) {
        this.inspectionId = inspectionId;
        this.fileId = fileId;
        this.cameraId = cameraId;
        this.streamUrl = streamUrl;
        this.sourceType = sourceType;
        this.sourceName = sourceName;
        this.mimeType = mimeType;
        this.durationSec = durationSec;
        this.frameCount = frameCount;
        this.roiMode = roiMode;
        this.roiCoordinateType = roiCoordinateType;
        this.roiX = roiX;
        this.roiY = roiY;
        this.roiWidth = roiWidth;
        this.roiHeight = roiHeight;
        this.qualityGateEnabled = qualityGateEnabled;
    }
}
