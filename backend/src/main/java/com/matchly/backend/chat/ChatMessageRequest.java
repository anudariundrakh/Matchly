package com.matchly.backend.chat;

public record ChatMessageRequest(
        String roomId,
        String sender,
        String content
) {
}