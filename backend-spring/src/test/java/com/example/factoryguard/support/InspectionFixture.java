package com.example.factoryguard.support;

import com.example.factoryguard.adapter.out.persistence.user.UserJpaEntity;
import com.example.factoryguard.adapter.out.persistence.user.UserJpaRepository;
import com.example.factoryguard.adapter.out.persistence.user.UserThresholdJpaEntity;
import com.example.factoryguard.adapter.out.persistence.user.UserThresholdJpaRepository;
import com.example.factoryguard.domain.user.model.UserRole;
import com.example.factoryguard.domain.user.model.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InspectionFixture {

    private final JdbcTemplate jdbcTemplate;
    private final UserJpaRepository userJpaRepository;
    private final UserThresholdJpaRepository userThresholdJpaRepository;

    public Long createOrganization(String name) {
        jdbcTemplate.update(
                "INSERT INTO ORGANIZATION (organization_name, status) VALUES (?, ?)",
                name, "ACTIVE"
        );
        return jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    public UserJpaEntity createUser(Long organizationId) {
        return userJpaRepository.save(UserJpaEntity.builder()
                .organizationId(organizationId)
                .email("user-" + UUID.randomUUID() + "@test.local")
                .name("Test User")
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build());
    }

    public Long createAnalysisTarget(Long organizationId) {
        jdbcTemplate.update(
                "INSERT INTO analysis_target (organization_id, target_name, equipment_name, product_name) "
                        + "VALUES (?, ?, ?, ?)",
                organizationId, "test-target", "equipment-1", "product-1"
        );
        return jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    public UserThresholdJpaEntity createUserThreshold(Long userId,
                                                      double anomalyThreshold,
                                                      double lowConfidenceThreshold) {
        return userThresholdJpaRepository.save(UserThresholdJpaEntity.builder()
                .userId(userId)
                .anomalyThreshold(anomalyThreshold)
                .lowConfidenceThreshold(lowConfidenceThreshold)
                .minAllowed(0.0)
                .maxAllowed(1.0)
                .applyScope("USER")
                .isActive(true)
                .build());
    }
}
