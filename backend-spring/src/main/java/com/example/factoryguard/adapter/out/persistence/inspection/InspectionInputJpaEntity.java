package com.example.factoryguard.adapter.out.persistence.inspection;

import com.example.factoryguard.domain.inspection.model.InputSourceType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
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

    @Column(name = "stream_url", length = 1024)
    private String streamUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 32)
    private InputSourceType sourceType;

    @Column(name = "source_name", length = 512)
    private String sourceName;

    @Column(name = "mime_type", length = 128)
    private String mimeType;

    @Column(name = "duration_sec")
    private Integer durationSec;

    @Column(name = "frame_count")
    private Integer frameCount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public InspectionInputJpaEntity(Long inspectionId, Long fileId, Long cameraId, String streamUrl,
                                    InputSourceType sourceType, String sourceName, String mimeType,
                                    Integer durationSec, Integer frameCount) {
        this.inspectionId = inspectionId;
        this.fileId = fileId;
        this.cameraId = cameraId;
        this.streamUrl = streamUrl;
        this.sourceType = sourceType;
        this.sourceName = sourceName;
        this.mimeType = mimeType;
        this.durationSec = durationSec;
        this.frameCount = frameCount;
    }
}
