package com.example.factoryguard.adapter.out.persistence.notification;

import com.example.factoryguard.domain.notification.vo.NotificationSeverity;
import com.example.factoryguard.domain.notification.vo.NotificationType;
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
@Table(name = "notification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", length = 50)
    private NotificationType notificationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", length = 20)
    private NotificationSeverity severity;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "related_type", length = 50)
    private String relatedType;

    @Column(name = "related_id")
    private Long relatedId;

    @Column(name = "target_url", columnDefinition = "TEXT")
    private String targetUrl;

    @Column(name = "dedup_key", length = 255)
    private String dedupKey;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public NotificationJpaEntity(Long userId,
                                 NotificationType notificationType,
                                 NotificationSeverity severity,
                                 String title,
                                 String message,
                                 String relatedType,
                                 Long relatedId,
                                 String targetUrl,
                                 String dedupKey,
                                 Boolean isRead) {
        this.userId = userId;
        this.notificationType = notificationType;
        this.severity = severity;
        this.title = title;
        this.message = message;
        this.relatedType = relatedType;
        this.relatedId = relatedId;
        this.targetUrl = targetUrl;
        this.dedupKey = dedupKey;
        this.isRead = isRead != null ? isRead : Boolean.FALSE;
    }

    public void markAsRead() {
        this.isRead = Boolean.TRUE;
    }
}
