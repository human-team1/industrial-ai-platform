package com.example.factoryguard.adapter.out.persistence.inspection;

import com.example.factoryguard.domain.inspection.model.CameraStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "camera_source")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CameraSourceJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "camera_id")
    private Long cameraId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "camera_name", nullable = false, length = 256)
    private String cameraName;

    @Column(name = "stream_url", nullable = false, columnDefinition = "TEXT")
    private String streamUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private CameraStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public CameraSourceJpaEntity(Long cameraId, Long organizationId, Long userId, String cameraName,
                                 String streamUrl, CameraStatus status) {
        this.cameraId = cameraId;
        this.organizationId = organizationId;
        this.userId = userId;
        this.cameraName = cameraName;
        this.streamUrl = streamUrl;
        this.status = status;
    }

    public void update(String cameraName, String streamUrl, CameraStatus status) {
        this.cameraName = cameraName;
        this.streamUrl = streamUrl;
        this.status = status;
    }
}
