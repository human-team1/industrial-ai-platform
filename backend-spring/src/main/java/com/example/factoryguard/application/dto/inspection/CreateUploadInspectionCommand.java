package com.example.factoryguard.application.dto.inspection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CreateUploadInspectionCommand {

    private final Long userId;
    private final String sessionId;
    private final Long targetId;
    private final Long thresholdId;
    private final Long fileId;
    private final String fileUrl;
    private final String originalFileName;
    private final String mimeType;
}
