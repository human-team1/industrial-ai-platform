package com.example.factoryguard.adapter.out.persistence.organization;

import com.example.factoryguard.domain.organization.model.OrganizationStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "organization")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrganizationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "organization_name", nullable = false)
    private String organizationName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrganizationStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public OrganizationJpaEntity(String organizationName, OrganizationStatus status) {
        this.organizationName = organizationName;
        this.status = status;
    }
}
