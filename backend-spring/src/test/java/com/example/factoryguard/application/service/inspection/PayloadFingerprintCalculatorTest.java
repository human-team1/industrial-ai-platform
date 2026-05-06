package com.example.factoryguard.application.service.inspection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PayloadFingerprintCalculatorTest {

    @Test
    @DisplayName("동일 입력은 동일 fingerprint를 생성한다")
    void sameInputProducesSameFingerprint() {
        String first = PayloadFingerprintCalculator.compute(
                10L, 1L, 100L, 300L, 200L,
                "checksum-abc", "sample.jpg", "image/jpeg", 1024L);
        String second = PayloadFingerprintCalculator.compute(
                10L, 1L, 100L, 300L, 200L,
                "checksum-abc", "sample.jpg", "image/jpeg", 1024L);

        assertThat(first).isEqualTo(second);
    }

    @Test
    @DisplayName("체크섬이 다르면 다른 fingerprint를 생성한다")
    void differentChecksumProducesDifferentFingerprint() {
        String first = PayloadFingerprintCalculator.compute(
                10L, 1L, 100L, 300L, 200L,
                "checksum-A", "sample.jpg", "image/jpeg", 1024L);
        String second = PayloadFingerprintCalculator.compute(
                10L, 1L, 100L, 300L, 200L,
                "checksum-B", "sample.jpg", "image/jpeg", 1024L);

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    @DisplayName("deploymentId null 케이스도 fingerprint 비교에 반영한다")
    void nullDeploymentIdStillProducesStableFingerprint() {
        String first = PayloadFingerprintCalculator.compute(
                10L, 1L, 100L, null, 200L,
                null, "sample.jpg", "image/jpeg", 1024L);
        String second = PayloadFingerprintCalculator.compute(
                10L, 2L, 100L, null, 200L,
                null, "sample.jpg", "image/jpeg", 1024L);

        assertThat(first).isNotEqualTo(second);
    }
}
