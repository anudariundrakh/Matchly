package com.matchly.backend.chat;

import java.security.Principal;
import java.time.Instant;
import java.util.UUID;

import com.matchly.backend.matchmaking.MatchmakingService;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MatchmakingService matchmakingService;

    public ChatController(
            SimpMessagingTemplate messagingTemplate,
            MatchmakingService matchmakingService
    ) {
        this.messagingTemplate = messagingTemplate;
        this.matchmakingService = matchmakingService;
    }

    @MessageMapping("/chat.send")
    public void sendMessage(
            ChatMessageRequest request,
            Principal principal
    ) {
        if (!(principal instanceof JwtAuthenticationToken authentication)) {
            throw new AccessDeniedException(
                    "Authenticated WebSocket user required"
            );
        }

        UUID userId = UUID.fromString(
                authentication.getToken().getSubject()
        );

        UUID roomId;

        try {
            roomId = UUID.fromString(
                    request.roomId()
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid room ID"
            );
        }

        if (!matchmakingService.isUserInRoom(
                userId,
                roomId
        )) {
            throw new AccessDeniedException(
                    "You are not a member of this chat room"
            );
        }

        String content = request.content().trim();

        if (content.isBlank()) {
            return;
        }

        String displayName =
                authentication
                        .getToken()
                        .getClaimAsString(
                                "displayName"
                        );

        ChatMessageResponse response =
                new ChatMessageResponse(
                        displayName,
                        content,
                        Instant.now()
                );

        messagingTemplate.convertAndSend(
                "/topic/chat/" + roomId,
                response
        );
    }
}