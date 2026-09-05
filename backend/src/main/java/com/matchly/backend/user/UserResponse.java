package com.matchly.backend.user;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String displayName,
        boolean emailVerified,
        Instant createdAt
) {

    public static UserResponse from(
            UserAccount user
    ) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.isEmailVerified(),
                user.getCreatedAt()
        );
    }
}