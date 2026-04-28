package com.example.factoryguard.application.port.in.document;

import com.example.factoryguard.application.dto.document.DocumentSummary;
import com.example.factoryguard.application.dto.document.ListDocumentsQuery;

import java.util.List;

public interface ListDocumentsUseCase {

    List<DocumentSummary> execute(ListDocumentsQuery query);
}
