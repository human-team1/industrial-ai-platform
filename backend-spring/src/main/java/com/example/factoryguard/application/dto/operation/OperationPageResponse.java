package com.example.factoryguard.application.dto.operation;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OperationPageResponse<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
}
