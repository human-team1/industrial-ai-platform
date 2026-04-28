package com.example.factoryguard.domain.chat.model;

import com.example.factoryguard.domain.chat.vo.ChatSourceType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatSource {

    private final Long chatSourceId;
    private final Long messageId;
    private final ChatSourceType sourceType;
    private final Long sourceId;
    private final Long chunkId;
    private final String sourceSnippet;
}
