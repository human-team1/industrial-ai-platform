package com.example.factoryguard.domain.chat.model;

import com.example.factoryguard.domain.chat.vo.ChatSourceType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class ChatSource {

    private final Long chatSourceId;
    private final Long messageId;
    private final ChatSourceType sourceType;
    private final Long sourceId;
    private final Long documentId;
    private final String documentTitle;
    private final String documentType;
    private final Long chunkId;
    private final Integer pageNo;
    private final String section;
    private final String sourceSnippet;
    private final BigDecimal score;
    private final LocalDateTime createdAt;
}
