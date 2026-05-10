package com.example.factoryguard.application.port.out.chat;

import com.example.factoryguard.domain.chat.model.ChatSource;

public interface SaveChatSourcePort {

    ChatSource save(ChatSource source);
}
