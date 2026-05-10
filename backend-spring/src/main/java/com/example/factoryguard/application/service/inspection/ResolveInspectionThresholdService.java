package com.example.factoryguard.application.service.inspection;

import com.example.factoryguard.application.dto.inspection.ResolvedThreshold;
import com.example.factoryguard.application.port.out.user.FindActiveThresholdByUserIdPort;
import com.example.factoryguard.application.port.out.user.LoadThresholdByIdPort;
import com.example.factoryguard.application.port.out.user.LoadUserThresholdHistoryPort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.user.model.UserThreshold;
import com.example.factoryguard.domain.user.model.UserThresholdHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ResolveInspectionThresholdService {

    private final LoadThresholdByIdPort loadThresholdByIdPort;
    private final FindActiveThresholdByUserIdPort findActiveThresholdByUserIdPort;
    private final LoadUserThresholdHistoryPort loadUserThresholdHistoryPort;

    public ResolvedThreshold resolve(Long userId, Long thresholdId) {
        if (thresholdId != null) {
            UserThreshold threshold = loadThresholdByIdPort.findById(thresholdId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.THRESHOLD_NOT_FOUND));
            if (!threshold.getUserId().equals(userId)) {
                throw new BusinessException(ErrorCode.FORBIDDEN);
            }
            return ResolvedThreshold.fromUserThreshold(threshold, latestVersionOf(threshold.getThresholdId()));
        }

        return findActiveThresholdByUserIdPort.findActiveByUserId(userId)
                .map(ut -> ResolvedThreshold.fromUserThreshold(ut, latestVersionOf(ut.getThresholdId())))
                .orElseGet(ResolvedThreshold::defaultThreshold);
    }

    private Integer latestVersionOf(Long thresholdId) {
        return loadUserThresholdHistoryPort.findLatestByThresholdId(thresholdId)
                .map(UserThresholdHistory::getVersion)
                .orElse(null);
    }
}
