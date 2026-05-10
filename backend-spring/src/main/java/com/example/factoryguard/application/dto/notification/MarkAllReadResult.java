package com.example.factoryguard.application.dto.notification;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MarkAllReadResult {

    private final int updatedCount;
}
