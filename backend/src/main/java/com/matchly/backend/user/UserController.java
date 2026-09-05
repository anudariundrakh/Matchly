package com.matchly.backend.user;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    private final EmailVerificationService
            emailVerificationService;

    public UserController(
            UserService userService,
            EmailVerificationService emailVerificationService
    ) {
        this.userService =
                userService;

        this.emailVerificationService =
                emailVerificationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(
            @Valid @RequestBody RegisterUserRequest request
    ) {
        return userService.register(request);
    }

    @GetMapping("/verify-email")
    public UserResponse verifyEmail(
            @RequestParam String token
    ) {
        UserAccount verifiedUser =
                emailVerificationService
                        .verifyEmail(token);

        return UserResponse.from(
                verifiedUser
        );
    }

    @GetMapping("/me")
    public UserResponse currentUser(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId =
                UUID.fromString(
                        jwt.getSubject()
                );

        return userService.getCurrentUser(
                userId
        );
    }
}