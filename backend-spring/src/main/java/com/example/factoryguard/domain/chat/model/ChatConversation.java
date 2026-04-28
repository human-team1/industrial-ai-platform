package com.example.factoryguard.domain.chat.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatConversation {

    private final Long conversationId;
    private final Long userId;
    private final String title;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;
}
