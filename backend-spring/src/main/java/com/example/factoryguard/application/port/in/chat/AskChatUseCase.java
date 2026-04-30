package com.example.factoryguard.application.port.in.chat;

import com.example.factoryguard.application.dto.chat.AskChatCommand;
import com.example.factoryguard.application.dto.chat.AskChatResult;

public interface AskChatUseCase {

    AskChatResult execute(AskChatCommand command);
}
