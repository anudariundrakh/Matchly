package com.matchly.backend.chat;

import java.time.Instant;
import java.util.UUID;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(
            SimpMessagingTemplate messagingTemplate
    ) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.send")
    public void sendMessage(
            ChatMessageRequest request
    ) {
        String roomId = UUID
                .fromString(request.roomId())
                .toString();

        String sender = request.sender().trim();
        String content = request.content().trim();

        ChatMessageResponse response =
                new ChatMessageResponse(
                        sender,
                        content,
                        Instant.now()
                );

        messagingTemplate.convertAndSend(
                "/topic/chat/" + roomId,
                response
        );
    }
}