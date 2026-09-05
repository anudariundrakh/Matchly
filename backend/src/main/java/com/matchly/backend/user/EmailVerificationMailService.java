package com.matchly.backend.user;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailVerificationMailService {

    private final JavaMailSender mailSender;

    private final String frontendUrl;

    private final String fromAddress;

    public EmailVerificationMailService(
            JavaMailSender mailSender,
            @Value("${app.frontend-url:http://localhost:5173}")
            String frontendUrl,
            @Value("${spring.mail.username}")
            String fromAddress
    ) {
        this.mailSender = mailSender;
        this.frontendUrl = frontendUrl;
        this.fromAddress = fromAddress;
    }

    public void sendVerificationEmail(
            UserAccount user,
            String rawToken
    ) {
        String encodedToken =
                URLEncoder.encode(
                        rawToken,
                        StandardCharsets.UTF_8
                );

        String verificationLink =
                frontendUrl
                        + "/verify-email?token="
                        + encodedToken;

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setFrom(fromAddress);

        message.setTo(
                user.getEmail()
        );

        message.setSubject(
                "Verify your Matchly email"
        );

        message.setText(
                """
                Welcome to Matchly!

                Please verify your email address by opening this link:

                %s

                This verification link expires in 24 hours.

                If you did not create a Matchly account, you can ignore this email.
                """
                        .formatted(
                                verificationLink
                        )
        );

        mailSender.send(message);
    }
}