package com.matchly.backend.matchmaking;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
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

    private static final DefaultRedisScript<String> JOIN_SCRIPT;

    private static final DefaultRedisScript<Long> LEAVE_SCRIPT;

    static {
        JOIN_SCRIPT = new DefaultRedisScript<>();
        JOIN_SCRIPT.setLocation(
                new ClassPathResource(
                        "redis/matchmaking-join.lua"
                )
        );
        JOIN_SCRIPT.setResultType(String.class);

        LEAVE_SCRIPT = new DefaultRedisScript<>();
        LEAVE_SCRIPT.setLocation(
                new ClassPathResource(
                        "redis/matchmaking-leave.lua"
                )
        );
        LEAVE_SCRIPT.setResultType(Long.class);
    }

    private final StringRedisTemplate redis;

    public MatchmakingService(
            StringRedisTemplate redis
    ) {
        this.redis = redis;
    }

    public MatchmakingResponse join(
            UUID userId
    ) {
        String currentUserId = userId.toString();
        String newRoomId = UUID.randomUUID().toString();

        String result = redis.execute(
                JOIN_SCRIPT,
                List.of(WAITING_QUEUE),
                currentUserId,
                ROOM_PREFIX,
                PARTNER_PREFIX,
                newRoomId,
                String.valueOf(
                        ROOM_EXPIRATION.toSeconds()
                )
        );

        if (result == null) {
            throw new IllegalStateException(
                    "Redis matchmaking script returned no result"
            );
        }

        String[] parts =
                result.split("\\|", -1);

        if (parts.length != 3) {
            throw new IllegalStateException(
                    "Unexpected matchmaking response: "
                            + result
            );
        }

        String status = parts[0];

        String roomId =
                parts[1].isBlank()
                        ? null
                        : parts[1];

        String partnerUserId =
                parts[2].isBlank()
                        ? null
                        : parts[2];

        return new MatchmakingResponse(
                status,
                roomId,
                partnerUserId
        );
    }

    public MatchmakingResponse status(
            UUID userId
    ) {
        String currentUserId = userId.toString();

        String roomId = redis
                .opsForValue()
                .get(
                        ROOM_PREFIX
                                + currentUserId
                );

        if (roomId == null) {
            return new MatchmakingResponse(
                    "WAITING",
                    null,
                    null
            );
        }

        String partnerUserId = redis
                .opsForValue()
                .get(
                        PARTNER_PREFIX
                                + currentUserId
                );

        return new MatchmakingResponse(
                "MATCHED",
                roomId,
                partnerUserId
        );
    }
    public boolean isUserInRoom(
        UUID userId,
        UUID roomId
) {
    String currentRoomId = redis
            .opsForValue()
            .get(
                    ROOM_PREFIX
                            + userId
            );

    return roomId.toString().equals(
            currentRoomId
    );
}

    public void leave(
            UUID userId
    ) {
        Long result = redis.execute(
                LEAVE_SCRIPT,
                List.of(WAITING_QUEUE),
                userId.toString(),
                ROOM_PREFIX,
                PARTNER_PREFIX
        );

        if (result == null) {
            throw new IllegalStateException(
                    "Redis leave script returned no result"
            );
        }
    }
}