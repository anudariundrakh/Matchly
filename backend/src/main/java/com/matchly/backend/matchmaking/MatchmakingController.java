package com.matchly.backend.matchmaking;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/matchmaking")
public class MatchmakingController {

    private final MatchmakingService matchmakingService;

    public MatchmakingController(
            MatchmakingService matchmakingService
    ) {
        this.matchmakingService = matchmakingService;
    }

    @PostMapping("/join")
    public MatchmakingResponse join(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(
                jwt.getSubject()
        );

        return matchmakingService.join(userId);
    }

    @GetMapping("/status")
    public MatchmakingResponse status(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(
                jwt.getSubject()
        );

        return matchmakingService.status(userId);
    }
    @PostMapping("/leave")
public void leave(
        @AuthenticationPrincipal Jwt jwt
) {
    UUID userId = UUID.fromString(
            jwt.getSubject()
    );

    matchmakingService.leave(userId);
}
}