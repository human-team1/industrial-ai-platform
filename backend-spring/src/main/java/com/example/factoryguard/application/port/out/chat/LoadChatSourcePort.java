package com.example.factoryguard.application.port.out.chat;

import com.example.factoryguard.domain.chat.model.ChatSource;
import com.example.factoryguard.application.dto.chat.ChatSourceResult;

import java.util.List;

public interface LoadChatSourcePort {

    List<ChatSource> findAllByMessageId(Long messageId);

    List<ChatSourceResult> findAllResultsByMessageId(Long messageId);
}
