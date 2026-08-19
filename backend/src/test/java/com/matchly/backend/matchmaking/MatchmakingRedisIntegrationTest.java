package com.matchly.backend.matchmaking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

class MatchmakingRedisIntegrationTest {

    private static LettuceConnectionFactory connectionFactory;
    private static StringRedisTemplate redis;

    private MatchmakingService serviceA;
    private MatchmakingService serviceB;

    @BeforeAll
    static void connectToRedis() {
        String redisPassword =
                System.getenv("REDIS_PASSWORD");

        if (redisPassword == null || redisPassword.isBlank()) {
            throw new IllegalStateException(
                    "REDIS_PASSWORD environment variable is required"
            );
        }

        RedisStandaloneConfiguration configuration =
                new RedisStandaloneConfiguration(
                        "localhost",
                        6379
                );

        configuration.setDatabase(1);
        configuration.setPassword(
                RedisPassword.of(redisPassword)
        );

        connectionFactory =
                new LettuceConnectionFactory(configuration);

        connectionFactory.afterPropertiesSet();

        redis =
                new StringRedisTemplate(
                        connectionFactory
                );

        redis.afterPropertiesSet();
    }

    @AfterAll
    static void disconnectFromRedis() {
        if (connectionFactory != null) {
            connectionFactory.destroy();
        }
    }

    @BeforeEach
    void setUp() {
        clearMatchmakingKeys();

        serviceA = new MatchmakingService(redis);
        serviceB = new MatchmakingService(redis);
    }

    @AfterEach
    void cleanUp() {
        clearMatchmakingKeys();
    }

    @Test
    void realRedisShouldMatchTwoUsers() {
        UUID userA = UUID.randomUUID();
        UUID userB = UUID.randomUUID();

        MatchmakingResponse firstResponse =
                serviceA.join(userA);

        assertEquals(
                "WAITING",
                firstResponse.status()
        );

        MatchmakingResponse secondResponse =
                serviceB.join(userB);

        assertEquals(
                "MATCHED",
                secondResponse.status()
        );

        assertNotNull(
                secondResponse.roomId()
        );

        assertEquals(
                userA.toString(),
                secondResponse.partnerUserId()
        );

        MatchmakingResponse userAStatus =
                serviceA.status(userA);

        assertEquals(
                "MATCHED",
                userAStatus.status()
        );

        assertEquals(
                secondResponse.roomId(),
                userAStatus.roomId()
        );

        assertEquals(
                userB.toString(),
                userAStatus.partnerUserId()
        );
    }

    @Test
void concurrentUsersShouldEndInOneReciprocalMatch()
        throws Exception {

    UUID userA = UUID.randomUUID();
    UUID userB = UUID.randomUUID();

    ExecutorService executor =
            Executors.newFixedThreadPool(2);

    CountDownLatch ready =
            new CountDownLatch(2);

    CountDownLatch start =
            new CountDownLatch(1);

    try {
        Future<MatchmakingResponse> first =
                executor.submit(() -> {
                    ready.countDown();
                    start.await();

                    return serviceA.join(userA);
                });

        Future<MatchmakingResponse> second =
                executor.submit(() -> {
                    ready.countDown();
                    start.await();

                    return serviceB.join(userB);
                });

        assertTrue(
                ready.await(
                        5,
                        TimeUnit.SECONDS
                )
        );

        start.countDown();

        first.get(
                5,
                TimeUnit.SECONDS
        );

        second.get(
                5,
                TimeUnit.SECONDS
        );

        MatchmakingResponse userAStatus =
                serviceA.status(userA);

        MatchmakingResponse userBStatus =
                serviceB.status(userB);

        assertEquals(
                "MATCHED",
                userAStatus.status()
        );

        assertEquals(
                "MATCHED",
                userBStatus.status()
        );

        assertNotNull(
                userAStatus.roomId()
        );

        assertEquals(
                userAStatus.roomId(),
                userBStatus.roomId()
        );

        assertEquals(
                userB.toString(),
                userAStatus.partnerUserId()
        );

        assertEquals(
                userA.toString(),
                userBStatus.partnerUserId()
        );

        Long queueSize =
                redis.opsForList().size(
                        "matchly:matchmaking:waiting"
                );

        assertNotNull(queueSize);

        assertEquals(
                0L,
                queueSize.longValue()
        );

    } finally {
        executor.shutdownNow();
    }
}

    private void clearMatchmakingKeys() {
        Set<String> keys =
                redis.keys(
                        "matchly:matchmaking:*"
                );

        if (keys != null && !keys.isEmpty()) {
            redis.delete(keys);
        }
    }
}