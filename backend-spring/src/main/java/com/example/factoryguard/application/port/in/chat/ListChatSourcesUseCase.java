package com.example.factoryguard.application.port.in.chat;

import com.example.factoryguard.application.dto.chat.ChatSourceResult;

import java.util.List;

public interface ListChatSourcesUseCase {

    List<ChatSourceResult> listSources(Long userId, Long messageId);
}
