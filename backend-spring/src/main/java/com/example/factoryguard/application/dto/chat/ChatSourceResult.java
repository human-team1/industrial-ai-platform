package com.example.factoryguard.application.dto.chat;

import com.example.factoryguard.domain.chat.vo.ChatSourceType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ChatSourceResult {

    private final Long chatSourceId;
    private final Long messageId;
    private final ChatSourceType sourceType;
    private final Long sourceId;
    private final Long documentId;
    private final String documentTitle;
    private final String documentType;
    private final Integer page;
    private final Long chunkId;
    private final String section;
    private final String sourceSnippet;
    private final BigDecimal score;
    private final java.time.LocalDateTime createdAt;
}
