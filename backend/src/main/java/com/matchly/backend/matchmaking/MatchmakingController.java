package com.matchly.backend.matchmaking;

import java.util.Map;
import java.util.UUID;

import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final SimpMessagingTemplate messagingTemplate;

    public MatchmakingController(
            MatchmakingService matchmakingService,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.matchmakingService =
                matchmakingService;

        this.messagingTemplate =
                messagingTemplate;
    }

    @PostMapping("/join")
    public MatchmakingResponse join(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(
                jwt.getSubject()
        );

        return matchmakingService.join(
                userId
        );
    }

    @GetMapping("/status")
    public MatchmakingResponse status(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(
                jwt.getSubject()
        );

        return matchmakingService.status(
                userId
        );
    }

    @PostMapping("/leave")
    public void leave(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(
                jwt.getSubject()
        );

        MatchmakingResponse result =
                matchmakingService.leave(
                        userId
                );

        if (result.roomId() == null
                || result.partnerUserId() == null) {
            return;
        }

        Map<String, Object> event =
                Map.of(
                        "type",
                        "PARTNER_LEFT"
                );

        Map<String, Object> headers =
                Map.of();

        messagingTemplate.convertAndSend(
                "/topic/chat/"
                        + result.roomId(),
                event,
                headers
        );
    }
}