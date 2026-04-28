package com.example.factoryguard.domain.chat.model;

import com.example.factoryguard.domain.chat.vo.ChatMessageRole;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatMessage {

    private final Long messageId;
    private final Long conversationId;
    private final ChatMessageRole role;
    private final String messageText;
    private final LocalDateTime createdAt;
}
