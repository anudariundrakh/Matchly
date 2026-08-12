package com.matchly.backend.matchmaking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class MatchmakingServiceTest {

    private StringRedisTemplate redis;
    private ValueOperations<String, String> valueOperations;
    private ListOperations<String, String> listOperations;
    private MatchmakingService matchmakingService;

    @BeforeEach
    void setUp() {
        redis = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        listOperations = mock(ListOperations.class);

        when(redis.opsForValue()).thenReturn(valueOperations);
        when(redis.opsForList()).thenReturn(listOperations);

        matchmakingService = new MatchmakingService(redis);
    }

    @Test
    void firstUserShouldWaitWhenQueueIsEmpty() {
        UUID userId = UUID.randomUUID();
        String userIdString = userId.toString();

        when(
                valueOperations.get(
                        "matchly:matchmaking:room:" + userIdString
                )
        ).thenReturn(null);

        when(
                listOperations.leftPop(
                        "matchly:matchmaking:waiting"
                )
        ).thenReturn(null);

        MatchmakingResponse response =
                matchmakingService.join(userId);

        assertEquals("WAITING", response.status());
        assertEquals(null, response.roomId());
        assertEquals(null, response.partnerUserId());

        verify(listOperations).rightPush(
                "matchly:matchmaking:waiting",
                userIdString
        );
    }
}