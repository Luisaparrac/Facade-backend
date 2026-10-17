package com.clinic.service;

import com.clinic.exception.UnauthorizedException;
import com.clinic.model.Patient;
import com.clinic.model.SessionToken;
import com.clinic.repository.SessionTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class AuthService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final SessionTokenRepository sessionTokenRepository;
    private final PatientService patientService;
    private final Clock clock;
    private final long sessionHours;

    public AuthService(SessionTokenRepository sessionTokenRepository, PatientService patientService, Clock clock,
                       @Value("${clinic.session.hours:8}") long sessionHours) {
        this.sessionTokenRepository = sessionTokenRepository;
        this.patientService = patientService;
        this.clock = clock;
        this.sessionHours = sessionHours;
    }

    @Transactional
    public LoginSession login(String documentNumber, String password) {
        Patient patient = patientService.authenticate(documentNumber, password);
        LocalDateTime now = LocalDateTime.now(clock);
        sessionTokenRepository.deleteByExpiresAtBefore(now);

        String token = newToken();
        LocalDateTime expiresAt = now.plusHours(sessionHours);
        sessionTokenRepository.save(new SessionToken(hash(token), patient.getId(), now, expiresAt));
        return new LoginSession(token, patient.getId(), expiresAt);
    }

    @Transactional(readOnly = true)
    public Long resolvePatientId(String token) {
        LocalDateTime now = LocalDateTime.now(clock);
        return sessionTokenRepository.findById(hash(token))
                .filter(session -> session.isActive(now))
                .map(SessionToken::getPatientId)
                .orElseThrow(() -> new UnauthorizedException("Your session is invalid or has expired. Please log in again."));
    }

    @Transactional
    public void logout(String token) {
        sessionTokenRepository.deleteById(hash(token));
    }

    private static String newToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    public record LoginSession(String token, Long patientId, LocalDateTime expiresAt) {
    }
}
