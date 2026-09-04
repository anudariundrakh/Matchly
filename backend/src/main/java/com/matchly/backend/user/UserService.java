package com.matchly.backend.user;

import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserAccountRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;
    private final EmailVerificationMailService emailVerificationMailService;
    private final boolean emailVerificationEnabled;

    public UserService(
            UserAccountRepository userRepository,
            PasswordEncoder passwordEncoder,
            EmailVerificationService emailVerificationService,
            EmailVerificationMailService emailVerificationMailService,
            @Value("${app.email-verification-enabled:true}")
            boolean emailVerificationEnabled
    ) {
        this.userRepository =
                userRepository;

        this.passwordEncoder =
                passwordEncoder;

        this.emailVerificationService =
                emailVerificationService;

        this.emailVerificationMailService =
                emailVerificationMailService;

        this.emailVerificationEnabled =
                emailVerificationEnabled;
    }

    @Transactional
    public UserResponse register(
            RegisterUserRequest request
    ) {
        String email = request
                .email()
                .trim()
                .toLowerCase(Locale.ROOT);

        String displayName = request
                .displayName()
                .trim();

        if (userRepository.existsByEmailIgnoreCase(
                email
        )) {
            throw new EmailAlreadyExistsException();
        }

        String passwordHash =
                passwordEncoder.encode(
                        request.password()
                );

        UserAccount user =
                new UserAccount(
                        email,
                        displayName,
                        passwordHash
                );

        String verificationToken = null;

        if (emailVerificationEnabled) {
            verificationToken =
                    emailVerificationService
                            .createVerificationToken(
                                    user
                            );
        }

        UserAccount savedUser =
                userRepository.save(user);

        if (emailVerificationEnabled) {
            emailVerificationMailService
                    .sendVerificationEmail(
                            savedUser,
                            verificationToken
                    );
        }

        return UserResponse.from(
                savedUser
        );
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(
            UUID userId
    ) {
        UserAccount user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "User not found"
                                )
                        );

        return UserResponse.from(user);
    }
}