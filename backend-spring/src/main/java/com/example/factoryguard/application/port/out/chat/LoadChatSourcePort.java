package com.example.factoryguard.application.port.out.chat;

import com.example.factoryguard.domain.chat.model.ChatSource;

import java.util.List;

public interface LoadChatSourcePort {

    List<ChatSource> findAllByMessageId(Long messageId);
}
