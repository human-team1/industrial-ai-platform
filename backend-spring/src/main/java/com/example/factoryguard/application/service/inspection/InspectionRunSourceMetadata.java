package com.example.factoryguard.application.service.inspection;

public final class InspectionRunSourceMetadata {

    private InspectionRunSourceMetadata() {
    }

    public static String forUpload(String originalFileName, Long deploymentId) {
        return "upload|deployment=" + deploymentId + "|file=" + sanitize(originalFileName);
    }

    public static String forRealtime(Long cameraId, Long deploymentId) {
        return "realtime|deployment=" + deploymentId + "|camera=" + cameraId;
    }

    public static Long parseDeploymentId(String sourceId) {
        if (sourceId == null || sourceId.isBlank()) {
            return null;
        }
        for (String token : sourceId.split("\\|")) {
            if (!token.startsWith("deployment=")) {
                continue;
            }
            String raw = token.substring("deployment=".length()).trim();
            if (raw.isBlank()) {
                return null;
            }
            try {
                return Long.parseLong(raw);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        return value.replace("|", "_");
    }
}
