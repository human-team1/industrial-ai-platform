package com.example.factoryguard.application.dto.chat;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ChatConversationDetail {

    private final Long conversationId;
    private final String title;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final List<ChatMessageResult> messages;
}
