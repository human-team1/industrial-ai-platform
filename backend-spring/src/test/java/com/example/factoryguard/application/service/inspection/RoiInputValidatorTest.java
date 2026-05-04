package com.example.factoryguard.application.service.inspection;

import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.inspection.vo.RoiMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoiInputValidatorTest {

    private final RoiInputValidator validator = new RoiInputValidator();

    @Test
    @DisplayName("No.9 roiMode 누락 시 FULL_FRAME 디폴트, 좌표는 무시된다")
    void nullRoiModeDefaultsToFullFrameAndIgnoresCoordinates() {
        RoiInputValidator.Result result = validator.validate(null,
                new BigDecimal("0.5"), new BigDecimal("0.5"),
                new BigDecimal("0.2"), new BigDecimal("0.2"));

        assertThat(result.getRoiMode()).isEqualTo(RoiMode.FULL_FRAME);
        assertThat(result.getRoiX()).isNull();
        assertThat(result.getRoiY()).isNull();
        assertThat(result.getRoiWidth()).isNull();
        assertThat(result.getRoiHeight()).isNull();
    }

    @Test
    @DisplayName("No.9 FULL_FRAME 명시 - 좌표 null 허용, 좌표는 무시")
    void fullFrameAllowsNullCoordinates() {
        RoiInputValidator.Result result = validator.validate("FULL_FRAME", null, null, null, null);

        assertThat(result.getRoiMode()).isEqualTo(RoiMode.FULL_FRAME);
    }

    @Test
    @DisplayName("No.8 FIXED + 정상 좌표 - 통과")
    void fixedWithValidCoordinatesPasses() {
        RoiInputValidator.Result result = validator.validate("FIXED",
                new BigDecimal("0.10"), new BigDecimal("0.10"),
                new BigDecimal("0.50"), new BigDecimal("0.50"));

        assertThat(result.getRoiMode()).isEqualTo(RoiMode.FIXED);
        assertThat(result.getRoiX()).isEqualByComparingTo("0.10");
        assertThat(result.getRoiWidth()).isEqualByComparingTo("0.50");
    }

    @Test
    @DisplayName("No.9 FIXED + roiX 누락 - INVALID_ROI_REQUIRED")
    void fixedMissingRoiXThrowsRequired() {
        assertThatThrownBy(() -> validator.validate("FIXED",
                null, new BigDecimal("0.1"),
                new BigDecimal("0.2"), new BigDecimal("0.2")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_ROI_REQUIRED);
    }

    @Test
    @DisplayName("No.9 FIXED + 좌표 전체 누락 - INVALID_ROI_REQUIRED")
    void fixedMissingAllCoordinatesThrowsRequired() {
        assertThatThrownBy(() -> validator.validate("FIXED", null, null, null, null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_ROI_REQUIRED);
    }

    @Test
    @DisplayName("No.8 roiX > 1 - INVALID_ROI_RANGE")
    void roiXAboveOneThrowsRange() {
        assertThatThrownBy(() -> validator.validate("FIXED",
                new BigDecimal("1.2"), new BigDecimal("0.1"),
                new BigDecimal("0.2"), new BigDecimal("0.2")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_ROI_RANGE);
    }

    @Test
    @DisplayName("No.8 roiY < 0 - INVALID_ROI_RANGE")
    void roiYBelowZeroThrowsRange() {
        assertThatThrownBy(() -> validator.validate("FIXED",
                new BigDecimal("0.1"), new BigDecimal("-0.1"),
                new BigDecimal("0.2"), new BigDecimal("0.2")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_ROI_RANGE);
    }

    @Test
    @DisplayName("No.8 roiWidth = 0 - INVALID_ROI_RANGE")
    void roiWidthZeroThrowsRange() {
        assertThatThrownBy(() -> validator.validate("FIXED",
                new BigDecimal("0.1"), new BigDecimal("0.1"),
                BigDecimal.ZERO, new BigDecimal("0.2")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_ROI_RANGE);
    }

    @Test
    @DisplayName("No.8 roiX + roiWidth > 1 - INVALID_ROI_RANGE")
    void roiXPlusWidthExceedsOneThrowsRange() {
        assertThatThrownBy(() -> validator.validate("FIXED",
                new BigDecimal("0.7"), new BigDecimal("0.1"),
                new BigDecimal("0.5"), new BigDecimal("0.2")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_ROI_RANGE);
    }

    @Test
    @DisplayName("roiMode 알 수 없는 값 - INVALID_ROI_RANGE")
    void unknownRoiModeThrowsRange() {
        assertThatThrownBy(() -> validator.validate("UNKNOWN", null, null, null, null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_ROI_RANGE);
    }
}
