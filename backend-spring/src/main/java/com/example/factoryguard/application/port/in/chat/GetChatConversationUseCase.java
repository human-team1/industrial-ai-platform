package com.example.factoryguard.application.port.in.chat;

import com.example.factoryguard.application.dto.chat.ChatConversationDetail;

public interface GetChatConversationUseCase {

    ChatConversationDetail execute(Long conversationId);
}
