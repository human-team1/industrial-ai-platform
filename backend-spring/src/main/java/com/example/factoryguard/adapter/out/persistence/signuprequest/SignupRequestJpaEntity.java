package com.example.factoryguard.adapter.out.persistence.signuprequest;

import com.example.factoryguard.domain.user.model.SignupRequestStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "signup_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SignupRequestJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_status", nullable = false)
    private SignupRequestStatus requestStatus;

    @Column(name = "reject_reason")
    private String rejectReason;

    @Column(name = "processed_by")
    private Long processedBy;

    @CreationTimestamp
    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Builder
    public SignupRequestJpaEntity(Long userId) {
        this.userId = userId;
        this.requestStatus = SignupRequestStatus.PENDING;
    }

    public void approve(Long adminUserId) {
        this.requestStatus = SignupRequestStatus.APPROVED;
        this.processedBy = adminUserId;
        this.processedAt = LocalDateTime.now();
    }

    public void reject(Long adminUserId, String reason) {
        this.requestStatus = SignupRequestStatus.REJECTED;
        this.rejectReason = reason;
        this.processedBy = adminUserId;
        this.processedAt = LocalDateTime.now();
    }
}
