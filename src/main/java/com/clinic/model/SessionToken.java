package com.clinic.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "session_tokens")
public class SessionToken {

    @Id
    @Column(name = "token_hash", length = 64)
    private String tokenHash;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    protected SessionToken() {
    }

    public SessionToken(String tokenHash, Long patientId, LocalDateTime createdAt, LocalDateTime expiresAt) {
        this.tokenHash = tokenHash;
        this.patientId = patientId;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public boolean isActive(LocalDateTime now) {
        return expiresAt.isAfter(now);
    }

    public String getTokenHash() { return tokenHash; }
    public Long getPatientId() { return patientId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
}
