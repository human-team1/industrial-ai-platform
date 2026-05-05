package com.example.factoryguard.application.port.in.document;

import com.example.factoryguard.application.dto.document.DocumentUploadCommand;
import com.example.factoryguard.application.dto.document.DocumentUploadResult;

public interface UploadDocumentUseCase {

    DocumentUploadResult execute(DocumentUploadCommand command);
}
