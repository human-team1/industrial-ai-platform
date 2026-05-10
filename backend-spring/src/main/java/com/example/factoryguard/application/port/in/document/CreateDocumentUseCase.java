package com.example.factoryguard.application.port.in.document;

import com.example.factoryguard.application.dto.document.CreateDocumentCommand;
import com.example.factoryguard.application.dto.document.DocumentDetail;

public interface CreateDocumentUseCase {

    DocumentDetail execute(CreateDocumentCommand command);
}
