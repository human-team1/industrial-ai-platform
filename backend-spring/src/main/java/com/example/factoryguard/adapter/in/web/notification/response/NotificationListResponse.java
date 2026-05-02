package com.example.factoryguard.adapter.in.web.notification.response;

import com.example.factoryguard.application.dto.notification.NotificationPageResult;

import java.util.List;

public record NotificationListResponse(
        List<NotificationSummaryResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static NotificationListResponse from(NotificationPageResult page) {
        List<NotificationSummaryResponse> content = page.getContent().stream()
                .map(NotificationSummaryResponse::from)
                .toList();
        return new NotificationListResponse(
                content,
                page.getPage(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
