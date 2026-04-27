package com.example.factoryguard.adapter.out.persistence.user;

import com.example.factoryguard.application.port.out.user.FindActiveThresholdByUserIdPort;
import com.example.factoryguard.application.port.out.user.LoadThresholdByIdPort;
import com.example.factoryguard.application.port.out.user.SaveThresholdHistoryPort;
import com.example.factoryguard.application.port.out.user.UpdateThresholdPort;
import com.example.factoryguard.domain.user.model.UserThreshold;
import com.example.factoryguard.domain.user.model.UserThresholdHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserThresholdPersistenceAdapter implements
        FindActiveThresholdByUserIdPort,
        LoadThresholdByIdPort,
        UpdateThresholdPort,
        SaveThresholdHistoryPort {

    private final UserThresholdJpaRepository userThresholdJpaRepository;
    private final UserThresholdHistoryJpaRepository userThresholdHistoryJpaRepository;

    @Override
    public Optional<UserThreshold> findActiveByUserId(Long userId) {
        return userThresholdJpaRepository
                .findTopByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId)
                .map(this::toDomain);
    }

    @Override
    public Optional<UserThreshold> findById(Long thresholdId) {
        return userThresholdJpaRepository.findById(thresholdId)
                .map(this::toDomain);
    }

    @Override
    public UserThreshold update(UserThreshold threshold) {
        UserThresholdJpaEntity entity = userThresholdJpaRepository.findById(threshold.getThresholdId())
                .orElseThrow();
        entity.update(
                threshold.getAnomalyThreshold(),
                threshold.getLowConfidenceThreshold(),
                threshold.getApplyScope()
        );
        return toDomain(entity);
    }

    @Override
    public void save(UserThresholdHistory history) {
        userThresholdHistoryJpaRepository.save(
                UserThresholdHistoryJpaEntity.builder()
                        .thresholdId(history.getThresholdId())
                        .oldAnomalyThreshold(history.getOldAnomalyThreshold())
                        .newAnomalyThreshold(history.getNewAnomalyThreshold())
                        .changeReason(history.getChangeReason())
                        .changedBy(history.getChangedBy())
                        .build()
        );
    }

    private UserThreshold toDomain(UserThresholdJpaEntity entity) {
        return UserThreshold.builder()
                .thresholdId(entity.getThresholdId())
                .userId(entity.getUserId())
                .anomalyThreshold(entity.getAnomalyThreshold())
                .lowConfidenceThreshold(entity.getLowConfidenceThreshold())
                .minAllowed(entity.getMinAllowed())
                .maxAllowed(entity.getMaxAllowed())
                .applyScope(entity.getApplyScope())
                .isActive(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}