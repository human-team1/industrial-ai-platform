package com.example.factoryguard.application.port.in.chat;

import com.example.factoryguard.application.dto.chat.AskDocumentQuestionCommand;
import com.example.factoryguard.application.dto.chat.ChatAnswerResult;

public interface AskDocumentQuestionUseCase {

    ChatAnswerResult execute(AskDocumentQuestionCommand command);
}
