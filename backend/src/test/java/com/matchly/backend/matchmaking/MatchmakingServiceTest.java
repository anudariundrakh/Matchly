package com.matchly.backend.matchmaking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

class MatchmakingServiceTest {

    private StringRedisTemplate redis;
    private MatchmakingService matchmakingService;

    @BeforeEach
    void setUp() {
        redis = mock(StringRedisTemplate.class);
        matchmakingService = new MatchmakingService(redis);
    }

    @Test
    void firstUserShouldWaitWhenQueueIsEmpty() {
        mockScriptResult("WAITING||");

        MatchmakingResponse response =
                matchmakingService.join(UUID.randomUUID());

        assertEquals("WAITING", response.status());
        assertNull(response.roomId());
        assertNull(response.partnerUserId());
    }

    @Test
    void secondUserShouldMatchWithWaitingUser() {
        String roomId = UUID.randomUUID().toString();
        String partnerUserId = UUID.randomUUID().toString();

        mockScriptResult(
                "MATCHED|" +
                roomId +
                "|" +
                partnerUserId
        );

        MatchmakingResponse response =
                matchmakingService.join(UUID.randomUUID());

        assertEquals("MATCHED", response.status());
        assertEquals(roomId, response.roomId());
        assertEquals(
                partnerUserId,
                response.partnerUserId()
        );
    }

    @Test
    void alreadyMatchedUserShouldReturnExistingMatch() {
        String roomId = UUID.randomUUID().toString();
        String partnerUserId = UUID.randomUUID().toString();

        mockScriptResult(
                "MATCHED|" +
                roomId +
                "|" +
                partnerUserId
        );

        MatchmakingResponse response =
                matchmakingService.join(UUID.randomUUID());

        assertEquals("MATCHED", response.status());
        assertEquals(roomId, response.roomId());
        assertEquals(
                partnerUserId,
                response.partnerUserId()
        );
    }

    private void mockScriptResult(String result) {
        when(
                redis.execute(
                        any(RedisScript.class),
                        anyList(),
                        any(Object[].class)
                )
        ).thenReturn(result);
    }
}