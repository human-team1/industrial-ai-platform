package com.example.factoryguard.adapter.out.persistence.inspection;

import com.example.factoryguard.domain.inspection.model.InspectionEventType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inspection_event_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InspectionEventLogJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "inspection_id", nullable = false)
    private Long inspectionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 32)
    private InspectionEventType eventType;

    @Column(name = "message", length = 2000)
    private String message;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public InspectionEventLogJpaEntity(Long inspectionId, InspectionEventType eventType, String message) {
        this.inspectionId = inspectionId;
        this.eventType = eventType;
        this.message = message;
    }
}
