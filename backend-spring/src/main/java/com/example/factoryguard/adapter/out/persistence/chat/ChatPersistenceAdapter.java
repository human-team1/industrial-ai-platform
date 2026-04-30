package com.example.factoryguard.adapter.out.persistence.chat;

import com.example.factoryguard.application.dto.chat.ChatConversationPageResult;
import com.example.factoryguard.application.dto.chat.ChatConversationSummary;
import com.example.factoryguard.application.dto.chat.ChatSourceResult;
import com.example.factoryguard.application.dto.chat.ListChatConversationsQuery;
import com.example.factoryguard.application.port.out.chat.DeleteChatConversationPort;
import com.example.factoryguard.application.port.out.chat.LoadChatConversationPort;
import com.example.factoryguard.application.port.out.chat.LoadChatMessagePort;
import com.example.factoryguard.application.port.out.chat.LoadChatSourcePort;
import com.example.factoryguard.application.port.out.chat.SaveChatConversationPort;
import com.example.factoryguard.application.port.out.chat.SaveChatMessagePort;
import com.example.factoryguard.application.port.out.chat.SaveChatSourcePort;
import com.example.factoryguard.domain.chat.model.ChatConversation;
import com.example.factoryguard.domain.chat.model.ChatMessage;
import com.example.factoryguard.domain.chat.model.ChatSource;
import com.example.factoryguard.domain.chat.vo.ChatAnswerStatus;
import com.example.factoryguard.domain.chat.vo.ChatMessageRole;
import com.example.factoryguard.domain.chat.vo.ChatMessageStatus;
import com.example.factoryguard.domain.chat.vo.ChatSourceType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ChatPersistenceAdapter implements LoadChatConversationPort, SaveChatConversationPort,
        LoadChatMessagePort, SaveChatMessagePort, LoadChatSourcePort, SaveChatSourcePort, DeleteChatConversationPort {

    private final EntityManager entityManager;

    @Override
    public Optional<ChatConversation> findById(Long conversationId) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery("""
                SELECT conversation_id, user_id, title, created_at, updated_at, deleted_at
                FROM chat_conversation
                WHERE conversation_id = :conversationId
                  AND deleted_at IS NULL
                """)
                .setParameter("conversationId", conversationId)
                .getResultList();
        return rows.stream().findFirst().map(this::toConversation);
    }

    @Override
    public List<ChatConversation> findAllByUserId(Long userId) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery("""
                SELECT conversation_id, user_id, title, created_at, updated_at, deleted_at
                FROM chat_conversation
                WHERE user_id = :userId
                  AND deleted_at IS NULL
                ORDER BY updated_at DESC
                """)
                .setParameter("userId", userId)
                .getResultList();
        return rows.stream().map(this::toConversation).toList();
    }

    @Override
    public ChatConversationPageResult findPageByUserId(ListChatConversationsQuery query) {
        StringBuilder where = new StringBuilder(" c.user_id = :userId AND c.deleted_at IS NULL ");
        if (hasText(query.getKeyword())) {
            where.append("""
                     AND (c.title LIKE :keyword OR EXISTS (
                        SELECT 1 FROM chat_message cm
                        WHERE cm.conversation_id = c.conversation_id
                          AND cm.message_text LIKE :keyword
                     ))
                    """);
        }
        if (query.getFrom() != null) {
            where.append(" AND c.updated_at >= :fromDate ");
        }
        if (query.getTo() != null) {
            where.append(" AND c.updated_at < :toDate ");
        }

        Query contentQuery = entityManager.createNativeQuery("""
                SELECT c.conversation_id, c.title,
                       COALESCE((
                           SELECT cm.message_text
                           FROM chat_message cm
                           WHERE cm.conversation_id = c.conversation_id
                           ORDER BY cm.created_at DESC, cm.message_id DESC
                           LIMIT 1
                       ), '') AS last_message_preview,
                       (SELECT COUNT(*) FROM chat_message cm WHERE cm.conversation_id = c.conversation_id) AS message_count,
                       (SELECT COUNT(*) FROM chat_source cs JOIN chat_message cm ON cm.message_id = cs.message_id WHERE cm.conversation_id = c.conversation_id) AS source_count,
                       c.created_at, c.updated_at
                FROM chat_conversation c
                WHERE
                """ + where + """
                ORDER BY c.updated_at DESC
                LIMIT :limit OFFSET :offset
                """);
        bindConversationQuery(contentQuery, query);
        contentQuery.setParameter("limit", query.getSize());
        contentQuery.setParameter("offset", query.getPage() * query.getSize());

        @SuppressWarnings("unchecked")
        List<Object[]> rows = contentQuery.getResultList();
        List<ChatConversationSummary> content = new ArrayList<>();
        for (Object[] row : rows) {
            content.add(ChatConversationSummary.builder()
                    .conversationId(toLong(row[0]))
                    .title(toString(row[1]))
                    .lastMessagePreview(trimPreview(toString(row[2])))
                    .messageCount(toLongOrZero(row[3]))
                    .sourceCount(toLongOrZero(row[4]))
                    .createdAt(toDateTime(row[5]))
                    .updatedAt(toDateTime(row[6]))
                    .build());
        }

        Query countQuery = entityManager.createNativeQuery("SELECT COUNT(*) FROM chat_conversation c WHERE " + where);
        bindConversationQuery(countQuery, query);
        long total = ((Number) countQuery.getSingleResult()).longValue();
        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / query.getSize());

        return ChatConversationPageResult.builder()
                .content(content)
                .page(query.getPage())
                .size(query.getSize())
                .totalElements(total)
                .totalPages(totalPages)
                .build();
    }

    @Override
    public ChatConversation save(ChatConversation conversation) {
        LocalDateTime now = LocalDateTime.now();
        if (conversation.getConversationId() == null) {
            entityManager.createNativeQuery("""
                    INSERT INTO chat_conversation (user_id, title, created_at, updated_at)
                    VALUES (:userId, :title, :createdAt, :updatedAt)
                    """)
                    .setParameter("userId", conversation.getUserId())
                    .setParameter("title", conversation.getTitle())
                    .setParameter("createdAt", Timestamp.valueOf(now))
                    .setParameter("updatedAt", Timestamp.valueOf(now))
                    .executeUpdate();
            Long id = ((Number) entityManager.createNativeQuery("SELECT LAST_INSERT_ID()").getSingleResult()).longValue();
            return findById(id).orElseThrow();
        }
        entityManager.createNativeQuery("""
                UPDATE chat_conversation
                SET title = :title, updated_at = :updatedAt
                WHERE conversation_id = :conversationId
                """)
                .setParameter("title", conversation.getTitle())
                .setParameter("updatedAt", Timestamp.valueOf(now))
                .setParameter("conversationId", conversation.getConversationId())
                .executeUpdate();
        return findById(conversation.getConversationId()).orElseThrow();
    }

    @Override
    public List<ChatMessage> findAllByConversationId(Long conversationId) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery("""
                SELECT message_id, conversation_id, role, message_text, message_status,
                       answer_status, error_code, model_name, created_at, updated_at
                FROM chat_message
                WHERE conversation_id = :conversationId
                ORDER BY created_at ASC, message_id ASC
                """)
                .setParameter("conversationId", conversationId)
                .getResultList();
        return rows.stream().map(this::toMessage).toList();
    }

    @Override
    public Optional<ChatMessage> findMessageById(Long messageId) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery("""
                SELECT message_id, conversation_id, role, message_text, message_status,
                       answer_status, error_code, model_name, created_at, updated_at
                FROM chat_message
                WHERE message_id = :messageId
                """)
                .setParameter("messageId", messageId)
                .getResultList();
        return rows.stream().findFirst().map(this::toMessage);
    }

    @Override
    public ChatMessage save(ChatMessage message) {
        LocalDateTime now = LocalDateTime.now();
        entityManager.createNativeQuery("""
                INSERT INTO chat_message (
                    conversation_id, role, message_text, message_status, answer_status,
                    error_code, model_name, created_at, updated_at
                ) VALUES (
                    :conversationId, :role, :messageText, :messageStatus, :answerStatus,
                    :errorCode, :modelName, :createdAt, :updatedAt
                )
                """)
                .setParameter("conversationId", message.getConversationId())
                .setParameter("role", message.getRole().name())
                .setParameter("messageText", message.getMessageText())
                .setParameter("messageStatus", nullableName(message.getMessageStatus(), ChatMessageStatus.SUCCESS.name()))
                .setParameter("answerStatus", nullableName(message.getAnswerStatus(), null))
                .setParameter("errorCode", message.getErrorCode())
                .setParameter("modelName", message.getModelName())
                .setParameter("createdAt", Timestamp.valueOf(now))
                .setParameter("updatedAt", Timestamp.valueOf(now))
                .executeUpdate();
        entityManager.createNativeQuery("""
                UPDATE chat_conversation
                SET updated_at = :updatedAt
                WHERE conversation_id = :conversationId
                """)
                .setParameter("updatedAt", Timestamp.valueOf(now))
                .setParameter("conversationId", message.getConversationId())
                .executeUpdate();
        Long id = ((Number) entityManager.createNativeQuery("SELECT LAST_INSERT_ID()").getSingleResult()).longValue();
        return findMessageById(id).orElseThrow();
    }

    @Override
    public List<ChatSource> findAllByMessageId(Long messageId) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery("""
                SELECT chat_source_id, message_id, source_type, source_id, document_id,
                       document_title, document_type, chunk_id, page_no, section,
                       source_snippet, score, created_at
                FROM chat_source
                WHERE message_id = :messageId
                ORDER BY chat_source_id ASC
                """)
                .setParameter("messageId", messageId)
                .getResultList();
        return rows.stream().map(this::toSource).toList();
    }

    @Override
    public List<ChatSourceResult> findAllResultsByMessageId(Long messageId) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery("""
                SELECT chat_source_id, message_id, source_type, source_id, document_id,
                       document_title, document_type, page_no, chunk_id, section,
                       source_snippet, score, created_at
                FROM chat_source
                WHERE message_id = :messageId
                ORDER BY chat_source_id ASC
                """)
                .setParameter("messageId", messageId)
                .getResultList();
        return rows.stream().map(this::toSourceResult).toList();
    }

    @Override
    public ChatSource save(ChatSource source) {
        entityManager.createNativeQuery("""
                INSERT INTO chat_source (
                    message_id, source_type, source_id, document_id, document_title, document_type,
                    chunk_id, page_no, section, source_snippet, score, created_at
                ) VALUES (
                    :messageId, :sourceType, :sourceId, :documentId, :documentTitle, :documentType,
                    :chunkId, :pageNo, :section, :sourceSnippet, :score, :createdAt
                )
                """)
                .setParameter("messageId", source.getMessageId())
                .setParameter("sourceType", source.getSourceType().name())
                .setParameter("sourceId", source.getSourceId())
                .setParameter("documentId", source.getDocumentId())
                .setParameter("documentTitle", source.getDocumentTitle())
                .setParameter("documentType", source.getDocumentType())
                .setParameter("chunkId", source.getChunkId())
                .setParameter("pageNo", source.getPageNo())
                .setParameter("section", source.getSection())
                .setParameter("sourceSnippet", source.getSourceSnippet())
                .setParameter("score", source.getScore())
                .setParameter("createdAt", Timestamp.valueOf(LocalDateTime.now()))
                .executeUpdate();
        Long id = ((Number) entityManager.createNativeQuery("SELECT LAST_INSERT_ID()").getSingleResult()).longValue();
        return findAllByMessageId(source.getMessageId()).stream()
                .filter(saved -> saved.getChatSourceId().equals(id))
                .findFirst()
                .orElseThrow();
    }

    @Override
    public void deleteById(Long conversationId) {
        entityManager.createNativeQuery("UPDATE chat_conversation SET deleted_at = :deletedAt WHERE conversation_id = :conversationId")
                .setParameter("deletedAt", Timestamp.valueOf(LocalDateTime.now()))
                .setParameter("conversationId", conversationId)
                .executeUpdate();
    }

    @Override
    public boolean softDelete(Long conversationId, Long userId) {
        LocalDateTime now = LocalDateTime.now();
        int updated = entityManager.createNativeQuery("""
                UPDATE chat_conversation
                SET deleted_at = :deletedAt, updated_at = :updatedAt
                WHERE conversation_id = :conversationId
                  AND user_id = :userId
                  AND deleted_at IS NULL
                """)
                .setParameter("deletedAt", Timestamp.valueOf(now))
                .setParameter("updatedAt", Timestamp.valueOf(now))
                .setParameter("conversationId", conversationId)
                .setParameter("userId", userId)
                .executeUpdate();
        return updated > 0;
    }

    private void bindConversationQuery(Query query, ListChatConversationsQuery request) {
        query.setParameter("userId", request.getUserId());
        if (hasText(request.getKeyword())) {
            query.setParameter("keyword", "%" + request.getKeyword().trim() + "%");
        }
        if (request.getFrom() != null) {
            query.setParameter("fromDate", Timestamp.valueOf(request.getFrom().atStartOfDay()));
        }
        if (request.getTo() != null) {
            query.setParameter("toDate", Timestamp.valueOf(request.getTo().plusDays(1).atStartOfDay()));
        }
    }

    private ChatConversation toConversation(Object[] row) {
        return ChatConversation.builder()
                .conversationId(toLong(row[0]))
                .userId(toLong(row[1]))
                .title(toString(row[2]))
                .createdAt(toDateTime(row[3]))
                .updatedAt(toDateTime(row[4]))
                .deletedAt(toDateTime(row[5]))
                .build();
    }

    private ChatMessage toMessage(Object[] row) {
        return ChatMessage.builder()
                .messageId(toLong(row[0]))
                .conversationId(toLong(row[1]))
                .role(ChatMessageRole.valueOf(toString(row[2])))
                .messageText(toString(row[3]))
                .messageStatus(toMessageStatus(row[4]))
                .answerStatus(toAnswerStatus(row[5]))
                .errorCode(toString(row[6]))
                .modelName(toString(row[7]))
                .createdAt(toDateTime(row[8]))
                .updatedAt(toDateTime(row[9]))
                .build();
    }

    private ChatSource toSource(Object[] row) {
        return ChatSource.builder()
                .chatSourceId(toLong(row[0]))
                .messageId(toLong(row[1]))
                .sourceType(toSourceType(row[2]))
                .sourceId(toLong(row[3]))
                .documentId(toLong(row[4]))
                .documentTitle(toString(row[5]))
                .documentType(toString(row[6]))
                .chunkId(toLong(row[7]))
                .pageNo(toInteger(row[8]))
                .section(toString(row[9]))
                .sourceSnippet(toString(row[10]))
                .score(toBigDecimal(row[11]))
                .createdAt(toDateTime(row[12]))
                .build();
    }

    private ChatSourceResult toSourceResult(Object[] row) {
        return ChatSourceResult.builder()
                .chatSourceId(toLong(row[0]))
                .messageId(toLong(row[1]))
                .sourceType(toSourceType(row[2]))
                .sourceId(toLong(row[3]))
                .documentId(toLong(row[4]))
                .documentTitle(toString(row[5]))
                .documentType(toString(row[6]))
                .page(toInteger(row[7]))
                .chunkId(toLong(row[8]))
                .section(toString(row[9]))
                .sourceSnippet(toString(row[10]))
                .score(toBigDecimal(row[11]))
                .createdAt(toDateTime(row[12]))
                .build();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String trimPreview(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        return trimmed.length() <= 120 ? trimmed : trimmed.substring(0, 120);
    }

    private String nullableName(Enum<?> value, String defaultValue) {
        return value == null ? defaultValue : value.name();
    }

    private Long toLong(Object value) {
        return value == null ? null : ((Number) value).longValue();
    }

    private long toLongOrZero(Object value) {
        return value == null ? 0L : ((Number) value).longValue();
    }

    private Integer toInteger(Object value) {
        return value == null ? null : ((Number) value).intValue();
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        return BigDecimal.valueOf(((Number) value).doubleValue());
    }

    private String toString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private LocalDateTime toDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        return ((Timestamp) value).toLocalDateTime();
    }

    private ChatMessageStatus toMessageStatus(Object value) {
        return value == null ? ChatMessageStatus.SUCCESS : ChatMessageStatus.valueOf(toString(value));
    }

    private ChatAnswerStatus toAnswerStatus(Object value) {
        return value == null ? null : ChatAnswerStatus.valueOf(toString(value));
    }

    private ChatSourceType toSourceType(Object value) {
        return value == null ? ChatSourceType.DOCUMENT : ChatSourceType.valueOf(toString(value));
    }
}
