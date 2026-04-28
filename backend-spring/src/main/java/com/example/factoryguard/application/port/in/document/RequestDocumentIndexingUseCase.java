package com.example.factoryguard.application.port.in.document;

import com.example.factoryguard.application.dto.document.DocumentIndexingStatusResult;
import com.example.factoryguard.application.dto.document.RequestDocumentIndexingCommand;

public interface RequestDocumentIndexingUseCase {

    DocumentIndexingStatusResult execute(RequestDocumentIndexingCommand command);
}
