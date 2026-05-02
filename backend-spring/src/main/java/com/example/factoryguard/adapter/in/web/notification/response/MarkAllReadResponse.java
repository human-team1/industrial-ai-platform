package com.example.factoryguard.adapter.in.web.notification.response;

import com.example.factoryguard.application.dto.notification.MarkAllReadResult;

public record MarkAllReadResponse(int updatedCount) {

    public static MarkAllReadResponse from(MarkAllReadResult result) {
        return new MarkAllReadResponse(result.getUpdatedCount());
    }
}
