package com.example.factoryguard.adapter.out.persistence.user;

import com.example.factoryguard.application.port.out.user.FindActiveThresholdByUserIdPort;
import com.example.factoryguard.application.port.out.user.LoadThresholdByIdPort;
import com.example.factoryguard.application.port.out.user.LoadUserThresholdHistoryPort;
import com.example.factoryguard.application.port.out.user.SaveThresholdHistoryPort;
import com.example.factoryguard.application.port.out.user.UpdateThresholdPort;
import com.example.factoryguard.domain.user.model.UserThreshold;
import com.example.factoryguard.domain.user.model.UserThresholdHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserThresholdPersistenceAdapter implements
        FindActiveThresholdByUserIdPort,
        LoadThresholdByIdPort,
        UpdateThresholdPort,
        SaveThresholdHistoryPort,
        LoadUserThresholdHistoryPort {

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
                        .version(history.getVersion())
                        .oldAnomalyThreshold(history.getOldAnomalyThreshold())
                        .newAnomalyThreshold(history.getNewAnomalyThreshold())
                        .changeReason(history.getChangeReason())
                        .changedBy(history.getChangedBy())
                        .build()
        );
    }

    @Override
    public List<UserThresholdHistory> findAllByThresholdId(Long thresholdId) {
        return userThresholdHistoryJpaRepository
                .findAllByThresholdIdOrderByChangedAtAsc(thresholdId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<UserThresholdHistory> findLatestByThresholdId(Long thresholdId) {
        return userThresholdHistoryJpaRepository
                .findTopByThresholdIdAndVersionIsNotNullOrderByVersionDesc(thresholdId)
                .map(this::toDomain);
    }

    private UserThresholdHistory toDomain(UserThresholdHistoryJpaEntity e) {
        return UserThresholdHistory.builder()
                .thresholdHistoryId(e.getThresholdHistoryId())
                .thresholdId(e.getThresholdId())
                .version(e.getVersion())
                .oldAnomalyThreshold(e.getOldAnomalyThreshold())
                .newAnomalyThreshold(e.getNewAnomalyThreshold())
                .changeReason(e.getChangeReason())
                .changedBy(e.getChangedBy())
                .changedAt(e.getChangedAt())
                .build();
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