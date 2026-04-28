package com.example.factoryguard.application.port.in.chat;

public interface DeleteChatConversationUseCase {

    void execute(Long userId, Long conversationId);
}
