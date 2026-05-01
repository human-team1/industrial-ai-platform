package com.example.factoryguard.application.port.in.chat;

import com.example.factoryguard.application.dto.chat.ChatConversationResult;
import com.example.factoryguard.application.dto.chat.CreateChatConversationCommand;

public interface CreateChatConversationUseCase {

    ChatConversationResult create(CreateChatConversationCommand command);
}
