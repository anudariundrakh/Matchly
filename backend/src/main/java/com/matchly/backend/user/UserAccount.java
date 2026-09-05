package com.matchly.backend.user;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_users")
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            nullable = false,
            unique = true,
            length = 255
    )
    private String email;

    @Column(
            nullable = false,
            length = 50
    )
    private String displayName;

    @Column(
            nullable = false,
            length = 255
    )
    private String passwordHash;

    @Column(
            nullable = false,
            columnDefinition = "boolean default false"
    )
    private boolean emailVerified = false;

    @Column(length = 64)
    private String emailVerificationTokenHash;

    private Instant emailVerificationTokenExpiresAt;

    @Column(
            nullable = false,
            updatable = false
    )
    private Instant createdAt = Instant.now();

    protected UserAccount() {
    }

    public UserAccount(
            String email,
            String displayName,
            String passwordHash
    ) {
        this.email = email;
        this.displayName = displayName;
        this.passwordHash = passwordHash;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public String getEmailVerificationTokenHash() {
        return emailVerificationTokenHash;
    }

    public Instant getEmailVerificationTokenExpiresAt() {
        return emailVerificationTokenExpiresAt;
    }

    String getPasswordHash() {
        return passwordHash;
    }

    public void setEmailVerificationToken(
            String tokenHash,
            Instant expiresAt
    ) {
        this.emailVerificationTokenHash =
                tokenHash;

        this.emailVerificationTokenExpiresAt =
                expiresAt;
    }

    public void markEmailVerified() {
        this.emailVerified = true;
        this.emailVerificationTokenHash = null;
        this.emailVerificationTokenExpiresAt = null;
    }
}