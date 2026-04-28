package com.example.factoryguard.application.port.in.chat;

import com.example.factoryguard.application.dto.chat.AskResultBasedQuestionCommand;
import com.example.factoryguard.application.dto.chat.ChatAnswerResult;

public interface AskResultBasedQuestionUseCase {

    ChatAnswerResult execute(AskResultBasedQuestionCommand command);
}
