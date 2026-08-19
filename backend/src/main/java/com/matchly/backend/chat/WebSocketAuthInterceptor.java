package com.matchly.backend.chat;

import java.util.UUID;

import com.matchly.backend.matchmaking.MatchmakingService;

import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class WebSocketAuthInterceptor
        implements ChannelInterceptor {

    private static final String CHAT_TOPIC_PREFIX =
            "/topic/chat/";

    private final JwtDecoder jwtDecoder;
    private final MatchmakingService matchmakingService;

    public WebSocketAuthInterceptor(
            JwtDecoder jwtDecoder,
            MatchmakingService matchmakingService
    ) {
        this.jwtDecoder = jwtDecoder;
        this.matchmakingService = matchmakingService;
    }

    @Override
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel
    ) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(
                        message,
                        StompHeaderAccessor.class
                );

        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(
                accessor.getCommand()
        )) {
            authenticateConnection(accessor);
        }

        if (StompCommand.SUBSCRIBE.equals(
                accessor.getCommand()
        )) {
            authorizeSubscription(accessor);
        }

        return message;
    }

    private void authenticateConnection(
            StompHeaderAccessor accessor
    ) {
        String authorizationHeader =
                accessor.getFirstNativeHeader(
                        HttpHeaders.AUTHORIZATION
                );

        if (authorizationHeader == null
                || !authorizationHeader.startsWith(
                        "Bearer "
                )) {
            throw new AccessDeniedException(
                    "Missing WebSocket authentication token"
            );
        }

        String token =
                authorizationHeader.substring(7);

        try {
            Jwt jwt =
                    jwtDecoder.decode(token);

            JwtAuthenticationToken authentication =
                    new JwtAuthenticationToken(jwt);

            accessor.setUser(authentication);

        } catch (JwtException exception) {
            throw new AccessDeniedException(
                    "Invalid WebSocket authentication token",
                    exception
            );
        }
    }

    private void authorizeSubscription(
            StompHeaderAccessor accessor
    ) {
        String destination =
                accessor.getDestination();

        if (destination == null
                || !destination.startsWith(
                        CHAT_TOPIC_PREFIX
                )) {
            return;
        }

        if (!(accessor.getUser()
                instanceof JwtAuthenticationToken authentication)) {
            throw new AccessDeniedException(
                    "Authenticated WebSocket user required"
            );
        }

        String roomIdText =
                destination.substring(
                        CHAT_TOPIC_PREFIX.length()
                );

        UUID roomId;

        try {
            roomId =
                    UUID.fromString(roomIdText);
        } catch (IllegalArgumentException exception) {
            throw new AccessDeniedException(
                    "Invalid chat room subscription"
            );
        }

        UUID userId;

        try {
            userId =
                    UUID.fromString(
                            authentication
                                    .getToken()
                                    .getSubject()
                    );
        } catch (IllegalArgumentException exception) {
            throw new AccessDeniedException(
                    "Invalid authenticated user"
            );
        }

        if (!matchmakingService.isUserInRoom(
                userId,
                roomId
        )) {
            throw new AccessDeniedException(
                    "You are not allowed to subscribe to this chat room"
            );
        }
    }
}