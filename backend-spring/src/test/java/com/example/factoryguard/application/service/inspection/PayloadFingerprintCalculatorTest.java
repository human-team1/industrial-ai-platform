package com.example.factoryguard.application.service.inspection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PayloadFingerprintCalculatorTest {

    @Test
    @DisplayName("No.10 동일 입력은 동일 fingerprint 생성 - idempotency 비교 기반")
    void sameInputProducesSameFingerprint() {
        String first = PayloadFingerprintCalculator.compute(
                10L, 1L, 100L, 200L,
                "checksum-abc", "sample.jpg", "image/jpeg", 1024L);
        String second = PayloadFingerprintCalculator.compute(
                10L, 1L, 100L, 200L,
                "checksum-abc", "sample.jpg", "image/jpeg", 1024L);

        assertThat(first).isEqualTo(second);
    }

    @Test
    @DisplayName("No.10 다른 파일 체크섬은 다른 fingerprint - 동일 키로 다른 페이로드 충돌 감지")
    void differentChecksumProducesDifferentFingerprint() {
        String first = PayloadFingerprintCalculator.compute(
                10L, 1L, 100L, 200L,
                "checksum-A", "sample.jpg", "image/jpeg", 1024L);
        String second = PayloadFingerprintCalculator.compute(
                10L, 1L, 100L, 200L,
                "checksum-B", "sample.jpg", "image/jpeg", 1024L);

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    @DisplayName("No.10 organizationId/userId 분리 - 다른 사용자의 동일 키는 다른 fingerprint")
    void differentUserProducesDifferentFingerprint() {
        String first = PayloadFingerprintCalculator.compute(
                10L, 1L, 100L, 200L,
                null, "sample.jpg", "image/jpeg", 1024L);
        String second = PayloadFingerprintCalculator.compute(
                10L, 2L, 100L, 200L,
                null, "sample.jpg", "image/jpeg", 1024L);

        assertThat(first).isNotEqualTo(second);
    }
}
