package com.example.factoryguard.adapter.out.cache.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenRedisAdapterTest {

    @Mock
    private RedisCacheAdapter redisCacheAdapter;

    private TokenRedisAdapter tokenRedisAdapter;

    @BeforeEach
    void setUp() {
        tokenRedisAdapter = new TokenRedisAdapter(redisCacheAdapter, new RedisKeyFactory());
    }

    @Test
    @DisplayName("findAllSessionIdsByUserId - SCAN 결과의 prefix 이후 부분만 sessionId로 추출한다")
    void extractsSessionIdsFromScanResult() {
        long userId = 1L;
        Set<String> scanned = new LinkedHashSet<>(List.of(
                "session:1:sess-001",
                "session:1:sess-002"
        ));
        when(redisCacheAdapter.scanKeys("session:1:*")).thenReturn(scanned);

        List<String> sessionIds = tokenRedisAdapter.findAllSessionIdsByUserId(userId);

        assertThat(sessionIds).containsExactly("sess-001", "sess-002");
    }

    @Test
    @DisplayName("findAllSessionIdsByUserId - prefix와 동일한 키는 무시된다")
    void ignoresExactPrefixKey() {
        long userId = 1L;
        Set<String> scanned = new LinkedHashSet<>(List.of(
                "session:1:",
                "session:1:sess-001"
        ));
        when(redisCacheAdapter.scanKeys("session:1:*")).thenReturn(scanned);

        List<String> sessionIds = tokenRedisAdapter.findAllSessionIdsByUserId(userId);

        assertThat(sessionIds).containsExactly("sess-001");
    }

    @Test
    @DisplayName("deleteAllRefreshTokensByUserId - userId 한정 SCAN 결과를 일괄 삭제한다")
    void deletesAllRefreshTokensForUser() {
        long userId = 1L;
        Set<String> scanned = new LinkedHashSet<>(List.of(
                "refresh:token:1:sess-001",
                "refresh:token:1:sess-002"
        ));
        lenient().when(redisCacheAdapter.scanKeys("refresh:token:1:*")).thenReturn(scanned);

        tokenRedisAdapter.deleteAllRefreshTokensByUserId(userId);

        verify(redisCacheAdapter).scanKeys("refresh:token:1:*");
        ArgumentCaptor<Set<String>> captor = setCaptor();
        verify(redisCacheAdapter).deleteAll(captor.capture());
        assertThat(captor.getValue()).containsExactlyInAnyOrder(
                "refresh:token:1:sess-001",
                "refresh:token:1:sess-002"
        );
    }

    @Test
    @DisplayName("deleteAllSessionsByUserId - SCAN 결과와 currentSessionKey를 함께 삭제한다")
    void deletesAllSessionsAndCurrentPointer() {
        long userId = 1L;
        Set<String> scanned = new LinkedHashSet<>(List.of(
                "session:1:sess-001",
                "session:1:sess-002"
        ));
        lenient().when(redisCacheAdapter.scanKeys("session:1:*")).thenReturn(scanned);

        tokenRedisAdapter.deleteAllSessionsByUserId(userId);

        ArgumentCaptor<Set<String>> captor = setCaptor();
        verify(redisCacheAdapter).deleteAll(captor.capture());
        assertThat(captor.getValue()).containsExactlyInAnyOrder(
                "session:1:sess-001",
                "session:1:sess-002",
                "session:current:1"
        );
    }

    @Test
    @DisplayName("deleteAllSessionsByUserId - SCAN 결과가 비어 있어도 currentSessionKey는 삭제 대상에 포함된다")
    void deletesCurrentPointerEvenWhenScanEmpty() {
        long userId = 9L;
        when(redisCacheAdapter.scanKeys("session:9:*")).thenReturn(new LinkedHashSet<>());

        tokenRedisAdapter.deleteAllSessionsByUserId(userId);

        ArgumentCaptor<Set<String>> captor = setCaptor();
        verify(redisCacheAdapter).deleteAll(captor.capture());
        assertThat(captor.getValue()).containsExactly("session:current:9");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private ArgumentCaptor<Set<String>> setCaptor() {
        return ArgumentCaptor.forClass((Class) Set.class);
    }
}
