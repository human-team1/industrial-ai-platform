package com.example.factoryguard.application.dto.chat;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatConversationSummary {

    private final Long conversationId;
    private final String title;
    private final LocalDateTime updatedAt;
}
