package com.example.factoryguard.application.dto.document;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DocumentListPageResult {

    private final List<DocumentListItem> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
}
