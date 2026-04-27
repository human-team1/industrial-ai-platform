package com.example.factoryguard.application.service.inspection;

import com.example.factoryguard.application.dto.inspection.ResolvedThreshold;
import com.example.factoryguard.application.port.out.user.FindActiveThresholdByUserIdPort;
import com.example.factoryguard.application.port.out.user.LoadThresholdByIdPort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.user.model.UserThreshold;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ResolveInspectionThresholdService {

    private final LoadThresholdByIdPort loadThresholdByIdPort;
    private final FindActiveThresholdByUserIdPort findActiveThresholdByUserIdPort;

    public ResolvedThreshold resolve(Long userId, Long thresholdId) {
        if (thresholdId != null) {
            UserThreshold threshold = loadThresholdByIdPort.findById(thresholdId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.THRESHOLD_NOT_FOUND));
            if (!threshold.getUserId().equals(userId)) {
                throw new BusinessException(ErrorCode.FORBIDDEN);
            }
            return ResolvedThreshold.fromUserThreshold(threshold);
        }

        return findActiveThresholdByUserIdPort.findActiveByUserId(userId)
                .map(ResolvedThreshold::fromUserThreshold)
                .orElseGet(ResolvedThreshold::defaultThreshold);
    }
}