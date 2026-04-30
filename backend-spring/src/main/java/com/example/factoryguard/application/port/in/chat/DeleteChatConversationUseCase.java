package com.example.factoryguard.application.port.in.chat;

public interface DeleteChatConversationUseCase {

    void delete(Long userId, Long conversationId);
}
