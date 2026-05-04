package com.example.factoryguard.application.port.out.document;

import com.example.factoryguard.application.dto.document.ConvertedDocument;

public interface ConvertDocumentPort {

    ConvertedDocument convertToMarkdown(byte[] fileBytes, String originalFilename);
}
