package com.example.factoryguard.application.dto.inspection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@RequiredArgsConstructor
public class SubmitInspectionCommand {

    private final Long userId;
    private final String sessionId;
    private final Long targetId;
    private final Long thresholdId;
    private final String inputMode;
    private final String sourceType;
    private final String roiMode;
    private final Boolean qualityGateEnabled;
    private final MultipartFile file;
    private final String idempotencyKey;
}
