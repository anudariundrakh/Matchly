package com.matchly.backend.user;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailVerificationService {

    private static final Duration TOKEN_EXPIRATION =
            Duration.ofHours(24);

    private final SecureRandom secureRandom =
            new SecureRandom();

    private final UserAccountRepository userRepository;

    public EmailVerificationService(
            UserAccountRepository userRepository
    ) {
        this.userRepository = userRepository;
    }

    public String createVerificationToken(
            UserAccount user
    ) {
        byte[] randomBytes =
                new byte[32];

        secureRandom.nextBytes(
                randomBytes
        );

        String rawToken =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(
                                randomBytes
                        );

        String tokenHash =
                hashToken(rawToken);

        Instant expiresAt =
                Instant.now().plus(
                        TOKEN_EXPIRATION
                );

        user.setEmailVerificationToken(
                tokenHash,
                expiresAt
        );

        return rawToken;
    }

    @Transactional
    public UserAccount verifyEmail(
            String rawToken
    ) {
        if (rawToken == null
                || rawToken.isBlank()) {
            throw new IllegalArgumentException(
                    "Verification token is required"
            );
        }

        String tokenHash =
                hashToken(
                        rawToken.trim()
                );

        UserAccount user =
                userRepository
                        .findByEmailVerificationTokenHash(
                                tokenHash
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid verification token"
                                )
                        );

        Instant expiresAt =
                user.getEmailVerificationTokenExpiresAt();

        if (expiresAt == null
                || expiresAt.isBefore(
                        Instant.now()
                )) {
            throw new IllegalArgumentException(
                    "Verification token has expired"
            );
        }

        user.markEmailVerified();

        return userRepository.save(user);
    }

    private String hashToken(
            String rawToken
    ) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] hash =
                    digest.digest(
                            rawToken.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return java.util.HexFormat
                    .of()
                    .formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is not available",
                    exception
            );
        }
    }
}