package com.example.factoryguard.application.port.out.chat;

public interface DeleteChatConversationPort {

    void deleteById(Long conversationId);

    boolean softDelete(Long conversationId, Long userId);
}
