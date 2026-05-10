package com.example.factoryguard.application.port.in.document;

import com.example.factoryguard.application.dto.document.DocumentDetail;
import com.example.factoryguard.application.dto.document.UpdateDocumentCommand;

public interface UpdateDocumentUseCase {

    DocumentDetail execute(UpdateDocumentCommand command);
}
