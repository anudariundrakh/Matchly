package com.matchly.backend.chat;

import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class WebSocketAuthInterceptor
        implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;

    public WebSocketAuthInterceptor(
            JwtDecoder jwtDecoder
    ) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel
    ) {
        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(
                accessor.getCommand()
        )) {
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

        return message;
    }
}