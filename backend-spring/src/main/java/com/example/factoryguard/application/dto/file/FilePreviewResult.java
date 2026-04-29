package com.example.factoryguard.application.dto.file;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FilePreviewResult {

    private final byte[] content;
    private final String contentType;
}
