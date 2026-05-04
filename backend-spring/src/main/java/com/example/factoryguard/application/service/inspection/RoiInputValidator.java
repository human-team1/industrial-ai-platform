package com.example.factoryguard.application.service.inspection;

import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.inspection.vo.RoiMode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Locale;

@Component
public class RoiInputValidator {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE = BigDecimal.ONE;

    public RoiMode resolveMode(String rawRoiMode) {
        if (rawRoiMode == null || rawRoiMode.isBlank()) {
            return RoiMode.FULL_FRAME;
        }
        String normalized = rawRoiMode.trim().toUpperCase(Locale.ROOT);
        try {
            return RoiMode.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.INVALID_ROI_RANGE,
                    "roiMode 값이 올바르지 않습니다: " + rawRoiMode);
        }
    }

    public Result validate(String rawRoiMode,
                           BigDecimal roiX, BigDecimal roiY,
                           BigDecimal roiWidth, BigDecimal roiHeight) {
        RoiMode mode = resolveMode(rawRoiMode);
        if (mode == RoiMode.FULL_FRAME) {
            return new Result(mode, null, null, null, null);
        }

        if (roiX == null || roiY == null || roiWidth == null || roiHeight == null) {
            throw new BusinessException(ErrorCode.INVALID_ROI_REQUIRED);
        }

        assertWithinUnitInterval("roiX", roiX);
        assertWithinUnitInterval("roiY", roiY);
        assertWithinUnitInterval("roiWidth", roiWidth);
        assertWithinUnitInterval("roiHeight", roiHeight);

        if (roiWidth.compareTo(ZERO) <= 0) {
            throw new BusinessException(ErrorCode.INVALID_ROI_RANGE, "roiWidth는 0보다 커야 합니다.");
        }
        if (roiHeight.compareTo(ZERO) <= 0) {
            throw new BusinessException(ErrorCode.INVALID_ROI_RANGE, "roiHeight는 0보다 커야 합니다.");
        }
        if (roiX.add(roiWidth).compareTo(ONE) > 0) {
            throw new BusinessException(ErrorCode.INVALID_ROI_RANGE,
                    "roiX + roiWidth는 1을 초과할 수 없습니다.");
        }
        if (roiY.add(roiHeight).compareTo(ONE) > 0) {
            throw new BusinessException(ErrorCode.INVALID_ROI_RANGE,
                    "roiY + roiHeight는 1을 초과할 수 없습니다.");
        }
        return new Result(mode, roiX, roiY, roiWidth, roiHeight);
    }

    private void assertWithinUnitInterval(String field, BigDecimal value) {
        if (value.compareTo(ZERO) < 0 || value.compareTo(ONE) > 0) {
            throw new BusinessException(ErrorCode.INVALID_ROI_RANGE,
                    field + " 값이 0~1 범위를 벗어났습니다: " + value);
        }
    }

    public static final class Result {
        private final RoiMode roiMode;
        private final BigDecimal roiX;
        private final BigDecimal roiY;
        private final BigDecimal roiWidth;
        private final BigDecimal roiHeight;

        public Result(RoiMode roiMode, BigDecimal roiX, BigDecimal roiY,
                      BigDecimal roiWidth, BigDecimal roiHeight) {
            this.roiMode = roiMode;
            this.roiX = roiX;
            this.roiY = roiY;
            this.roiWidth = roiWidth;
            this.roiHeight = roiHeight;
        }

        public RoiMode getRoiMode() { return roiMode; }
        public BigDecimal getRoiX() { return roiX; }
        public BigDecimal getRoiY() { return roiY; }
        public BigDecimal getRoiWidth() { return roiWidth; }
        public BigDecimal getRoiHeight() { return roiHeight; }
    }
}
