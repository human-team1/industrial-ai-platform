package com.example.factoryguard.adapter.in.web.user.dto;

import com.example.factoryguard.application.dto.user.UpdateThresholdCommand;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class UpdateThresholdRequest {

    // anomalyThreshold는 Anomalib raw pred_score 기준값으로, 0~1 범위가 아닌 모델별 raw score 범위를 허용한다.
    @NotNull(message = "anomalyThreshold는 필수입니다.")
    @DecimalMin(value = "0.0", message = "anomalyThreshold는 0.0 이상이어야 합니다.")
    private BigDecimal anomalyThreshold;

    // lowConfidenceThreshold는 confidence (0~1) 기준값이므로 1.0 이하를 유지한다.
    @NotNull(message = "lowConfidenceThreshold는 필수입니다.")
    @DecimalMin(value = "0.0", message = "lowConfidenceThreshold는 0.0 이상이어야 합니다.")
    @DecimalMax(value = "1.0", message = "lowConfidenceThreshold는 1.0 이하여야 합니다.")
    private BigDecimal lowConfidenceThreshold;

    private String applyScope;
    private String changeReason;

    public UpdateThresholdCommand toCommand() {
        return new UpdateThresholdCommand(anomalyThreshold, lowConfidenceThreshold, applyScope, changeReason);
    }
}
