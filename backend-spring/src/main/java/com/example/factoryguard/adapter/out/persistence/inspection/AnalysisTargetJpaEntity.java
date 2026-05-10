package com.example.factoryguard.adapter.out.persistence.inspection;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "analysis_target")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnalysisTargetJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "target_name", nullable = false)
    private String targetName;

    @Column(name = "equipment_name")
    private String equipmentName;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "location_name")
    private String locationName;
}
