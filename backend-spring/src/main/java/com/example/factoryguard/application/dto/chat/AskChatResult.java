package com.example.factoryguard.application.dto.chat;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AskChatResult {

    private final Long conversationId;
    private final ChatMessageResult userMessage;
    private final ChatMessageResult assistantMessage;
}
