package com.example.factoryguard.application.port.out.chat;

import com.example.factoryguard.application.dto.chat.RagAnswerRequest;
import com.example.factoryguard.application.dto.chat.RagAnswerResponse;

public interface RequestRagAnswerPort {

    RagAnswerResponse requestAnswer(RagAnswerRequest request);
}
