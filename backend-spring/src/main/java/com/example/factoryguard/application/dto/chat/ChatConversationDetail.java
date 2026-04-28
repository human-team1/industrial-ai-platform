package com.example.factoryguard.application.dto.chat;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChatConversationDetail {

    private final Long conversationId;
    private final String title;
    private final List<ChatMessageResult> messages;
}
