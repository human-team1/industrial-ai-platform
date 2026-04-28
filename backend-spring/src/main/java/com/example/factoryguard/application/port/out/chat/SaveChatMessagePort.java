package com.example.factoryguard.application.port.out.chat;

import com.example.factoryguard.domain.chat.model.ChatMessage;

public interface SaveChatMessagePort {

    ChatMessage save(ChatMessage message);
}
