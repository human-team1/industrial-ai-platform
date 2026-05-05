package com.example.factoryguard.application.dto.document;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConvertedDocument {

    private final String markdown;
    private final String convertedFileName;
    private final String mimeType;
    private final int pageCount;
}
