package com.example.factoryguard.application.dto.chat;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateChatConversationCommand {

    private final Long userId;
    private final String title;
}
