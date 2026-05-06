package com.example.factoryguard.application.service.inspection;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class PayloadFingerprintCalculator {

    private PayloadFingerprintCalculator() {}

    public static String compute(Long organizationId, Long userId, Long targetId, Long deploymentId, Long thresholdId,
                                 String fileChecksum, String originalFileName,
                                 String mimeType, Long fileSize) {
        StringBuilder sb = new StringBuilder();
        appendField(sb, "organizationId", organizationId);
        appendField(sb, "userId", userId);
        appendField(sb, "targetId", targetId);
        appendField(sb, "deploymentId", deploymentId);
        appendField(sb, "thresholdId", thresholdId);
        if (fileChecksum != null && !fileChecksum.isBlank()) {
            appendField(sb, "fileChecksum", fileChecksum);
            appendField(sb, "mimeType", mimeType);
        } else {
            appendField(sb, "originalFileName", originalFileName);
            appendField(sb, "mimeType", mimeType);
            appendField(sb, "fileSize", fileSize);
        }
        return sha256Hex(sb.toString());
    }

    private static void appendField(StringBuilder sb, String name, Object value) {
        sb.append(name).append('=')
                .append(value == null ? "<null>" : value.toString())
                .append('|');
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
