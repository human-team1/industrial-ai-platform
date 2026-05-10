package com.example.factoryguard.application.port.in.document;

import com.example.factoryguard.application.dto.document.DeleteDocumentCommand;

public interface DeleteDocumentUseCase {

    void execute(DeleteDocumentCommand command);
}
