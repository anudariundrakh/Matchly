package com.matchly.backend.chat;

import java.time.Instant;

public record ChatMessageResponse(
        String sender,
        String content,
        Instant sentAt
) {
}