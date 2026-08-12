package com.matchly.backend.matchmaking;

import java.time.Duration;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class MatchmakingService {

    private static final String WAITING_QUEUE =
            "matchly:matchmaking:waiting";

    private static final String ROOM_PREFIX =
            "matchly:matchmaking:room:";

    private static final String PARTNER_PREFIX =
            "matchly:matchmaking:partner:";

    private static final Duration ROOM_EXPIRATION =
            Duration.ofMinutes(30);

    private final StringRedisTemplate redis;

    public MatchmakingService(
            StringRedisTemplate redis
    ) {
        this.redis = redis;
    }

    public synchronized MatchmakingResponse join(
            UUID userId
    ) {
        String currentUserId = userId.toString();

        String existingRoom = redis
                .opsForValue()
                .get(ROOM_PREFIX + currentUserId);

        if (existingRoom != null) {
            String existingPartner = redis
                    .opsForValue()
                    .get(PARTNER_PREFIX + currentUserId);

            return new MatchmakingResponse(
                    "MATCHED",
                    existingRoom,
                    existingPartner
            );
        }

        redis.opsForList().remove(
                WAITING_QUEUE,
                0,
                currentUserId
        );

        String waitingUserId = redis
                .opsForList()
                .leftPop(WAITING_QUEUE);

        if (waitingUserId == null) {
            redis.opsForList().rightPush(
                    WAITING_QUEUE,
                    currentUserId
            );

            return new MatchmakingResponse(
                    "WAITING",
                    null,
                    null
            );
        }

        String roomId = UUID.randomUUID().toString();

        saveMatch(
                currentUserId,
                waitingUserId,
                roomId
        );

        saveMatch(
                waitingUserId,
                currentUserId,
                roomId
        );

        return new MatchmakingResponse(
                "MATCHED",
                roomId,
                waitingUserId
        );
    }

    public MatchmakingResponse status(UUID userId) {
        String currentUserId = userId.toString();

        String roomId = redis
                .opsForValue()
                .get(ROOM_PREFIX + currentUserId);

        if (roomId == null) {
            return new MatchmakingResponse(
                    "WAITING",
                    null,
                    null
            );
        }

        String partnerUserId = redis
                .opsForValue()
                .get(PARTNER_PREFIX + currentUserId);

        return new MatchmakingResponse(
                "MATCHED",
                roomId,
                partnerUserId
        );
    }

    private void saveMatch(
            String userId,
            String partnerUserId,
            String roomId
    ) {
        redis.opsForValue().set(
                ROOM_PREFIX + userId,
                roomId,
                ROOM_EXPIRATION
        );

        redis.opsForValue().set(
                PARTNER_PREFIX + userId,
                partnerUserId,
                ROOM_EXPIRATION
        );
    }

    public void leave(UUID userId) {
    String currentUserId = userId.toString();

    redis.opsForList().remove(
            WAITING_QUEUE,
            0,
            currentUserId
    );

    String partnerUserId = redis
            .opsForValue()
            .get(PARTNER_PREFIX + currentUserId);

    redis.delete(ROOM_PREFIX + currentUserId);
    redis.delete(PARTNER_PREFIX + currentUserId);

    if (partnerUserId != null) {
        redis.delete(ROOM_PREFIX + partnerUserId);
        redis.delete(PARTNER_PREFIX + partnerUserId);
    }
}
}