package com.example.factoryguard.application.dto.notification;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class NotificationPageResult {

    private final List<NotificationSummary> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
}
