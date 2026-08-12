package com.matchly.backend.matchmaking;

public record MatchmakingResponse(
        String status,
        String roomId,
        String partnerUserId
) {
}