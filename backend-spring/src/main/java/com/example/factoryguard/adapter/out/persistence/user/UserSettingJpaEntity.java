package com.example.factoryguard.adapter.out.persistence.user;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_setting")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSettingJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_setting_id")
    private Long userSettingId;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "notification_enabled", nullable = false)
    private boolean notificationEnabled;

    @Column(name = "default_dashboard_range")
    private String defaultDashboardRange;

    @Column(name = "default_camera_id")
    private Long defaultCameraId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public UserSettingJpaEntity(Long userId, boolean notificationEnabled,
                                String defaultDashboardRange, Long defaultCameraId) {
        this.userId = userId;
        this.notificationEnabled = notificationEnabled;
        this.defaultDashboardRange = defaultDashboardRange;
        this.defaultCameraId = defaultCameraId;
    }

    public void update(boolean notificationEnabled, String defaultDashboardRange, Long defaultCameraId) {
        this.notificationEnabled = notificationEnabled;
        this.defaultDashboardRange = defaultDashboardRange;
        this.defaultCameraId = defaultCameraId;
    }
}
