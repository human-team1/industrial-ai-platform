package com.example.factoryguard.application.dto.chat;

import com.example.factoryguard.domain.chat.vo.ChatMessageRole;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatMessageResult {

    private final Long messageId;
    private final Long conversationId;
    private final ChatMessageRole role;
    private final String messageText;
    private final LocalDateTime createdAt;
}
